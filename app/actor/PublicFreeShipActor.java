package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import com.google.common.base.Throwables;
import domain.CouponVo;
import domain.SettleFeeVo;
import domain.SettleVo;
import play.Logger;
import service.CartService;
import util.GenCouponCode;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * 发放免邮的优惠券
 * Created by howen on 15/12/18.
 */
public class PublicFreeShipActor extends AbstractActor {

    @Inject
    public PublicFreeShipActor(CartService cartService) {

        receive(ReceiveBuilder.match(SettleVo.class, settleVo -> {

            List<SettleFeeVo> settleFeeVos = settleVo.getSingleCustoms();
            Long userId = settleVo.getUserId();
            Long orderId = settleVo.getOrderId();
            BigDecimal freeShipLimit =settleVo.getFreeShipLimit();

            settleFeeVos.forEach(m->{
                //免邮
                if (m.getFreeShip()) {
                    CouponVo couponVo = new CouponVo();
                    couponVo.setUserId(userId);
                    couponVo.setDenomination(m.getShipSingleCustomsFee());
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
                        Logger.error("PublicFreeShipActor error: "+ Throwables.getStackTraceAsString(e));
                        e.printStackTrace();
                    }
                }
            });
        }).matchAny(s -> Logger.error("PublicFreeShipActor received messages not matched: {}", s.toString())).build());
    }
}
