package domain;

import java.io.Serializable;

/**
 * Created by sibyl.sun on 16/3/1.
 */
public class PushMsg implements Serializable {
    private static final long serialVersionUID = -1L;
    private String alert;
    private String title;
    private String url;
    private String targetType;
//    private String platform; //all android ios android_ios
    private String audience;//all tag  alias
    private String[] aliasOrTag;

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
    @Override
    public String toString(){
        return "PushMsg [alert="+alert+
                ",title="+title+
                ",url="+url+
                ",targetType="+targetType+
                ",audience="+audience+
                ",aliasOrTag="+aliasOrTag;
    }
}
