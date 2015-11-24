package service;

import com.fasterxml.jackson.databind.JsonNode;
import domain.Cart;
import domain.Sku;

import java.util.List;

/**
 * Cart service
 * Created by howen on 15/11/22.
 */
public interface CartService {

    List<Cart> getCarts(Long userId) throws Exception;

    Integer updateCart(Cart cart) throws Exception;

    Integer addCart(Cart cart) throws Exception;
}
