package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**购物车VO
 * Created by howen on 15/11/22.
 */
public class Cart implements Serializable{

    private Long         cartId;
    @JsonIgnore
    private Long         userId;
    private Long         skuId;
    private Long         itemId;
    private Integer      amount;
    @JsonIgnore
    private BigDecimal   price;
    @JsonIgnore
    private Timestamp    createAt;
    @JsonIgnore
    private Timestamp    destroyAt;
    @JsonIgnore
    private Timestamp    updateAt;
    private Long         orderId;
    private String       status;    //I:初始化，O：已下单，F：已完成，N：已删除，S:失效，G:勾选状态
    private String       skuTitle;  //sku标题
    private String       skuImg;//sku图片
    private String       skuType;//商品类型
    private Long         skuTypeId;//商品类型ID

    public Cart() {
    }

    public Cart(Long cartId, Long userId, Long skuId, Long itemId, Integer amount, BigDecimal price, Timestamp createAt, Timestamp destroyAt, Timestamp updateAt, Long orderId, String status, String skuTitle, String skuImg, String skuType, Long skuTypeId) {
        this.cartId = cartId;
        this.userId = userId;
        this.skuId = skuId;
        this.itemId = itemId;
        this.amount = amount;
        this.price = price;
        this.createAt = createAt;
        this.destroyAt = destroyAt;
        this.updateAt = updateAt;
        this.orderId = orderId;
        this.status = status;
        this.skuTitle = skuTitle;
        this.skuImg = skuImg;
        this.skuType = skuType;
        this.skuTypeId = skuTypeId;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
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

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    @Override
    public String toString() {
        return "Cart{" +
                "cartId=" + cartId +
                ", userId=" + userId +
                ", skuId=" + skuId +
                ", itemId=" + itemId +
                ", amount=" + amount +
                ", price=" + price +
                ", createAt=" + createAt +
                ", destroyAt=" + destroyAt +
                ", updateAt=" + updateAt +
                ", orderId=" + orderId +
                ", status='" + status + '\'' +
                ", skuTitle='" + skuTitle + '\'' +
                ", skuImg='" + skuImg + '\'' +
                ", skuType='" + skuType + '\'' +
                ", skuTypeId=" + skuTypeId +
                '}';
    }
}
