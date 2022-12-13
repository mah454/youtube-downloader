package ir.moke.youtube.model;

import java.util.Arrays;

public class PlayListResponse {
    private String[] videos;
    private boolean status;

    public PlayListResponse() {
    }

    public PlayListResponse(String[] videos, boolean status) {
        this.videos = videos;
        this.status = status;
    }

    public String[] getVideos() {
        return videos;
    }

    public void setVideos(String[] videos) {
        this.videos = videos;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "PlayListResponse{" +
                "videos=" + Arrays.toString(videos) +
                ", status=" + status +
                '}';
    }
}
