package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import domain.Carriage;
import play.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 计算邮费Actor
 * Created by howen on 15/12/14.
 */
public class CalculateShipFeeActor extends AbstractActor {

    public CalculateShipFeeActor() {
        receive(ReceiveBuilder.match(ArrayList.class, maps -> {
            //                    OrderSplit orderSplit = new OrderSplit();
//                    orderSplit.setCbeCode(c.get("invCustoms").toString());
//                    orderSplit.setPostalFee(((BigDecimal)c.get("postalFeeSingle")).setScale(2,BigDecimal.ROUND_HALF_UP));
//                    orderSplit.setShipFee(((BigDecimal)c.get("shipFeeSingle")).setScale(2,BigDecimal.ROUND_HALF_UP));
//                    orderSplit.setOrderId(order.getOrderId());
//                    orderSplit.setTotalAmount((Integer)c.get("totalAmount"));
//                    orderSplit.setTotalFee(((BigDecimal)c.get("totalFeeSingle")).setScale(2,BigDecimal.ROUND_HALF_UP));
//                    orderSplit.setTotalFee(((BigDecimal)c.get("totalPayFeeSingle")).setScale(2,BigDecimal.ROUND_HALF_UP));
//                    try {
//                        cartService.insertOrderSplit(orderSplit);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    c.put("splitId",orderSplit.getSplitId());
                    Logger.error("Received String message: "+maps.toString());
                }).matchAny(s->Logger.info("Received String message: {}", s.toString())).build());
    }

}
