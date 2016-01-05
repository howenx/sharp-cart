package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.DiscountSerializer;
import util.MoneySerializer;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 商品库存
 * Created by howen on 15/11/22.
 */
public class Sku {

    private     Long                id;//库存ID
    private     String              itemColor;//颜色
    private     String              itemSize;//尺码
    @JsonSerialize(using = MoneySerializer.class)
    private     BigDecimal          itemSrcPrice;//商品原价
    @JsonSerialize(using = MoneySerializer.class)
    private     BigDecimal          itemPrice;//商品价格

    @JsonSerialize(using = DiscountSerializer.class)
    private     BigDecimal          itemDiscount;//商品折扣
    private     Boolean             orMasterInv;//是否主商品
    private     String              state;//状态
    private     String              invArea;//库存区域区分：'B'保税区仓库发货，‘Z’韩国直邮
    private     Integer             restrictAmount;//限购数量
    private     Integer             restAmount;//商品余量
    private     String              invImg;//sku主图
    private     String              itemPreviewImgs;//sku预览图
    private     String              invUrl;//用于方便前段获取库存跳转链接
    private     String              invTitle;//sku标题
    private     String              invCustoms;//报关单位
    private     String              postalTaxRate;//税率
    private     String              postalStandard;//关税收费标准
    private     String              postalLimit;//海关规定的单笔订单金额不能超过的量值
    private     String              invAreaNm;//库存区域名称

    @JsonIgnore
    private     Long                itemId;
    @JsonIgnore
    private     Integer             amount;//库存总量
    @JsonIgnore
    private     BigDecimal          itemCostPrice; //商品成本价
    @JsonIgnore
    private     Integer             soldAmount;
    @JsonIgnore
    private     Boolean             orDestroy;
    @JsonIgnore
    private     Timestamp           destroyAt;
    @JsonIgnore
    private     Timestamp           updateAt;
    @JsonIgnore
    private     Timestamp           createAt;
    @JsonIgnore
    private     String              carriageModelCode;

    public Sku() {
    }

    public Sku(Long id, String itemColor, String itemSize, BigDecimal itemSrcPrice, BigDecimal itemPrice, BigDecimal itemDiscount, Boolean orMasterInv, String state, String invArea, Integer restrictAmount, Integer restAmount, String invImg, String itemPreviewImgs, String invUrl, String invTitle, String invCustoms, String postalTaxRate, String postalStandard, String postalLimit, String invAreaNm, Long itemId, Integer amount, BigDecimal itemCostPrice, Integer soldAmount, Boolean orDestroy, Timestamp destroyAt, Timestamp updateAt, Timestamp createAt, String carriageModelCode) {
        this.id = id;
        this.itemColor = itemColor;
        this.itemSize = itemSize;
        this.itemSrcPrice = itemSrcPrice;
        this.itemPrice = itemPrice;
        this.itemDiscount = itemDiscount;
        this.orMasterInv = orMasterInv;
        this.state = state;
        this.invArea = invArea;
        this.restrictAmount = restrictAmount;
        this.restAmount = restAmount;
        this.invImg = invImg;
        this.itemPreviewImgs = itemPreviewImgs;
        this.invUrl = invUrl;
        this.invTitle = invTitle;
        this.invCustoms = invCustoms;
        this.postalTaxRate = postalTaxRate;
        this.postalStandard = postalStandard;
        this.postalLimit = postalLimit;
        this.invAreaNm = invAreaNm;
        this.itemId = itemId;
        this.amount = amount;
        this.itemCostPrice = itemCostPrice;
        this.soldAmount = soldAmount;
        this.orDestroy = orDestroy;
        this.destroyAt = destroyAt;
        this.updateAt = updateAt;
        this.createAt = createAt;
        this.carriageModelCode = carriageModelCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getItemSrcPrice() {
        return itemSrcPrice;
    }

    public void setItemSrcPrice(BigDecimal itemSrcPrice) {
        this.itemSrcPrice = itemSrcPrice;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    public BigDecimal getItemDiscount() {
        return itemDiscount;
    }

    public void setItemDiscount(BigDecimal itemDiscount) {
        this.itemDiscount = itemDiscount;
    }

    public Boolean getOrMasterInv() {
        return orMasterInv;
    }

    public void setOrMasterInv(Boolean orMasterInv) {
        this.orMasterInv = orMasterInv;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getInvArea() {
        return invArea;
    }

    public void setInvArea(String invArea) {
        this.invArea = invArea;
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

    public String getItemPreviewImgs() {
        return itemPreviewImgs;
    }

    public void setItemPreviewImgs(String itemPreviewImgs) {
        this.itemPreviewImgs = itemPreviewImgs;
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

    public String getInvAreaNm() {
        return invAreaNm;
    }

    public void setInvAreaNm(String invAreaNm) {
        this.invAreaNm = invAreaNm;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public BigDecimal getItemCostPrice() {
        return itemCostPrice;
    }

    public void setItemCostPrice(BigDecimal itemCostPrice) {
        this.itemCostPrice = itemCostPrice;
    }

    public Integer getSoldAmount() {
        return soldAmount;
    }

    public void setSoldAmount(Integer soldAmount) {
        this.soldAmount = soldAmount;
    }

    public Boolean getOrDestroy() {
        return orDestroy;
    }

    public void setOrDestroy(Boolean orDestroy) {
        this.orDestroy = orDestroy;
    }

    public Timestamp getDestroyAt() {
        return destroyAt;
    }

    public void setDestroyAt(Timestamp destroyAt) {
        this.destroyAt = destroyAt;
    }

    public Timestamp getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Timestamp updateAt) {
        this.updateAt = updateAt;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public String getCarriageModelCode() {
        return carriageModelCode;
    }

    public void setCarriageModelCode(String carriageModelCode) {
        this.carriageModelCode = carriageModelCode;
    }

    @Override
    public String toString() {
        return "Sku{" +
                "id=" + id +
                ", itemColor='" + itemColor + '\'' +
                ", itemSize='" + itemSize + '\'' +
                ", itemSrcPrice=" + itemSrcPrice +
                ", itemPrice=" + itemPrice +
                ", itemDiscount=" + itemDiscount +
                ", orMasterInv=" + orMasterInv +
                ", state='" + state + '\'' +
                ", invArea='" + invArea + '\'' +
                ", restrictAmount=" + restrictAmount +
                ", restAmount=" + restAmount +
                ", invImg='" + invImg + '\'' +
                ", itemPreviewImgs='" + itemPreviewImgs + '\'' +
                ", invUrl='" + invUrl + '\'' +
                ", invTitle='" + invTitle + '\'' +
                ", invCustoms='" + invCustoms + '\'' +
                ", postalTaxRate='" + postalTaxRate + '\'' +
                ", postalStandard='" + postalStandard + '\'' +
                ", postalLimit='" + postalLimit + '\'' +
                ", invAreaNm='" + invAreaNm + '\'' +
                ", itemId=" + itemId +
                ", amount=" + amount +
                ", itemCostPrice=" + itemCostPrice +
                ", soldAmount=" + soldAmount +
                ", orDestroy=" + orDestroy +
                ", destroyAt=" + destroyAt +
                ", updateAt=" + updateAt +
                ", createAt=" + createAt +
                ", carriageModelCode='" + carriageModelCode + '\'' +
                '}';
    }
}
