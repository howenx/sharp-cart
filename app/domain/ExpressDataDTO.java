package domain;

/**
 * 物流
 * Created by sibyl.sun on 16/8/12.
 */
public class ExpressDataDTO {
    private String time;
    private String context;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "ExpressDataDTO{" +
                "time='" + time + '\'' +
                ", context='" + context + '\'' +
                '}';
    }
}
