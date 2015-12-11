package service;

import com.fasterxml.jackson.databind.JsonNode;
import domain.Cart;
import domain.CouponVo;
import domain.Order;
import domain.Sku;
import mapper.ShoppingCartMapper;
import mapper.SkuMapper;
import play.Logger;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 购物车service实现
 * Created by howen on 15/11/22.
 */
public class CartServiceImpl  implements CartService{


    @Inject
    private ShoppingCartMapper shoppingCartMapper;

    @Override
    public List<Cart> getCarts(Cart cart) throws Exception{

        return shoppingCartMapper.getCartByID(cart);
    }

    @Override
    public Integer updateCart(Cart cart) throws Exception {
        return shoppingCartMapper.updateCart(cart);
    }

    @Override
    public Integer addCart(Cart cart) throws Exception {
        return shoppingCartMapper.addCart(cart);
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
        return shoppingCartMapper.insertCoupon(c)>=0;
    }

    @Override
    public Boolean updateCoupon(CouponVo c) throws Exception {
        return shoppingCartMapper.updateCoupon(c)>=0;
    }

    @Override
    public List<CouponVo> getUserCouponAll(CouponVo c) throws Exception {
        return shoppingCartMapper.getUserCouponAll(c);
    }

    @Override
    public Boolean updateCouponInvalid(CouponVo c) throws Exception {
        return shoppingCartMapper.updateCouponInvalid(c)>=0;
    }
}
