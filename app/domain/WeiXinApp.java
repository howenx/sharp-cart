package domain;

import play.data.validation.Constraints;

import java.io.Serializable;

/**
 * 微信支付JSAPI
 * Created by sibyl.sun on 16/5/10.
 */
public class WeiXinApp implements Serializable {
//    @Constraints.Required
    private String prepayid;
    @Constraints.Required
    private String tradeType;
    @Constraints.Required
    private Long orderId;
  //  @Constraints.Required
    private String token;
    @Constraints.Required
    private String securityCode; //md5(orderId+token+'HMM')

    public String getPrepayid() {
        return prepayid;
    }
    public void setPrepayid(String prepayid) {
        this.prepayid = prepayid;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
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
