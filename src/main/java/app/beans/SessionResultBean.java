package app.beans;

import java.math.BigDecimal;

import app.service.TrackingService.SessionResultData;

public class SessionResultBean {
    private String info;
    private String error;
    private String backUrl;
    private String backLabel;
    private SessionResultData resultData;

    public SessionResultBean() {
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

    public String getBackUrl() {
        return backUrl;
    }

    public void setBackUrl(String backUrl) {
        this.backUrl = backUrl;
    }

    public String getBackLabel() {
        return backLabel;
    }

    public void setBackLabel(String backLabel) {
        this.backLabel = backLabel;
    }

    public SessionResultData getResultData() {
        return resultData;
    }

    public void setResultData(SessionResultData resultData) {
        this.resultData = resultData;
    }

    public String getDurationText() {
        if (resultData == null) {
            return "0h 0m";
        }
        long totalSeconds = resultData.getDurationSeconds();
        long durationHours = totalSeconds / 3600;
        long durationMinutes = (totalSeconds % 3600) / 60;
        return durationHours + "h " + durationMinutes + "m";
    }

    public String getTotalVolumeText() {
        if (resultData == null) {
            return "0";
        }
        BigDecimal totalVolume = resultData.getTotalVolume();
        if (totalVolume == null) {
            return "0";
        }
        return totalVolume.stripTrailingZeros().toPlainString();
    }
}
