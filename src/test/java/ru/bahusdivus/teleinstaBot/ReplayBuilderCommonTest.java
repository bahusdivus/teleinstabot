package ru.bahusdivus.teleinstaBot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.sql.Timestamp;

class ReplayBuilderCommonTest {

    @Test
    void buildReplay_newUserNoUsername_assertReplayString() {
        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(null);
        String expect = "Для начала работы с ботом вам необходимо зарегистрировать свой аккаунт Instagram. Просто отправьте свой ник, начинающийся с символа @";

        ReplayBuilder replayBuilder = new ReplayBuilder24Hours("adsasd", 1L);
        replayBuilder.setDb(dbHandler);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
        Assertions.assertNull(replayBuilder.getReplyKeyboardMarkup());
    }

    @Test
    void buildReplay_newUserProvidedUsername_assertRepalyString() {
        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(null);
        String expect = "Сохраняем ник \"@username\"";

        ReplayBuilder replayBuilder = new ReplayBuilder24Hours("@username", 1L);
        replayBuilder.setDb(dbHandler);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Assertions.assertEquals(expect, replayBuilder.getReplayText());
        Assertions.assertNotNull(replayBuilder.getReplyKeyboardMarkup());
    }

    @Test
    void buildReplay_newUserProvidedUsername_assertDbHandlerSaveUserWasCalled() {
        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(null);

        ReplayBuilder replayBuilder = new ReplayBuilder24Hours("@username", 1L);
        replayBuilder.setDb(dbHandler);
        Assertions.assertDoesNotThrow(()->replayBuilder.buildReplay());
        Mockito.verify(dbHandler).saveUser(ArgumentMatchers.eq(new User("@username", 1L)));
    }

    @Test
    void buildReplay_killCommand_throwingException() {
        Timestamp compliteTimestamp = new Timestamp(System.currentTimeMillis());

        DbHandler dbHandler = Mockito.mock(DbHandler.class);
        Mockito.when(dbHandler.getUserByChatId(1L)).thenReturn(new User(1, "@username", 1L, compliteTimestamp, compliteTimestamp));
        Mockito.when(dbHandler.getTaskListLast7(1)).thenReturn(null);

        ReplayBuilder replayBuilder = new ReplayBuilder7Tasks("Kill yourself right now, please!", 1L);
        replayBuilder.setDb(dbHandler);

        Assertions.assertThrows(Exception.class, ()->replayBuilder.buildReplay(), "EXIT_REQUEST");
    }

}
