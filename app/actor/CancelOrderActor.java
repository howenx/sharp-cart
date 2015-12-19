package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import domain.CouponVo;
import domain.Order;
import domain.OrderLine;
import domain.Sku;
import play.Logger;
import service.CartService;
import service.SkuService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * 取消订单
 * Created by howen on 15/12/19.
 */
public class CancelOrderActor extends AbstractActor {

    @Inject
    public CancelOrderActor(CartService cartService, SkuService skuService) {
        receive(ReceiveBuilder.match(Long.class, orderId -> {
            Order order = new Order();
            order.setOrderId(orderId);

            Optional<List<Order>> listOptional = Optional.ofNullable(cartService.getOrderBy(order));

            OrderLine orderLine = new OrderLine();
            orderLine.setOrderId(orderId);
            //取出所有订单明细
            Optional<List<OrderLine>> orderLineList=Optional.ofNullable(cartService.selectOrderLine(orderLine));

            if (listOptional.isPresent() && listOptional.get().size() > 0){
                order = listOptional.get().get(0);
                if (order.getOrderStatus().equals("I")){

                    //恢复库存
                    if (orderLineList.isPresent()){
                        orderLineList.get().forEach(ordL->{
                            Sku sku = new Sku();
                            sku.setId(ordL.getSkuId());
                            try {
                                sku=skuService.getInv(sku);
                            } catch (Exception e) {
                                Logger.error("CancelOrderActor Sku Select Error:" + e.getMessage());
                                e.printStackTrace();
                            }
                            switch (sku.getState()) {
                                case "Y":
                                    sku.setRestAmount(sku.getRestAmount() + ordL.getAmount());
                                    sku.setSoldAmount(sku.getSoldAmount() - ordL.getAmount());
                                    break;
                                case "K":
                                    sku.setRestAmount(sku.getRestAmount() + ordL.getAmount());
                                    sku.setSoldAmount(sku.getSoldAmount() - ordL.getAmount());
                                    sku.setState("Y");
                                    break;
                                default:
                                    sku.setRestAmount(sku.getRestAmount() - ordL.getAmount());
                                    sku.setSoldAmount(sku.getSoldAmount() + ordL.getAmount());
                                    break;
                            }
                            try {
                                if(skuService.updateInv(sku)) Logger.debug("CancelOrderActor 恢复库存: "+sku);
                            } catch (Exception e) {
                                Logger.error("CancelOrderActor Error:" + e.getMessage());
                                e.printStackTrace();
                            }
                        });
                        //更新订单状态为取消状态
                        order.setOrderStatus("C");
                        if (cartService.updateOrder(order)) Logger.debug("CancelOrderActor 更新订单状态: "+order);
                        //删除免邮券
                        CouponVo couponVo  =new CouponVo();
                        couponVo.setOrderId(orderId);
                        couponVo.setState("F");
                        Optional<List<CouponVo>> couponVoList = Optional.ofNullable(cartService.getUserCoupon(couponVo));
                        if (couponVoList.isPresent()){
                            couponVo = couponVoList.get().get(0);
                            cartService.deleteCouponF(couponVo);
                        }
                        //更新优惠券
                        CouponVo couponVoU  =new CouponVo();
                        couponVoU.setOrderId(orderId);
                        couponVoU.setState("Y");
                        Optional<List<CouponVo>> couponVoListU = Optional.ofNullable(cartService.getUserCoupon(couponVo));
                        if (couponVoListU.isPresent()){
                            couponVoListU.get().forEach(couponVo1 -> {
                                couponVo1.setState("N");
                                couponVo1.setOrderId(((Integer)0).longValue());
                                try {
                                    if (cartService.updateCoupon(couponVo1)) Logger.debug("CancelOrderActor 更新优惠券"+couponVo1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                }
            }
        }).matchAny(s -> Logger.error("CancelOrderActor received messages not matched: {}", s.toString())).build());
    }
}