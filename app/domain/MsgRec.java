package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.sql.Timestamp;

/**
 * 消息
 * Created by sibyl.sun on 16/2/22.
 */
public class MsgRec {
    private long id;//唯一键
    private long userId;//用户ID
    @JsonIgnore
    private int msgRecType; //收到的消息类型  1-普通消息  2-系统消息
    @JsonIgnore
    private long msgId;//收到消息的id,系统消息去取
    private String msgTitle; //消息标题
    private String msgContent;//消息内容
    private String msgImg;  //消息商品图片
    private String msgUrl;  //消息跳转的URL
    private int msgType; //消息类型
    private Timestamp createAt; //创建时间
    @JsonIgnore
    private int status; //0-正常 1-删除

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

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
