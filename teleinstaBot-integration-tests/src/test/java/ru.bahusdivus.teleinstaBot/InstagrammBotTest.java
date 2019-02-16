package ru.bahusdivus.teleinstaBot;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;


class InstagrammBotTest {

    private InstagrammBot bot = new InstagrammBot();
    @Test
    void onUpdateReceived() {
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Mockito.when(update.hasMessage()).thenReturn(true);
        Mockito.when(update.getMessage()).thenReturn(message);
        Mockito.when(message.hasText()).thenReturn(true);
        Mockito.when(message.getText()).thenReturn("blabla");
        Mockito.when(message.getChatId()).thenReturn(225479481L);

        bot.onUpdateReceived(update);
    }

}