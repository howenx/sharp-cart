package domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.MoneySerializer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 拼购活动
 * Created by tiffany on 16/1/20.
 */
public class PinActivity implements Serializable {
    private Long pinActiveId;   //拼购活动ID
    private String pinUrl;      //此团的分享短连接
    private Long pinId;         //拼购ID
    private Long masterUserId;  //团长用户ID
    private Integer personNum;      //拼购人数
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal pinPrice;//拼购价格
    private Integer joinPersons;    //已参加活动人数
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createAt; //发起时间
    private String status;      //状态
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp endAt;    //截止时间
    private Long    pinTieredId;//拼购阶梯价格ID

    public PinActivity() {
    }

    public PinActivity(Long pinActiveId, String pinUrl, Long pinId, Long masterUserId, int personNum, BigDecimal pinPrice, int joinPersons, Timestamp createAt, String status, Timestamp endAt, Long pinTieredId) {
        this.pinActiveId = pinActiveId;
        this.pinUrl = pinUrl;
        this.pinId = pinId;
        this.masterUserId = masterUserId;
        this.personNum = personNum;
        this.pinPrice = pinPrice;
        this.joinPersons = joinPersons;
        this.createAt = createAt;
        this.status = status;
        this.endAt = endAt;
        this.pinTieredId = pinTieredId;
    }

    public Long getPinActiveId() {
        return pinActiveId;
    }

    public void setPinActiveId(Long pinActiveId) {
        this.pinActiveId = pinActiveId;
    }

    public String getPinUrl() {
        return pinUrl;
    }

    public void setPinUrl(String pinUrl) {
        this.pinUrl = pinUrl;
    }

    public Long getPinId() {
        return pinId;
    }

    public void setPinId(Long pinId) {
        this.pinId = pinId;
    }

    public Long getMasterUserId() {
        return masterUserId;
    }

    public void setMasterUserId(Long masterUserId) {
        this.masterUserId = masterUserId;
    }

    public Integer getPersonNum() {
        return personNum;
    }

    public void setPersonNum(int personNum) {
        this.personNum = personNum;
    }

    public BigDecimal getPinPrice() {
        return pinPrice;
    }

    public void setPinPrice(BigDecimal pinPrice) {
        this.pinPrice = pinPrice;
    }

    public Integer getJoinPersons() {
        return joinPersons;
    }

    public void setJoinPersons(int joinPersons) {
        this.joinPersons = joinPersons;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getEndAt() {
        return endAt;
    }

    public void setEndAt(Timestamp endAt) {
        this.endAt = endAt;
    }

    public Long getPinTieredId() {
        return pinTieredId;
    }

    public void setPinTieredId(Long pinTieredId) {
        this.pinTieredId = pinTieredId;
    }

    @Override
    public String toString() {
        return "PinActivity{" +
                "pinActiveId=" + pinActiveId +
                ", pinUrl='" + pinUrl + '\'' +
                ", pinId=" + pinId +
                ", masterUserId=" + masterUserId +
                ", personNum=" + personNum +
                ", pinPrice=" + pinPrice +
                ", joinPersons=" + joinPersons +
                ", createAt=" + createAt +
                ", status='" + status + '\'' +
                ", endAt=" + endAt +
                ", pinTieredId=" + pinTieredId +
                '}';
    }
}
