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
    private String  idCardNum;//身份证号码

    @JsonIgnore
    private Long userId;//用户ID
    @JsonIgnore
    private String userToken;//客户端返回的token
    @JsonIgnore
    private Boolean orDefault;//是否默认收获地址
    @JsonIgnore
    private String provinceCode;//省份代码

    public Address() {
    }

    public Address(Long addId, String tel, String name, String deliveryCity, String deliveryDetail, String idCardNum, Long userId, String userToken, Boolean orDefault, String provinceCode) {
        this.addId = addId;
        this.tel = tel;
        this.name = name;
        this.deliveryCity = deliveryCity;
        this.deliveryDetail = deliveryDetail;
        this.idCardNum = idCardNum;
        this.userId = userId;
        this.userToken = userToken;
        this.orDefault = orDefault;
        this.provinceCode = provinceCode;
    }

    public Long getAddId() {
        return addId;
    }

    public void setAddId(Long addId) {
        this.addId = addId;
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

    public String getIdCardNum() {
        return idCardNum;
    }

    public void setIdCardNum(String idCardNum) {
        this.idCardNum = idCardNum;
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

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    @Override
    public String toString() {
        return "Address{" +
                "addId=" + addId +
                ", tel='" + tel + '\'' +
                ", name='" + name + '\'' +
                ", deliveryCity='" + deliveryCity + '\'' +
                ", deliveryDetail='" + deliveryDetail + '\'' +
                ", idCardNum='" + idCardNum + '\'' +
                ", userId=" + userId +
                ", userToken='" + userToken + '\'' +
                ", orDefault=" + orDefault +
                ", provinceCode='" + provinceCode + '\'' +
                '}';
    }
}
