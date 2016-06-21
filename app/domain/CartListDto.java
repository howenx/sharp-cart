package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.MoneySerializer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 用于返回购物车列表的DTO
 * Created by howen on 15/11/24.
 */
public class CartListDto implements Serializable{

    private     Long            cartId;//购物车id
    private     Long            skuId;//库存id
    private     Integer         amount;//购物车数量
    private     String          itemColor;//颜色
    private     String          itemSize;//尺码
    @JsonSerialize(using = MoneySerializer.class)
    private     BigDecimal      itemPrice;//商品价格
    private     String          state;//状态
    @JsonSerialize(using = MoneySerializer.class)
    private     BigDecimal      shipFee;//邮费
    private     String          invArea;//库存区域区分：'B'保税区仓库发货，‘Z’韩国直邮
    private     String          invAreaNm;//库存区域名称
    private     Integer         restrictAmount;//限购数量
    private     Integer         restAmount;//商品余量
    private     String          invImg;//sku主图
    private     String          invUrl;//用于方便前段获取库存跳转链接
    private     String          invTitle;//sku标题
    private     String          cartDelUrl;//用于删除操作的链接
    private     Timestamp       createAt;
    private     String          invCustoms;//报关单位
    private     String          postalTaxRate;//税率
    private     String          postalStandard;//关税收费标准
    private     String          postalLimit;//海关规定的单笔订单金额不能超过的量值
    @JsonIgnore
    private     String          invUrlAndroid;//安卓端的URL
    private     String          skuType;//商品类型
    private     Long            skuTypeId;//商品类型ID
    private     String          orCheck;//是否勾选,'Y'为已经选中,null为未勾选

    public CartListDto() {
    }

    public CartListDto(Long cartId, Long skuId, Integer amount, String itemColor, String itemSize, BigDecimal itemPrice, String state, BigDecimal shipFee, String invArea, String invAreaNm, Integer restrictAmount, Integer restAmount, String invImg, String invUrl, String invTitle, String cartDelUrl, Timestamp createAt, String invCustoms, String postalTaxRate, String postalStandard, String postalLimit, String invUrlAndroid, String skuType, Long skuTypeId, String orCheck) {
        this.cartId = cartId;
        this.skuId = skuId;
        this.amount = amount;
        this.itemColor = itemColor;
        this.itemSize = itemSize;
        this.itemPrice = itemPrice;
        this.state = state;
        this.shipFee = shipFee;
        this.invArea = invArea;
        this.invAreaNm = invAreaNm;
        this.restrictAmount = restrictAmount;
        this.restAmount = restAmount;
        this.invImg = invImg;
        this.invUrl = invUrl;
        this.invTitle = invTitle;
        this.cartDelUrl = cartDelUrl;
        this.createAt = createAt;
        this.invCustoms = invCustoms;
        this.postalTaxRate = postalTaxRate;
        this.postalStandard = postalStandard;
        this.postalLimit = postalLimit;
        this.invUrlAndroid = invUrlAndroid;
        this.skuType = skuType;
        this.skuTypeId = skuTypeId;
        this.orCheck = orCheck;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getItemColor() {
        return itemColor;
    }

    public void setItemColor(String itemColor) {
        this.itemColor = itemColor;
    }

    public String getItemSize() {
        return itemSize;
    }

    public void setItemSize(String itemSize) {
        this.itemSize = itemSize;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public BigDecimal getShipFee() {
        return shipFee;
    }

    public void setShipFee(BigDecimal shipFee) {
        this.shipFee = shipFee;
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

    public Integer getRestrictAmount() {
        return restrictAmount;
    }

    public void setRestrictAmount(Integer restrictAmount) {
        this.restrictAmount = restrictAmount;
    }

    public Integer getRestAmount() {
        return restAmount;
    }

    public void setRestAmount(Integer restAmount) {
        this.restAmount = restAmount;
    }

    public String getInvImg() {
        return invImg;
    }

    public void setInvImg(String invImg) {
        this.invImg = invImg;
    }

    public String getInvUrl() {
        return invUrl;
    }

    public void setInvUrl(String invUrl) {
        this.invUrl = invUrl;
    }

    public String getInvTitle() {
        return invTitle;
    }

    public void setInvTitle(String invTitle) {
        this.invTitle = invTitle;
    }

    public String getCartDelUrl() {
        return cartDelUrl;
    }

    public void setCartDelUrl(String cartDelUrl) {
        this.cartDelUrl = cartDelUrl;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public String getInvCustoms() {
        return invCustoms;
    }

    public void setInvCustoms(String invCustoms) {
        this.invCustoms = invCustoms;
    }

    public String getPostalTaxRate() {
        return postalTaxRate;
    }

    public void setPostalTaxRate(String postalTaxRate) {
        this.postalTaxRate = postalTaxRate;
    }

    public String getPostalStandard() {
        return postalStandard;
    }

    public void setPostalStandard(String postalStandard) {
        this.postalStandard = postalStandard;
    }

    public String getPostalLimit() {
        return postalLimit;
    }

    public void setPostalLimit(String postalLimit) {
        this.postalLimit = postalLimit;
    }

    public String getInvUrlAndroid() {
        return invUrlAndroid;
    }

    public void setInvUrlAndroid(String invUrlAndroid) {
        this.invUrlAndroid = invUrlAndroid;
    }

    public String getSkuType() {
        return skuType;
    }

    public void setSkuType(String skuType) {
        this.skuType = skuType;
    }

    public Long getSkuTypeId() {
        return skuTypeId;
    }

    public void setSkuTypeId(Long skuTypeId) {
        this.skuTypeId = skuTypeId;
    }

    public String getOrCheck() {
        return orCheck;
    }

    public void setOrCheck(String orCheck) {
        this.orCheck = orCheck;
    }

    @Override
    public String toString() {
        return "CartListDto{" +
                "cartId=" + cartId +
                ", skuId=" + skuId +
                ", amount=" + amount +
                ", itemColor='" + itemColor + '\'' +
                ", itemSize='" + itemSize + '\'' +
                ", itemPrice=" + itemPrice +
                ", state='" + state + '\'' +
                ", shipFee=" + shipFee +
                ", invArea='" + invArea + '\'' +
                ", invAreaNm='" + invAreaNm + '\'' +
                ", restrictAmount=" + restrictAmount +
                ", restAmount=" + restAmount +
                ", invImg='" + invImg + '\'' +
                ", invUrl='" + invUrl + '\'' +
                ", invTitle='" + invTitle + '\'' +
                ", cartDelUrl='" + cartDelUrl + '\'' +
                ", createAt=" + createAt +
                ", invCustoms='" + invCustoms + '\'' +
                ", postalTaxRate='" + postalTaxRate + '\'' +
                ", postalStandard='" + postalStandard + '\'' +
                ", postalLimit='" + postalLimit + '\'' +
                ", invUrlAndroid='" + invUrlAndroid + '\'' +
                ", skuType='" + skuType + '\'' +
                ", skuTypeId=" + skuTypeId +
                ", orCheck='" + orCheck + '\'' +
                '}';
    }
}
