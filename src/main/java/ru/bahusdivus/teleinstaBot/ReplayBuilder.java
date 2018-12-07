package ru.bahusdivus.teleinstaBot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;
import java.util.regex.*;

class ReplayBuilder {
    private String messageText;
    private Long chatId;
    private String replayText;
    private DbHandler db;

    ReplayBuilder(String messageText, Long chatId) {
        this.messageText = messageText;
        this.chatId = chatId;
        db = DbHandler.getInstance();
    }

    String getReplayText() {
        return replayText;
    }

    ReplyKeyboardMarkup getReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
//        keyboardFirstRow.add(new KeyboardButton("Привет"));
//        keyboardFirstRow.add(new KeyboardButton("Помощь"));

        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    void buildReplay() {
        User user = db.getUserByChatId(chatId);
        if (user == null) {
//            Pattern p = Pattern.compile("^@@[A-Za-z0-9\\._]+", Pattern.DOTALL);
//            Matcher m = p.matcher(messageText);
//            if(m.matches()) {
            if(Pattern.matches("^@[\\w.]+$", messageText)) {
                user = new User(messageText, chatId);
                db.saveUser(user);
                replayText = "Сохраняем ник \"" + messageText + "\"";
            } else {
                replayText = "Для начала работы с ботом вам необходимо зарегистрировать свой аккаунт Instagram. Просто отправьте свой ник, начинающийся с символа @";
            }
        } else {
            switch (messageText) {
                case "Получить задание":
                    replayText = "Тут будет задание";
                    break;
                case "Проверить задание":
                    replayText = "Тут будет результат проверки";
                    break;
                default:
                    replayText = "Тут будут парситься другие строки";
            }
        }
    }
}