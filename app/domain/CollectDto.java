package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 展示收藏数据
 * Created by sibyl.sun on 16/2/17.
 */
public class CollectDto implements Serializable {
    /**收藏ID*/
    private Long collectId;

    /**创建时间*/
    private Timestamp createAt;

    /**商品类型 1.vary,2.item,3.customize,4.pin*/
    private String skuType;
    /**商品类型所对应的ID*/
    private Long skuTypeId;

    private CartSkuDto cartSkuDto;




    public Long getCollectId() {
        return collectId;
    }

    public void setCollectId(Long collectId) {
        this.collectId = collectId;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public CartSkuDto getCartSkuDto() {
        return cartSkuDto;
    }

    public void setCartSkuDto(CartSkuDto cartSkuDto) {
        this.cartSkuDto = cartSkuDto;
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
}
