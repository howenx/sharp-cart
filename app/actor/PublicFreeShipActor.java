package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import domain.CouponVo;
import play.Logger;
import service.CartService;
import util.GenCouponCode;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 发放免邮的优惠券
 * Created by howen on 15/12/18.
 */
@SuppressWarnings("unchecked")
public class PublicFreeShipActor extends AbstractActor {

    @Inject
    public PublicFreeShipActor(CartService cartService) {

        receive(ReceiveBuilder.match(HashMap.class, maps -> {

            Map<String, Object> orderInfo = (Map<String, Object>) maps;
            Long userId = (Long) orderInfo.get("userId");
            Long orderId = (Long) orderInfo.get("orderId");
            BigDecimal freeShipLimit = (BigDecimal) orderInfo.get("freeShipLimit");
            List<Map<String, Object>> orderSplitList = (List<Map<String, Object>>) orderInfo.get("singleCustoms");

            orderSplitList.forEach(m->{
                //免邮
                if ((Boolean)m.get("freeShip")) {
                    CouponVo couponVo = new CouponVo();
                    couponVo.setUserId(userId);
                    couponVo.setDenomination((BigDecimal)m.get("shipFeeSingle"));
                    Calendar cal = Calendar.getInstance();
                    couponVo.setStartAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime()));
                    couponVo.setEndAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime()));
                    String coupId = GenCouponCode.GetCode(GenCouponCode.CouponClassCode.SHIP_FREE.getIndex(), 8);
                    couponVo.setCoupId(coupId);
                    couponVo.setCateId(((Integer) GenCouponCode.CouponClassCode.SHIP_FREE.getIndex()).longValue());
                    couponVo.setState("F");
                    couponVo.setCateNm(GenCouponCode.CouponClassCode.SHIP_FREE.getName());
                    couponVo.setLimitQuota(freeShipLimit);
                    couponVo.setOrderId(orderId);
                    try {
                        if (cartService.insertCoupon(couponVo)) Logger.debug("发放的免邮券ID: "+couponVo.getCoupId());
                    } catch (Exception e) {
                        Logger.error("PublicFreeShipActor error: "+e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }).matchAny(s -> Logger.error("PublicFreeShipActor received messages not matched: {}", s.toString())).build());
    }
}
