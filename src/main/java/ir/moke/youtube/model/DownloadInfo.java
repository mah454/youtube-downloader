package ir.moke.youtube.model;

public class DownloadInfo {
    private String download_url;
    private int progress;
    private int success;
    private String text;

    public DownloadInfo() {
    }

    public DownloadInfo(String download_url, int progress, int success, String text) {
        this.download_url = download_url;
        this.progress = progress;
        this.success = success;
        this.text = text;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "download_url='" + download_url + '\'' +
                ", progress=" + progress +
                ", success=" + success +
                ", text='" + text + '\'' +
                '}';
    }
}
