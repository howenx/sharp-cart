package domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 优惠券
 * Created by howen on 15/12/1.
 */
public class Coupon implements Serializable {

    private String coupId;
    private Long userId;
    private Long cateId;
    private BigDecimal denomination;
    private Timestamp startAt;
    private Timestamp endAt;
    private String state;
    private Long orderId;
    private Timestamp useAt;

    public Coupon() {
    }

    public Coupon(String coupId, Long userId, Long cateId, BigDecimal denomination, Timestamp startAt, Timestamp endAt, String state, Long orderId, Timestamp useAt) {
        this.coupId = coupId;
        this.userId = userId;
        this.cateId = cateId;
        this.denomination = denomination;
        this.startAt = startAt;
        this.endAt = endAt;
        this.state = state;
        this.orderId = orderId;
        this.useAt = useAt;
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

    @Override
    public String toString() {
        return "Coupon{" +
                "coupId='" + coupId + '\'' +
                ", userId=" + userId +
                ", cateId=" + cateId +
                ", denomination=" + denomination +
                ", startAt=" + startAt +
                ", endAt=" + endAt +
                ", state='" + state + '\'' +
                ", orderId=" + orderId +
                ", useAt=" + useAt +
                '}';
    }
}
