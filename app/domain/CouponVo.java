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
    private Timestamp startAt;
    private Timestamp endAt;
    private String state;
    @JsonIgnore
    private Long orderId;
    @JsonIgnore
    private Timestamp useAt;
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal limitQuota;


    public CouponVo() {
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

    public Timestamp getStartAt() {
        return startAt;
    }

    public void setStartAt(Timestamp startAt) {
        this.startAt = startAt;
    }

    public Timestamp getEndAt() {
        return endAt;
    }

    public void setEndAt(Timestamp endAt) {
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

    public Timestamp getUseAt() {
        return useAt;
    }

    public void setUseAt(Timestamp useAt) {
        this.useAt = useAt;
    }

    public BigDecimal getLimitQuota() {
        return limitQuota;
    }

    public void setLimitQuota(BigDecimal limitQuota) {
        this.limitQuota = limitQuota;
    }

    public CouponVo(String coupId, Long userId, Long cateId, BigDecimal denomination, Timestamp startAt, Timestamp endAt, String state, Long orderId, Timestamp useAt, BigDecimal limitQuota) {
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
    }

    @Override
    public String toString() {
        return "CouponVo{" +
                "coupId='" + coupId + '\'' +
                ", userId=" + userId +
                ", cateId=" + cateId +
                ", denomination=" + denomination +
                ", startAt=" + startAt +
                ", endAt=" + endAt +
                ", state='" + state + '\'' +
                ", orderId=" + orderId +
                ", useAt=" + useAt +
                ", limitQuota=" + limitQuota +
                '}';
    }
}
