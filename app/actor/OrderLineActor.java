package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import domain.*;
import play.Logger;
import service.CartService;
import service.SkuService;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单明细
 * Created by howen on 15/12/18.
 */
@SuppressWarnings("unchecked")
public class OrderLineActor extends AbstractActor {

    @Inject
    public OrderLineActor(CartService cartService, SkuService skuService) {

        receive(ReceiveBuilder.match(HashMap.class, maps -> {

            Map<String, Object> orderInfo = (Map<String, Object>) maps;
            Long orderId = (Long) orderInfo.get("orderId");
            List<Map<String, Object>> orderSplitList = (List<Map<String, Object>>) orderInfo.get("singleCustoms");

            orderSplitList.forEach(m->{
                Long splitId = (Long)m.get("splitId");
                List<CartDto> cartDtos = (List<CartDto>)m.get("cartDtos");
                cartDtos.forEach(cartDto -> {
                    Sku sku = new Sku();
                    sku.setId(cartDto.getSkuId());
                    try {
                        sku=skuService.getInv(sku);
                    } catch (Exception e) {
                        Logger.error("Sku Select Error:" + e.getMessage());
                        e.printStackTrace();
                    }
                    OrderLine orderLine = new OrderLine();
                    orderLine.setItemId(sku.getItemId());
                    orderLine.setOrderId(orderId);
                    orderLine.setAmount(cartDto.getAmount());
                    orderLine.setPrice(sku.getItemPrice());
                    orderLine.setSkuColor(sku.getItemColor());
                    orderLine.setSkuSize(sku.getItemSize());
                    orderLine.setSkuTitle(sku.getInvTitle());
                    orderLine.setSplitId(splitId);
                    orderLine.setSkuId(sku.getId());
                    orderLine.setSkuImg(sku.getInvImg());
                    try {
                        if(cartService.insertOrderLine(orderLine)) Logger.debug("OrderLine: "+orderLine);
                    } catch (Exception e) {
                        Logger.error("OrderLineActor Error:" + e.getMessage());
                        e.printStackTrace();
                    }
                });

            });
        }).matchAny(s -> Logger.error("OrderLineActor received messages not matched: {}", s.toString())).build());
    }
}
