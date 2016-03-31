package domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 拼购商品设置优惠券策略
 * Created by tiffany on 16/1/20.
 */
public class PinCoupon implements Serializable {

    private Long id;                        // 主键ID
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp memberCouponEndAt;    //团员优惠券结束时间
    private int memberCouponQuota;          //团员优惠券限额
    private int masterCoupon;               //团长返券额度
    private String masterCouponClass;       //团长返券类别
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp masterCouponStartAt;  //团长优惠券开始时间
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp masterCouponEndAt;    //团长优惠券结束时间
    private int masterCouponQuota;          //团长优惠券限额
    private int memberCoupon;               //团员返券额度
    private String memberCouponClass;       //团员返券类别
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp memberCouponStartAt;  //团员优惠券开始时间
    private Long pinId;                     //拼购活动ID

    public PinCoupon() {
    }

    public PinCoupon(Long id, Timestamp memberCouponEndAt, int memberCouponQuota, int masterCoupon, String masterCouponClass, Timestamp masterCouponStartAt, Timestamp masterCouponEndAt, int masterCouponQuota, int memberCoupon, String memberCouponClass, Timestamp memberCouponStartAt, Long pinId) {
        this.id = id;
        this.memberCouponEndAt = memberCouponEndAt;
        this.memberCouponQuota = memberCouponQuota;
        this.masterCoupon = masterCoupon;
        this.masterCouponClass = masterCouponClass;
        this.masterCouponStartAt = masterCouponStartAt;
        this.masterCouponEndAt = masterCouponEndAt;
        this.masterCouponQuota = masterCouponQuota;
        this.memberCoupon = memberCoupon;
        this.memberCouponClass = memberCouponClass;
        this.memberCouponStartAt = memberCouponStartAt;
        this.pinId = pinId;
    }

    @Override
    public String toString() {
        return "PinCoupon{" +
                "id=" + id +
                ", memberCouponEndAt=" + memberCouponEndAt +
                ", memberCouponQuota=" + memberCouponQuota +
                ", masterCoupon=" + masterCoupon +
                ", masterCouponClass='" + masterCouponClass + '\'' +
                ", masterCouponStartAt=" + masterCouponStartAt +
                ", masterCouponEndAt=" + masterCouponEndAt +
                ", masterCouponQuota=" + masterCouponQuota +
                ", memberCoupon=" + memberCoupon +
                ", memberCouponClass='" + memberCouponClass + '\'' +
                ", memberCouponStartAt=" + memberCouponStartAt +
                ", pinId=" + pinId +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timestamp getMemberCouponEndAt() {
        return memberCouponEndAt;
    }

    public void setMemberCouponEndAt(Timestamp memberCouponEndAt) {
        this.memberCouponEndAt = memberCouponEndAt;
    }

    public int getMemberCouponQuota() {
        return memberCouponQuota;
    }

    public void setMemberCouponQuota(int memberCouponQuota) {
        this.memberCouponQuota = memberCouponQuota;
    }

    public int getMasterCoupon() {
        return masterCoupon;
    }

    public void setMasterCoupon(int masterCoupon) {
        this.masterCoupon = masterCoupon;
    }

    public String getMasterCouponClass() {
        return masterCouponClass;
    }

    public void setMasterCouponClass(String masterCouponClass) {
        this.masterCouponClass = masterCouponClass;
    }

    public Timestamp getMasterCouponStartAt() {
        return masterCouponStartAt;
    }

    public void setMasterCouponStartAt(Timestamp masterCouponStartAt) {
        this.masterCouponStartAt = masterCouponStartAt;
    }

    public Timestamp getMasterCouponEndAt() {
        return masterCouponEndAt;
    }

    public void setMasterCouponEndAt(Timestamp masterCouponEndAt) {
        this.masterCouponEndAt = masterCouponEndAt;
    }

    public int getMasterCouponQuota() {
        return masterCouponQuota;
    }

    public void setMasterCouponQuota(int masterCouponQuota) {
        this.masterCouponQuota = masterCouponQuota;
    }

    public int getMemberCoupon() {
        return memberCoupon;
    }

    public void setMemberCoupon(int memberCoupon) {
        this.memberCoupon = memberCoupon;
    }

    public String getMemberCouponClass() {
        return memberCouponClass;
    }

    public void setMemberCouponClass(String memberCouponClass) {
        this.memberCouponClass = memberCouponClass;
    }

    public Timestamp getMemberCouponStartAt() {
        return memberCouponStartAt;
    }

    public void setMemberCouponStartAt(Timestamp memberCouponStartAt) {
        this.memberCouponStartAt = memberCouponStartAt;
    }

    public Long getPinId() {
        return pinId;
    }

    public void setPinId(Long pinId) {
        this.pinId = pinId;
    }
}
