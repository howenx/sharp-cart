package domain;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 子订单
 * Created by howen on 15/12/16.
 */
public class OrderSplit implements Serializable{

    private Long splitId;
    private Long orderId;
    private String state;
    private String cbeCode;
    private String inspReturnCode;
    private String inspReturnMsg;
    private String customsReturnCode;
    private String customsReturnMsg;
    private BigDecimal totalFee;
    private BigDecimal totalPayFee;
    private Integer totalAmount;
    private BigDecimal shipFee;
    private BigDecimal postalFee;
    private String  cbeArea;

    public OrderSplit() {
    }

    public OrderSplit(Long splitId, Long orderId, String state, String cbeCode, String inspReturnCode, String inspReturnMsg, String customsReturnCode, String customsReturnMsg, BigDecimal totalFee, BigDecimal totalPayFee, Integer totalAmount, BigDecimal shipFee, BigDecimal postalFee, String cbeArea) {
        this.splitId = splitId;
        this.orderId = orderId;
        this.state = state;
        this.cbeCode = cbeCode;
        this.inspReturnCode = inspReturnCode;
        this.inspReturnMsg = inspReturnMsg;
        this.customsReturnCode = customsReturnCode;
        this.customsReturnMsg = customsReturnMsg;
        this.totalFee = totalFee;
        this.totalPayFee = totalPayFee;
        this.totalAmount = totalAmount;
        this.shipFee = shipFee;
        this.postalFee = postalFee;
        this.cbeArea = cbeArea;
    }

    public Long getSplitId() {
        return splitId;
    }

    public void setSplitId(Long splitId) {
        this.splitId = splitId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCbeCode() {
        return cbeCode;
    }

    public void setCbeCode(String cbeCode) {
        this.cbeCode = cbeCode;
    }

    public String getInspReturnCode() {
        return inspReturnCode;
    }

    public void setInspReturnCode(String inspReturnCode) {
        this.inspReturnCode = inspReturnCode;
    }

    public String getInspReturnMsg() {
        return inspReturnMsg;
    }

    public void setInspReturnMsg(String inspReturnMsg) {
        this.inspReturnMsg = inspReturnMsg;
    }

    public String getCustomsReturnCode() {
        return customsReturnCode;
    }

    public void setCustomsReturnCode(String customsReturnCode) {
        this.customsReturnCode = customsReturnCode;
    }

    public String getCustomsReturnMsg() {
        return customsReturnMsg;
    }

    public void setCustomsReturnMsg(String customsReturnMsg) {
        this.customsReturnMsg = customsReturnMsg;
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

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getShipFee() {
        return shipFee;
    }

    public void setShipFee(BigDecimal shipFee) {
        this.shipFee = shipFee;
    }

    public BigDecimal getPostalFee() {
        return postalFee;
    }

    public void setPostalFee(BigDecimal postalFee) {
        this.postalFee = postalFee;
    }

    public String getCbeArea() {
        return cbeArea;
    }

    public void setCbeArea(String cbeArea) {
        this.cbeArea = cbeArea;
    }

    @Override
    public String toString() {
        return "OrderSplit{" +
                "splitId=" + splitId +
                ", orderId=" + orderId +
                ", state='" + state + '\'' +
                ", cbeCode='" + cbeCode + '\'' +
                ", inspReturnCode='" + inspReturnCode + '\'' +
                ", inspReturnMsg='" + inspReturnMsg + '\'' +
                ", customsReturnCode='" + customsReturnCode + '\'' +
                ", customsReturnMsg='" + customsReturnMsg + '\'' +
                ", totalFee=" + totalFee +
                ", totalPayFee=" + totalPayFee +
                ", totalAmount=" + totalAmount +
                ", shipFee=" + shipFee +
                ", postalFee=" + postalFee +
                ", cbeArea='" + cbeArea + '\'' +
                '}';
    }
}
