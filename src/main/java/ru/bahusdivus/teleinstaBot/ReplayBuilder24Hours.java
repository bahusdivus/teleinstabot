package ru.bahusdivus.teleinstaBot;

import java.util.ArrayList;

class ReplayBuilder24Hours extends ReplayBuilder {

    ReplayBuilder24Hours(String messageText, Long chatId) {
        super(messageText, chatId);
    }

    String postTask(User user, UserTask userTask, DbHandler db) {
        long difference = getCurrentTime() - user.getTaskComplite().getTime();
        String replayText;
        if (difference < (24 * 60 * 60 * 1000)) {
            replayText = "С момента предыдущего размещения прошло " + getInterval(difference) + "\n";
            replayText += "Вы сможете разместить ссылку через " + getInterval((24 * 60 * 60 * 1000) - difference) + "\n";
        } else {
            db.saveTask(userTask);
            replayText = "Ссылка размещена:\n";
            replayText += "https://www.instagram.com/p/" + userTask.getPostId() + "/\n";
            if (userTask.isLikeRequired()) replayText += "Нужен лайк\n";
            if (userTask.getCommentRequiredLength() > 0)
                replayText += "Комментарий от " + userTask.getCommentRequiredLength() + " слов\n";
            replayText += userTask.getComment();
        }
        return replayText;
    }

    String getPostingInstructions(User user) {
        long difference = getCurrentTime() - user.getTaskComplite().getTime();
        String replayText;
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
        return replayText;
    }

    ArrayList<UserTask> getTaskList(User user, DbHandler db) {
        return db.getTaskListLast24Hours(user.getId());
    }
}