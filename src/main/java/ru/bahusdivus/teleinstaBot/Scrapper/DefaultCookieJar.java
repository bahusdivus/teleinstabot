package ru.bahusdivus.teleinstaBot.Scrapper;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultCookieJar implements CookieJar {

    private final List<Cookie> cache = new ArrayList<>();
    private File cookieFile = new File("cookie");

    DefaultCookieJar() {
        if (cookieFile.exists() && cookieFile.isFile()) {
            try {
                Files.lines(cookieFile.toPath(), StandardCharsets.UTF_8).forEach((String line) -> cache.add(Cookie.parse(HttpUrl.parse("https://www.instagram.com"), line)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cache.isEmpty()) {
            cache.addAll(cookies);
        } else {
            for (Cookie savedCookie : cookies) {
                if (!savedCookie.value().isEmpty() && !cache.contains(savedCookie)) {
                    cache.removeIf(cookie -> cookie.name().equals(savedCookie.name()));
                    cache.add(savedCookie);
                }
            }
        }
        saveCookies();
    }

    private void saveCookies() {
        try (FileWriter writer = new FileWriter(cookieFile)){
            cache.forEach((Cookie cookie) -> {
                try {
                    writer.write(cookie.toString());
                    writer.write(System.lineSeparator());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = new ArrayList<>();
        for (Iterator<Cookie> it = cache.iterator(); it.hasNext(); ) {
            Cookie cookie = it.next();
            if (cookie.expiresAt() < System.currentTimeMillis()) {
                it.remove();
            } else if (cookie.matches(url)) {
                cookies.add(cookie);
            }
        }
        return cookies;
    }
}