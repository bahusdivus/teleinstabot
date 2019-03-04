package ru.bahusdivus.teleinstaBot;

import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

class ReplayBuilder7TasksTest {

    @Test
    void buildReplay_userAskForTaskHaveNone_assertReplayString() {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        String expect = "Все задания выполнены, можно размещать ссылку";
        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskListLast7(1)).thenReturn(null);

        ReplayBuilder replayBuilder = new ReplayBuilder7Tasks("Получить задание", 1L);
        replayBuilder.setDb(dbHandler);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
        Assertions.assertNotNull(replayBuilder.getReplyKeyboardMarkup());
    }

    @Test
    void buildReplay_userAskForTaskHaveOne_assertReplayString() {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ArrayList<UserTask> tasks = new ArrayList<>();
        tasks.add(new UserTask(1, "sadad", true, 1, "asfasf", currentTimestamp));
        String expect = "Ваше задание:\n\n";
        expect += "https://www.instagram.com/p/sadad/\n";
        expect += "Нужен лайк и комментарий от 1 слов\n";
        expect += "Комментарий к заданию: asfasf\n\n";
        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskListLast7(1)).thenReturn(tasks);

        ReplayBuilder replayBuilder = new ReplayBuilder7Tasks("Получить задание", 1L);
        replayBuilder.setDb(dbHandler);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
        Assertions.assertNotNull(replayBuilder.getReplyKeyboardMarkup());
    }

    @Test
    void buildReplay_userCheckTaskNoTasks_assertReplayString() {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskListLast7(1)).thenReturn(null);

        String expect = "Все задания выполнены, можно размещать ссылку";

        ReplayBuilder replayBuilder = new ReplayBuilder7Tasks("Проверить задание", 1L);
        replayBuilder.setDb(dbHandler);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
        Assertions.assertNotNull(replayBuilder.getReplyKeyboardMarkup());
    }

    @Test
    void buildReplay_userCheckTaskAllDone_assertReplayString() throws IOException {
        TaskResultParser parser = Mockito.mock(TaskResultParser.class);
        Mockito.when(parser.checkComment(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(parser.checkLike(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ArrayList<UserTask> tasks = new ArrayList<>();
        tasks.add(new UserTask(1, "sadad", true, 1, "asfasf", currentTimestamp));

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskListLast7(1)).thenReturn(tasks);

        String expect = "Вы выполнили все условия и можете разместить ссылку!";

        ReplayBuilder replayBuilder = new ReplayBuilder7Tasks("Проверить задание", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.setParser(parser);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
        Assertions.assertNotNull(replayBuilder.getReplyKeyboardMarkup());
    }

    @Test
    void buildReplay_userCheckTask_throwsException() throws IOException {
        TaskResultParser parser = Mockito.mock(TaskResultParser.class);
        Mockito.when(parser.checkComment(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenThrow(JSONException.class);
        Mockito.when(parser.checkLike(Mockito.anyString(), Mockito.anyString())).thenThrow(JSONException.class);

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ArrayList<UserTask> tasks = new ArrayList<>();
        tasks.add(new UserTask(1, "sadad", true, 1, "asfasf", currentTimestamp));

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskListLast7(1)).thenReturn(tasks);

        ReplayBuilder replayBuilder = new ReplayBuilder7Tasks("Проверить задание", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.setParser(parser);
        String expexted = "Извините, при выполнении запроса произошла ошибка =(\n" +
                "Перешлите, пожалуйста, нижеследующую информацию @bahusdivus.\n" +
                "Скучная техническая информация:\n" +
                "null";

        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Assertions.assertEquals(expexted, replayBuilder.getReplayText());
    }

    @Test
    void buildReplay_userCheckTaskNoLike_assertReplayString() throws IOException {
        TaskResultParser parser = Mockito.mock(TaskResultParser.class);
        Mockito.when(parser.checkComment(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(parser.checkLike(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ArrayList<UserTask> tasks = new ArrayList<>();
        tasks.add(new UserTask(1, "sadad", true, 1, "asfasf", currentTimestamp));

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskListLast7(1)).thenReturn(tasks);

        String expect = "https://www.instagram.com/p/sadad/\nНужен лайк\nasfasf\n\n";

        ReplayBuilder replayBuilder = new ReplayBuilder7Tasks("Проверить задание", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.setParser(parser);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
        Assertions.assertNotNull(replayBuilder.getReplyKeyboardMarkup());
    }

    @Test
    void buildReplay_userCheckTaskNoComment_assertReplayString() throws IOException {
        TaskResultParser parser = Mockito.mock(TaskResultParser.class);
        Mockito.when(parser.checkComment(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(false);
        Mockito.when(parser.checkLike(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ArrayList<UserTask> tasks = new ArrayList<>();
        tasks.add(new UserTask(1, "sadad", true, 1, "asfasf", currentTimestamp));

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskListLast7(1)).thenReturn(tasks);

        String expect = "https://www.instagram.com/p/sadad/\nНужен комментарий не менее 1 слов\nasfasf\n\n";

        ReplayBuilder replayBuilder = new ReplayBuilder7Tasks("Проверить задание", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.setParser(parser);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
        Assertions.assertNotNull(replayBuilder.getReplyKeyboardMarkup());
    }

    @Test
    void buildReplay_userCheckTaskNoLikeNoComment_assertReplayString() throws IOException {
        TaskResultParser parser = Mockito.mock(TaskResultParser.class);
        Mockito.when(parser.checkComment(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(false);
        Mockito.when(parser.checkLike(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ArrayList<UserTask> tasks = new ArrayList<>();
        tasks.add(new UserTask(1, "sadad", true, 1, "asfasf", currentTimestamp));

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskListLast7(1)).thenReturn(tasks);

        String expect = "https://www.instagram.com/p/sadad/\nНужен лайк\nНужен комментарий не менее 1 слов\nasfasf\n\n";

        ReplayBuilder replayBuilder = new ReplayBuilder7Tasks("Проверить задание", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.setParser(parser);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
        Assertions.assertNotNull(replayBuilder.getReplyKeyboardMarkup());
    }

    @Test
    void buildReplay_userCheckTaskAllDone_assertDbWasCalled() throws IOException {
        TaskResultParser parser = Mockito.mock(TaskResultParser.class);
        Mockito.when(parser.checkComment(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(parser.checkLike(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ArrayList<UserTask> tasks = new ArrayList<>();
        tasks.add(new UserTask(3,1, "sadad", true, 1, "asfasf", currentTimestamp));

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskListLast7(1)).thenReturn(tasks);

        ReplayBuilder replayBuilder = new ReplayBuilder7Tasks("Проверить задание", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.setParser(parser);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Mockito.verify(dbHandler).compliteTask(1,3);
    }

    @Test
    void buildReplay_userCheckTaskNotAllDone_assertDbWasNotCalled() throws IOException {

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ArrayList<UserTask> tasks = new ArrayList<>();
        tasks.add(new UserTask(3,1, "sadad", true, 1, "asfasf", currentTimestamp));

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskListLast7(1)).thenReturn(tasks);

        TaskResultParser parser = Mockito.mock(TaskResultParser.class);

        ReplayBuilder replayBuilder = new ReplayBuilder7Tasks("Проверить задание", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.setParser(parser);

        Mockito.when(parser.checkComment(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(false);
        Mockito.when(parser.checkLike(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());

        Mockito.when(parser.checkComment(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(parser.checkLike(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());

        Mockito.when(parser.checkComment(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(false);
        Mockito.when(parser.checkLike(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());

        Mockito.verify(dbHandler, Mockito.never()).compliteTask(1,3);
    }

    @Test
    void buildReplay_userWantToPostLinkNotDone_assertReplayString() {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ArrayList<UserTask> tasks = new ArrayList<>();
        tasks.add(new UserTask(1, "sadad", true, 1, "asfasf", currentTimestamp));
        String expect = "Прежде, чем вы сможете разместить ссылку, вы должны выполнить все задания.\n";
        expect += "Используйте команды \"Получить задание\" и \"Проверить задание\"\n";
        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskListLast7(1)).thenReturn(tasks);

        ReplayBuilder replayBuilder = new ReplayBuilder7Tasks("Разместить ссылку", 1L);
        replayBuilder.setDb(dbHandler);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
        Assertions.assertNotNull(replayBuilder.getReplyKeyboardMarkup());
    }

    @Test
    void buildReplay_userWantToPostLinkAllDone_assertReplayString() {
        long ms = System.currentTimeMillis();
        Timestamp compliteTimestamp = new Timestamp(ms - 25 * 60 * 60 * 1000);

        String expect = "Для размещения ссылки отправьте сообщение, содержащее сдедующие строки:\n";
        expect += "1. Ссылка на пост в Instagram, например https://www.instagram.com/p/BtPN5xJBojL/\n";
        expect += "2. Если нужен лайк, строка должна содержать слово \"лайк\". Если лайк не нужен, пропустите эту строку.\n";
        expect += "3. Если нужен комментарий, строка должна содержать слово \"комментарий\" и минимальное количество слов в коментарии (если нужно). Минимальное количество слов в коментарии не может быть больше 4. Например: \"Комментарий от 3 слов\" или просто \"комментарий 3\". Если комментарий не нужен, пропустите эту строку.\n";
        expect += "4. Если вы хотите оставить какое то пояснение к своему заданию, вы можете сделать это в этой строке.\n";

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, compliteTimestamp, compliteTimestamp));
        Mockito.when(dbHandler.getTaskListLast7(1)).thenReturn(null);

        ReplayBuilder replayBuilder = new ReplayBuilder7Tasks("Разместить ссылку", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.setCurrentTime(ms);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
        Assertions.assertNotNull(replayBuilder.getReplyKeyboardMarkup());
    }

    @Test
    void buildReplay_userPostLinkNotDone_assertReplayString() {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ArrayList<UserTask> tasks = new ArrayList<>();
        tasks.add(new UserTask(1, "sadad", true, 1, "asfasf", currentTimestamp));
        String expect = "Прежде, чем вы сможете разместить ссылку, вы должны выполнить все задания.\n";
        expect += "Используйте команды \"Получить задание\" и \"Проверить задание\"\n";
        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskListLast7(1)).thenReturn(tasks);

        String linkTest = "https://www.instagram.com/p/BtPN5xJBojL/\n";
        linkTest += "Нужен лайк\n";
        linkTest += "Комментарий от 3 слов\n";
        linkTest += "Мама мыла раму";

        ReplayBuilder replayBuilder = new ReplayBuilder7Tasks(linkTest, 1L);
        replayBuilder.setDb(dbHandler);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
        Mockito.verify(dbHandler, Mockito.never()).saveTask(Mockito.any(UserTask.class));
        Assertions.assertNotNull(replayBuilder.getReplyKeyboardMarkup());
    }

    @Test
    void buildReplay_userPostLinkAllDoneWrongFormat_assertReplayString() {
        long ms = System.currentTimeMillis();
        Timestamp compliteTimestamp = new Timestamp(ms - 25 * 60 * 60 * 1000);

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, compliteTimestamp, compliteTimestamp));
        Mockito.when(dbHandler.getTaskListLast7(1)).thenReturn(null);

        String linkTest = "https://www.instagram.com/p/\n";
        linkTest += "Нужен лайк\n";
        linkTest += "Комментарий от 3 слов\n";
        linkTest += "Мама мыла раму";
        ReplayBuilder replayBuilder = new ReplayBuilder7Tasks(linkTest, 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.setCurrentTime(ms);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Assertions.assertEquals("Команда не распознана =(", replayBuilder.getReplayText());
        Assertions.assertNotNull(replayBuilder.getReplyKeyboardMarkup());

        linkTest = "https://www.instagram.com/p/BtPN5xJBojL/\n";
        linkTest += "Мама мыла раму";
        ReplayBuilder replayBuilder2 = new ReplayBuilder7Tasks(linkTest, 1L);
        replayBuilder2.setDb(dbHandler);
        replayBuilder2.setCurrentTime(ms);
        Assertions.assertDoesNotThrow(()->replayBuilder2.buildReplay());

        Assertions.assertEquals("Команда не распознана =(", replayBuilder2.getReplayText());
        Assertions.assertNotNull(replayBuilder2.getReplyKeyboardMarkup());

        Mockito.verify(dbHandler, Mockito.never()).saveTask(Mockito.any(UserTask.class));
    }

    @Test
    void buildReplay_userPostLinkAllDone_assertReplayString() {
        long ms = System.currentTimeMillis();
        Timestamp compliteTimestamp = new Timestamp(ms - 25 * 60 * 60 * 1000);

        String expect = "Ссылка размещена:\n";
        expect += "https://www.instagram.com/p/BtPN5xJBojL/\n";
        expect += "Нужен лайк\n";
        expect += "Комментарий от 3 слов\n";
        expect += "Мама мыла раму";

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, compliteTimestamp, compliteTimestamp));
        Mockito.when(dbHandler.getTaskListLast7(1)).thenReturn(null);

        String linkTest = "https://www.instagram.com/p/BtPN5xJBojL/\n";
        linkTest += "Нужен лайк\n";
        linkTest += "Комментарий от 3 слов\n";
        linkTest += "Мама мыла раму";

        ReplayBuilder replayBuilder = new ReplayBuilder7Tasks(linkTest, 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.setCurrentTime(ms);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
        Mockito.verify(dbHandler).saveTask(ArgumentMatchers.eq(new UserTask(1, "BtPN5xJBojL", true, 3, "Мама мыла раму", new Timestamp(ms))));
        Assertions.assertNotNull(replayBuilder.getReplyKeyboardMarkup());
    }

    @Test
    void buildReplay_meanlessString_assertReplayString() {
        Timestamp compliteTimestamp = new Timestamp(System.currentTimeMillis());

        String expect = "Команда не распознана =(";

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, compliteTimestamp, compliteTimestamp));
        Mockito.when(dbHandler.getTaskListLast7(1)).thenReturn(null);

        ReplayBuilder replayBuilder = new ReplayBuilder7Tasks("Meanless String", 1L);
        replayBuilder.setDb(dbHandler);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
        Assertions.assertNotNull(replayBuilder.getReplyKeyboardMarkup());
    }

}
