package mapper;

import domain.*;

import java.util.List;

/**
 * Theme mapper interface.
 * Created by howen on 15/10/26.
 */
public interface ShoppingCartMapper {

    List<Cart> getCartByID(Cart cart) throws Exception;

    Integer updateCart(Cart cart) throws Exception;

    Integer addCart(Cart cart) throws Exception;

    List<Order> getOrderBy(Order order) throws Exception;

    Integer updateOrder(Order order) throws Exception;

    List<Cart> getCartByUserSku (Cart cart) throws Exception;

    List<CouponVo> getUserCoupon(CouponVo c) throws Exception;

    int insertCoupon(CouponVo c) throws Exception;

    int updateCoupon(CouponVo c) throws Exception;

    List<CouponVo> getUserCouponAll(CouponVo c) throws Exception;

    int updateCouponInvalid(CouponVo c) throws Exception;

}
