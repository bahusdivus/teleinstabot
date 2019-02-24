package ru.bahusdivus.teleinstaBot;

import java.util.ArrayList;

public class ReplayBuilder7Tasks extends ReplayBuilder {

    ReplayBuilder7Tasks(String messageText, Long chatId) {
        super(messageText, chatId);
    }

    String postTask(User user, UserTask userTask, DbHandler db) {

        db.saveTask(userTask);
        StringBuilder replayText = new StringBuilder("Ссылка размещена:\n");
        replayText.append("https://www.instagram.com/p/").append(userTask.getPostId()).append("/\n");
        if (userTask.isLikeRequired()) replayText.append("Нужен лайк\n");
        if (userTask.getCommentRequiredLength() > 0)
            replayText.append("Комментарий от ").append(userTask.getCommentRequiredLength()).append(" слов\n");
        replayText.append(userTask.getComment());
        return replayText.toString();
    }

    String getPostingInstructions(User user) {
        String replayText = "Для размещения ссылки отправьте сообщение, содержащее сдедующие строки:\n";
        replayText += "1. Ссылка на пост в Instagram, например https://www.instagram.com/p/BtPN5xJBojL/\n";
        replayText += "2. Если нужен лайк, строка должна содержать слово \"лайк\". Если лайк не нужен, пропустите эту строку.\n";
        replayText += "3. Если нужен комментарий, строка должна содержать слово \"комментарий\" и минимальное количество слов в коментарии (если нужно). Минимальное количество слов в коментарии не может быть больше 4. Например: \"Комментарий от 3 слов\" или просто \"комментарий 3\". Если комментарий не нужен, пропустите эту строку.\n";
        replayText += "4. Если вы хотите оставить какое то пояснение к своему заданию, вы можете сделать это в этой строке.\n";
        return replayText;
    }

    ArrayList<UserTask> getTaskList(User user, DbHandler db) {
        return db.getTaskListLast7(user.getId());
    }
}
