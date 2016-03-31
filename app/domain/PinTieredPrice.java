package domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.MoneySerializer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 *拼购价格表
 * Created by tiffany on 16/1/28.
 */
public class PinTieredPrice implements Serializable {

    @JsonIgnore
    private Long id;                        //阶梯价格ID
    @JsonIgnore
    private String masterCouponClass;       //团长返券类别
    private String masterCouponClassName;   //团长返券类别名称

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp masterCouponStartAt;  //团长优惠券开始时间
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp masterCouponEndAt;    //团长优惠券结束时间
    private Integer masterCouponQuota;          //团长优惠券限额
    private Integer memberCoupon;               //团员优惠券额度
    @JsonIgnore
    private String memberCouponClass;       //团员返券类别
    private String memberCouponClassName;   //团长返券类别名称
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp memberCouponStartAt;  //团员优惠券开始时间
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp memberCouponEndAt;     //团员优惠券结束时间
    private Integer memberCouponQuota;          //团员优惠券限额
    @JsonIgnore
    private Long pinId;                         //pin库存ID
    private Integer peopleNum;                  //人数
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal price;                   //价格
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal masterMinPrice;          //团长减价
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal memberMinPrice;          //团员减价
    private Integer masterCoupon;               //团长返券额度

    public PinTieredPrice() {
    }

    public PinTieredPrice(Long id, String masterCouponClass, String masterCouponClassName, Timestamp masterCouponStartAt, Timestamp masterCouponEndAt, Integer masterCouponQuota, Integer memberCoupon, String memberCouponClass, String memberCouponClassName, Timestamp memberCouponStartAt, Timestamp memberCouponEndAt, Integer memberCouponQuota, Long pinId, Integer peopleNum, BigDecimal price, BigDecimal masterMinPrice, BigDecimal memberMinPrice, Integer masterCoupon) {
        this.id = id;
        this.masterCouponClass = masterCouponClass;
        this.masterCouponClassName = masterCouponClassName;
        this.masterCouponStartAt = masterCouponStartAt;
        this.masterCouponEndAt = masterCouponEndAt;
        this.masterCouponQuota = masterCouponQuota;
        this.memberCoupon = memberCoupon;
        this.memberCouponClass = memberCouponClass;
        this.memberCouponClassName = memberCouponClassName;
        this.memberCouponStartAt = memberCouponStartAt;
        this.memberCouponEndAt = memberCouponEndAt;
        this.memberCouponQuota = memberCouponQuota;
        this.pinId = pinId;
        this.peopleNum = peopleNum;
        this.price = price;
        this.masterMinPrice = masterMinPrice;
        this.memberMinPrice = memberMinPrice;
        this.masterCoupon = masterCoupon;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMasterCouponClass() {
        return masterCouponClass;
    }

    public void setMasterCouponClass(String masterCouponClass) {
        this.masterCouponClass = masterCouponClass;
    }

    public String getMasterCouponClassName() {
        return masterCouponClassName;
    }

    public void setMasterCouponClassName(String masterCouponClassName) {
        this.masterCouponClassName = masterCouponClassName;
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

    public Integer getMasterCouponQuota() {
        return masterCouponQuota;
    }

    public void setMasterCouponQuota(Integer masterCouponQuota) {
        this.masterCouponQuota = masterCouponQuota;
    }

    public Integer getMemberCoupon() {
        return memberCoupon;
    }

    public void setMemberCoupon(Integer memberCoupon) {
        this.memberCoupon = memberCoupon;
    }

    public String getMemberCouponClass() {
        return memberCouponClass;
    }

    public void setMemberCouponClass(String memberCouponClass) {
        this.memberCouponClass = memberCouponClass;
    }

    public String getMemberCouponClassName() {
        return memberCouponClassName;
    }

    public void setMemberCouponClassName(String memberCouponClassName) {
        this.memberCouponClassName = memberCouponClassName;
    }

    public Timestamp getMemberCouponStartAt() {
        return memberCouponStartAt;
    }

    public void setMemberCouponStartAt(Timestamp memberCouponStartAt) {
        this.memberCouponStartAt = memberCouponStartAt;
    }

    public Timestamp getMemberCouponEndAt() {
        return memberCouponEndAt;
    }

    public void setMemberCouponEndAt(Timestamp memberCouponEndAt) {
        this.memberCouponEndAt = memberCouponEndAt;
    }

    public Integer getMemberCouponQuota() {
        return memberCouponQuota;
    }

    public void setMemberCouponQuota(Integer memberCouponQuota) {
        this.memberCouponQuota = memberCouponQuota;
    }

    public Long getPinId() {
        return pinId;
    }

    public void setPinId(Long pinId) {
        this.pinId = pinId;
    }

    public Integer getPeopleNum() {
        return peopleNum;
    }

    public void setPeopleNum(Integer peopleNum) {
        this.peopleNum = peopleNum;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getMasterMinPrice() {
        return masterMinPrice;
    }

    public void setMasterMinPrice(BigDecimal masterMinPrice) {
        this.masterMinPrice = masterMinPrice;
    }

    public BigDecimal getMemberMinPrice() {
        return memberMinPrice;
    }

    public void setMemberMinPrice(BigDecimal memberMinPrice) {
        this.memberMinPrice = memberMinPrice;
    }

    public Integer getMasterCoupon() {
        return masterCoupon;
    }

    public void setMasterCoupon(Integer masterCoupon) {
        this.masterCoupon = masterCoupon;
    }

    @Override
    public String toString() {
        return "PinTieredPrice{" +
                "id=" + id +
                ", masterCouponClass='" + masterCouponClass + '\'' +
                ", masterCouponClassName='" + masterCouponClassName + '\'' +
                ", masterCouponStartAt=" + masterCouponStartAt +
                ", masterCouponEndAt=" + masterCouponEndAt +
                ", masterCouponQuota=" + masterCouponQuota +
                ", memberCoupon=" + memberCoupon +
                ", memberCouponClass='" + memberCouponClass + '\'' +
                ", memberCouponClassName='" + memberCouponClassName + '\'' +
                ", memberCouponStartAt=" + memberCouponStartAt +
                ", memberCouponEndAt=" + memberCouponEndAt +
                ", memberCouponQuota=" + memberCouponQuota +
                ", pinId=" + pinId +
                ", peopleNum=" + peopleNum +
                ", price=" + price +
                ", masterMinPrice=" + masterMinPrice +
                ", memberMinPrice=" + memberMinPrice +
                ", masterCoupon=" + masterCoupon +
                '}';
    }
}
