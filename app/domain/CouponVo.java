package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.MoneySerializer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 优惠券
 * Created by howen on 15/12/1.
 */
public class CouponVo implements Serializable {

    private String coupId;
    @JsonIgnore
    private Long userId;
    @JsonIgnore
    private Long cateId;
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal denomination;

    private String startAt;

    private String endAt;
    private String state;
    @JsonIgnore
    private Long orderId;

    private String useAt;
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal limitQuota;
    private String cateNm;


    public CouponVo() {
    }

    public CouponVo(String coupId, Long userId, Long cateId, BigDecimal denomination, String startAt, String endAt, String state, Long orderId, String useAt, BigDecimal limitQuota, String cateNm) {
        this.coupId = coupId;
        this.userId = userId;
        this.cateId = cateId;
        this.denomination = denomination;
        this.startAt = startAt;
        this.endAt = endAt;
        this.state = state;
        this.orderId = orderId;
        this.useAt = useAt;
        this.limitQuota = limitQuota;
        this.cateNm = cateNm;
    }

    public String getCoupId() {
        return coupId;
    }

    public void setCoupId(String coupId) {
        this.coupId = coupId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCateId() {
        return cateId;
    }

    public void setCateId(Long cateId) {
        this.cateId = cateId;
    }

    public BigDecimal getDenomination() {
        return denomination;
    }

    public void setDenomination(BigDecimal denomination) {
        this.denomination = denomination;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getUseAt() {
        return useAt;
    }

    public void setUseAt(String useAt) {
        this.useAt = useAt;
    }

    public BigDecimal getLimitQuota() {
        return limitQuota;
    }

    public void setLimitQuota(BigDecimal limitQuota) {
        this.limitQuota = limitQuota;
    }

    public String getCateNm() {
        return cateNm;
    }

    public void setCateNm(String cateNm) {
        this.cateNm = cateNm;
    }

    @Override
    public String toString() {
        return "CouponVo{" +
                "coupId='" + coupId + '\'' +
                ", userId=" + userId +
                ", cateId=" + cateId +
                ", denomination=" + denomination +
                ", startAt='" + startAt + '\'' +
                ", endAt='" + endAt + '\'' +
                ", state='" + state + '\'' +
                ", orderId=" + orderId +
                ", useAt='" + useAt + '\'' +
                ", limitQuota=" + limitQuota +
                ", cateNm='" + cateNm + '\'' +
                '}';
    }
}
