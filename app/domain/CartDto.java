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

    public CartDto() {
    }

    public CartDto(Long cartId, Long skuId, Integer amount, String state) {
        this.cartId = cartId;
        this.skuId = skuId;
        this.amount = amount;
        this.state = state;
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

    @Override
    public String toString() {
        return "CartDto{" +
                "cartId=" + cartId +
                ", skuId=" + skuId +
                ", amount=" + amount +
                ", state='" + state + '\'' +
                '}';
    }
}
