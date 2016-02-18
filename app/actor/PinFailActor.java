package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import domain.CouponVo;
import domain.SettleVo;
import play.Logger;
import service.CartService;

import javax.inject.Inject;

/**
 * 拼购失败
 * Created by howen on 16/2/17.
 */
public class PinFailActor extends AbstractActor {

    @Inject
    public PinFailActor(CartService cartService) {

        receive(ReceiveBuilder.match(SettleVo.class, settleVo -> {
            Long userId = settleVo.getUserId();
            Long orderId = settleVo.getOrderId();
            if (settleVo.getCouponId()!=null){
                CouponVo couponVo = new CouponVo();
                couponVo.setUserId(userId);
                couponVo.setCoupId(settleVo.getCouponId());
                couponVo.setState("Y");
                couponVo.setOrderId(orderId);
                if (cartService.updateCoupon(couponVo)) Logger.debug("PublicCouponActor 发放优惠券ID :"+couponVo.getCoupId());
            }
        }).matchAny(s -> Logger.error("PublicCouponActor received messages not matched: {}", s.toString())).build());
    }
}