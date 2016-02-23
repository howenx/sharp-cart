package domain;

import java.io.Serializable;
import java.util.List;

/**
 * 购物车商品列表
 * Created by howen on 16/2/22.
 */
public class CartItemDTO implements Serializable {

    private String invCustoms;
    private String invArea;
    private String invAreaNm;
    private List<CartListDto> carts;
    private String postalStandard;
    private String postalLimit;
    private String freeShip;


    public CartItemDTO() {
    }

    public CartItemDTO(String invCustoms, String invArea, String invAreaNm, List<CartListDto> carts, String postalStandard, String postalLimit, String freeShip) {
        this.invCustoms = invCustoms;
        this.invArea = invArea;
        this.invAreaNm = invAreaNm;
        this.carts = carts;
        this.postalStandard = postalStandard;
        this.postalLimit = postalLimit;
        this.freeShip = freeShip;
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

    public List<CartListDto> getCarts() {
        return carts;
    }

    public void setCarts(List<CartListDto> carts) {
        this.carts = carts;
    }

    public String getPostalStandard() {
        return postalStandard;
    }

    public void setPostalStandard(String postalStandard) {
        this.postalStandard = postalStandard;
    }

    public String getPostalLimit() {
        return postalLimit;
    }

    public void setPostalLimit(String postalLimit) {
        this.postalLimit = postalLimit;
    }

    public String getFreeShip() {
        return freeShip;
    }

    public void setFreeShip(String freeShip) {
        this.freeShip = freeShip;
    }

    @Override
    public String toString() {
        return "CartItemDTO{" +
                "invCustoms='" + invCustoms + '\'' +
                ", invArea='" + invArea + '\'' +
                ", invAreaNm='" + invAreaNm + '\'' +
                ", carts=" + carts +
                ", postalStandard='" + postalStandard + '\'' +
                ", postalLimit='" + postalLimit + '\'' +
                ", freeShip='" + freeShip + '\'' +
                '}';
    }
}
