package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import domain.Address;
import domain.Cart;
import domain.CartDto;
import domain.OrderAddress;
import play.Logger;
import scala.concurrent.duration.FiniteDuration;
import service.CartService;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
                        try {
                            if (cartService.updateCart(cart)) Logger.debug("ClearCartActor 清空购物车"+cart);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.error("ClearCartActor 更新购物车"+e.getMessage());
                            context().system().scheduler().scheduleOnce(FiniteDuration.create(5, TimeUnit.MILLISECONDS),self(),orderInfo,context().dispatcher(), ActorRef.noSender());
                        }
                    });
                });
            }
        }).matchAny(s -> Logger.error("ClearCartActor received messages not matched: {}", s.toString())).build());
    }
}
