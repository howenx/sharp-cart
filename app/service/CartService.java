package service;

import com.fasterxml.jackson.databind.JsonNode;
import domain.Cart;
import domain.CouponVo;
import domain.Order;
import domain.Sku;

import java.util.List;

/**
 * Cart service
 * Created by howen on 15/11/22.
 */
public interface CartService {

    List<Cart> getCarts(Cart cart) throws Exception;

    Integer updateCart(Cart cart) throws Exception;

    Integer addCart(Cart cart) throws Exception;

    List<Order> getOrderBy(Order order) throws Exception;

    Boolean updateOrder(Order order) throws Exception;

    List<Cart> getCartByUserSku (Cart cart) throws Exception;

    List<CouponVo> getUserCoupon(CouponVo c) throws Exception;

    Boolean insertCoupon(CouponVo c) throws Exception;

    Boolean updateCoupon(CouponVo c) throws Exception;

    List<CouponVo> getUserCouponAll(CouponVo c) throws Exception;

    Boolean updateCouponInvalid(CouponVo c) throws Exception;
}
