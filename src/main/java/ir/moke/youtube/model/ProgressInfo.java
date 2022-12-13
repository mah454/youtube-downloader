package ir.moke.youtube.model;

public class ProgressInfo {
    private boolean success;
    private String progress_url;

    public ProgressInfo() {
    }

    public ProgressInfo(boolean success, String progress_url) {
        this.success = success;
        this.progress_url = progress_url;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getProgress_url() {
        return progress_url;
    }

    public void setProgress_url(String progress_url) {
        this.progress_url = progress_url;
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "success=" + success +
                ", progress_url='" + progress_url + '\'' +
                '}';
    }
}
