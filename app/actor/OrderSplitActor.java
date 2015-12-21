package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Scheduler;
import akka.japi.pf.ReceiveBuilder;
import domain.OrderSplit;
import play.Logger;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import service.CartService;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * 创建子订单Actor
 * Created by howen on 15/12/14.
 */
@SuppressWarnings("unchecked")
public class OrderSplitActor extends AbstractActor {

    @Inject
    public OrderSplitActor(CartService cartService,@Named("orderShipActor") ActorRef orderShipActor,@Named("orderDetailActor") ActorRef orderDetailActor,@Named("clearCartActor") ActorRef clearCartActor
    ,@Named("publicFreeShipActor") ActorRef  publicFreeShipActor,@Named("reduceInvActor") ActorRef reduceInvActor,@Named("cancelOrderActor") ActorRef cancelOrderActor
    ,@Named("publicCouponActor") ActorRef publicCouponActor) {

        receive(ReceiveBuilder.match(HashMap.class, maps -> {

            Map<String, Object> orderInfo = (Map<String, Object>) maps;
            Long orderId = (Long) orderInfo.get("orderId");
            List<Map<String, Object>> orderSplitList = (List<Map<String, Object>>) orderInfo.get("singleCustoms");
            try {
                orderSplitList = orderSplitList.stream().map(c -> {
                    OrderSplit orderSplit = new OrderSplit();
                    orderSplit.setCbeCode(c.get("invCustoms").toString());
                    orderSplit.setPostalFee((BigDecimal) c.get("postalFeeSingle"));
                    orderSplit.setShipFee((BigDecimal) c.get("shipFeeSingle"));
                    orderSplit.setOrderId(orderId);
                    orderSplit.setTotalAmount((Integer) c.get("totalAmount"));
                    orderSplit.setTotalFee((BigDecimal) c.get("totalFeeSingle"));
                    orderSplit.setTotalPayFee((BigDecimal) c.get("totalPayFeeSingle"));
                    try {
                        if (cartService.insertOrderSplit(orderSplit)) Logger.debug("子订单信息: " + orderSplit);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    c.put("splitId", orderSplit.getSplitId());
                    return c;
                }).collect(Collectors.toList());
                orderInfo.put("singleCustoms",orderSplitList);
                //调用订单地址信息创建Actor
                orderShipActor.tell(orderInfo,ActorRef.noSender());
                //调用订单详细信息创建Actor
                orderDetailActor.tell(orderInfo,ActorRef.noSender());
                //清空购物车
                if ((Integer)orderInfo.get("buyNow")==2) clearCartActor.tell(orderInfo,ActorRef.noSender());
                //发放免邮券
                publicFreeShipActor.tell(orderInfo,ActorRef.noSender());
                //更改优惠券
                publicCouponActor.tell(orderInfo,ActorRef.noSender());
                //减库存
                reduceInvActor.tell(orderInfo,ActorRef.noSender());
                //24小时内未结算恢复库存并自动取消订单
                context().system().scheduler().scheduleOnce(FiniteDuration.create(24,HOURS),cancelOrderActor,orderInfo.get("orderId"),context().dispatcher(), ActorRef.noSender());

            } catch (Exception e) {
                Logger.error("OrderSplitActor Error:" + e.getMessage());
                e.printStackTrace();
            }
        }).matchAny(s -> Logger.error("OrderSplitActor received messages not matched: {}", s.toString())).build());
    }

}
