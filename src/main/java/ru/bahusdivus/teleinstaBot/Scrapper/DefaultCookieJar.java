package ru.bahusdivus.teleinstaBot.Scrapper;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultCookieJar implements CookieJar {

    private List<Cookie> cache = new ArrayList<>();

    DefaultCookieJar() {}

    @Override
    public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cache.addAll(cookies);
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