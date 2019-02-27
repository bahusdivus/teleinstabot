package ru.bahusdivus.teleinstaBot.Scrapper;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.*;
import java.security.SecureRandom;
import java.util.stream.Collectors;

public class TibInstagramScrapper {
    private OkHttpClient httpClient;
    private ThreadLocal<Long> lastRequestTime = ThreadLocal.withInitial(System::currentTimeMillis);
    private SecureRandom random = new SecureRandom();
    private static TibInstagramScrapper instance;

    public static TibInstagramScrapper getInstance() {
        if (instance == null) instance = new TibInstagramScrapper();
        return instance;
    }

    private TibInstagramScrapper() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        httpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(loggingInterceptor)
                .cookieJar(new DefaultCookieJar())
                .build();
    }

    public String getPageBody(String url, String postId) throws IOException {
        Request request = new Request.Builder()
                .addHeader("User-Agent", " Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0")
                .addHeader("Referer", "https://www.instagram.com/p/" + postId + "/")
                .url(url)
                .build();

        Response response = executeHttpRequest(request);
        try (ResponseBody body = response.body()){
            return new BufferedReader(new InputStreamReader(body.byteStream()))
                    .lines().collect(Collectors.joining("\n"));
        }
    }

    private Response executeHttpRequest(Request request) throws IOException {
        Response response = this.httpClient.newCall(request).execute();
        long currentTime = System.currentTimeMillis();
        if((currentTime - lastRequestTime.get()) < 500){
            lastRequestTime.set(currentTime);
            try {
                Thread.sleep(500L + random.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return response;
    }
}