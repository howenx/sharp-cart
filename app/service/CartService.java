package service;

import domain.*;

import java.util.List;

/**
 * Cart service
 * Created by howen on 15/11/22.
 */
public interface CartService {

    List<Cart> getCarts(Cart cart) throws Exception;

    Boolean updateCart(Cart cart) throws Exception;

    Boolean addCart(Cart cart) throws Exception;

    List<Order> getOrderBy(Order order) throws Exception;

    Boolean updateOrder(Order order) throws Exception;

    List<Cart> getCartByUserSku(Cart cart) throws Exception;

    List<CouponVo> getUserCoupon(CouponVo c) throws Exception;

    Boolean insertCoupon(CouponVo c) throws Exception;

    Boolean updateCoupon(CouponVo c) throws Exception;

    List<CouponVo> getUserCouponAll(CouponVo c) throws Exception;

    Boolean updateCouponInvalid(CouponVo c) throws Exception;

    Boolean insertOrderSplit(OrderSplit orderSplit) throws Exception;

    Boolean updateOrderSplit(OrderSplit orderSplit) throws Exception;

    List<OrderSplit> selectOrderSplit(OrderSplit orderSplit) throws Exception;

    Boolean insertOrderLine(OrderLine orderLine) throws Exception;

    List<OrderLine> selectOrderLine(OrderLine orderLine) throws Exception;

    Boolean insertOrderAddress(OrderAddress orderAddress) throws Exception;

    List<OrderAddress> selectOrderAddress(OrderAddress orderAddress) throws Exception;

    Boolean insertOrder(Order order) throws Exception;

    Boolean deleteCouponF(CouponVo vo) throws Exception;

    Boolean insertRefund(Refund refund) throws Exception;

    List<Refund> selectRefund(Refund refund) throws Exception;

    Boolean updateRefund(Refund refund) throws Exception;

    List<Order> getPinOrder(Order order) throws Exception;

    List<Order> getOrder(Order order) throws Exception;

    List<Order> getPinUserOrder(Order order) throws Exception;


    Boolean insertCollect(Collect collect) throws Exception;

    List<Collect> selectCollect(Collect collect) throws Exception;

    Boolean deleteCollect(Collect collect) throws Exception;

    Integer UpdateCartBy(Cart cart) throws Exception;

    /**
     * 查询评价
     *
     * @param remark remark
     * @return List<Remark>
     */
    List<Remark> selectRemark(Remark remark);

    /**
     * 分页查询评价
     *
     * @param remark remark
     * @return List<Remark>
     */
    List<Remark> selectRemarkPaging(Remark remark);

    /**
     * 插入评价信息
     *
     * @param remark remark
     * @return Boolean
     */
    Boolean insertRemark(Remark remark);

    /**
     * 更新评价信息
     *
     * @param remark remark
     * @return Boolean
     */
    Boolean updateRemark(Remark remark);
}
