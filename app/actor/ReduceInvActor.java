package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import com.google.common.base.Throwables;
import domain.*;
import play.Logger;
import service.SkuService;

import javax.inject.Inject;
import java.util.List;

/**
 * 减库存
 * Created by howen on 15/12/18.
 */
public class ReduceInvActor extends AbstractActor {
    @Inject
    public ReduceInvActor(SkuService skuService) {

        receive(ReceiveBuilder.match(SettleVo.class, settleVo -> {
            List<SettleFeeVo> settleFeeVos = settleVo.getSingleCustoms();
            settleFeeVos.forEach(m -> {
                List<CartDto> cartDtos = m.getCartDtos();
                cartDtos.forEach(cartDto -> {

                    Sku sku = new Sku();
                    sku.setId(cartDto.getSkuId());
                    try {
                        sku = skuService.getInv(sku);
                    } catch (Exception e) {
                        Logger.error("ReduceInvActor Sku Select Error:" + Throwables.getStackTraceAsString(e));
                        e.printStackTrace();
                    }

                    if (cartDto.getSkuType().equals("vary")) {
                        VaryPrice varyPrice = new VaryPrice();
                        varyPrice.setId(cartDto.getSkuTypeId());
                        varyPrice.setSoldAmount(cartDto.getAmount() + varyPrice.getSoldAmount());
                        if (varyPrice.getSoldAmount() >= varyPrice.getLimitAmount()) {
                            varyPrice.setSoldAmount(varyPrice.getLimitAmount());
                            varyPrice.setStatus("K");
                        }
                        skuService.updateVaryPrice(varyPrice);
                    }

                    if (sku.getRestAmount() - cartDto.getAmount() == 0) {
                        sku.setRestAmount(0);
                        sku.setState("K");
                        sku.setSoldAmount(sku.getSoldAmount() + cartDto.getAmount());
                    } else if (sku.getRestAmount() - cartDto.getAmount() < 0) {
                        Logger.error("ReduceInvActor: 出现库存负数,不能支付");
                    } else {
                        sku.setRestAmount(sku.getRestAmount() - cartDto.getAmount());
                        sku.setSoldAmount(sku.getSoldAmount() + cartDto.getAmount());
                    }
                    try {
                        if (skuService.updateInv(sku))
                            Logger.debug("需要被减的库存ID: " + sku.getId() + " 减库存的数量: " + cartDto.getAmount());
                    } catch (Exception e) {
                        Logger.error("ReduceInvActor Error:" +Throwables.getStackTraceAsString(e));
                        e.printStackTrace();
                    }
                });

            });
        }).matchAny(s -> Logger.error("ReduceInvActor received messages not matched: {}", s.toString())).build());
    }
}
