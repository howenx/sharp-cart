package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import com.google.common.base.Throwables;
import domain.Address;
import domain.OrderAddress;
import domain.SettleVo;
import play.Logger;
import service.CartService;

import javax.inject.Inject;

/**
 * 订单地址信息
 * Created by howen on 15/12/18.
 */
public class OrderAddressActor extends AbstractActor {
    @Inject
    public OrderAddressActor(CartService cartService) {

        receive(ReceiveBuilder.match(SettleVo.class, settleVo -> {

            Address address = settleVo.getAddress();
            Long orderId = settleVo.getOrderId();
            try {
                OrderAddress orderAddress = new OrderAddress();
                orderAddress.setOrderId(orderId);
                orderAddress.setDeliveryName(address.getName());
                orderAddress.setDeliveryTel(address.getTel());
                orderAddress.setDeliveryCity(address.getDeliveryCity());
                orderAddress.setDeliveryCardNum(address.getIdCardNum());
                orderAddress.setDeliveryAddress(address.getDeliveryDetail());
                if (cartService.insertOrderAddress(orderAddress)) Logger.debug("订单地址信息ID: "+orderAddress.getShipId());
            } catch (Exception e) {
                Logger.error("OrderAddressActor Error:" + Throwables.getStackTraceAsString(e));
                e.printStackTrace();
            }
        }).matchAny(s -> Logger.error("OrderAddressActor received messages not matched: {}", s.toString())).build());
    }
}
