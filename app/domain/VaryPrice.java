package domain;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by Sunny Wu on 16/1/19.
 * kakao china.
 * 多样化价格
 */
public class VaryPrice implements Serializable {

    private Long id;                //主键id
    private Long invId;             //库存Id
    private BigDecimal price;       //销售价
    private Integer soldAmount;     //售出数量
    private Integer limitAmount;    //限制销售数量
    private String status;          //状态(Y-正常，N-下架)
    private String themeId;        //主题ID

    public VaryPrice() {
    }

    public VaryPrice(Long id, Long invId, BigDecimal price, Integer soldAmount, Integer limitAmount, String status, String themeId) {
        this.id = id;
        this.invId = invId;
        this.price = price;
        this.soldAmount = soldAmount;
        this.limitAmount = limitAmount;
        this.status = status;
        this.themeId = themeId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvId() {
        return invId;
    }

    public void setInvId(Long invId) {
        this.invId = invId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getSoldAmount() {
        return soldAmount;
    }

    public void setSoldAmount(Integer soldAmount) {
        this.soldAmount = soldAmount;
    }

    public Integer getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(Integer limitAmount) {
        this.limitAmount = limitAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }

    @Override
    public String toString() {
        return "VaryPrice{" +
                "id=" + id +
                ", invId=" + invId +
                ", price=" + price +
                ", soldAmount=" + soldAmount +
                ", limitAmount=" + limitAmount +
                ", status='" + status + '\'' +
                ", themeId='" + themeId + '\'' +
                '}';
    }
}