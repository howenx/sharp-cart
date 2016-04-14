package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import domain.CouponVo;
import domain.SettleVo;
import play.Logger;
import play.data.format.Formats;
import service.CartService;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 发放购物券
 * Created by howen on 15/12/19.
 */
public class PublicCouponActor extends AbstractActor {

    @Inject
    public PublicCouponActor(CartService cartService) {

        receive(ReceiveBuilder.match(SettleVo.class, settleVo -> {
            Long userId = settleVo.getUserId();
            Long orderId = settleVo.getOrderId();
            if (settleVo.getCouponId()!=null){
                CouponVo couponVo = new CouponVo();
                couponVo.setUserId(userId);
                couponVo.setCoupId(settleVo.getCouponId());
                couponVo.setState("Y");
                couponVo.setOrderId(orderId);
                LocalDateTime date = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
                String text = date.format(formatter);
                couponVo.setUseAt(text);
                if (cartService.updateCoupon(couponVo)) Logger.debug("PublicCouponActor 发放优惠券ID :"+couponVo.getCoupId());
            }
        }).matchAny(s -> Logger.error("PublicCouponActor received messages not matched: {}", s.toString())).build());
    }
}