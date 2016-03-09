package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.MoneySerializer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 用于返回所有结算商品明细Vo
 * Created by howen on 16/2/16.
 */
public class SettleFeeVo  implements Serializable {

    private  String invCustoms; //海关名称

    private String invArea; //区分不同仓储地的code

    private String invAreaNm;//仓储地名称

    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal singleCustomsSumFee;//每个海关的总费用统计

    @JsonIgnore
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal singleCustomsSumPayFee;//每个海关的总支付费用统计

    private Integer singleCustomsSumAmount;//每个海关购买的总数量

    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal shipSingleCustomsFee;//每个海关邮费统计

    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal factSingleCustomsShipFee;//每个海关的实际邮费统计

    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal portalSingleCustomsFee;//每个海关的关税统计

    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal factPortalFeeSingleCustoms; //统计如果各个海关的实际关税,如果关税小于50元,则免税

    @JsonIgnore
    private Integer messageCode;

    @JsonIgnore
    private List<CartDto> cartDtos;

    @JsonIgnore
    private Boolean freeShip;

    @JsonIgnore
    private List<String> skuTypeList;//用于保存该笔订单的所有sku的类型

    @JsonIgnore
    private Long splitId;//

    @JsonIgnore
    private BigDecimal discountFeeSingleCustoms;

    public SettleFeeVo() {
    }

    public SettleFeeVo(String invCustoms, String invArea, String invAreaNm, BigDecimal singleCustomsSumFee, BigDecimal singleCustomsSumPayFee, Integer singleCustomsSumAmount, BigDecimal shipSingleCustomsFee, BigDecimal factSingleCustomsShipFee, BigDecimal portalSingleCustomsFee, BigDecimal factPortalFeeSingleCustoms, Integer messageCode, List<CartDto> cartDtos, Boolean freeShip, List<String> skuTypeList, Long splitId, BigDecimal discountFeeSingleCustoms) {
        this.invCustoms = invCustoms;
        this.invArea = invArea;
        this.invAreaNm = invAreaNm;
        this.singleCustomsSumFee = singleCustomsSumFee;
        this.singleCustomsSumPayFee = singleCustomsSumPayFee;
        this.singleCustomsSumAmount = singleCustomsSumAmount;
        this.shipSingleCustomsFee = shipSingleCustomsFee;
        this.factSingleCustomsShipFee = factSingleCustomsShipFee;
        this.portalSingleCustomsFee = portalSingleCustomsFee;
        this.factPortalFeeSingleCustoms = factPortalFeeSingleCustoms;
        this.messageCode = messageCode;
        this.cartDtos = cartDtos;
        this.freeShip = freeShip;
        this.skuTypeList = skuTypeList;
        this.splitId = splitId;
        this.discountFeeSingleCustoms = discountFeeSingleCustoms;
    }

    public String getInvCustoms() {
        return invCustoms;
    }

    public void setInvCustoms(String invCustoms) {
        this.invCustoms = invCustoms;
    }

    public String getInvArea() {
        return invArea;
    }

    public void setInvArea(String invArea) {
        this.invArea = invArea;
    }

    public String getInvAreaNm() {
        return invAreaNm;
    }

    public void setInvAreaNm(String invAreaNm) {
        this.invAreaNm = invAreaNm;
    }

    public BigDecimal getSingleCustomsSumFee() {
        return singleCustomsSumFee;
    }

    public void setSingleCustomsSumFee(BigDecimal singleCustomsSumFee) {
        this.singleCustomsSumFee = singleCustomsSumFee;
    }

    public BigDecimal getSingleCustomsSumPayFee() {
        return singleCustomsSumPayFee;
    }

    public void setSingleCustomsSumPayFee(BigDecimal singleCustomsSumPayFee) {
        this.singleCustomsSumPayFee = singleCustomsSumPayFee;
    }

    public Integer getSingleCustomsSumAmount() {
        return singleCustomsSumAmount;
    }

    public void setSingleCustomsSumAmount(Integer singleCustomsSumAmount) {
        this.singleCustomsSumAmount = singleCustomsSumAmount;
    }

    public BigDecimal getShipSingleCustomsFee() {
        return shipSingleCustomsFee;
    }

    public void setShipSingleCustomsFee(BigDecimal shipSingleCustomsFee) {
        this.shipSingleCustomsFee = shipSingleCustomsFee;
    }

    public BigDecimal getFactSingleCustomsShipFee() {
        return factSingleCustomsShipFee;
    }

    public void setFactSingleCustomsShipFee(BigDecimal factSingleCustomsShipFee) {
        this.factSingleCustomsShipFee = factSingleCustomsShipFee;
    }

    public BigDecimal getPortalSingleCustomsFee() {
        return portalSingleCustomsFee;
    }

    public void setPortalSingleCustomsFee(BigDecimal portalSingleCustomsFee) {
        this.portalSingleCustomsFee = portalSingleCustomsFee;
    }

    public BigDecimal getFactPortalFeeSingleCustoms() {
        return factPortalFeeSingleCustoms;
    }

    public void setFactPortalFeeSingleCustoms(BigDecimal factPortalFeeSingleCustoms) {
        this.factPortalFeeSingleCustoms = factPortalFeeSingleCustoms;
    }

    public Integer getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(Integer messageCode) {
        this.messageCode = messageCode;
    }

    public List<CartDto> getCartDtos() {
        return cartDtos;
    }

    public void setCartDtos(List<CartDto> cartDtos) {
        this.cartDtos = cartDtos;
    }

    public Boolean getFreeShip() {
        return freeShip;
    }

    public void setFreeShip(Boolean freeShip) {
        this.freeShip = freeShip;
    }

    public List<String> getSkuTypeList() {
        return skuTypeList;
    }

    public void setSkuTypeList(List<String> skuTypeList) {
        this.skuTypeList = skuTypeList;
    }

    public Long getSplitId() {
        return splitId;
    }

    public void setSplitId(Long splitId) {
        this.splitId = splitId;
    }

    public BigDecimal getDiscountFeeSingleCustoms() {
        return discountFeeSingleCustoms;
    }

    public void setDiscountFeeSingleCustoms(BigDecimal discountFeeSingleCustoms) {
        this.discountFeeSingleCustoms = discountFeeSingleCustoms;
    }

    @Override
    public String toString() {
        return "SettleFeeVo{" +
                "invCustoms='" + invCustoms + '\'' +
                ", invArea='" + invArea + '\'' +
                ", invAreaNm='" + invAreaNm + '\'' +
                ", singleCustomsSumFee=" + singleCustomsSumFee +
                ", singleCustomsSumPayFee=" + singleCustomsSumPayFee +
                ", singleCustomsSumAmount=" + singleCustomsSumAmount +
                ", shipSingleCustomsFee=" + shipSingleCustomsFee +
                ", factSingleCustomsShipFee=" + factSingleCustomsShipFee +
                ", portalSingleCustomsFee=" + portalSingleCustomsFee +
                ", factPortalFeeSingleCustoms=" + factPortalFeeSingleCustoms +
                ", messageCode=" + messageCode +
                ", cartDtos=" + cartDtos +
                ", freeShip=" + freeShip +
                ", skuTypeList=" + skuTypeList +
                ", splitId=" + splitId +
                ", discountFeeSingleCustoms=" + discountFeeSingleCustoms +
                '}';
    }
}
