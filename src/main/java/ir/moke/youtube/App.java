package ir.moke.youtube;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import ir.moke.youtube.download.Downloader;
import ir.moke.youtube.model.DownloadInfo;
import ir.moke.youtube.model.PlayListResponse;
import ir.moke.youtube.model.ProgressInfo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class App {
    private static final String CURRENT_WORKING_DIR = System.getProperty("user.dir");

    private static final String YOUTUBE_ENGINE_URL = "https://ddownr.com/api/info/checkPlaylist.php?url=";
    private static final String YOUTUBE_DOWNLOAD_URL = "https://ddownr.com/download.php?url=";
    private static final String VIDEO_INFO = "https://ddownr.com/api/info/index.php?url=";
    private static final String QUERY_PARAM = "&format-option=6";
    //    private static final String QUERY_PARAM = "&format-option=6&playlist=1&playliststart=1&playlistend=1&index=1";
    private static final Gson gson = new GsonBuilder().create();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: ydl [APARAT_PLAYLIST_LINK]");
            System.out.println("Example : aparat-dl https://www.youtube.com/watch?v=...");
            System.out.println("Example : aparat-dl https://youtube.com/playlist?list=...");
            System.exit(1);
        }

        String youtube_url = args[0];

        if (youtube_url.contains("playlist")) {
            downloadPlaylist(youtube_url);
        } else if (youtube_url.contains("watch")) {
            downloadSingle(youtube_url, null);
        } else {
            System.out.println("[Warning] Url does not supported");
        }

    }

    private static void downloadPlaylist(String url) throws Exception {
        HttpResponse<String> response = sendRequest(YOUTUBE_ENGINE_URL + url);

        PlayListResponse playListResponse = gson.fromJson(response.body(), PlayListResponse.class);
        System.out.println("Playlist length: " + playListResponse.getVideos().length);

        if (playListResponse.isStatus()) {
            for (int i = 0; i < playListResponse.getVideos().length; i++) {
                String videoUrl = playListResponse.getVideos()[i];
                downloadSingle(videoUrl, i);

            }
        }
    }

    private static void downloadSingle(String videoUrl, Integer index) throws Exception {
        String url = YOUTUBE_DOWNLOAD_URL + videoUrl + QUERY_PARAM;
        HttpResponse<String> infoResponse = sendRequest(VIDEO_INFO + videoUrl);
        String title = gson.fromJson(infoResponse.body(), JsonObject.class).get("title").getAsString();

        String finalFileName = title;
        if (index != null) {
            String id = String.format("%02d", index);
            finalFileName = id + "_" + title + ".mp4";
        }
        File targetFile = new File(CURRENT_WORKING_DIR + "/" + finalFileName);
        System.out.println(finalFileName);
        if (targetFile.exists()) return;

        HttpResponse<String> downloadOrderResponse = sendRequest(url);
        ProgressInfo progressInfo = gson.fromJson(downloadOrderResponse.body(), ProgressInfo.class);

        boolean done = false;
        URL downloadFileUrl = null;
        do {
            HttpResponse<String> downloadInfoResponse = sendRequest(progressInfo.getProgress_url());
            DownloadInfo downloadInfo = gson.fromJson(downloadInfoResponse.body(), DownloadInfo.class);
            System.out.print("\rRemote Download: " + downloadInfo.getProgress() + "/100 %");
            if (downloadInfo.getSuccess() == 1 && downloadInfo.getText().equalsIgnoreCase("Finished")) {
                done = true;
                downloadFileUrl = new URL(downloadInfo.getDownload_url());
            }
        } while (!done);

        Downloader.instance.download(downloadFileUrl, targetFile);
        System.out.println("\n");
    }

    private static HttpResponse<String> sendRequest(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
