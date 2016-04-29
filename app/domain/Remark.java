package domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户评价
 * Created by howen on 16/4/25.
 */
public class Remark implements Serializable {

    private static final long serialVersionUID = 21L;
    @JsonIgnore
    private Long id;//主键
    @JsonIgnore
    private Long userId;//用户ID
    @JsonIgnore
    private Long orderId;//订单ID
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createAt;//评价时间
    @Constraints.MinLength(10)
    @Constraints.MaxLength(500)
    private String content;//评价内容
    private String picture;//晒图
    @Constraints.Required
    private Integer grade;//评分1,2,3,4,5
    @Constraints.Required
    private String skuType;//商品类型
    @Constraints.Required
    private Long skuTypeId;//商品类型ID

    @JsonIgnore
    private Integer pageSize;
    @JsonIgnore
    private Integer offset;

    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();
        if (content.trim().length() < 10 || content.trim().length() > 500) {
            errors.add(new ValidationError("content", "This content length is wrong"));
        }
        if (grade!=1 && grade!=2 && grade!=3 && grade!=4  && grade!=5 ) {
            errors.add(new ValidationError("grade", "This grade is wrong"));
        }
        return errors.isEmpty() ? null : errors;
    }

    public Remark() {
    }

    public Remark(Long id, Long userId, Long orderId, Timestamp createAt, String content, String picture, Integer grade, String skuType, Long skuTypeId, Integer pageSize, Integer offset) {
        this.id = id;
        this.userId = userId;
        this.orderId = orderId;
        this.createAt = createAt;
        this.content = content;
        this.picture = picture;
        this.grade = grade;
        this.skuType = skuType;
        this.skuTypeId = skuTypeId;
        this.pageSize = pageSize;
        this.offset = offset;
    }


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
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

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "Remark{" +
                "id=" + id +
                ", userId=" + userId +
                ", orderId=" + orderId +
                ", createAt=" + createAt +
                ", content='" + content + '\'' +
                ", picture='" + picture + '\'' +
                ", grade=" + grade +
                ", skuType='" + skuType + '\'' +
                ", skuTypeId=" + skuTypeId +
                ", pageSize=" + pageSize +
                ", offset=" + offset +
                '}';
    }
}
