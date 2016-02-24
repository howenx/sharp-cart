package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import domain.CouponVo;
import domain.Order;
import domain.OrderLine;
import domain.Sku;
import play.Logger;
import play.libs.Json;
import service.CartService;
import service.SkuService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * 用于定时取消订单的Actor
 * Created by howen on 15/12/24.
 */
public class SchedulerCancelOrderActor extends AbstractActor {

    @Inject
    public SchedulerCancelOrderActor(CartService cartService, SkuService skuService) {
        receive(ReceiveBuilder.match(Long.class, orderId -> {
            Order order = new Order();
            order.setOrderId(orderId);
            List<Order> orders =cartService.getOrder(order);
            Logger.error("撒发生的发生地方--->\n"+orders.toString());
        }).matchAny(s -> {
            Logger.error("CancelOrderActor received messages not matched: {}", s.toString());
            unhandled(s);
        }).build());
    }
}
