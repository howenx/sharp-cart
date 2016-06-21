package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.MoneySerializer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 订单
 * Created by howen on 15/11/25.
 */
public class Order implements Serializable {

    private Long                orderId;//订单ID
    @JsonIgnore
    private Long                userId;//用户ID
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal          payTotal;//这笔订单的实际需要支付的总费用
    private String              payMethod;//充值渠道
    private String              orderCreateAt;//用户创建订单时间
    @JsonIgnore
    private String              orderIp;//订单IP
    @JsonIgnore
    private String              pgTradeNo;//支付订单号
    private String              orderStatus;//I:初始化，S:成功，C：取消，F:失败
    @JsonIgnore
    private String              errorStr;//支付返回的错误信息
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal          discount;//优惠了多少钱
    @JsonIgnore
    private Timestamp           updatedAt;//订单最后修改时间
    private String              orderDesc;//订单备注
    private Long                addId;//用户订单地址
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal          shipFee;//邮费
    private String              confirmReceiveAt;//确认收货时间
    private String              orderDetailUrl;//订单相信页面url
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal          postalFee;//行邮税
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal          totalFee;//购买商品总费用
    @JsonIgnore
    private Integer             shipTime;//发货时间
    @JsonIgnore
    private Integer             clientType;//客户端类型
    private Integer             orderAmount;//订单购买货物总数
    private Long                orderSplitId;//子订单编号
    private Long                countDown;//倒计时
    @JsonIgnore
    private Integer             orderType;//订单类型
    @JsonIgnore
    private Long                pinActiveId;       //拼购活动ID
    private String              remark;//是否评价完成,Y评价完成,N未评价完成,null空
    @JsonIgnore
    private String              payMethodSub;//子支付方式，如微信JSAPI,NATIVE,APP

    @JsonIgnore
    private Boolean             orDel;//是否删除

    public Order() {
    }

    public Order(Long orderId, Long userId, BigDecimal payTotal, String payMethod, String orderCreateAt, String orderIp, String pgTradeNo, String orderStatus, String errorStr, BigDecimal discount, Timestamp updatedAt, String orderDesc, Long addId, BigDecimal shipFee, String confirmReceiveAt, String orderDetailUrl, BigDecimal postalFee, BigDecimal totalFee, Integer shipTime, Integer clientType, Integer orderAmount, Long orderSplitId, Long countDown, Integer orderType, Long pinActiveId, String remark, String payMethodSub, Boolean orDel) {
        this.orderId = orderId;
        this.userId = userId;
        this.payTotal = payTotal;
        this.payMethod = payMethod;
        this.orderCreateAt = orderCreateAt;
        this.orderIp = orderIp;
        this.pgTradeNo = pgTradeNo;
        this.orderStatus = orderStatus;
        this.errorStr = errorStr;
        this.discount = discount;
        this.updatedAt = updatedAt;
        this.orderDesc = orderDesc;
        this.addId = addId;
        this.shipFee = shipFee;
        this.confirmReceiveAt = confirmReceiveAt;
        this.orderDetailUrl = orderDetailUrl;
        this.postalFee = postalFee;
        this.totalFee = totalFee;
        this.shipTime = shipTime;
        this.clientType = clientType;
        this.orderAmount = orderAmount;
        this.orderSplitId = orderSplitId;
        this.countDown = countDown;
        this.orderType = orderType;
        this.pinActiveId = pinActiveId;
        this.remark = remark;
        this.payMethodSub = payMethodSub;
        this.orDel = orDel;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getPayTotal() {
        return payTotal;
    }

    public void setPayTotal(BigDecimal payTotal) {
        this.payTotal = payTotal;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getOrderCreateAt() {
        return orderCreateAt;
    }

    public void setOrderCreateAt(String orderCreateAt) {
        this.orderCreateAt = orderCreateAt;
    }

    public String getOrderIp() {
        return orderIp;
    }

    public void setOrderIp(String orderIp) {
        this.orderIp = orderIp;
    }

    public String getPgTradeNo() {
        return pgTradeNo;
    }

    public void setPgTradeNo(String pgTradeNo) {
        this.pgTradeNo = pgTradeNo;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getErrorStr() {
        return errorStr;
    }

    public void setErrorStr(String errorStr) {
        this.errorStr = errorStr;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getOrderDesc() {
        return orderDesc;
    }

    public void setOrderDesc(String orderDesc) {
        this.orderDesc = orderDesc;
    }

    public Long getAddId() {
        return addId;
    }

    public void setAddId(Long addId) {
        this.addId = addId;
    }

    public BigDecimal getShipFee() {
        return shipFee;
    }

    public void setShipFee(BigDecimal shipFee) {
        this.shipFee = shipFee;
    }

    public String getConfirmReceiveAt() {
        return confirmReceiveAt;
    }

    public void setConfirmReceiveAt(String confirmReceiveAt) {
        this.confirmReceiveAt = confirmReceiveAt;
    }

    public String getOrderDetailUrl() {
        return orderDetailUrl;
    }

    public void setOrderDetailUrl(String orderDetailUrl) {
        this.orderDetailUrl = orderDetailUrl;
    }

    public BigDecimal getPostalFee() {
        return postalFee;
    }

    public void setPostalFee(BigDecimal postalFee) {
        this.postalFee = postalFee;
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }

    public Integer getShipTime() {
        return shipTime;
    }

    public void setShipTime(Integer shipTime) {
        this.shipTime = shipTime;
    }

    public Integer getClientType() {
        return clientType;
    }

    public void setClientType(Integer clientType) {
        this.clientType = clientType;
    }

    public Integer getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(Integer orderAmount) {
        this.orderAmount = orderAmount;
    }

    public Long getOrderSplitId() {
        return orderSplitId;
    }

    public void setOrderSplitId(Long orderSplitId) {
        this.orderSplitId = orderSplitId;
    }

    public Long getCountDown() {
        return countDown;
    }

    public void setCountDown(Long countDown) {
        this.countDown = countDown;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public Long getPinActiveId() {
        return pinActiveId;
    }

    public void setPinActiveId(Long pinActiveId) {
        this.pinActiveId = pinActiveId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPayMethodSub() {
        return payMethodSub;
    }

    public void setPayMethodSub(String payMethodSub) {
        this.payMethodSub = payMethodSub;
    }

    public Boolean getOrDel() {
        return orDel;
    }

    public void setOrDel(Boolean orDel) {
        this.orDel = orDel;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", payTotal=" + payTotal +
                ", payMethod='" + payMethod + '\'' +
                ", orderCreateAt='" + orderCreateAt + '\'' +
                ", orderIp='" + orderIp + '\'' +
                ", pgTradeNo='" + pgTradeNo + '\'' +
                ", orderStatus='" + orderStatus + '\'' +
                ", errorStr='" + errorStr + '\'' +
                ", discount=" + discount +
                ", updatedAt=" + updatedAt +
                ", orderDesc='" + orderDesc + '\'' +
                ", addId=" + addId +
                ", shipFee=" + shipFee +
                ", confirmReceiveAt='" + confirmReceiveAt + '\'' +
                ", orderDetailUrl='" + orderDetailUrl + '\'' +
                ", postalFee=" + postalFee +
                ", totalFee=" + totalFee +
                ", shipTime=" + shipTime +
                ", clientType=" + clientType +
                ", orderAmount=" + orderAmount +
                ", orderSplitId=" + orderSplitId +
                ", countDown=" + countDown +
                ", orderType=" + orderType +
                ", pinActiveId=" + pinActiveId +
                ", remark='" + remark + '\'' +
                ", payMethodSub='" + payMethodSub + '\'' +
                ", orDel=" + orDel +
                '}';
    }
}
