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
//            Order order = new Order();
//            order.setOrderId(orderId);
//
//            Optional<List<Order>> listOptional = Optional.ofNullable(cartService.getOrderBy(order));
//
//            OrderLine orderLine = new OrderLine();
//            orderLine.setOrderId(orderId);
//            //取出所有订单明细
//            Optional<List<OrderLine>> orderLineList = Optional.ofNullable(cartService.selectOrderLine(orderLine));
//
//            if (listOptional.isPresent() && listOptional.get().size() > 0) {
//                order = listOptional.get().get(0);
//                if (order.getOrderStatus().equals("I")) {
//                    //恢复库存
//                    if (orderLineList.isPresent()) {
//                        orderLineList.get().forEach(ordL -> {
//                            Sku sku = new Sku();
//                            sku.setId(ordL.getSkuId());
//                            try {
//                                sku = skuService.getInv(sku);
//                            } catch (Exception e) {
//                                Logger.error("CancelOrderActor Sku Select Error:" + e.getMessage());
//                                e.printStackTrace();
//                            }
//                            switch (sku.getState()) {
//                                case "Y":
//                                    sku.setRestAmount(sku.getRestAmount() + ordL.getAmount());
//                                    sku.setSoldAmount(sku.getSoldAmount() - ordL.getAmount());
//                                    break;
//                                case "K":
//                                    sku.setRestAmount(sku.getRestAmount() + ordL.getAmount());
//                                    sku.setSoldAmount(sku.getSoldAmount() - ordL.getAmount());
//                                    sku.setState("Y");
//                                    break;
//                                default:
//                                    sku.setRestAmount(sku.getRestAmount() - ordL.getAmount());
//                                    sku.setSoldAmount(sku.getSoldAmount() + ordL.getAmount());
//                                    break;
//                            }
//                            try {
//                                if (skuService.updateInv(sku))
//                                    Logger.debug("CancelOrderActor 恢复库存: " + Json.toJson(sku));
//                            } catch (Exception e) {
//                                Logger.error("CancelOrderActor Error:" + e.getMessage());
//                                e.printStackTrace();
//                            }
//                        });
//                        //更新订单状态为取消状态
//                        order.setOrderStatus("C");
//                        try {
//                            if (cartService.updateOrder(order))
//                                Logger.debug("CancelOrderActor 更新订单状态: " + Json.toJson(order));
//                        } catch (Exception e) {
//                            Logger.error("CancelOrderActor 更新订单状态 Error:" + e.getMessage());
//                            e.printStackTrace();
//                        }
//
//                        //删除免邮券
//                        CouponVo couponVo = new CouponVo();
//                        couponVo.setOrderId(orderId);
//                        couponVo.setState("F");
//                        Optional<List<CouponVo>> couponVoList = Optional.ofNullable(cartService.getUserCoupon(couponVo));
//                        if (couponVoList.isPresent() && couponVoList.get().size() > 0) {
//                            couponVo = couponVoList.get().get(0);
//                            try {
//                                if (cartService.deleteCouponF(couponVo))
//                                    Logger.debug("CancelOrderActor 删除免邮券: " + Json.toJson(order));
//                            } catch (Exception e) {
//                                Logger.error("CancelOrderActor 删除免邮券 Error:" + e.getMessage());
//                                e.printStackTrace();
//                            }
//
//                        }
//                        //更新优惠券
//                        CouponVo couponVoU = new CouponVo();
//                        couponVoU.setOrderId(orderId);
//                        couponVoU.setState("Y");
//                        Optional<List<CouponVo>> couponVoListU = Optional.ofNullable(cartService.getUserCoupon(couponVo));
//                        if (couponVoListU.isPresent()) {
//                            couponVoListU.get().forEach(couponVo1 -> {
//                                couponVo1.setState("N");
//                                couponVo1.setOrderId(((Integer) 0).longValue());
//                                try {
//                                    if (cartService.updateCoupon(couponVo1))
//                                        Logger.debug("CancelOrderActor 更新优惠券" + Json.toJson(couponVo1));
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            });
//                        }
//                    }
//                }
//            }
            Logger.error("撒发生的发生地方");
        }).matchAny(s -> {
            Logger.error("CancelOrderActor received messages not matched: {}", s.toString());
            unhandled(s);
        }).build());
    }
}
