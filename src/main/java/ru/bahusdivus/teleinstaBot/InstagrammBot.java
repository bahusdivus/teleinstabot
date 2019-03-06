package ru.bahusdivus.teleinstaBot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

class InstagrammBot extends TelegramLongPollingBot {
    private ReplayBuilder replayBuilder;

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            if (replayBuilder == null) replayBuilder = new ReplayBuilder7Tasks(update.getMessage().getText(), update.getMessage().getChatId());
            try {
                replayBuilder.buildReplay();
                SendMessage message = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText(replayBuilder.getReplayText());
                message.setReplyMarkup(replayBuilder.getReplyKeyboardMarkup());
                try {
                    execute(message);
                } catch (TelegramApiException e) {e.printStackTrace();}
            } catch (Exception e) {
                if (e.getMessage().equals("EXIT_REQUEST")) {
                    SendMessage message = new SendMessage().setChatId(update.getMessage().getChatId()).setText("Shutting down now");
                    try {
                        execute(message);
                    } catch (TelegramApiException te) {te.printStackTrace();}
                    throw new RuntimeException();
                } else e.printStackTrace();
            } finally {
                replayBuilder = null;
            }
        }
    }

    void setReplayBuilder(ReplayBuilder replayBuilder) {
        this.replayBuilder = replayBuilder;
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
