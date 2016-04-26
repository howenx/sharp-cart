package mapper;

import domain.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Theme mapper interface.
 * Created by howen on 15/10/26.
 */
public interface ShoppingCartMapper {

    List<Cart> getCartByID(Cart cart) throws Exception;

    Integer updateCart(Cart cart) throws Exception;

    Integer UpdateCartBy(Cart cart) throws Exception;

    Integer addCart(Cart cart) throws Exception;

    List<Order> getOrderBy(Order order) throws Exception;

    Integer updateOrder(Order order) throws Exception;

    List<Cart> getCartByUserSku(Cart cart) throws Exception;

    List<CouponVo> getUserCoupon(CouponVo c) throws Exception;

    int insertCoupon(CouponVo c) throws Exception;

    int updateCoupon(CouponVo c) throws Exception;

    List<CouponVo> getUserCouponAll(CouponVo c) throws Exception;

    int updateCouponInvalid(CouponVo c) throws Exception;

    int insertOrderSplit(OrderSplit orderSplit) throws Exception;

    int updateOrderSplit(OrderSplit orderSplit) throws Exception;

    List<OrderSplit> selectOrderSplit(OrderSplit orderSplit) throws Exception;

    int insertOrderLine(OrderLine orderLine) throws Exception;

    List<OrderLine> selectOrderLine(OrderLine orderLine) throws Exception;

    int insertOrderAddress(OrderAddress orderAddress) throws Exception;

    List<OrderAddress> selectOrderAddress(OrderAddress orderAddress) throws Exception;

    int insertOrder(Order order) throws Exception;

    int deleteCouponF(CouponVo vo) throws Exception;

    int insertRefund(Refund refund) throws Exception;

    List<Refund> selectRefund(Refund refund) throws Exception;

    int updateRefund(Refund refund) throws Exception;

    List<Order> getPinOrder(Order order) throws Exception;

    List<Order> getPinUserOrder(Order order) throws Exception;


    List<Order> getOrder(Order order) throws Exception;

    int insertCollect(Collect collect) throws Exception;

    List<Collect> selectCollect(Collect collect) throws Exception;

    int deleteCollect(Collect collect) throws Exception;

    List<Remark> selectRemark(Remark remark);

    List<Remark> selectRemarkPaging(Remark remark);

    int insertRemark(Remark remark);

    int updateRemark(Remark remark);
}
