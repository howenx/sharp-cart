package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import domain.CartDto;
import domain.Sku;
import play.Logger;
import service.CartService;
import service.SkuService;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 减库存
 * Created by howen on 15/12/18.
 */
@SuppressWarnings("unchecked")
public class ReduceInvActor extends AbstractActor {
    @Inject
    public ReduceInvActor(CartService cartService, SkuService skuService) {

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
                        Logger.error("ReduceInvActor Sku Select Error:" + e.getMessage());
                        e.printStackTrace();
                    }
                    if (sku.getRestAmount()- cartDto.getAmount() ==0){
                        sku.setRestAmount(0);
                        sku.setState("K");
                        sku.setSoldAmount(sku.getSoldAmount()+cartDto.getAmount());
                    }else if (sku.getRestAmount()- cartDto.getAmount() <0){
                        Logger.error("ReduceInvActor: 出现库存负数,不能支付");
                    }else{
                        sku.setRestAmount(sku.getRestAmount()- cartDto.getAmount());
                        sku.setSoldAmount(sku.getSoldAmount()+cartDto.getAmount());
                    }
                    try {
                        if(skuService.updateInv(sku)) Logger.debug("需要被减的库存ID: "+sku.getId()+" 减库存的数量: "+cartDto.getAmount());
                    } catch (Exception e) {
                        Logger.error("ReduceInvActor Error:" + e.getMessage());
                        e.printStackTrace();
                    }
                });

            });
        }).matchAny(s -> Logger.error("ReduceInvActor received messages not matched: {}", s.toString())).build());
    }
}
