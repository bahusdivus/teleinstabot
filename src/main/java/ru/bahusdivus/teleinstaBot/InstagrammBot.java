package ru.bahusdivus.teleinstaBot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class InstagrammBot extends TelegramLongPollingBot {
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            try {
                DbHandler db = DbHandler.getInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String replay_text;
            switch (message_text) {
                case "Получить задание":
                    replay_text = "Тут будет задание";
                    break;
                case "Проверить задание":
                    replay_text = "Тут будет результат проверки";
                    break;
                default:
                    replay_text = "Тут будут парситься другие строки";
            }

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(false);

            List<KeyboardRow> keyboard = new ArrayList<>();
            KeyboardRow keyboardFirstRow = new KeyboardRow();
            keyboardFirstRow.add(new KeyboardButton("Привет"));
            keyboardFirstRow.add(new KeyboardButton("Помощь"));

            keyboard.add(keyboardFirstRow);
            replyKeyboardMarkup.setKeyboard(keyboard);

            SendMessage message = new SendMessage().setChatId(chat_id).setText(replay_text);
            message.setReplyMarkup(replyKeyboardMarkup);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
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
