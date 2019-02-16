package ru.bahusdivus.teleinstaBot;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.bahusdivus.teleinstaBot.Scrapper.TibInstagramScrapper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.*;

class ReplayBuilder {
    private String messageText;
    private Long chatId;
    private String replayText;
    private DbHandler db;
    private ReplyKeyboardMarkup replyKeyboardMarkup;
    private TibInstagramScrapper scrapper;

    ReplayBuilder(String messageText, Long chatId) {
        this.messageText = messageText;
        this.chatId = chatId;
        db = DbHandler.getInstance();
    }

    //Test purpose constructor
    ReplayBuilder(String messageText, Long chatId, DbHandler db) {
        this.messageText = messageText;
        this.chatId = chatId;
        this.db = db;
    }

    String getReplayText() {
        return replayText;
    }

    private void buildMarkup() {
        replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton("Проверить задание"));
//        keyboardFirstRow.add(new KeyboardButton("Помощь"));

        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

    }

    ReplyKeyboardMarkup getReplyKeyboardMarkup() {
        return replyKeyboardMarkup;
    }

    void buildReplay() {
        User user = db.getUserByChatId(chatId);
        if (user == null) {
            if(Pattern.matches("^@[\\w.]+$", messageText)) {
                user = new User(messageText, chatId);
                db.saveUser(user);
                replayText = "Сохраняем ник \"" + messageText + "\"";
            } else {
                replayText = "Для начала работы с ботом вам необходимо зарегистрировать свой аккаунт Instagram. Просто отправьте свой ник, начинающийся с символа @";
            }
        } else {
            switch (messageText) {
                case "Получить задание":
                    ArrayList<UserTask> tasks = db.getTaskList(user.getId());
                    if (tasks == null) {
                        replayText = "Все задания выполнены, можно размещать ссылку";
                    } else {
                        StringBuilder replay = new StringBuilder("Ваше задание:\n");
                        for (UserTask task : tasks) {
                            replay.append("https://www.instagram.com/p/").append(task.getPostId()).append("/\n");
                            replay.append("Нужен ");
                            if (task.isLikeRequired()) replay.append("лайк");
                            if (task.isLikeRequired() && (task.getCommentRequiredLength() > 0)) replay.append(" и ");
                            if (task.getCommentRequiredLength() > 0) replay.append("комментарий от ").append(task.getCommentRequiredLength()).append(" слов\n");
                            if (task.getComment() != null) replay.append("Комментарий к заданию: ").append(task.getComment()).append("\n");
                            replay.append("\n");
                        }
                        replayText = replay.toString();
                    }
                    break;
                case "Проверить задание":
                    replayText = checkTask(user);
                    break;
                default:
                    replayText = "Тут будут парситься другие строки";
            }
        }
        buildMarkup();
    }

    private String checkTask(User user) {
        scrapper = TibInstagramScrapper.getInstance();
        StringBuilder replay = new StringBuilder();
        Timestamp dayBefore = new Timestamp(System.currentTimeMillis() - 1000 * 60 * 60 * 24);

        //Logic is: if user completed all task after {dayBefore}, he can post their own
        if (user.getTaskComplite() != null && user.getTaskTaken().before(user.getTaskComplite())) {
            return "Вы выполнили все условия и можете разместить ссылку!";
        }


        ArrayList<UserTask> taskList = db.getTaskList(user.getId());
        for(UserTask task : taskList) {
            boolean isPass = true;
            StringBuilder taskResult = new StringBuilder("https://www.instagram.com/p/" + task.getPostId() + "/");
            taskResult.append(System.lineSeparator());
            if (task.isLikeRequired()) {
                if (!checkLike(user.getInstId(), task.getPostId())) {
                    taskResult.append("Нужен лайк");
                    taskResult.append(System.lineSeparator());
                    isPass = false;
                }
            }
            if (task.getCommentRequiredLength() > 0) {
                if (!checkComment(user.getInstId(), task.getPostId(), task.getCommentRequiredLength())) {
                    taskResult.append("Нужен комментарий не менее ");
                    taskResult.append(task.getCommentRequiredLength());
                    taskResult.append(" слов");
                    taskResult.append(System.lineSeparator());
                    isPass = false;
                }
            }
            if (isPass) {
                db.compliteTask(user.getId(), task.getId());
            } else {
                if (task.getComment().length() > 0) {
                    taskResult.append(task.getComment());
                    taskResult.append(System.lineSeparator());
                    taskResult.append(System.lineSeparator());
                }
                replay.append(taskResult.toString());
            }
        }
        if (replay.length() == 0) {
            db.setTaskCompliteTime(user);
            return "Вы выполнили все условия и можете разместить ссылку!";
        }
        return replay.toString();
    }

    private boolean checkComment(String instId, String postId, int commentsRequiresLength) {
        String dataResult = null;

        try {
            String inputLine = scrapper.getPageBody("https://www.instagram.com/p/" + postId + "/");
            Pattern p = Pattern.compile("(.*?)_sharedData = (.*?);</script>(.*?)", Pattern.DOTALL);
            Matcher m = p.matcher(inputLine);
            if (m.matches()) dataResult = m.group(2);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (dataResult == null) return false;
        JSONObject rootJson = new JSONObject(dataResult);
        JSONObject edgesJson = rootJson.getJSONObject("entry_data").getJSONArray("PostPage")
                .getJSONObject(0).getJSONObject("graphql").getJSONObject("shortcode_media")
                .getJSONObject("edge_media_to_comment");
        boolean hasNext = true;
        String endCursor = null;
        while (hasNext) {
            if (edgesJson == null) {
                try {
                    endCursor = URLEncoder.encode(Objects.requireNonNull(endCursor), StandardCharsets.UTF_8.toString());
                    String url = "https://www.instagram.com/graphql/query/?query_hash=f0986789a5c5d17c2400faebf16efd0d&variables=%7B%22shortcode%22%3A%22" + postId + "%22%2C%22first%22%3A32%2C%22after%22%3A%22" + endCursor + "%22%7D";
                    dataResult = scrapper.getPageBody(url);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                edgesJson = new JSONObject(dataResult).getJSONObject("data").getJSONObject("shortcode_media").getJSONObject("edge_media_to_comment");
            }

            JSONArray commentsArray = edgesJson.getJSONArray("edges");
            for (int i = 0; i < commentsArray.length(); i++) {
                JSONObject currentNode = commentsArray.getJSONObject(i).getJSONObject("node");
                if (instId.equals("@" + currentNode.getJSONObject("owner").getString("username"))) {
                    String[] words = currentNode.getString("text").split(" ");
                    if (words.length >= commentsRequiresLength) {
                        return true;
                    }
                }
            }

            hasNext = edgesJson.getJSONObject("page_info").getBoolean("has_next_page");
            if (hasNext) endCursor = edgesJson.getJSONObject("page_info").getString("end_cursor");
            edgesJson = null;
        }
        return false;
    }

    private boolean checkLike(String instId, String postId) {
        String dataResult;

        try {
            String url = "https://www.instagram.com/graphql/query/?query_hash=e0f59e4a1c8d78d0161873bc2ee7ec44&variables=%7B%22shortcode%22%3A%22" + postId + "%22%2C%22include_reel%22%3Atrue%2C%22first%22%3A24%7D";
            dataResult = scrapper.getPageBody(url);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (dataResult == null) return false;
        JSONObject rootJson = new JSONObject(dataResult);
        JSONObject edgesJson = rootJson.getJSONObject("data").getJSONObject("shortcode_media").getJSONObject("edge_liked_by");
        boolean hasNext = true;
        String endCursor = null;
        while (hasNext) {
            if (edgesJson == null) {
                try {
                    endCursor = URLEncoder.encode(Objects.requireNonNull(endCursor), StandardCharsets.UTF_8.toString());
                    String url = "https://www.instagram.com/graphql/query/?query_hash=e0f59e4a1c8d78d0161873bc2ee7ec44&variables=%7B%22shortcode%22%3A%22" + postId + "%22%2C%22include_reel%22%3Atrue%2C%22first%22%3A24%2C%22after%22%3A%22" + endCursor + "%22%7D";
                    dataResult = scrapper.getPageBody(url);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                edgesJson = new JSONObject(dataResult).getJSONObject("data").getJSONObject("shortcode_media").getJSONObject("edge_liked_by");
            }

            JSONArray commentsArray = edgesJson.getJSONArray("edges");
            for (int i = 0; i < commentsArray.length(); i++) {
                JSONObject currentNode = commentsArray.getJSONObject(i).getJSONObject("node");
                if (instId.equals("@" + currentNode.getString("username"))) return true;
            }

            hasNext = edgesJson.getJSONObject("page_info").getBoolean("has_next_page");
            if (hasNext) endCursor = edgesJson.getJSONObject("page_info").getString("end_cursor");
            edgesJson = null;
        }
        return false;
    }
}