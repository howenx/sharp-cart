package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import domain.Order;
import domain.PinActivity;
import play.Logger;
import service.CartService;
import service.PromotionService;

import javax.inject.Inject;
import java.util.List;

/**
 * 拼购失败
 * Created by howen on 16/2/17.
 */
public class PinFailActor extends AbstractActor {

    @Inject
    public PinFailActor(PromotionService promotionService, CartService cartService) {

        receive(ReceiveBuilder.match(Long.class, activityId -> {

            PinActivity pinActivity = promotionService.selectPinActivityById(activityId);

            //如果加入人数小于要求成团的人数就拼购失败
            if (pinActivity.getJoinPersons()<pinActivity.getPersonNum()){
                pinActivity.setStatus("F");
            }
            promotionService.updatePinActivity(pinActivity);

            Order order = new Order();
            order.setPinActiveId(activityId);

            List<Order> orders = cartService.getPinOrder(order);

            orders.stream().forEach(o->{



            });

        }).matchAny(s -> Logger.error("PublicCouponActor received messages not matched: {}", s.toString())).build());
    }
}