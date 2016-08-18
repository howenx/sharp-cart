package domain;

import java.io.Serializable;

/**
 * 优惠券领取调用actor参数对象
 * Created by sibyl.sun on 16/8/18.
 */
public class CouponRec implements Serializable {
    private Long userId;
    private Long coupCateId;
    private Integer couponType;

    public CouponRec(Long userId, Long coupCateId, Integer couponType) {
        this.userId = userId;
        this.coupCateId = coupCateId;
        this.couponType = couponType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCoupCateId() {
        return coupCateId;
    }

    public void setCoupCateId(Long coupCateId) {
        this.coupCateId = coupCateId;
    }

    public Integer getCouponType() {
        return couponType;
    }

    public void setCouponType(Integer couponType) {
        this.couponType = couponType;
    }

    @Override
    public String toString() {
        return "CouponRec{" +
                "userId=" + userId +
                ", coupCateId=" + coupCateId +
                ", couponType=" + couponType +
                '}';
    }
}
