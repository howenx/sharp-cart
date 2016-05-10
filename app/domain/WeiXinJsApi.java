package domain;

import play.data.validation.Constraints;

import java.io.Serializable;

/**
 * 微信支付JSAPI
 * Created by sibyl.sun on 16/5/10.
 */
public class WeiXinJsApi implements Serializable {
    @Constraints.Required
    private String appId;
    @Constraints.Required
    private String timeStamp ;
    @Constraints.Required
    private String nonceStr ;
    @Constraints.Required
    private String pg;
    @Constraints.Required
    private String signType ;
    @Constraints.Required
    private String paySign ;
    @Constraints.Required
    private Long orderId;
    @Constraints.Required
    private String token;
    @Constraints.Required
    private String securityCode; //md5(orderId+token+'HMM')

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getPaySign() {
        return paySign;
    }

    public void setPaySign(String paySign) {
        this.paySign = paySign;
    }

    public String getPg() {
        return pg;
    }

    public void setPg(String pg) {
        this.pg = pg;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }
}
