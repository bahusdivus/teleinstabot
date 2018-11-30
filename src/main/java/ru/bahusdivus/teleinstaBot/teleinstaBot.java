package ru.bahusdivus.teleinstaBot;

import org.json.*;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.*;
import java.util.regex.*;

public class teleinstaBot {

    public static void main(String[] args) throws Exception {

        //IG media parser (to get comments)
        /*
        URL insta = new URL("https://www.instagram.com/p/9BDXa_L7bm/");
        URLConnection yc = insta.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            Pattern p = Pattern.compile("(.*?)_sharedData = (.*?);</script>(.*?)", Pattern.DOTALL);
            Matcher m = p.matcher(inputLine);
            if(m.matches()) {
                String dataResult = m.group(2);
                JSONObject dataObj = new JSONObject(dataResult);
                //JsonArray nodes = dataObj.getJsonArray("edges");
                System.out.println(dataObj.toString(2));
                //https://www.instagram.com/graphql/query/?query_id=17864450716183058&variables=%7B%22shortcode%22%3A%22BqSUTcbBln0%22%2C%22include_reel%22%3Atrue%2C%22first%22%3A5000%7D
            }
            //System.out.println(inputLine);
        }
        in.close();
        */

        //TODO: Log in first!
        //Likes parser
        /*
        URL insta2 = new URL("https://www.instagram.com/graphql/query/?query_id=17864450716183058&variables=%7B%22shortcode%22%3A%22BqSUTcbBln0%22%2C%22include_reel%22%3Atrue%2C%22first%22%3A5000%7D");
        URLConnection yc2 = insta2.openConnection();
        BufferedReader in2 = new BufferedReader(new InputStreamReader(yc2.getInputStream()));
        String inputLine2;
        while ((inputLine2 = in2.readLine()) != null) {

            JSONObject dataObj2 = new JSONObject(inputLine2);
            //JsonArray nodes = dataObj.getJsonArray("edges");
            System.out.println(dataObj2.toString(2));
            //https://www.instagram.com/graphql/query/?query_id=17864450716183058&variables=%7B%22shortcode%22%3A%22BqSUTcbBln0%22%2C%22include_reel%22%3Atrue%2C%22first%22%3A5000%7

        }
        in2.close();
        */



        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(new InstagrammBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}