package domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.MoneySerializer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * 用于返回拼购活动的DTO
 * Created by howen on 16/2/17.
 */
public class PinActivityDTO implements Serializable {

    private Long            pinActiveId;    //拼购活动ID
    private String          pinUrl;         //此团的分享短连接
    private Long            pinId;          //拼购ID
    private Long            masterUserId;   //团长用户ID
    private Integer         personNum;      //拼购人数
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal      pinPrice;       //拼购价格
    private Integer         joinPersons;    //已参加活动人数
    @JsonIgnore
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp       createAt;       //发起时间
    private String          status;         //状态    状态 Y-正常 N－取消 C－完成
    @JsonIgnore
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp       endAt;          //截止时间
    private String          pay;//是否新开团,normal,new
    private Long            endCountDown;//结束倒计时

    //拼购商品数据
    private String          pinImg;      //生成后列表图
    private String          pinSkuUrl;   //拼购商品链接
    private String          pinTitle;    //拼购商品标题

    private List<PinUser>   pinUsers;       //参与拼购活动的用于


    public PinActivityDTO() {
    }

    public PinActivityDTO(Long pinActiveId, String pinUrl, Long pinId, Long masterUserId, Integer personNum, BigDecimal pinPrice, Integer joinPersons, Timestamp createAt, String status, Timestamp endAt, String pay, Long endCountDown, List<PinUser> pinUsers, String pinImg, String pinSkuUrl, String pinTitle) {
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
        this.pay = pay;
        this.endCountDown = endCountDown;
        this.pinUsers = pinUsers;
        this.pinImg = pinImg;
        this.pinSkuUrl = pinSkuUrl;
        this.pinTitle = pinTitle;
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

    public void setPersonNum(Integer personNum) {
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

    public void setJoinPersons(Integer joinPersons) {
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

    public String getPay() {
        return pay;
    }

    public void setPay(String pay) {
        this.pay = pay;
    }

    public Long getEndCountDown() {
        return endCountDown;
    }

    public void setEndCountDown(Long endCountDown) {
        this.endCountDown = endCountDown;
    }

    public List<PinUser> getPinUsers() {
        return pinUsers;
    }

    public void setPinUsers(List<PinUser> pinUsers) {
        this.pinUsers = pinUsers;
    }

    public String getPinImg() {
        return pinImg;
    }

    public void setPinImg(String pinImg) {
        this.pinImg = pinImg;
    }

    public String getPinSkuUrl() {
        return pinSkuUrl;
    }

    public void setPinSkuUrl(String pinSkuUrl) {
        this.pinSkuUrl = pinSkuUrl;
    }

    public String getPinTitle() {
        return pinTitle;
    }

    public void setPinTitle(String pinTitle) {
        this.pinTitle = pinTitle;
    }

    @Override
    public String toString() {
        return "PinActivityDTO{" +
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
                ", pay='" + pay + '\'' +
                ", endCountDown=" + endCountDown +
                ", pinUsers=" + pinUsers +
                ", pinImg='" + pinImg + '\'' +
                ", pinSkuUrl='" + pinSkuUrl + '\'' +
                ", pinTitle='" + pinTitle + '\'' +
                '}';
    }
}
