package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.MoneySerializer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 用于返回订单预览页的entity
 * Created by howen on 16/2/16.
 */
public class SettleVo implements Serializable {

    private Address address;//用户地址

    private List<SettleFeeVo> singleCustoms;//单个海关下的费用明细

    private String postalStandard;//行邮税扣税标准

    private List<CouponVo> coupons;//优惠券列表

    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal factShipFee;//实际邮费

    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal shipFee;//邮费

    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal portalFee;//行邮税

    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal factPortalFee;//实际行邮税


    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal totalFee;//总费用


    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal totalPayFee;//总支付费用

    @JsonIgnore
    private Long userId;//用户ID


//    @JsonSerialize(using = MoneySerializer.class)
    @JsonIgnore
    private BigDecimal freeShipLimit;//免邮费限额

    @JsonIgnore
    private Integer messageCode;

    @JsonIgnore
    private List<String> skuTypeList;//用于保存该笔订单的所有sku的类型


    @JsonSerialize(using = MoneySerializer.class)
//    @JsonIgnore
    private BigDecimal discountFee;//优惠券优惠的金额

    @JsonIgnore
    private String couponId;

    @JsonIgnore
    private Integer buyNow;//购买方式

    @JsonIgnore
    private Long orderId;//订单ID

    public SettleVo() {
    }

    public SettleVo(Address address, List<SettleFeeVo> singleCustoms, String postalStandard, List<CouponVo> coupons, BigDecimal factShipFee, BigDecimal shipFee, BigDecimal portalFee, BigDecimal factPortalFee, BigDecimal totalFee, BigDecimal totalPayFee, Long userId, BigDecimal freeShipLimit, Integer messageCode, List<String> skuTypeList, BigDecimal discountFee, String couponId, Integer buyNow, Long orderId) {
        this.address = address;
        this.singleCustoms = singleCustoms;
        this.postalStandard = postalStandard;
        this.coupons = coupons;
        this.factShipFee = factShipFee;
        this.shipFee = shipFee;
        this.portalFee = portalFee;
        this.factPortalFee = factPortalFee;
        this.totalFee = totalFee;
        this.totalPayFee = totalPayFee;
        this.userId = userId;
        this.freeShipLimit = freeShipLimit;
        this.messageCode = messageCode;
        this.skuTypeList = skuTypeList;
        this.discountFee = discountFee;
        this.couponId = couponId;
        this.buyNow = buyNow;
        this.orderId = orderId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<SettleFeeVo> getSingleCustoms() {
        return singleCustoms;
    }

    public void setSingleCustoms(List<SettleFeeVo> singleCustoms) {
        this.singleCustoms = singleCustoms;
    }

    public String getPostalStandard() {
        return postalStandard;
    }

    public void setPostalStandard(String postalStandard) {
        this.postalStandard = postalStandard;
    }

    public List<CouponVo> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<CouponVo> coupons) {
        this.coupons = coupons;
    }

    public BigDecimal getFactShipFee() {
        return factShipFee;
    }

    public void setFactShipFee(BigDecimal factShipFee) {
        this.factShipFee = factShipFee;
    }

    public BigDecimal getShipFee() {
        return shipFee;
    }

    public void setShipFee(BigDecimal shipFee) {
        this.shipFee = shipFee;
    }

    public BigDecimal getPortalFee() {
        return portalFee;
    }

    public void setPortalFee(BigDecimal portalFee) {
        this.portalFee = portalFee;
    }

    public BigDecimal getFactPortalFee() {
        return factPortalFee;
    }

    public void setFactPortalFee(BigDecimal factPortalFee) {
        this.factPortalFee = factPortalFee;
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }

    public BigDecimal getTotalPayFee() {
        return totalPayFee;
    }

    public void setTotalPayFee(BigDecimal totalPayFee) {
        this.totalPayFee = totalPayFee;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getFreeShipLimit() {
        return freeShipLimit;
    }

    public void setFreeShipLimit(BigDecimal freeShipLimit) {
        this.freeShipLimit = freeShipLimit;
    }

    public Integer getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(Integer messageCode) {
        this.messageCode = messageCode;
    }

    public List<String> getSkuTypeList() {
        return skuTypeList;
    }

    public void setSkuTypeList(List<String> skuTypeList) {
        this.skuTypeList = skuTypeList;
    }

    public BigDecimal getDiscountFee() {
        return discountFee;
    }

    public void setDiscountFee(BigDecimal discountFee) {
        this.discountFee = discountFee;
    }

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String couponId) {
        this.couponId = couponId;
    }

    public Integer getBuyNow() {
        return buyNow;
    }

    public void setBuyNow(Integer buyNow) {
        this.buyNow = buyNow;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "SettleVo{" +
                "address=" + address +
                ", singleCustoms=" + singleCustoms +
                ", postalStandard='" + postalStandard + '\'' +
                ", coupons=" + coupons +
                ", factShipFee=" + factShipFee +
                ", shipFee=" + shipFee +
                ", portalFee=" + portalFee +
                ", factPortalFee=" + factPortalFee +
                ", totalFee=" + totalFee +
                ", totalPayFee=" + totalPayFee +
                ", userId=" + userId +
                ", freeShipLimit=" + freeShipLimit +
                ", messageCode=" + messageCode +
                ", skuTypeList=" + skuTypeList +
                ", discountFee=" + discountFee +
                ", couponId='" + couponId + '\'' +
                ", buyNow=" + buyNow +
                ", orderId=" + orderId +
                '}';
    }
}
