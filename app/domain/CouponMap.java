package domain;

import java.io.Serializable;

/**
 * Created by sibyl.sun on 16/8/25.
 */
public class CouponMap implements Serializable {
    private Long id;
    private Long cateTypeId ;  //0表示任意商品，item_id，inv_id，pin_id，cate_id，theme_id
    private Integer cateType;//1.全场，2.商品，3.sku，4.拼购商品，5.商品分类，6.主题
    private Long couponCateId;//优惠券类别ID
    private Boolean orDestory; //true：已经删除

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCateTypeId() {
        return cateTypeId;
    }

    public void setCateTypeId(Long cateTypeId) {
        this.cateTypeId = cateTypeId;
    }

    public Integer getCateType() {
        return cateType;
    }

    public void setCateType(Integer cateType) {
        this.cateType = cateType;
    }

    public Long getCouponCateId() {
        return couponCateId;
    }

    public void setCouponCateId(Long couponCateId) {
        this.couponCateId = couponCateId;
    }

    public Boolean getOrDestory() {
        return orDestory;
    }

    public void setOrDestory(Boolean orDestory) {
        this.orDestory = orDestory;
    }

    @Override
    public String toString() {
        return "CouponMap{" +
                "id=" + id +
                ", cateTypeId=" + cateTypeId +
                ", cateType=" + cateType +
                ", couponCateId=" + couponCateId +
                ", orDestory=" + orDestory +
                '}';
    }
}
