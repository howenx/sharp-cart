package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import domain.OrderSplit;
import domain.SettleFeeVo;
import domain.SettleVo;
import play.Logger;
import scala.concurrent.duration.FiniteDuration;
import service.CartService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.HOURS;

/**
 * 创建子订单Actor
 * Created by howen on 15/12/14.
 */
public class OrderSplitActor extends AbstractActor {

    @Inject
    public OrderSplitActor(CartService cartService, @Named("orderAddressActor") ActorRef orderAddressActor, @Named("orderLineActor") ActorRef orderLineActor, @Named("clearCartActor") ActorRef clearCartActor
            , @Named("publicFreeShipActor") ActorRef publicFreeShipActor, @Named("reduceInvActor") ActorRef reduceInvActor, @Named("cancelOrderActor") ActorRef cancelOrderActor, @Named("schedulerCancelOrderActor") ActorRef schedulerCancelOrderActor
            , @Named("publicCouponActor") ActorRef publicCouponActor) {

        receive(ReceiveBuilder.match(SettleVo.class, settleVo -> {

            List<SettleFeeVo> settleFeeVos = settleVo.getSingleCustoms();
            try {
                settleFeeVos = settleFeeVos.stream().map(c -> {
                    OrderSplit orderSplit = new OrderSplit();
                    orderSplit.setCbeCode(c.getInvCustoms());
                    orderSplit.setCbeArea(c.getInvArea());
                    orderSplit.setPostalFee(c.getPortalSingleCustomsFee());
                    orderSplit.setShipFee(c.getShipSingleCustomsFee());
                    orderSplit.setOrderId(settleVo.getOrderId());
                    orderSplit.setTotalAmount(c.getSingleCustomsSumAmount());
                    orderSplit.setTotalFee(c.getSingleCustomsSumFee());
                    orderSplit.setTotalPayFee(c.getSingleCustomsSumPayFee());
                    try {
                        if (cartService.insertOrderSplit(orderSplit)) Logger.debug("子订单ID: " + orderSplit.getSplitId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    c.setSplitId(orderSplit.getSplitId());
                    return c;
                }).collect(Collectors.toList());

                settleVo.setSingleCustoms(settleFeeVos);

                //调用订单地址信息创建Actor
                orderAddressActor.tell(settleVo, ActorRef.noSender());
                //调用订单详细信息创建Actor
                orderLineActor.tell(settleVo, ActorRef.noSender());
                //清空购物车
                if (settleVo.getBuyNow() == 2) clearCartActor.tell(settleVo, ActorRef.noSender());
                //发放免邮券
                publicFreeShipActor.tell(settleVo, ActorRef.noSender());
                //更改优惠券
                publicCouponActor.tell(settleVo, ActorRef.noSender());
                //减库存
                reduceInvActor.tell(settleVo, ActorRef.noSender());
                //24小时内未结算恢复库存并自动取消订单


                context().system().scheduler().scheduleOnce(FiniteDuration.create(24, HOURS), cancelOrderActor, settleVo.getOrderId(), context().dispatcher(), ActorRef.noSender());

            } catch (Exception e) {
                Logger.error("OrderSplitActor Error:" + e.getMessage());
                e.printStackTrace();
            }
        }).matchAny(s -> Logger.error("OrderSplitActor received messages not matched: {}", s.toString())).build());
    }

}
