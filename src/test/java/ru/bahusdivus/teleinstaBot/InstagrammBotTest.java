package ru.bahusdivus.teleinstaBot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.objects.Update;


class InstagrammBotTest {

    private InstagrammBot bot = new InstagrammBot();
    @Test
    void onUpdateReceived() {
        Update update = Mockito.mock(Update.class);
        Mockito.when(update.hasMessage()).thenReturn(false);

        Assertions.assertDoesNotThrow(() -> bot.onUpdateReceived(update));
    }

    @Test
    void getBotUsername() {
        Assertions.assertEquals(bot.getBotUsername(), "GoogInsta_bot");
    }

    @Test
    void getBotToken() {
        Assertions.assertEquals(bot.getBotToken(), "672283353:AAEXzupH5J4HsBJGmaNJRU465mlfJWQl8xk");
    }
}