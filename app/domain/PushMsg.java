package domain;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by sibyl.sun on 16/3/1.
 */
public class PushMsg implements Serializable {
    private static final long serialVersionUID = -1L;
    private String alert; //内容
    private String title;  //标题
    private String url;   //跳转url
    private String targetType;  //跳转目标
//    private String platform; //all android ios android_ios
    private String audience;//all tag  alias
    private String[] aliasOrTag;
    private Long timeToLive; //推送当前用户不在线时，为该用户保留多长时间的离线消息，以便其上线时再次推送。默认 86400 （1 天），最长 10 天。设置为 0 表示不保留离线消息，只有推送当前在线的用户可以收到。

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String[] getAliasOrTag() {
        return aliasOrTag;
    }

    public void setAliasOrTag(String[] aliasOrTag) {
        this.aliasOrTag = aliasOrTag;
    }

    public Long getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(Long timeToLive) {
        this.timeToLive = timeToLive;
    }

    @Override
    public String toString() {
        return "PushMsg{" +
                "alert='" + alert + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", targetType='" + targetType + '\'' +
                ", audience='" + audience + '\'' +
                ", aliasOrTag=" + Arrays.toString(aliasOrTag) +
                ", timeToLive=" + timeToLive +
                '}';
    }
}
