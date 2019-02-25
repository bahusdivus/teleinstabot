package ru.bahusdivus.teleinstaBot.Scrapper;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.*;
import java.security.SecureRandom;
import java.util.stream.Collectors;

public class TibInstagramScrapper {
    private String csrf_token;
    private OkHttpClient httpClient;
    private ThreadLocal<Long> lastRequestTime = ThreadLocal.withInitial(System::currentTimeMillis);
    private SecureRandom random = new SecureRandom();
    private static TibInstagramScrapper instance;

    private static final String BASE_URL = "https://www.instagram.com";

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

        try {
            basePage();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Request withCsrfToken(Request request) {
        return request.newBuilder()
                .addHeader("X-CSRFToken", csrf_token)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
                .build();
    }

    private void basePage() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
                .build();

        Response response = executeHttpRequest(request);
        try (ResponseBody body = response.body()){
            if(csrf_token == null) csrf_token = getCSRFToken(body);
        }
    }

    public String getPageBody(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = executeHttpRequest(withCsrfToken(request));
        try (ResponseBody body = response.body()){
            return new BufferedReader(new InputStreamReader(body.byteStream()))
                    .lines().collect(Collectors.joining("\n"));
        }
    }

    private String getCSRFToken(ResponseBody body) throws IOException {
        String seek = "\"csrf_token\":\"";
        BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(body.byteStream())));
        String line;
        while((line = br.readLine())!=null) {
            int index = line.indexOf(seek);
            if(index != -1) {
                return line.substring(index+seek.length(),index+seek.length()+32);
            }
        }
        throw new NullPointerException("Couldn't find CSRFToken");
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