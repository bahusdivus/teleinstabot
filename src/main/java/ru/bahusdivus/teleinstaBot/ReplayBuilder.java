package ru.bahusdivus.teleinstaBot;

import org.json.JSONException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class ReplayBuilder {
    private String messageText;
    private Long chatId;
    private String replayText;
    private DbHandler db;
    private ReplyKeyboardMarkup replyKeyboardMarkup;
    private TaskResultParser parser;
    private Long currentTime;

    ReplayBuilder(String messageText, Long chatId) {
        this.messageText = messageText;
        this.chatId = chatId;
    }

    void setDb(DbHandler db) {
        this.db = db;
    }

    void setParser(TaskResultParser parser) {
        this.parser = parser;
    }

    void setCurrentTime(Long currentTime) {
        this.currentTime = currentTime;
    }

    String getReplayText() {
        return replayText;
    }

    Long getCurrentTime() {return currentTime;}

    private void buildMarkup() {
        replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton("Получить задание"));
        keyboardFirstRow.add(new KeyboardButton("Проверить задание"));
        keyboardFirstRow.add(new KeyboardButton("Разместить ссылку"));

        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

    }

    ReplyKeyboardMarkup getReplyKeyboardMarkup() {
        return replyKeyboardMarkup;
    }

    void buildReplay() throws Exception {
        //Lazy initialization. If class on test there will be stubs
        if (db == null) db = DbHandler.getInstance();
        if (currentTime == null) currentTime = System.currentTimeMillis();
        User user = db.getUserByChatId(chatId);
        if (user == null) {
            if(Pattern.matches("^@[\\w.]+$", messageText)) {
                user = new User(messageText, chatId);
                db.saveUser(user);
                replayText = "Сохраняем ник \"" + messageText + "\"";
                buildMarkup();
            } else {
                replayText = "Для начала работы с ботом вам необходимо зарегистрировать свой аккаунт Instagram. Просто отправьте свой ник, начинающийся с символа @";
                replyKeyboardMarkup = null;
            }
        } else {
            ArrayList<UserTask> tasks = getTaskList(user, db);
            switch (messageText) {
                case "Kill yourself right now, please!":
                    if (user.getId() == 1) throw new Exception("EXIT_REQUEST");
                case "Получить задание":
                    if (tasks == null) {
                        replayText = "Все задания выполнены, можно размещать ссылку";
                    } else {
                        StringBuilder replay = new StringBuilder("Ваше задание:\n\n");
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
                    if (tasks == null) {
                        replayText = "Все задания выполнены, можно размещать ссылку";
                    } else {
                        try {
                            replayText = checkTask(user, tasks);
                        } catch (IOException | JSONException e) {
                            replayText = "Извините, при выполнении запроса произошла ошибка =(\n";
                            replayText += "Перешлите, пожалуйста, нижеследующую информацию @bahusdivus.\nСкучная техническая информация:\n";
                            replayText += e.getMessage();
                        }
                    }
                    break;
                case "Разместить ссылку":
                    if (tasks != null) {
                        replayText = "Прежде, чем вы сможете разместить ссылку, вы должны выполнить все задания.\n";
                        replayText += "Используйте команды \"Получить задание\" и \"Проверить задание\"\n";
                    } else {
                        replayText = getPostingInstructions(user);
                    }
                    break;
                default:
                    UserTask userTask = parseTask(messageText, user.getId());
                    if (userTask == null) {
                        replayText = "Команда не распознана =(";
                    } else {
                        if (tasks != null) {
                            replayText = "Прежде, чем вы сможете разместить ссылку, вы должны выполнить все задания.\n";
                            replayText += "Используйте команды \"Получить задание\" и \"Проверить задание\"\n";
                        } else {
                            replayText = postTask(user, userTask, db);
                        }
                    }

            }
            buildMarkup();
        }
    }

    abstract String postTask(User user, UserTask userTask, DbHandler db);

    abstract String getPostingInstructions(User user);

    abstract ArrayList<UserTask> getTaskList(User user, DbHandler db);

    private String checkTask(User user, ArrayList<UserTask> tasks) throws IOException, JSONException {
        //Lazy initialization. If class on test there will be stub
        if (parser == null) parser = new TaskResultParser();
        StringBuilder replay = new StringBuilder();
        for(UserTask task : tasks) {
            boolean isPass = true;
            StringBuilder taskResult = new StringBuilder("https://www.instagram.com/p/" + task.getPostId() + "/\n");
            if (task.isLikeRequired()) {
                if (!parser.checkLike(user.getInstId(), task.getPostId())) {
                    taskResult.append("Нужен лайк\n");
                    isPass = false;
                }
            }
            if (task.getCommentRequiredLength() > 0) {
                if (!parser.checkComment(user.getInstId(), task.getPostId(), task.getCommentRequiredLength())) {
                    taskResult.append("Нужен комментарий не менее ");
                    taskResult.append(task.getCommentRequiredLength());
                    taskResult.append(" слов\n");
                    isPass = false;
                }
            }
            if (isPass) {
                db.compliteTask(user.getId(), task.getId());
            } else {
                if (task.getComment().length() > 0) {
                    taskResult.append(task.getComment());
                    taskResult.append("\n\n");
                }
                replay.append(taskResult.toString());
            }
        }
        if (replay.length() == 0) {
            return "Вы выполнили все условия и можете разместить ссылку!";
        }
        return replay.toString();
    }

    String getInterval(Long ms) {
        long x = ms / 1000;
        long seconds = x % 60;
        x /= 60;
        long minutes = x % 60;
        x /= 60;
        long hours = x % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private UserTask parseTask (String task, int userId) {
        String postId;
        boolean isLikeRequired = false;
        int commentRequiredLength = 0;
        String comment = null;

        String[] lines = task.split("\n");
        if (lines.length > 4 || lines.length < 2) return null;

        int i = 0;
        Pattern p = Pattern.compile("(.*?)instagram.com/p/(.*?)/(.*?)", Pattern.DOTALL);
        Matcher m = p.matcher(lines[i]);
        if (m.matches()) {
            postId = m.group(2);
            i++;
        } else return null;

        p = Pattern.compile("(.*?)(лайк|Лайк)(.*?)", Pattern.DOTALL);
        m = p.matcher(lines[i]);
        if (m.matches()) {
            isLikeRequired = true;
            i++;
        }

        if (lines.length > i) {
            p = Pattern.compile("(.*?)(комментарий|Комментарий)(.*?)([0-9]+)(.*?)", Pattern.DOTALL);
            m = p.matcher(lines[i]);
            if (m.matches()) {
                commentRequiredLength = Integer.parseInt(m.group(4));
                if (commentRequiredLength > 4) commentRequiredLength = 4;
                i++;
            }
        }

        if (lines.length > i) {
            comment = lines[i];
        }

        if (!isLikeRequired && commentRequiredLength == 0) return null;

        return new UserTask(userId, postId, isLikeRequired, commentRequiredLength, comment, new Timestamp(System.currentTimeMillis()));
    }
}