package ir.moke.youtube;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import ir.moke.youtube.download.Downloader;
import ir.moke.youtube.model.DownloadInfo;
import ir.moke.youtube.model.PlayListResponse;
import ir.moke.youtube.model.ProgressInfo;

import javax.swing.plaf.IconUIResource;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;

public class App {
    private static final String CURRENT_WORKING_DIR = System.getProperty("user.dir");

    private static final String YOUTUBE_ENGINE_URL = "https://ddownr.com/api/info/checkPlaylist.php?url=";
    private static final String YOUTUBE_DOWNLOAD_URL = "https://ddownr.com/download.php?url=";
    private static final String VIDEO_INFO = "https://ddownr.com/api/info/index.php?url=";
    private static final Gson gson = new GsonBuilder().create();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) throws Exception {
        String youtube_url = "https://youtube.com/playlist?list=PLkdlfRTihVK6JqDUByQMTlrrQM5NSejon";
        String url = YOUTUBE_ENGINE_URL + youtube_url;
        HttpResponse<String> response = sendRequest(url);

        PlayListResponse playListResponse = gson.fromJson(response.body(), PlayListResponse.class);
        System.out.println("Playlist length: " + playListResponse.getVideos().length);

//        String queryParam = "&format-option=6&playlist=1&playliststart=1&playlistend=1&index=1";
        String queryParam = "&format-option=6";

        if (playListResponse.isStatus()) {
            for (int i = 0; i < playListResponse.getVideos().length; i++) {
                String videoUrl = playListResponse.getVideos()[i];
                url = YOUTUBE_DOWNLOAD_URL + videoUrl + queryParam;
                HttpResponse<String> infoResponse = sendRequest(VIDEO_INFO + videoUrl);
                String title = gson.fromJson(infoResponse.body(), JsonObject.class).get("title").getAsString();

                String id = String.format("%02d", i);
                String finalFileName = id + "_" + title + ".mp4";
                File targetFile = new File(CURRENT_WORKING_DIR + "/" + finalFileName);
                System.out.println(finalFileName);
                if (targetFile.exists()) continue;

                HttpResponse<String> downloadOrderResponse = sendRequest(url);
                ProgressInfo progressInfo = gson.fromJson(downloadOrderResponse.body(), ProgressInfo.class);

                boolean done = false;
                URL downloadFileUrl = null;
                do {
                    HttpResponse<String> downloadInfoResponse = sendRequest(progressInfo.getProgress_url());
                    DownloadInfo downloadInfo = gson.fromJson(downloadInfoResponse.body(), DownloadInfo.class);
                    if (downloadInfo.getSuccess() == 1 && downloadInfo.getText().equalsIgnoreCase("Finished")) {
                        done = true;
                        downloadFileUrl = new URL(downloadInfo.getDownload_url());
                    }
                } while (!done);


                Downloader.instance.download(downloadFileUrl, targetFile);
                System.out.println("\n");

            }
        }

    }

    private static HttpResponse<String> sendRequest(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
