package ru.bahusdivus.teleinstaBot.Scrapper;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.json.JSONObject;

import java.io.*;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.stream.Collectors;

public class TibInstagramScrapper {
    private String csrf_token;
    private OkHttpClient httpClient;
    private ThreadLocal<Long> lastRequestTime = ThreadLocal.withInitial(System::currentTimeMillis);
    private SecureRandom random = new SecureRandom();
    private static TibInstagramScrapper instance;

    private static final String LOGIN_URL = "https://www.instagram.com/accounts/login/ajax/";
    private static final String REFERER = "Referer";
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

        try (InputStream reader = this.getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (reader != null) {
                basePage();
                Properties properties = new Properties();
                properties.load(reader);
                //TODO Next method isn't reliable. Need some work to deal with IG login challenges
                login(properties.getProperty("ig.user"), properties.getProperty("ig.password"));
            }
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

    private void login(String username, String password) throws IOException {
        if (username == null || password == null) {
            throw new IOException("Specify username and password");
        }else if(csrf_token == null) {
            throw new NullPointerException("Please run before base()");
        }

        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .header(REFERER, BASE_URL + "/")
                .post(formBody)
                .build();
        Response response = executeHttpRequest(withCsrfToken(request));
        try(InputStream jsonStream = response.body().byteStream()) {
            JSONObject result = new JSONObject(new BufferedReader(new InputStreamReader(jsonStream))
                    .lines().collect(Collectors.joining("\n")));
            if(!(result.get("authenticated") instanceof Boolean && (Boolean)result.get("authenticated"))) {
                throw new IOException("Credentials rejected by instagram");
            }
        }
    }

    private Response executeHttpRequest(Request request) throws IOException {
        Response response = this.httpClient.newCall(request).execute();
        long currentTime = System.currentTimeMillis();
        if((currentTime - lastRequestTime.get()) < 200){
            lastRequestTime.set(currentTime);
            try {
                Thread.sleep(200L + random.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return response;
    }
}