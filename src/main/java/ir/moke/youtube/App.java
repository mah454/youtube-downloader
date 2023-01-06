package ir.moke.youtube;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ir.moke.youtube.download.Downloader;
import ir.moke.youtube.model.ProgressResponse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class App {
    private static final String CURRENT_WORKING_DIR = System.getProperty("user.dir");
    private static final File PLAYLIST_FILE = new File("youtube_playlist.urls");
    ;

    private static final String YOUTUBE_PLAYLIST_URL = "https://api.youtubeplaylist.cc/playlist?url=";
    private static final String YOUTUBE_ENGINE_URL = "https://ddownr.com/api/info/checkPlaylist.php?url=";
    private static final String YOUTUBE_DOWNLOAD_URL = "https://loader.to/ajax/download.php?format=720&url=";
    private static final String PROGRESS_URL = "https://loader.to/ajax/progress.php?id=";
    private static final String SINGLE_QUERY_PARAM = "?format=720";
    private static final String PLAYLIST_QUERY_PARAM = "&format-option=6&playlist=1&playliststart=1&playlistend=1&index=";
    private static boolean DEBUG_MODE = false;
    private static final Gson gson = new GsonBuilder().create();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(60))
            .build();

    static {
        String env = System.getenv("JVM_DEBUG_MODE");
        if (Objects.equals(env, "1")) DEBUG_MODE = true;
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: ydl [APARAT_PLAYLIST_LINK]");
            System.out.println("Example : aparat-dl https://www.youtube.com/watch?v=...");
            System.out.println("Example : aparat-dl https://youtube.com/playlist?list=...");
            System.exit(1);
        }

        /*
         * Example Playlist :
         * https://www.youtube.com/playlist?list=PLjxrf2q8roU23XGwz3Km7sQZFTdB996iG
         * */

        String youtube_url = args[0];

        if (youtube_url.contains("playlist")) {
            downloadPlaylist(youtube_url);
        } else if (youtube_url.contains("watch")) {
            downloadSingle(youtube_url, null, false);
        } else {
            System.out.println("[Warning] Url does not supported");
        }
    }

    private static void downloadPlaylist(String url) throws Exception {
        List<String> playlistUrls = new ArrayList<>();
        if (PLAYLIST_FILE.exists()) {
            playlistUrls.addAll(readPlaylistUrls());
        } else {
            System.out.println("Fetch playlist information ...");
            HttpResponse<String> response = sendRequest(YOUTUBE_PLAYLIST_URL + url);
            printLog(YOUTUBE_ENGINE_URL + url);
            JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);
            JsonElement items = jsonObject.get("items");
            if (items != null && items.isJsonArray()) {
                for (JsonElement element : items.getAsJsonArray()) {
                    String u = element.getAsJsonObject().get("url").getAsString();
                    playlistUrls.add(u);
                }
            }
            writePlaylistUrls(playlistUrls);
        }

        System.out.println("Playlist length: " + playlistUrls.size());
        for (int i = 0; i < playlistUrls.size(); i++) {
            String videoUrl = playlistUrls.get(i);
            downloadSingle(videoUrl, i, true);
        }
    }

    private static void downloadSingle(String videoUrl, Integer index, boolean isPlayList) throws Exception {
        String url = YOUTUBE_DOWNLOAD_URL + videoUrl;
        if (isPlayList) {
            int i = index + 1;
            url = url + PLAYLIST_QUERY_PARAM + i;
        } else {
            url = url + SINGLE_QUERY_PARAM;
        }
        printLog(url);
        HttpResponse<String> infoResponse = sendRequest(url);
        String title = gson.fromJson(infoResponse.body(), JsonObject.class).get("title").getAsString();
        String link_id = gson.fromJson(infoResponse.body(), JsonObject.class).get("id").getAsString();

        String finalFileName = title + ".mp4";
        if (index != null) {
            String id = String.format("%02d", index);
            finalFileName = id + "_" + title + ".mp4";
        }
        File targetFile = new File(CURRENT_WORKING_DIR + "/" + finalFileName);
        System.out.println(finalFileName);
        if (targetFile.exists()) return;

        boolean done = false;
        URL downloadFileUrl = null;
        String body = null;
        HttpResponse<String> progressInfo ;
        try {
            do {
                progressInfo = sendRequest(PROGRESS_URL + link_id);
                body = progressInfo.body();
                ProgressResponse progressResponse = gson.fromJson(body, ProgressResponse.class);
                printLog(progressResponse);
                System.out.print("\rRemote progress: " + (progressResponse.getProgress() * 100) / 1000 + "% " + progressResponse.getText());
                if (progressResponse.getSuccess() == 1 && progressResponse.getText().equalsIgnoreCase("Finished")) {
                    done = true;
                    downloadFileUrl = new URL(progressResponse.getDownload_url());
                }
            } while (!done);

            Downloader.instance.download(downloadFileUrl, targetFile);
            System.out.println("\n");
        } catch (Exception e) {
            System.out.println(body);
            e.printStackTrace();
        }
    }

    private static HttpResponse<String> sendRequest(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static void printLog(Object o) {
        if (DEBUG_MODE) {
            System.out.println("[DEBUG] " + o);
        }
    }

    private static void writePlaylistUrls(List<String> urls) {
        try {
            StringBuilder content = new StringBuilder();
            urls.forEach(item -> content.append(item).append("\n"));
            try (FileWriter writer = new FileWriter(PLAYLIST_FILE)) {
                writer.write(content.toString());
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> readPlaylistUrls() {
        try {
            StringBuilder content = new StringBuilder();
            return Files.readAllLines(PLAYLIST_FILE.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
