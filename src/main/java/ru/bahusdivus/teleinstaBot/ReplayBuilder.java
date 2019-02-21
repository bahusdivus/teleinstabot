package ru.bahusdivus.teleinstaBot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ReplayBuilder {
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

    public void setDb (DbHandler db) {
        this.db = db;
    }

    public void setParser (TaskResultParser parser) {
        this.parser = parser;
    }

    public void setCurrentTime (Long currentTime) {
        this.currentTime = currentTime;
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
        if (currentTime == null) currentTime = System.currentTimeMillis();
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
                case "Разместить ссылку":
                    tasks = db.getTaskList(user.getId());
                    if (tasks != null) {
                        replayText = "Прежде, чем вы сможете разместить ссылку, вы должны выполнить все задания.\n";
                        replayText += "Используйте команды \"Получить задание\" и \"Проверить задание\"\n";
                    } else {
                        long difference = currentTime - user.getTaskComplite().getTime();
                        if (difference > (24 * 60 * 60 * 1000)) {
                            replayText = "Для размещения ссылки отправьте сообщение, содержащее сдедующие строки:\n";
                            replayText += "1. Ссылка на пост в Instagram, например https://www.instagram.com/p/BtPN5xJBojL/\n";
                            replayText += "2. Если нужен лайк, строка должна содержать слово \"лайк\". Если лайк не нужен, пропустите эту строку.\n";
                            replayText += "3. Если нужен комментарий, строка должна содержать слово \"комментарий\" и минимальное количество слов в коментарии (если нужно). Минимальное количество слов в коментарии не может быть больше 4. Например: \"Комментарий от 3 слов\" или просто \"комментарий 3\". Если комментарий не нужен, пропустите эту строку.\n";
                            replayText += "4. Если вы хотите оставить какое то пояснение к своему заданию, вы можете сделать это в этой строке.\n";
                        } else {
                            replayText = "С момента предыдущего размещения прошло " + getInterval(difference) + "\n";
                            replayText += "Вы сможете разместить ссылку через " + getInterval((24 * 60 * 60 * 1000) - difference) + "\n";
                        }
                    }
                    break;
                default:
                    UserTask userTask = parseTask(messageText, user.getId());
                    if (userTask == null) {
                        replayText = "Команда не распознана =(";
                    } else {
                        tasks = db.getTaskList(user.getId());
                        if (tasks != null) {
                            replayText = "Прежде, чем вы сможете разместить ссылку, вы должны выполнить все задания.\n";
                            replayText += "Используйте команды \"Получить задание\" и \"Проверить задание\"\n";
                        } else {
                            long difference = currentTime - user.getTaskComplite().getTime();
                            if (difference < (24 * 60 * 60 * 1000)) {
                                replayText = "С момента предыдущего размещения прошло " + getInterval(difference) + "\n";
                                replayText += "Вы сможете разместить ссылку через " + getInterval((24 * 60 * 60 * 1000) - difference) + "\n";
                            } else {
                                replayText = "Ссылка размещена:\n";
                                replayText += "https://www.instagram.com/p/" + userTask.getPostId() + "/\n";
                                if (userTask.isLikeRequired()) replayText += "Нужен лайк\n";
                                if (userTask.getCommentRequiredLength() > 0)
                                    replayText += "Комментарий от " + userTask.getCommentRequiredLength() + " слов\n";
                                replayText += userTask.getComment();
                            }
                        }
                    }

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
            return "Вы выполнили все условия и можете разместить ссылку!";
        }
        return replay.toString();
    }

    private String getInterval(Long ms) {
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
            p = Pattern.compile("(.*?)(комментарий|Комментарий)(.*?)([0-9]{1,})(.*?)", Pattern.DOTALL);
            m = p.matcher(lines[i]);
            if (m.matches()) {
                commentRequiredLength = Integer.parseInt(m.group(4));
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