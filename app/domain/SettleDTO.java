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
    private     String              invAreaNm;//库存区域名称
    private     List<CartDto>       cartDtos;

    public SettleDTO() {
    }

    public SettleDTO(String invCustoms, String invArea, String invAreaNm, List<CartDto> cartDtos) {
        this.invCustoms = invCustoms;
        this.invArea = invArea;
        this.invAreaNm = invAreaNm;
        this.cartDtos = cartDtos;
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

    public String getInvAreaNm() {
        return invAreaNm;
    }

    public void setInvAreaNm(String invAreaNm) {
        this.invAreaNm = invAreaNm;
    }

    public List<CartDto> getCartDtos() {
        return cartDtos;
    }

    public void setCartDtos(List<CartDto> cartDtos) {
        this.cartDtos = cartDtos;
    }

    @Override
    public String toString() {
        return "SettleDTO{" +
                "invCustoms='" + invCustoms + '\'' +
                ", invArea='" + invArea + '\'' +
                ", invAreaNm='" + invAreaNm + '\'' +
                ", cartDtos=" + cartDtos +
                '}';
    }
}
