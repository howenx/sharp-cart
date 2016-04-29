package domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.MoneySerializer;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用于只显示价格,数量,标题,sku主图
 * Created by howen on 15/11/25.
 */
public class CartSkuDto implements Serializable {
    private static final long serialVersionUID = 21L;

    private Long skuId;
    private Integer amount;//购买数量
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal price;//下单时价格
    private String skuTitle;  //sku标题
    private String invImg;//sku主图
    private String invUrl;//用于方便前段获取库存跳转链接
    private String invAndroidUrl;//用于方便前段获取库存跳转链接
    private String itemColor;//颜色
    private String itemSize;//尺码
    private String skuType;//商品类型
    private Long skuTypeId;//商品类型ID
    private Long orderId;//订单编号


    public CartSkuDto() {
    }

    public CartSkuDto(Long skuId, Integer amount, BigDecimal price, String skuTitle, String invImg, String invUrl, String invAndroidUrl, String itemColor, String itemSize, String skuType, Long skuTypeId, Long orderId) {
        this.skuId = skuId;
        this.amount = amount;
        this.price = price;
        this.skuTitle = skuTitle;
        this.invImg = invImg;
        this.invUrl = invUrl;
        this.invAndroidUrl = invAndroidUrl;
        this.itemColor = itemColor;
        this.itemSize = itemSize;
        this.skuType = skuType;
        this.skuTypeId = skuTypeId;
        this.orderId = orderId;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
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

    public String getInvAndroidUrl() {
        return invAndroidUrl;
    }

    public void setInvAndroidUrl(String invAndroidUrl) {
        this.invAndroidUrl = invAndroidUrl;
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

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "CartSkuDto{" +
                "skuId=" + skuId +
                ", amount=" + amount +
                ", price=" + price +
                ", skuTitle='" + skuTitle + '\'' +
                ", invImg='" + invImg + '\'' +
                ", invUrl='" + invUrl + '\'' +
                ", invAndroidUrl='" + invAndroidUrl + '\'' +
                ", itemColor='" + itemColor + '\'' +
                ", itemSize='" + itemSize + '\'' +
                ", skuType='" + skuType + '\'' +
                ", skuTypeId=" + skuTypeId +
                ", orderId=" + orderId +
                '}';
    }
}
