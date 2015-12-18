package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import domain.Address;
import domain.Cart;
import domain.CartDto;
import domain.OrderAddress;
import play.Logger;
import service.CartService;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 清空购物车
 * Created by howen on 15/12/18.
 */
@SuppressWarnings("unchecked")
public class ClearCartActor extends AbstractActor {
    @Inject
    public ClearCartActor(CartService cartService) {

        receive(ReceiveBuilder.match(HashMap.class, maps -> {

            Map<String, Object> orderInfo = (Map<String, Object>) maps;
            Long orderId = (Long) orderInfo.get("orderId");
            List<Map<String, Object>> orderSplitList = (List<Map<String, Object>>) orderInfo.get("singleCustoms");

            Integer buyNow = (Integer)orderInfo.get("buyNow");
            //如果是购物车页结算,就去清空购物车
            if (buyNow==2){
                orderSplitList.forEach(m -> {
                    List<CartDto> cartDtos = (List<CartDto>) m.get("cartDtos");
                    cartDtos.forEach(cartDto -> {
                        Cart cart = new Cart();
                        cart.setCartId(cartDto.getCartId());
                        cart.setStatus("O");
                        cart.setOrderId(orderId);
                    });
                });
            }
        }).matchAny(s -> Logger.error("ClearCartActor received messages not matched: {}", s.toString())).build());
    }
}
