package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 收藏
 * Created by sibyl.sun on 16/2/16.
 */
public class Collect implements Serializable {
    /**收藏ID*/
    private Long collectId;
    /**用户ID*/
    private Long userId;
    /**商品ID*/
    private Long skuId;
    /**创建时间*/
    private Timestamp createAt;
    /**商品类型 1.vary,2.item,3.customize,4.pin*/
    private String skuType;
    /**商品类型所对应的ID*/
    private Long skuTypeId;

    public Long getCollectId() {
        return collectId;
    }

    public void setCollectId(Long collectId) {
        this.collectId = collectId;
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

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
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
        return "Collect{" +
                "collectId=" + collectId +
                ", userId=" + userId +
                ", skuId=" + skuId +
                ", createAt=" + createAt +
                ", skuType='" + skuType + '\'' +
                ", skuTypeId=" + skuTypeId +
                '}';
    }
}
