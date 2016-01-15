package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.MoneySerializer;
import util.StringUnicodeSerializer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 用于只显示价格,数量,标题,sku主图
 * Created by howen on 15/11/25.
 */
public class CartSkuDto implements Serializable{

    private     Long            skuId;
    private     Integer         amount;//购买数量
    @JsonSerialize(using = MoneySerializer.class)
    private     BigDecimal      price;//下单时价格
//    @JsonRawValue
//    @JsonSerialize(using = StringUnicodeSerializer.class)
    private     String          skuTitle;  //sku标题
    private     String          invImg;//sku主图
    private     String          invUrl;//用于方便前段获取库存跳转链接
    private     String          itemColor;//颜色
    private     String          itemSize;//尺码

    public CartSkuDto() {
    }

    public CartSkuDto(Long skuId, Integer amount, BigDecimal price, String skuTitle, String invImg, String invUrl, String itemColor, String itemSize) {
        this.skuId = skuId;
        this.amount = amount;
        this.price = price;
        this.skuTitle = skuTitle;
        this.invImg = invImg;
        this.invUrl = invUrl;
        this.itemColor = itemColor;
        this.itemSize = itemSize;
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

    @Override
    public String toString() {
        return "CartSkuDto{" +
                "skuId=" + skuId +
                ", amount=" + amount +
                ", price=" + price +
                ", skuTitle='" + skuTitle + '\'' +
                ", invImg='" + invImg + '\'' +
                ", invUrl='" + invUrl + '\'' +
                ", itemColor='" + itemColor + '\'' +
                ", itemSize='" + itemSize + '\'' +
                '}';
    }
}
