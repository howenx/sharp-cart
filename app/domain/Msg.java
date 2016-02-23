package domain;

import java.sql.Timestamp;

/**
 * 消息
 * Created by sibyl.sun on 16/2/22.
 */
public class Msg {
    private long msgId;//消息id
    private String msgTitle; //消息标题
    private String msgContent;//消息内容
    private String msgImg;  //消息商品图片
    private String msgUrl;  //消息跳转的URL
    private int msgType; //消息类型
    private String msgTargetType; //推送目标类型 1-全体
    private String msgTarget;//消息目标,可为空
    private Timestamp createAt; //创建时

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

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getMsgTargetType() {
        return msgTargetType;
    }

    public void setMsgTargetType(String msgTargetType) {
        this.msgTargetType = msgTargetType;
    }

    public String getMsgTarget() {
        return msgTarget;
    }

    public void setMsgTarget(String msgTarget) {
        this.msgTarget = msgTarget;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }
}
