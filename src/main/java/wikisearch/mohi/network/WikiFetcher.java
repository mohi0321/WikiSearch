package wikisearch.mohi.network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class WikiFetcher {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();
    private static final String API_URL = "https://minecraft.wiki/api.php";

    public static CompletableFuture<WikiPageInfo> fetchPageInfo(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // First do an opensearch to find the exact page title
                String searchUrl = API_URL + "?action=opensearch&search=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&limit=1&format=json";
                HttpRequest searchReq = HttpRequest.newBuilder().uri(URI.create(searchUrl)).build();
                HttpResponse<String> searchResp = client.send(searchReq, HttpResponse.BodyHandlers.ofString());

                JsonArray searchArray = GSON.fromJson(searchResp.body(), JsonArray.class);
                if (searchArray.size() < 2 || searchArray.get(1).getAsJsonArray().isEmpty()) {
                    return new WikiPageInfo("No results found for '" + query + "'.", null);
                }

                String pageTitle = searchArray.get(1).getAsJsonArray().get(0).getAsString();

                // Now fetch the page text and image
                String pageUrl = API_URL + "?action=query&prop=extracts|pageimages&titles=" + URLEncoder.encode(pageTitle, StandardCharsets.UTF_8) + "&exintro=1&explaintext=1&pithumbsize=256&format=json";
                HttpRequest pageReq = HttpRequest.newBuilder().uri(URI.create(pageUrl)).build();
                HttpResponse<String> pageResp = client.send(pageReq, HttpResponse.BodyHandlers.ofString());

                JsonObject root = GSON.fromJson(pageResp.body(), JsonObject.class);
                JsonObject pages = root.getAsJsonObject("query").getAsJsonObject("pages");

                for (String key : pages.keySet()) {
                    JsonObject page = pages.getAsJsonObject(key);
                    String extract = page.has("extract") ? page.get("extract").getAsString() : "No summary available.";
                    String imageUrl = null;
                    if (page.has("thumbnail")) {
                        imageUrl = page.getAsJsonObject("thumbnail").get("source").getAsString();
                    }
                    return new WikiPageInfo(extract, imageUrl);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new WikiPageInfo("Error fetching data: " + e.getMessage(), null);
            }
            return new WikiPageInfo("Error processing wiki data.", null);
        });
    }

    public static class WikiPageInfo {
        public final String text;
        public final String imageUrl;

        public WikiPageInfo(String text, String imageUrl) {
            this.text = text;
            this.imageUrl = imageUrl;
        }
    }
}
