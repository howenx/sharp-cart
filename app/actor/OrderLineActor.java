package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import com.google.common.base.Throwables;
import domain.*;
import play.Logger;
import service.CartService;
import service.PromotionService;
import service.SkuService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单明细
 * Created by howen on 15/12/18.
 */
public class OrderLineActor extends AbstractActor {

    @Inject
    public OrderLineActor(CartService cartService, SkuService skuService, PromotionService promotionService) {

        receive(ReceiveBuilder.match(SettleVo.class, settleVo -> {

            Long orderId = settleVo.getOrderId();
            List<SettleFeeVo> settleFeeVos = settleVo.getSingleCustoms();

            settleFeeVos.forEach(m->{
                Long splitId = m.getSplitId();
                List<CartDto> cartDtos = m.getCartDtos();
                cartDtos.forEach(cartDto -> {
                    Sku sku = new Sku();
                    sku.setId(cartDto.getSkuId());
                    try {
                        sku=skuService.getInv(sku);
                    } catch (Exception e) {
                        Logger.error("Sku Select Error:" + Throwables.getStackTraceAsString(e));
                        e.printStackTrace();
                    }
                    OrderLine orderLine = new OrderLine();
                    orderLine.setItemId(sku.getItemId());
                    orderLine.setOrderId(orderId);
                    orderLine.setAmount(cartDto.getAmount());
                    orderLine.setSkuTitle(sku.getInvTitle());



                    switch (cartDto.getSkuType()) {
                        case "item":
                            orderLine.setPrice(sku.getItemPrice());
                            break;
                        case "vary":
                            VaryPrice varyPrice = new VaryPrice();
                            varyPrice.setId(cartDto.getSkuTypeId());
                            List<VaryPrice> varyPriceList = skuService.getVaryPriceBy(varyPrice);
                            if (varyPriceList.size() > 0) {
                                varyPrice = varyPriceList.get(0);
                            }
                            orderLine.setPrice(varyPrice.getPrice());
                            break;
                        case "customize":
                            SubjectPrice subjectPrice = skuService.getSbjPriceById(cartDto.getSkuTypeId());
                            orderLine.setPrice(subjectPrice.getPrice());
                            break;
                        case "pin":
                            PinTieredPrice pinTieredPrice = new PinTieredPrice();
                            pinTieredPrice.setId(cartDto.getPinTieredPriceId());
                            pinTieredPrice.setPinId(cartDto.getSkuTypeId());
                            pinTieredPrice = promotionService.getTieredPriceById(pinTieredPrice);
                            orderLine.setPrice(pinTieredPrice.getPrice());
                            PinSku pinSku = promotionService.getPinSkuById(cartDto.getSkuTypeId());
                            orderLine.setSkuTitle(pinSku.getPinTitle());
                            break;
                    }

                    orderLine.setSkuColor(sku.getItemColor());
                    orderLine.setSkuSize(sku.getItemSize());

                    orderLine.setSplitId(splitId);
                    orderLine.setSkuId(sku.getId());
                    orderLine.setSkuImg(sku.getInvImg());
                    orderLine.setSkuType(cartDto.getSkuType());
                    orderLine.setSkuTypeId(cartDto.getSkuTypeId());
                    if (cartDto.getPinTieredPriceId()!=null){
                        orderLine.setPinTieredPriceId(cartDto.getPinTieredPriceId());
                    }
                    try {
                        if(cartService.insertOrderLine(orderLine)) Logger.debug("订单明细ID: "+orderLine.getLineId());
                    } catch (Exception e) {
                        Logger.error("OrderLineActor Error:" + Throwables.getStackTraceAsString(e));
                        e.printStackTrace();
                    }
                });
            });
        }).matchAny(s -> Logger.error("OrderLineActor received messages not matched: {}", s.toString())).build());
    }
}
