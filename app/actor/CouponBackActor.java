package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import com.google.common.base.Throwables;
import domain.CouponVo;
import domain.Order;
import domain.SettleVo;
import play.Logger;
import service.CartService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * 用于恢复优惠券的Actor
 * Created by howen on 15/12/24.
 */
public class CouponBackActor extends AbstractActor {

    @Inject
    public CouponBackActor(CartService cartService) {
        receive(ReceiveBuilder.match(SettleVo.class, settleVo -> {
            Long userId = settleVo.getUserId();
            Long orderId = settleVo.getOrderId();
            Order order = new Order();
            order.setOrderId(orderId);
            List<Order> orders =cartService.getOrder(order);
            if (orders!=null && orders.size()>0){
                order = orders.get(0);
                if (order.getOrderStatus().equals("C")){
                    CouponVo couponVo = new CouponVo();
                    couponVo.setUserId(userId);
                    couponVo.setOrderId(orderId);

                    Optional<List<CouponVo>> couponVos = Optional.ofNullable(cartService.getUserCoupon(couponVo));
                    if (couponVos.isPresent()) {
                        if (couponVos.get().size() > 0) {
                            couponVos.get().forEach(couponVo1 -> {
                                couponVo1.setState("N");
                                try {
                                    cartService.updateCoupon(couponVo1);
                                } catch (Exception e) {
                                    Logger.error("CouponBackActor恢复用户优惠券失败:" + Throwables.getStackTraceAsString(e));
                                }
                            });
                        }
                    }
                }
            }

        }).matchAny(s -> {
            Logger.error("CouponBackActor received messages not matched: {}", s.toString());
            unhandled(s);
        }).build());
    }
}
