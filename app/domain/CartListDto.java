package domain;

import java.io.Serializable;
import java.math.BigDecimal;

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
    private     BigDecimal      itemPrice;//商品价格
    private     String          state;//状态
    private     BigDecimal      shipFee;//邮费
    private     String          invArea;//库存区域区分：'B'保税区仓库发货，‘Z’韩国直邮
    private     Integer         restrictAmount;//限购数量
    private     Integer         restAmount;//商品余量
    private     String          invImg;//sku主图
    private     String          invUrl;//用于方便前段获取库存跳转链接
    private     String          invTitle;//sku标题

    public CartListDto() {
    }

    public CartListDto(Long cartId, Long skuId, Integer amount, String itemColor, String itemSize, BigDecimal itemPrice, String state, BigDecimal shipFee, String invArea, Integer restrictAmount, Integer restAmount, String invImg, String invUrl, String invTitle) {
        this.cartId = cartId;
        this.skuId = skuId;
        this.amount = amount;
        this.itemColor = itemColor;
        this.itemSize = itemSize;
        this.itemPrice = itemPrice;
        this.state = state;
        this.shipFee = shipFee;
        this.invArea = invArea;
        this.restrictAmount = restrictAmount;
        this.restAmount = restAmount;
        this.invImg = invImg;
        this.invUrl = invUrl;
        this.invTitle = invTitle;
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
                ", restrictAmount=" + restrictAmount +
                ", restAmount=" + restAmount +
                ", invImg='" + invImg + '\'' +
                ", invUrl='" + invUrl + '\'' +
                ", invTitle='" + invTitle + '\'' +
                '}';
    }
}
