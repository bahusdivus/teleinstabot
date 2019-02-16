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

        ReplayBuilder replayBuilder = new ReplayBuilder("adsasd", 1L, dbHandler);
        replayBuilder.buildReplay();
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
    }

    @Test
    void buildReplay_newUserProvidedUsername_assertRepalyString() {
        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(null);
        String expect = "Сохраняем ник \"@username\"";

        ReplayBuilder replayBuilder = new ReplayBuilder("@username", 1L, dbHandler);
        replayBuilder.buildReplay();
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
    }

    @Test
    void buildReplay_newUserProvidedUsername_assertDbHandlerSaveUserWasCalled() {
        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(null);

        ReplayBuilder replayBuilder = new ReplayBuilder("@username", 1L, dbHandler);
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

        ReplayBuilder replayBuilder = new ReplayBuilder("Получить задание", 1L, dbHandler);
        replayBuilder.buildReplay();
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
    }
    @Test
    void buildReplay_userAskForTaskHaveOne_assertReplayString() {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ArrayList<UserTask> tasks = new ArrayList<>();
        tasks.add(new UserTask(1, "sadad", true, 1, "asfasf", currentTimestamp));
        String expect = "Ваше задание:\n";
        expect += "https://www.instagram.com/p/sadad/\n";
        expect += "Нужен лайк и комментарий от 1 слов\n";
        expect += "Комментарий к заданию: asfasf\n\n";
        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, currentTimestamp, currentTimestamp));
        Mockito.when(dbHandler.getTaskList(1)).thenReturn(tasks);

        ReplayBuilder replayBuilder = new ReplayBuilder("Получить задание", 1L, dbHandler);
        replayBuilder.buildReplay();
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
    }
}