package domain;

import java.io.Serializable;
import java.util.List;

/**
 * 用于提交购物车列表页面
 * Created by howen on 15/12/8.
 */
public class SettleDTO implements Serializable {

    private     String              invCustoms;//报关单位
    private     String              invArea;//保税仓名称
    private     List<CartDto>       cartDtos;
    private     Long                addressId;//用户收获地址ID

    public SettleDTO() {
    }

    public SettleDTO(String invCustoms, String invArea, List<CartDto> cartDtos, Long addressId) {
        this.invCustoms = invCustoms;
        this.invArea = invArea;
        this.cartDtos = cartDtos;
        this.addressId = addressId;
    }

    public String getInvCustoms() {
        return invCustoms;
    }

    public void setInvCustoms(String invCustoms) {
        this.invCustoms = invCustoms;
    }

    public String getInvArea() {
        return invArea;
    }

    public void setInvArea(String invArea) {
        this.invArea = invArea;
    }

    public List<CartDto> getCartDtos() {
        return cartDtos;
    }

    public void setCartDtos(List<CartDto> cartDtos) {
        this.cartDtos = cartDtos;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    @Override
    public String toString() {
        return "SettleDTO{" +
                "invCustoms='" + invCustoms + '\'' +
                ", invArea='" + invArea + '\'' +
                ", cartDtos=" + cartDtos +
                ", addressId=" + addressId +
                '}';
    }
}
