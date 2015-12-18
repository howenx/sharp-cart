package domain;

import java.io.Serializable;
import java.security.Timestamp;

/**
 * 用户附加信息表
 * Created by howen on 15/12/18.
 */
public class IdPlus implements Serializable{

    private Long plusId;
    private Long userId;
    private String payJdToken;//京东支付token
    private Timestamp updateAt;
    private Timestamp createAt;
    private Integer updateTimes;//更新次数

    public IdPlus(Long plusId, Long userId, String payJdToken, Timestamp updateAt, Timestamp createAt, Integer updateTimes) {
        this.plusId = plusId;
        this.userId = userId;
        this.payJdToken = payJdToken;
        this.updateAt = updateAt;
        this.createAt = createAt;
        this.updateTimes = updateTimes;
    }

    public IdPlus() {
    }

    public Long getPlusId() {
        return plusId;
    }

    public void setPlusId(Long plusId) {
        this.plusId = plusId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPayJdToken() {
        return payJdToken;
    }

    public void setPayJdToken(String payJdToken) {
        this.payJdToken = payJdToken;
    }

    public Timestamp getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Timestamp updateAt) {
        this.updateAt = updateAt;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public Integer getUpdateTimes() {
        return updateTimes;
    }

    public void setUpdateTimes(Integer updateTimes) {
        this.updateTimes = updateTimes;
    }

    @Override
    public String toString() {
        return "IdPlus{" +
                "plusId=" + plusId +
                ", userId=" + userId +
                ", payJdToken='" + payJdToken + '\'' +
                ", updateAt=" + updateAt +
                ", createAt=" + createAt +
                ", updateTimes=" + updateTimes +
                '}';
    }
}
