package domain;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import util.Crypto;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 用于M端跳转收银台页面的接口
 * Created by howen on 16/3/24.
 */
public class RedirectCash implements Serializable{

    @Constraints.Required
    private Long orderId;
    @Constraints.Required
    private String token;
    @Constraints.Required
    private String securityCode; //md5(orderId+token+'HMM')

    public List<ValidationError> validate() {
        Map<String,String> map = new TreeMap<>();
        map.put("orderId",orderId.toString());
        map.put("token",token);

        List<ValidationError> errors = new ArrayList<>();
        try {
            if (!Crypto.getSignature(map,"HMM").equals(securityCode))
                errors.add(new ValidationError("code", "This code is wrong"));
        } catch (IOException e) {
            errors.add(new ValidationError("code", "This code is wrong"));
        }
        return errors.isEmpty() ? null : errors;
    }

    public RedirectCash() {
    }

    public RedirectCash(Long orderId, String token, String securityCode) {
        this.orderId = orderId;
        this.token = token;
        this.securityCode = securityCode;
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

    @Override
    public String toString() {
        return "RedirectCash{" +
                "orderId=" + orderId +
                ", token='" + token + '\'' +
                ", securityCode='" + securityCode + '\'' +
                '}';
    }
}
