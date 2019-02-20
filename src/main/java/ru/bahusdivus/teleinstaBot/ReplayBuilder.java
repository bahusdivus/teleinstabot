package ru.bahusdivus.teleinstaBot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

class ReplayBuilder {
    private String messageText;
    private Long chatId;
    private String replayText;
    private DbHandler db;
    private ReplyKeyboardMarkup replyKeyboardMarkup;
    private TaskResultParser parser;

    ReplayBuilder(String messageText, Long chatId) {
        this.messageText = messageText;
        this.chatId = chatId;
    }

    public void setDb (DbHandler db) {
        this.db = db;
    }

    public void setParser (TaskResultParser parser) {
        this.parser = parser;
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
        if (db == null) db = DbHandler.getInstance();
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
            ArrayList<UserTask> tasks;
            switch (messageText) {
                case "Получить задание":
                    tasks = db.getTaskList(user.getId());
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
                    tasks = db.getTaskList(user.getId());
                    if (tasks == null) {
                        replayText = "Все задания выполнены, можно размещать ссылку";
                    } else {
                        replayText = checkTask(user, tasks);
                    }
                    break;
                default:
                    replayText = "Тут будут парситься другие строки";
            }
        }
        buildMarkup();
    }

    private String checkTask(User user, ArrayList<UserTask> tasks) {
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
            db.setTaskCompliteTime(user);
            return "Вы выполнили все условия и можете в течении суток разместить одну ссылку!";
        }
        return replay.toString();
    }

}