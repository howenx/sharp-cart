package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import domain.Address;
import domain.OrderAddress;
import play.Logger;
import service.CartService;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单地址信息
 * Created by howen on 15/12/18.
 */
@SuppressWarnings("unchecked")
public class OrderAddressActor extends AbstractActor {
    @Inject
    public OrderAddressActor(CartService cartService) {

        receive(ReceiveBuilder.match(HashMap.class, maps -> {

            Map<String, Object> orderInfo = (Map<String, Object>) maps;
            Address address = (Address) orderInfo.get("address");
            Long orderId = (Long) orderInfo.get("orderId");
            try {
                OrderAddress orderAddress = new OrderAddress();
                orderAddress.setOrderId(orderId);
                orderAddress.setDeliveryName(address.getName());
                orderAddress.setDeliveryTel(address.getTel());
                orderAddress.setDeliveryCity(address.getDeliveryCity());
                orderAddress.setDeliveryCardNum(address.getIdCardNum());
                orderAddress.setDeliveryAddress(address.getDeliveryDetail());
                if (cartService.insertOrderAddress(orderAddress)) Logger.debug("orderAddress:"+orderAddress.toString());
            } catch (Exception e) {
                Logger.error("OrderAddressActor Error:" + e.getMessage());
                e.printStackTrace();
            }
        }).matchAny(s -> Logger.error("OrderAddressActor received messages not matched: {}", s.toString())).build());
    }
}
