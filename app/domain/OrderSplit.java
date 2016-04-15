package domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.MoneySerializer;

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
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal totalFee;
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal totalPayFee;
    private Integer totalAmount;
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal shipFee;
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal postalFee;

    private String expressNum;
    private String expressCode;
    private String expressNm;

    private String  cbeArea;
    private String payInspReturnCode      ;
    private String payInspReturnMsg       ;
    private String payCustomsReturnCode   ;
    private String payCustomsReturnMsg    ;

    private String subPgTradeNo       ;
    private String payResponseCode    ;
    private String payResponseMsg     ;

    public OrderSplit() {
    }

    public OrderSplit(Long splitId, Long orderId, String state, String cbeCode, String inspReturnCode, String inspReturnMsg, String customsReturnCode, String customsReturnMsg, BigDecimal totalFee, BigDecimal totalPayFee, Integer totalAmount, BigDecimal shipFee, BigDecimal postalFee, String expressNum, String expressCode, String expressNm, String cbeArea, String payInspReturnCode, String payInspReturnMsg, String payCustomsReturnCode, String payCustomsReturnMsg, String subPgTradeNo, String payResponseCode, String payResponseMsg) {
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
        this.expressNum = expressNum;
        this.expressCode = expressCode;
        this.expressNm = expressNm;
        this.cbeArea = cbeArea;
        this.payInspReturnCode = payInspReturnCode;
        this.payInspReturnMsg = payInspReturnMsg;
        this.payCustomsReturnCode = payCustomsReturnCode;
        this.payCustomsReturnMsg = payCustomsReturnMsg;
        this.subPgTradeNo = subPgTradeNo;
        this.payResponseCode = payResponseCode;
        this.payResponseMsg = payResponseMsg;
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

    public String getExpressNum() {
        return expressNum;
    }

    public void setExpressNum(String expressNum) {
        this.expressNum = expressNum;
    }

    public String getExpressCode() {
        return expressCode;
    }

    public void setExpressCode(String expressCode) {
        this.expressCode = expressCode;
    }

    public String getExpressNm() {
        return expressNm;
    }

    public void setExpressNm(String expressNm) {
        this.expressNm = expressNm;
    }

    public String getCbeArea() {
        return cbeArea;
    }

    public void setCbeArea(String cbeArea) {
        this.cbeArea = cbeArea;
    }

    public String getPayInspReturnCode() {
        return payInspReturnCode;
    }

    public void setPayInspReturnCode(String payInspReturnCode) {
        this.payInspReturnCode = payInspReturnCode;
    }

    public String getPayInspReturnMsg() {
        return payInspReturnMsg;
    }

    public void setPayInspReturnMsg(String payInspReturnMsg) {
        this.payInspReturnMsg = payInspReturnMsg;
    }

    public String getPayCustomsReturnCode() {
        return payCustomsReturnCode;
    }

    public void setPayCustomsReturnCode(String payCustomsReturnCode) {
        this.payCustomsReturnCode = payCustomsReturnCode;
    }

    public String getPayCustomsReturnMsg() {
        return payCustomsReturnMsg;
    }

    public void setPayCustomsReturnMsg(String payCustomsReturnMsg) {
        this.payCustomsReturnMsg = payCustomsReturnMsg;
    }

    public String getSubPgTradeNo() {
        return subPgTradeNo;
    }

    public void setSubPgTradeNo(String subPgTradeNo) {
        this.subPgTradeNo = subPgTradeNo;
    }

    public String getPayResponseCode() {
        return payResponseCode;
    }

    public void setPayResponseCode(String payResponseCode) {
        this.payResponseCode = payResponseCode;
    }

    public String getPayResponseMsg() {
        return payResponseMsg;
    }

    public void setPayResponseMsg(String payResponseMsg) {
        this.payResponseMsg = payResponseMsg;
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
                ", expressNum='" + expressNum + '\'' +
                ", expressCode='" + expressCode + '\'' +
                ", expressNm='" + expressNm + '\'' +
                ", cbeArea='" + cbeArea + '\'' +
                ", payInspReturnCode='" + payInspReturnCode + '\'' +
                ", payInspReturnMsg='" + payInspReturnMsg + '\'' +
                ", payCustomsReturnCode='" + payCustomsReturnCode + '\'' +
                ", payCustomsReturnMsg='" + payCustomsReturnMsg + '\'' +
                ", subPgTradeNo='" + subPgTradeNo + '\'' +
                ", payResponseCode='" + payResponseCode + '\'' +
                ", payResponseMsg='" + payResponseMsg + '\'' +
                '}';
    }
}
