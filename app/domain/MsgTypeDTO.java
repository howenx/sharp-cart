package domain;

import java.sql.Timestamp;

/**
 * 消息类型展示数据
 * Created by sibyl.sun on 16/4/7.
 */
public class MsgTypeDTO {
    private String msgType; //消息类型
    private Integer num;//消息个数
    private String content;//消息内容
    private Timestamp createAt; //创建时间

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }
}
