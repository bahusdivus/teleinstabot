package ru.bahusdivus.teleinstaBot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.bahusdivus.teleinstaBot.Scrapper.TibInstagramScrapper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskResultParser {

    private TibInstagramScrapper scrapper;

    public TaskResultParser (TibInstagramScrapper scrapper) {
        this.scrapper = scrapper;
    }

    public TaskResultParser () {
        this.scrapper = TibInstagramScrapper.getInstance();
    }

    public boolean checkComment(String instId, String postId, int commentsRequiresLength) throws IOException, JSONException {
        String dataResult = null;

        String inputLine = scrapper.getPageBody("https://www.instagram.com/p/" + postId + "/", postId);
        Pattern p = Pattern.compile("(.*?)_sharedData = (.*?);</script>(.*?)", Pattern.DOTALL);
        Matcher m = p.matcher(inputLine);
        if (m.matches()) dataResult = m.group(2);
        if (dataResult == null) return false;
        JSONObject rootJson = new JSONObject(dataResult);
        JSONObject edgesJson = rootJson.getJSONObject("entry_data").getJSONArray("PostPage")
                .getJSONObject(0).getJSONObject("graphql").getJSONObject("shortcode_media")
                .getJSONObject("edge_media_to_comment");
        boolean hasNext = true;
        String endCursor = null;
        while (hasNext) {
            if (edgesJson == null) {
                endCursor = URLEncoder.encode(Objects.requireNonNull(endCursor), StandardCharsets.UTF_8.toString());
                String url = "https://www.instagram.com/graphql/query/?query_hash=f0986789a5c5d17c2400faebf16efd0d&variables=%7B%22shortcode%22%3A%22" + postId + "%22%2C%22first%22%3A32%2C%22after%22%3A%22" + endCursor + "%22%7D";
                dataResult = scrapper.getPageBody(url, postId);
                rootJson = new JSONObject(dataResult);
                checkStatus(rootJson);
                edgesJson = rootJson.getJSONObject("data").getJSONObject("shortcode_media").getJSONObject("edge_media_to_comment");
            }

            JSONArray commentsArray = edgesJson.getJSONArray("edges");
            for (int i = 0; i < commentsArray.length(); i++) {
                JSONObject currentNode = commentsArray.getJSONObject(i).getJSONObject("node");
                if (instId.equals("@" + currentNode.getJSONObject("owner").getString("username"))) {
                    String[] words = currentNode.getString("text").split(" ");
                    if (words.length >= commentsRequiresLength) {
                        return true;
                    }
                }
            }

            hasNext = edgesJson.getJSONObject("page_info").getBoolean("has_next_page");
            if (hasNext) endCursor = edgesJson.getJSONObject("page_info").getString("end_cursor");
            edgesJson = null;
        }
        return false;
    }

    public boolean checkLike(String instId, String postId) throws IOException, JSONException {
        String dataResult;
        String url = "https://www.instagram.com/graphql/query/?query_hash=e0f59e4a1c8d78d0161873bc2ee7ec44&variables=%7B%22shortcode%22%3A%22" + postId + "%22%2C%22include_reel%22%3Atrue%2C%22first%22%3A24%7D";
        dataResult = scrapper.getPageBody(url, postId);
        if (dataResult == null) return false;
        JSONObject rootJson = new JSONObject(dataResult);
        checkStatus(rootJson);
        JSONObject edgesJson = rootJson.getJSONObject("data").getJSONObject("shortcode_media").getJSONObject("edge_liked_by");
        boolean hasNext = true;
        String endCursor = null;
        while (hasNext) {
            if (edgesJson == null) {
                endCursor = URLEncoder.encode(Objects.requireNonNull(endCursor), StandardCharsets.UTF_8.toString());
                url = "https://www.instagram.com/graphql/query/?query_hash=e0f59e4a1c8d78d0161873bc2ee7ec44&variables=%7B%22shortcode%22%3A%22" + postId + "%22%2C%22include_reel%22%3Atrue%2C%22first%22%3A24%2C%22after%22%3A%22" + endCursor + "%22%7D";
                dataResult = scrapper.getPageBody(url, postId);
                rootJson = new JSONObject(dataResult);
                checkStatus(rootJson);
                edgesJson = rootJson.getJSONObject("data").getJSONObject("shortcode_media").getJSONObject("edge_liked_by");
            }

            JSONArray commentsArray = edgesJson.getJSONArray("edges");
            for (int i = 0; i < commentsArray.length(); i++) {
                JSONObject currentNode = commentsArray.getJSONObject(i).getJSONObject("node");
                if (instId.equals("@" + currentNode.getString("username"))) return true;
            }

            hasNext = edgesJson.getJSONObject("page_info").getBoolean("has_next_page");
            if (hasNext) endCursor = edgesJson.getJSONObject("page_info").getString("end_cursor");
            edgesJson = null;
        }
        return false;
    }

    private void checkStatus(JSONObject jsonObject) throws JSONException {
        if (!jsonObject.getString("status").equals("ok")) {
            throw new JSONException("Status: " + jsonObject.getString("status") +
                    ". Message: " + jsonObject.getString("message"));
        }
    }
}
