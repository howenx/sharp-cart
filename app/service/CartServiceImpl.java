package service;

import com.fasterxml.jackson.databind.JsonNode;
import domain.Cart;
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
    public List<Cart> getCarts(Long userId) throws Exception{
        Cart cart = new Cart();
        cart.setUserId(userId);
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
}
