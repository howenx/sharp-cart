package domain;

import java.io.Serializable;

/**
 * cartDto
 * Created by howen on 15/11/24.
 */
public class CartDto implements Serializable {

    private Long cartId;//购物车ID
    private Long skuId;//skuID
    private Integer amount;//购买数量
    private String state;//状态
    private String skuType;//商品类型 1.vary,2.item,3.customize,4.pin
    private Long skuTypeId;//商品类型所对应的ID
    private Long pinTieredPriceId;//拼购价格ID

    public CartDto() {
    }

    public CartDto(Long cartId, Long skuId, Integer amount, String state, String skuType, Long skuTypeId, Long pinTieredPriceId) {
        this.cartId = cartId;
        this.skuId = skuId;
        this.amount = amount;
        this.state = state;
        this.skuType = skuType;
        this.skuTypeId = skuTypeId;
        this.pinTieredPriceId = pinTieredPriceId;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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
        return "CartDto{" +
                "cartId=" + cartId +
                ", skuId=" + skuId +
                ", amount=" + amount +
                ", state='" + state + '\'' +
                ", skuType='" + skuType + '\'' +
                ", skuTypeId=" + skuTypeId +
                ", pinTieredPriceId=" + pinTieredPriceId +
                '}';
    }
}
