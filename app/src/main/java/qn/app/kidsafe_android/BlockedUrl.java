package qn.app.kidsafe_android;

public class BlockedUrl {
    private String id;
    private String url;
    private long addedAt;
    private String addedBy;
    private String status;

    // Default constructor required for Firebase
    public BlockedUrl() {
    }

    public BlockedUrl(String url, long addedAt, String addedBy, String status) {
        this.url = url;
        this.addedAt = addedAt;
        this.addedBy = addedBy;
        this.status = status;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "BlockedUrl{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", addedAt=" + addedAt +
                ", addedBy='" + addedBy + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
