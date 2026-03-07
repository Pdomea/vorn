package app.beans;

public class FlashMessageBean {
    private String info;
    private String error;

    public FlashMessageBean() {
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean hasInfo() {
        return info != null && !info.isBlank();
    }

    public boolean hasError() {
        return error != null && !error.isBlank();
    }
}

