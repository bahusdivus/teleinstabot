package ru.bahusdivus.teleinstaBot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

class TeleInstaBot {

    public static void main(String[] args) {

        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(new InstagrammBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}