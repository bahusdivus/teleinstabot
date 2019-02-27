package ru.bahusdivus.teleinstaBot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

class InstagrammBot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {

            ReplayBuilder replay = new ReplayBuilder7Tasks(update.getMessage().getText(), update.getMessage().getChatId());
            try {
                replay.buildReplay();
                SendMessage message = new SendMessage().setChatId(update.getMessage().getChatId()).setText(replay.getReplayText());
                message.setReplyMarkup(replay.getReplyKeyboardMarkup());
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (e.getMessage().equals("EXIT_REQUEST")) {
                    SendMessage message = new SendMessage().setChatId(update.getMessage().getChatId()).setText("Shutting down now");
                    try {
                        execute(message);
                    } catch (TelegramApiException te) {
                        te.printStackTrace();
                    }
                    System.exit(0);
                } else e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "GoogInsta_bot";
    }

    @Override
    public String getBotToken() {
        return "672283353:AAEXzupH5J4HsBJGmaNJRU465mlfJWQl8xk";
    }
}
