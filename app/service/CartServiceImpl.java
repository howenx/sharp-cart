package service;

import domain.*;
import mapper.ShoppingCartMapper;

import javax.inject.Inject;
import java.util.List;

/**
 * 购物车service实现
 * Created by howen on 15/11/22.
 */
public class CartServiceImpl implements CartService {


    @Inject
    private ShoppingCartMapper shoppingCartMapper;

    @Override
    public List<Cart> getCarts(Cart cart) throws Exception {

        return shoppingCartMapper.getCartByID(cart);
    }

    @Override
    public Boolean updateCart(Cart cart) throws Exception {
        return shoppingCartMapper.updateCart(cart) >= 0;
    }

    @Override
    public Boolean addCart(Cart cart) throws Exception {
        return shoppingCartMapper.addCart(cart) >= 0;
    }

    @Override
    public List<Order> getOrderBy(Order order) throws Exception {
        return shoppingCartMapper.getOrderBy(order);
    }

    @Override
    public Boolean updateOrder(Order order) throws Exception {

        return shoppingCartMapper.updateOrder(order) >= 0;
    }

    @Override
    public List<Cart> getCartByUserSku(Cart cart) throws Exception {
        return shoppingCartMapper.getCartByUserSku(cart);
    }

    @Override
    public List<CouponVo> getUserCoupon(CouponVo c) throws Exception {
        return shoppingCartMapper.getUserCoupon(c);
    }

    @Override
    public Boolean insertCoupon(CouponVo c) throws Exception {
        return shoppingCartMapper.insertCoupon(c) >= 0;
    }

    @Override
    public Boolean updateCoupon(CouponVo c) throws Exception {
        return shoppingCartMapper.updateCoupon(c) >= 0;
    }

    @Override
    public List<CouponVo> getUserCouponAll(CouponVo c) throws Exception {
        return shoppingCartMapper.getUserCouponAll(c);
    }

    @Override
    public Boolean updateCouponInvalid(CouponVo c) throws Exception {
        return shoppingCartMapper.updateCouponInvalid(c) >= 0;
    }

    @Override
    public Boolean insertOrderSplit(OrderSplit orderSplit) throws Exception {
        return shoppingCartMapper.insertOrderSplit(orderSplit) >= 0;
    }

    @Override
    public Boolean updateOrderSplit(OrderSplit orderSplit) throws Exception {
        return shoppingCartMapper.updateOrderSplit(orderSplit) >= 0;
    }

    @Override
    public List<OrderSplit> selectOrderSplit(OrderSplit orderSplit) throws Exception {
        return shoppingCartMapper.selectOrderSplit(orderSplit);
    }

    @Override
    public Boolean insertOrderLine(OrderLine orderLine) throws Exception {
        return shoppingCartMapper.insertOrderLine(orderLine) >= 0;
    }

    @Override
    public List<OrderLine> selectOrderLine(OrderLine orderLine) throws Exception {
        return shoppingCartMapper.selectOrderLine(orderLine);
    }

    @Override
    public Boolean insertOrderAddress(OrderAddress orderAddress) throws Exception {
        return shoppingCartMapper.insertOrderAddress(orderAddress) >= 0;
    }

    @Override
    public List<OrderAddress> selectOrderAddress(OrderAddress orderAddress) throws Exception {
        return shoppingCartMapper.selectOrderAddress(orderAddress);
    }

    @Override
    public Boolean insertOrder(Order order) throws Exception {
        return shoppingCartMapper.insertOrder(order) >= 0;
    }

    @Override
    public Boolean deleteCouponF(CouponVo vo) throws Exception {
        return shoppingCartMapper.deleteCouponF(vo) >= 0;
    }

    @Override
    public Boolean insertRefund(Refund refund) throws Exception {
        return shoppingCartMapper.insertRefund(refund) >= 0;
    }

    @Override
    public List<Refund> selectRefund(Refund refund) throws Exception {
        return shoppingCartMapper.selectRefund(refund);
    }

    @Override
    public Boolean updateRefund(Refund refund) throws Exception {
        return shoppingCartMapper.updateRefund(refund) >= 0;
    }

    @Override
    public List<Order> getPinOrder(Order order) throws Exception {
        return shoppingCartMapper.getPinOrder(order);
    }

    @Override
    public List<Order> getOrder(Order order) throws Exception {
        return shoppingCartMapper.getOrder(order);
    }

    @Override
    public List<Order> getPinUserOrder(Order order) throws Exception {
        return shoppingCartMapper.getPinUserOrder(order);
    }

    public Boolean insertCollect(Collect collect) throws Exception {
        return shoppingCartMapper.insertCollect(collect) >= 0;
    }

    @Override
    public List<Collect> selectCollect(Collect collect) throws Exception {
        return shoppingCartMapper.selectCollect(collect);
    }

    @Override
    public Boolean deleteCollect(Collect collect) throws Exception {
        return shoppingCartMapper.deleteCollect(collect) >= 0;
    }

    @Override
    public Integer UpdateCartBy(Cart cart) throws Exception {
        return shoppingCartMapper.UpdateCartBy(cart);
    }

    @Override
    public List<Remark> selectRemark(Remark remark) {
        return shoppingCartMapper.selectRemark(remark);
    }

    @Override
    public List<Remark> selectRemarkPaging(Remark remark) {
        return shoppingCartMapper.selectRemarkPaging(remark);
    }

    @Override
    public Boolean insertRemark(Remark remark) {
        return shoppingCartMapper.insertRemark(remark) >= 0;
    }

    @Override
    public Boolean updateRemark(Remark remark) {
        return shoppingCartMapper.updateRemark(remark) >= 0;
    }
}
