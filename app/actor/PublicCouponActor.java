package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import domain.CouponVo;
import play.Logger;
import service.CartService;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * 发放购物券
 * Created by howen on 15/12/19.
 */
@SuppressWarnings("unchecked")
public class PublicCouponActor extends AbstractActor {

    @Inject
    public PublicCouponActor(CartService cartService) {

        receive(ReceiveBuilder.match(HashMap.class, maps -> {
            Map<String, Object> orderInfo = (Map<String, Object>) maps;
            Long userId = (Long) orderInfo.get("userId");
            Long orderId = (Long) orderInfo.get("orderId");
            if (orderInfo.containsKey("couponId")){
                CouponVo couponVo = new CouponVo();
                couponVo.setUserId(userId);
                couponVo.setCoupId(orderInfo.get("couponId").toString());
                couponVo.setState("Y");
                couponVo.setOrderId(orderId);
                if (cartService.updateCoupon(couponVo)) Logger.debug("PublicCouponActor 发放优惠券 :"+couponVo);
            }
        }).matchAny(s -> Logger.error("PublicCouponActor received messages not matched: {}", s.toString())).build());
    }
}