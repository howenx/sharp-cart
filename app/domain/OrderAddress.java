package domain;

import java.io.Serializable;

/**
 * 订单地址信息
 * Created by howen on 15/12/16.
 */
public class OrderAddress implements Serializable {

    private Long shipId;
    private Long orderId;
    private String deliveryName;
    private String deliveryTel;
    private String deliveryCity;
    private String deliveryAddress;
    private String deliveryCardNum;

    public OrderAddress(Long shipId, Long orderId, String deliveryName, String deliveryTel, String deliveryCity, String deliveryAddress, String deliveryCardNum) {
        this.shipId = shipId;
        this.orderId = orderId;
        this.deliveryName = deliveryName;
        this.deliveryTel = deliveryTel;
        this.deliveryCity = deliveryCity;
        this.deliveryAddress = deliveryAddress;
        this.deliveryCardNum = deliveryCardNum;
    }

    public OrderAddress() {
    }

    public Long getShipId() {
        return shipId;
    }

    public void setShipId(Long shipId) {
        this.shipId = shipId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getDeliveryName() {
        return deliveryName;
    }

    public void setDeliveryName(String deliveryName) {
        this.deliveryName = deliveryName;
    }

    public String getDeliveryTel() {
        return deliveryTel;
    }

    public void setDeliveryTel(String deliveryTel) {
        this.deliveryTel = deliveryTel;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public void setDeliveryCity(String deliveryCity) {
        this.deliveryCity = deliveryCity;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryCardNum() {
        return deliveryCardNum;
    }

    public void setDeliveryCardNum(String deliveryCardNum) {
        this.deliveryCardNum = deliveryCardNum;
    }

    @Override
    public String toString() {
        return "OrderAddressActor{" +
                "shipId=" + shipId +
                ", orderId=" + orderId +
                ", deliveryName='" + deliveryName + '\'' +
                ", deliveryTel='" + deliveryTel + '\'' +
                ", deliveryCity='" + deliveryCity + '\'' +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", deliveryCardNum='" + deliveryCardNum + '\'' +
                '}';
    }
}
