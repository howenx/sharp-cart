package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import com.google.common.base.Throwables;
import domain.Cart;
import domain.CartDto;
import domain.SettleFeeVo;
import domain.SettleVo;
import play.Logger;
import scala.concurrent.duration.FiniteDuration;
import service.CartService;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 清空购物车
 * Created by howen on 15/12/18.
 */
public class ClearCartActor extends AbstractActor {
    @Inject
    public ClearCartActor(CartService cartService) {

        receive(ReceiveBuilder.match(SettleVo.class, settleVo -> {

            Long orderId = settleVo.getOrderId();
            List<SettleFeeVo> settleFeeVos = settleVo.getSingleCustoms();

            Integer buyNow = settleVo.getBuyNow();
            //如果是购物车页结算,就去清空购物车
            if (buyNow==2){
                settleFeeVos.forEach(m -> {
                    List<CartDto> cartDtos = m.getCartDtos();
                    cartDtos.forEach(cartDto -> {
                        Cart cart = new Cart();
                        cart.setCartId(cartDto.getCartId());
                        cart.setStatus("O");
                        cart.setOrderId(orderId);
                        try {
                            if (cartService.updateCart(cart)) Logger.debug("清空购物车ID: "+cart.getCartId());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.error("ClearCartActor 更新购物车异常" + Throwables.getStackTraceAsString(e));

                            context().system().scheduler().scheduleOnce(FiniteDuration.create(5, TimeUnit.MILLISECONDS),self(),settleVo,context().dispatcher(), ActorRef.noSender());
                        }
                    });
                });
            }
        }).matchAny(s -> Logger.error("ClearCartActor received messages not matched: {}", s.toString())).build());
    }
}
