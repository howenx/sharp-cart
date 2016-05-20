package domain;

import play.data.validation.Constraints;

import java.io.Serializable;

/**
 * 用于M端微信支付统一下单
 * Created by sibyl.sun on 16/5/9.
 */
public class RedirectWeiXin implements Serializable {
    @Constraints.Required
    private Long orderId;
   // @Constraints.Required
    private String token;
  //  @Constraints.Required
    private String securityCode; //md5(orderId+token+'HMM')
    @Constraints.Required
    private String tradeType;

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

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }
    @Override
    public String toString() {
        return "RedirectCash{" +
                "orderId=" + orderId +
                ", token='" + token + '\'' +
                ", securityCode='" + securityCode + '\'' +
                ", tradeType='" + tradeType + '\'' +
                '}';
    }
}
