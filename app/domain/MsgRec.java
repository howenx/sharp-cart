package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 消息
 * Created by sibyl.sun on 16/2/22.
 */
public class MsgRec implements Serializable {
    private static final long serialVersionUID = 1L;
    private long id;//唯一键
    @JsonIgnore
    private long userId;//用户ID
    @JsonIgnore
    private int msgRecType; //收到的消息类型  1-普通消息  2-系统消息
    @JsonIgnore
    private long msgId;//收到消息的id,系统消息去取
    private String msgTitle; //消息标题
    private String msgContent;//消息内容
    private String msgImg;  //消息商品图片
    private String msgUrl;  //消息跳转的URL
    private String msgType; //消息类型
    private Timestamp createAt; //创建时间
    @JsonIgnore
    private int readStatus; //1-未读 2-已读
    @JsonIgnore
    private int delStatus;  //1-未删 2-已删

    private String targetType; //T:主题，D:详细页面，P:拼购商品页，A:拼购活动页面，U:一个促销活动的链接

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getMsgRecType() {
        return msgRecType;
    }

    public void setMsgRecType(int msgRecType) {
        this.msgRecType = msgRecType;
    }

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

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public int getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }

    public int getDelStatus() {
        return delStatus;
    }

    public void setDelStatus(int delStatus) {
        this.delStatus = delStatus;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }
    @Override
    public String toString(){
        return "Msg [userId=" +userId+
                "msgTitle="+msgTitle+
                ",msgContent="+msgContent+
                ",msgImg="+msgImg+
                ",msgUrl="+msgUrl+
                ",msgType="+msgType+
                ",createAt="+createAt+
                ",msgId="+msgId+
                ",targetType="+targetType;
    }
}
