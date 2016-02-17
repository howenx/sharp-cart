package domain;

import java.util.List;

/**
 * 提交订单VO
 * Created by howen on 15/12/16.
 */
public class SettleOrderDTO {

    private List<SettleDTO> settleDTOs;
    private Long addressId;         //用户收获地址ID
    private String couponId;          //优惠券ID
    private String clientIp;        //客户端IP
    private Integer clientType;     //1.android,2.ios,3.web
    private Integer shipTime;       //1.工作日双休日与假期均可送货,2.只工作日送货,3.只双休日与假日送货
    private String  orderDesc;      //订单备注
    private String payMethod;       //支付方式JD.京东 APAY.支付宝 WEIXIN.微信
    private Integer buyNow;         //1.立即支付,2.购物车结算
    private Long pinActiveId;       //拼购活动ID

    public SettleOrderDTO() {
    }

    public SettleOrderDTO(List<SettleDTO> settleDTOs, Long addressId, String couponId, String clientIp, Integer clientType, Integer shipTime, String orderDesc, String payMethod, Integer buyNow, Long pinActiveId) {
        this.settleDTOs = settleDTOs;
        this.addressId = addressId;
        this.couponId = couponId;
        this.clientIp = clientIp;
        this.clientType = clientType;
        this.shipTime = shipTime;
        this.orderDesc = orderDesc;
        this.payMethod = payMethod;
        this.buyNow = buyNow;
        this.pinActiveId = pinActiveId;
    }

    public List<SettleDTO> getSettleDTOs() {
        return settleDTOs;
    }

    public void setSettleDTOs(List<SettleDTO> settleDTOs) {
        this.settleDTOs = settleDTOs;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String couponId) {
        this.couponId = couponId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Integer getClientType() {
        return clientType;
    }

    public void setClientType(Integer clientType) {
        this.clientType = clientType;
    }

    public Integer getShipTime() {
        return shipTime;
    }

    public void setShipTime(Integer shipTime) {
        this.shipTime = shipTime;
    }

    public String getOrderDesc() {
        return orderDesc;
    }

    public void setOrderDesc(String orderDesc) {
        this.orderDesc = orderDesc;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public Integer getBuyNow() {
        return buyNow;
    }

    public void setBuyNow(Integer buyNow) {
        this.buyNow = buyNow;
    }

    public Long getPinActiveId() {
        return pinActiveId;
    }

    public void setPinActiveId(Long pinActiveId) {
        this.pinActiveId = pinActiveId;
    }

    @Override
    public String toString() {
        return "SettleOrderDTO{" +
                "settleDTOs=" + settleDTOs +
                ", addressId=" + addressId +
                ", couponId='" + couponId + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", clientType=" + clientType +
                ", shipTime=" + shipTime +
                ", orderDesc='" + orderDesc + '\'' +
                ", payMethod='" + payMethod + '\'' +
                ", buyNow=" + buyNow +
                ", pinActiveId=" + pinActiveId +
                '}';
    }
}
