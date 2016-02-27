package domain;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 全体系统消息
 * Created by sibyl.sun on 16/2/22.
 */
public class Msg implements Serializable {
    private static final long serialVersionUID = 1L;
    private long msgId;//消息id
    private String msgTitle; //消息标题
    private String msgContent;//消息内容
    private String msgImg;  //消息商品图片
    private String msgUrl;  //消息跳转的URL
    private String msgType; //消息类型
    private Timestamp createAt; //创建时间
    private Timestamp endAt;//失效时间
    private String targetType; //T:主题，D:详细页面，P:拼购商品页，A:拼购活动页面，U:一个促销活动的链接

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public String getMsgTitle() {
        return msgTitle;
    }

    public void setMsgTitle(String msgTitle) {
        this.msgTitle = msgTitle;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public String getMsgImg() {
        return msgImg;
    }

    public void setMsgImg(String msgImg) {
        this.msgImg = msgImg;
    }

    public String getMsgUrl() {
        return msgUrl;
    }

    public void setMsgUrl(String msgUrl) {
        this.msgUrl = msgUrl;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public Timestamp getEndAt() {
        return endAt;
    }

    public void setEndAt(Timestamp endAt) {
        this.endAt = endAt;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
    @Override
    public String toString(){
        return "Msg [msgTitle="+msgTitle+
                ",msgContent="+msgContent+
                ",msgImg="+msgImg+
                ",msgUrl="+msgUrl+
                ",msgType="+msgType+
                ",createAt="+createAt+
                ",endAt="+endAt+
                ",targetType="+targetType;
    }

}
