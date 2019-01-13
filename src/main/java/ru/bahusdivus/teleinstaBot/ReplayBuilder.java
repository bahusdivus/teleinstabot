package ru.bahusdivus.teleinstaBot;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.bahusdivus.teleinstaBot.Scrapper.TibInstagramScrapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

class ReplayBuilder {
    private String messageText;
    private Long chatId;
    private String replayText;
    private DbHandler db;
    private ReplyKeyboardMarkup replyKeyboardMarkup;

    ReplayBuilder(String messageText, Long chatId) {
        this.messageText = messageText;
        this.chatId = chatId;
        db = DbHandler.getInstance();
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
//            Pattern p = Pattern.compile("^@@[A-Za-z0-9\\._]+", Pattern.DOTALL);
//            Matcher m = p.matcher(messageText);
//            if(m.matches()) {
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
                    replayText = "Тут будет задание";
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
        StringBuilder replay = new StringBuilder();
        ArrayList<UserTask> taskList = db.getTaskList(user.getId());
        for(UserTask task : taskList) {
            if (task.isLikeRequired()) {
                if (!checkLike(user.getInstId(), task.getPostId())) replay.append("Нужен лайк\n");
            }
            if (task.getCommentRequiredLength() > 0) {
                if (!checkComment(user.getInstId(), task.getPostId(), task.getCommentRequiredLength())) {
                    replay.append("Нужен комментарий не менее ");
                    replay.append(task.getCommentRequiredLength());
                    replay.append(" слов\n");
                }
            }
            if (task.getComment().length() > 0) {
                replay.append(task.getComment());
                replay.append(System.lineSeparator());
                replay.append(System.lineSeparator());
            }
            replay.append(task.toString()).append("\n");
        }
        replay.append("Тут будет результат проверки");
        return replay.toString();
    }

    private boolean checkComment(String instId, String postId, int commentsRequiresLength) {
        String dataResult = null;
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new URL("https://www.instagram.com/p/" + postId + "/").openConnection().getInputStream(),
                        StandardCharsets.UTF_8))){
            String inputLine;
            while ((inputLine = in.readLine()) != null){
                Pattern p = Pattern.compile("(.*?)_sharedData = (.*?);</script>(.*?)", Pattern.DOTALL);
                Matcher m = p.matcher(inputLine);
                if (m.matches()) {
                    dataResult = m.group(2);
                    break;
                }
            }
        } catch (Exception e) {
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
                    TibInstagramScrapper scrapper = TibInstagramScrapper.getInstance();
                    endCursor = URLEncoder.encode(Objects.requireNonNull(endCursor), StandardCharsets.UTF_8.toString());
                    String url = "https://www.instagram.com/graphql/query/?query_hash=f0986789a5c5d17c2400faebf16efd0d&variables=%7B%22shortcode%22%3A%22BrKs5pyltwM%22%2C%22first%22%3A32%2C%22after%22%3A%22" + endCursor + "%22%7D";
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
        return false;
    }
}