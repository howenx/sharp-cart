package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * 用户配送地址
 * Created by howen on 15/11/19.
 */
public class Address implements Serializable {

    private Long addId;//用户地址主键
    private String tel;//电话
    private String name;//姓名
    private String deliveryCity;//配送城市
    private String deliveryDetail;//配送详细地址

    @JsonIgnore
    private Long userId;//用户ID
    @JsonIgnore
    private String userToken;//客户端返回的token
    @JsonIgnore
    private Boolean orDefault;//是否默认收获地址

    public Address() {
    }

    public Address(Long addId, Long userId, String userToken, Boolean orDefault, String tel, String name, String deliveryCity, String deliveryDetail) {
        this.addId = addId;
        this.userId = userId;
        this.userToken = userToken;
        this.orDefault = orDefault;
        this.tel = tel;
        this.name = name;
        this.deliveryCity = deliveryCity;
        this.deliveryDetail = deliveryDetail;
    }

    public Long getAddId() {
        return addId;
    }

    public void setAddId(Long addId) {
        this.addId = addId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public Boolean getOrDefault() {
        return orDefault;
    }

    public void setOrDefault(Boolean orDefault) {
        this.orDefault = orDefault;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public void setDeliveryCity(String deliveryCity) {
        this.deliveryCity = deliveryCity;
    }

    public String getDeliveryDetail() {
        return deliveryDetail;
    }

    public void setDeliveryDetail(String deliveryDetail) {
        this.deliveryDetail = deliveryDetail;
    }

    @Override
    public String toString() {
        return "Address{" +
                "addId=" + addId +
                ", userId=" + userId +
                ", userToken='" + userToken + '\'' +
                ", orDefault=" + orDefault +
                ", tel='" + tel + '\'' +
                ", name='" + name + '\'' +
                ", deliveryCity='" + deliveryCity + '\'' +
                ", deliveryDetail='" + deliveryDetail + '\'' +
                '}';
    }
}
