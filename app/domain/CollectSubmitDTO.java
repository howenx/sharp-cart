package domain;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 客户端发过来的收藏数据
 * Created by sibyl.sun on 16/2/17.
 */
public class CollectSubmitDTO implements Serializable {

    private Long skuId; //sku id
    private String skuType;//商品类型 1.vary,2.item,3.customize,4.pin
    private Long skuTypeId;//商品类型所对应的ID

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
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
        return "CollectSubmitDTO{" +
                "skuId=" + skuId +
                ", skuTypeId="+skuTypeId +
                ", skuType=" + skuType +
                "}";
    }
}
