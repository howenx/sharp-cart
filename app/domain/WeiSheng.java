package domain;

import java.io.Serializable;

/**
 * 威盛
 * Created by sibyl.sun on 16/8/11.
 */
public class WeiSheng implements Serializable {
    private String trackingId;
    private String expressNo;
    private Boolean orUse;

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getExpressNo() {
        return expressNo;
    }

    public void setExpressNo(String expressNo) {
        this.expressNo = expressNo;
    }

    public Boolean getOrUse() {
        return orUse;
    }

    public void setOrUse(Boolean orUse) {
        this.orUse = orUse;
    }

    @Override
    public String toString() {
        return "WeiSheng{" +
                "trackingId='" + trackingId + '\'' +
                ", expressNo='" + expressNo + '\'' +
                ", orUse=" + orUse +
                '}';
    }
}
