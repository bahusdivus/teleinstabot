package ru.bahusdivus.teleinstaBot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


class InstagrammBotTest {

    private InstagrammBot bot = new InstagrammBot();

    @Test
    void onUpdateReceived_emptyUpdate_doesNotThrowException() {
        Update update = Mockito.mock(Update.class);
        Mockito.when(update.hasMessage()).thenReturn(false);

        Assertions.assertDoesNotThrow(() -> bot.onUpdateReceived(update));
    }

    @Test
    void onUpdateReceived_hasMessageWithText_executesExecuteMethod() throws TelegramApiException {
        Message message = Mockito.mock(Message.class);
        Mockito.when(message.hasText()).thenReturn(true);
        Mockito.when(message.getChatId()).thenReturn(1L);

        Update update = Mockito.mock(Update.class);
        Mockito.when(update.hasMessage()).thenReturn(true);
        Mockito.when(update.getMessage()).thenReturn(message);

        ReplayBuilder replayBuilder = Mockito.mock(ReplayBuilder7Tasks.class);
        Mockito.when(replayBuilder.getReplayText()).thenReturn("blabla");
        Mockito.when(replayBuilder.getReplyKeyboardMarkup()).thenReturn(null);

        bot.setReplayBuilder(replayBuilder);
        InstagrammBot instagrammBot = Mockito.spy(bot);
        Mockito.doReturn(null).when((TelegramLongPollingBot)instagrammBot).execute(Mockito.any(SendMessage.class));

        SendMessage sendMessage = new SendMessage().setChatId(1L).setText("blabla").setReplyMarkup(null);

        instagrammBot.onUpdateReceived(update);

        Mockito.verify(instagrammBot).execute(ArgumentMatchers.eq(sendMessage));
    }

    @Test
    void onUpdateReceived_hasMessageWithText_replayBuilderThrowException() throws Exception {
        Message message = Mockito.mock(Message.class);
        Mockito.when(message.hasText()).thenReturn(true);
        Mockito.when(message.getChatId()).thenReturn(1L);

        Update update = Mockito.mock(Update.class);
        Mockito.when(update.hasMessage()).thenReturn(true);
        Mockito.when(update.getMessage()).thenReturn(message);

        ReplayBuilder replayBuilder = Mockito.mock(ReplayBuilder7Tasks.class);
        Mockito.doThrow(new Exception("EXIT_REQUEST")).when(replayBuilder).buildReplay();

        bot.setReplayBuilder(replayBuilder);
        InstagrammBot instagrammBot = Mockito.spy(bot);
        Mockito.doReturn(null).when((TelegramLongPollingBot)instagrammBot).execute(Mockito.any(SendMessage.class));

        SendMessage sendMessage = new SendMessage().setChatId(1L).setText("Shutting down now");

        Assertions.assertThrows(RuntimeException.class, ()->instagrammBot.onUpdateReceived(update));
        Mockito.verify(instagrammBot).execute(ArgumentMatchers.eq(sendMessage));
    }

    @Test
    void getBotUsername_returnsProperName() {
        Assertions.assertEquals(bot.getBotUsername(), "GoogInsta_bot");
    }

    @Test
    void getBotToken_returnsProperToken() {
        Assertions.assertEquals(bot.getBotToken(), "672283353:AAEXzupH5J4HsBJGmaNJRU465mlfJWQl8xk");
    }

}