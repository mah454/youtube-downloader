package ir.moke.youtube.model;

public class ProgressInfo {
    private boolean success;
    private String download_url;

    public ProgressInfo() {
    }

    public ProgressInfo(boolean success, String download_url) {
        this.success = success;
        this.download_url = download_url;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "success=" + success +
                ", progress_url='" + download_url + '\'' +
                '}';
    }
}
