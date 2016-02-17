package domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.MoneySerializer;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细表
 * Created by howen on 15/12/16.
 */
public class OrderLine implements Serializable {

    private Long lineId;
    private Long orderId;
    private Long skuId;
    private Long itemId;
    private Integer amount;
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal price;
    private String skuTitle;
    private String skuImg;
    private Long splitId;
    private String skuSize;
    private String skuColor;
    private String skuType;
    private Long    skuTypeId;
    private Long    pinTieredPriceId;

    public OrderLine() {
    }

    public OrderLine(Long lineId, Long orderId, Long skuId, Long itemId, Integer amount, BigDecimal price, String skuTitle, String skuImg, Long splitId, String skuSize, String skuColor, String skuType, Long skuTypeId, Long pinTieredPriceId) {
        this.lineId = lineId;
        this.orderId = orderId;
        this.skuId = skuId;
        this.itemId = itemId;
        this.amount = amount;
        this.price = price;
        this.skuTitle = skuTitle;
        this.skuImg = skuImg;
        this.splitId = splitId;
        this.skuSize = skuSize;
        this.skuColor = skuColor;
        this.skuType = skuType;
        this.skuTypeId = skuTypeId;
        this.pinTieredPriceId = pinTieredPriceId;
    }

    public Long getLineId() {
        return lineId;
    }

    public void setLineId(Long lineId) {
        this.lineId = lineId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getSkuTitle() {
        return skuTitle;
    }

    public void setSkuTitle(String skuTitle) {
        this.skuTitle = skuTitle;
    }

    public String getSkuImg() {
        return skuImg;
    }

    public void setSkuImg(String skuImg) {
        this.skuImg = skuImg;
    }

    public Long getSplitId() {
        return splitId;
    }

    public void setSplitId(Long splitId) {
        this.splitId = splitId;
    }

    public String getSkuSize() {
        return skuSize;
    }

    public void setSkuSize(String skuSize) {
        this.skuSize = skuSize;
    }

    public String getSkuColor() {
        return skuColor;
    }

    public void setSkuColor(String skuColor) {
        this.skuColor = skuColor;
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

    public Long getPinTieredPriceId() {
        return pinTieredPriceId;
    }

    public void setPinTieredPriceId(Long pinTieredPriceId) {
        this.pinTieredPriceId = pinTieredPriceId;
    }

    @Override
    public String toString() {
        return "OrderLine{" +
                "lineId=" + lineId +
                ", orderId=" + orderId +
                ", skuId=" + skuId +
                ", itemId=" + itemId +
                ", amount=" + amount +
                ", price=" + price +
                ", skuTitle='" + skuTitle + '\'' +
                ", skuImg='" + skuImg + '\'' +
                ", splitId=" + splitId +
                ", skuSize='" + skuSize + '\'' +
                ", skuColor='" + skuColor + '\'' +
                ", skuType='" + skuType + '\'' +
                ", skuTypeId=" + skuTypeId +
                ", pinTieredPriceId=" + pinTieredPriceId +
                '}';
    }
}
