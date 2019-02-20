package ru.bahusdivus.teleinstaBot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.util.ArrayList;

class ReplayBuilderTest {

    @Test
    void buildReplay_newUserNoUsername_assertReplayString() {
        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(null);
        String expect = "Для начала работы с ботом вам необходимо зарегистрировать свой аккаунт Instagram. Просто отправьте свой ник, начинающийся с символа @";

        ReplayBuilder replayBuilder = new ReplayBuilder("adsasd", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.buildReplay();
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
    }

    @Test
    void buildReplay_newUserProvidedUsername_assertRepalyString() {
        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(null);
        String expect = "Сохраняем ник \"@username\"";

        ReplayBuilder replayBuilder = new ReplayBuilder("@username", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.buildReplay();
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
    }

    @Test
    void buildReplay_newUserProvidedUsername_assertDbHandlerSaveUserWasCalled() {
        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(null);

        ReplayBuilder replayBuilder = new ReplayBuilder("@username", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.buildReplay();
        Mockito.verify(dbHandler).saveUser(ArgumentMatchers.eq(new User("@username", 1L)));
    }

    @Test
    void buildReplay_userAskForTaskHaveNone_assertReplayString() {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        String expect = "Все задания выполнены, можно размещать ссылку";
        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskList(1)).thenReturn(null);

        ReplayBuilder replayBuilder = new ReplayBuilder("Получить задание", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.buildReplay();
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
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
        Mockito.when(dbHandler.getTaskList(1)).thenReturn(tasks);

        ReplayBuilder replayBuilder = new ReplayBuilder("Получить задание", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.buildReplay();
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
    }

    @Test
    void buildReplay_userCheckTaskAllDone_assertReplayString() {
        TaskResultParser parser = Mockito.mock(TaskResultParser.class);
        Mockito.when(parser.checkComment(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(parser.checkLike(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ArrayList<UserTask> tasks = new ArrayList<>();
        tasks.add(new UserTask(1, "sadad", true, 1, "asfasf", currentTimestamp));

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskList(1)).thenReturn(tasks);

        String expect = "Вы выполнили все условия и можете в течении суток разместить одну ссылку!";

        ReplayBuilder replayBuilder = new ReplayBuilder("Проверить задание", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.setParser(parser);
        replayBuilder.buildReplay();
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
    }

    @Test
    void buildReplay_userCheckTaskNoLike_assertReplayString() {
        TaskResultParser parser = Mockito.mock(TaskResultParser.class);
        Mockito.when(parser.checkComment(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(parser.checkLike(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ArrayList<UserTask> tasks = new ArrayList<>();
        tasks.add(new UserTask(1, "sadad", true, 1, "asfasf", currentTimestamp));

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskList(1)).thenReturn(tasks);

        String expect = "https://www.instagram.com/p/sadad/\nНужен лайк\nasfasf\n\n";

        ReplayBuilder replayBuilder = new ReplayBuilder("Проверить задание", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.setParser(parser);
        replayBuilder.buildReplay();
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
    }

    @Test
    void buildReplay_userCheckTaskNoComment_assertReplayString() {
        TaskResultParser parser = Mockito.mock(TaskResultParser.class);
        Mockito.when(parser.checkComment(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(false);
        Mockito.when(parser.checkLike(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ArrayList<UserTask> tasks = new ArrayList<>();
        tasks.add(new UserTask(1, "sadad", true, 1, "asfasf", currentTimestamp));

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskList(1)).thenReturn(tasks);

        String expect = "https://www.instagram.com/p/sadad/\nНужен комментарий не менее 1 слов\nasfasf\n\n";

        ReplayBuilder replayBuilder = new ReplayBuilder("Проверить задание", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.setParser(parser);
        replayBuilder.buildReplay();
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
    }

    @Test
    void buildReplay_userCheckTaskNoLikeNoComment_assertReplayString() {
        TaskResultParser parser = Mockito.mock(TaskResultParser.class);
        Mockito.when(parser.checkComment(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(false);
        Mockito.when(parser.checkLike(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ArrayList<UserTask> tasks = new ArrayList<>();
        tasks.add(new UserTask(1, "sadad", true, 1, "asfasf", currentTimestamp));

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskList(1)).thenReturn(tasks);

        String expect = "https://www.instagram.com/p/sadad/\nНужен лайк\nНужен комментарий не менее 1 слов\nasfasf\n\n";

        ReplayBuilder replayBuilder = new ReplayBuilder("Проверить задание", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.setParser(parser);
        replayBuilder.buildReplay();
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
    }

    @Test
    void buildReplay_userCheckTaskAllDone_assertDbWasCalled() {
        TaskResultParser parser = Mockito.mock(TaskResultParser.class);
        Mockito.when(parser.checkComment(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(parser.checkLike(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ArrayList<UserTask> tasks = new ArrayList<>();
        tasks.add(new UserTask(3,1, "sadad", true, 1, "asfasf", currentTimestamp));

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskList(1)).thenReturn(tasks);

        ReplayBuilder replayBuilder = new ReplayBuilder("Проверить задание", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.setParser(parser);
        replayBuilder.buildReplay();
        Mockito.verify(dbHandler).compliteTask(1,3);
    }

    @Test
    void buildReplay_userCheckTaskNotAllDone_assertDbWasNotCalled() {

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ArrayList<UserTask> tasks = new ArrayList<>();
        tasks.add(new UserTask(3,1, "sadad", true, 1, "asfasf", currentTimestamp));

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskList(1)).thenReturn(tasks);

        TaskResultParser parser = Mockito.mock(TaskResultParser.class);

        ReplayBuilder replayBuilder = new ReplayBuilder("Проверить задание", 1L);
        replayBuilder.setDb(dbHandler);
        replayBuilder.setParser(parser);

        Mockito.when(parser.checkComment(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(false);
        Mockito.when(parser.checkLike(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        replayBuilder.buildReplay();

        Mockito.when(parser.checkComment(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
        Mockito.when(parser.checkLike(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        replayBuilder.buildReplay();

        Mockito.when(parser.checkComment(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(false);
        Mockito.when(parser.checkLike(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        replayBuilder.buildReplay();

        Mockito.verify(dbHandler, Mockito.never()).compliteTask(1,3);
    }
}