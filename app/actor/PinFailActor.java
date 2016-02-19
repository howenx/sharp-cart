package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.JDPay;
import domain.*;
import play.Logger;
import play.libs.ws.WSClient;
import service.CartService;
import service.PromotionService;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * 拼购失败
 * Created by howen on 16/2/17.
 */
public class PinFailActor extends AbstractActor {

    @Inject
    public PinFailActor(PromotionService promotionService, CartService cartService, WSClient ws) {

        receive(ReceiveBuilder.match(Long.class, activityId -> {

            PinActivity pinActivity = promotionService.selectPinActivityById(activityId);

            //如果加入人数小于要求成团的人数就拼购失败
            if (pinActivity.getJoinPersons() < pinActivity.getPersonNum()) {
                pinActivity.setStatus("F");
            }
            promotionService.updatePinActivity(pinActivity);


            PinUser pinUser = new PinUser();
            pinUser.setOrMaster(false);
            pinUser.setPinActiveId(activityId);
            List<PinUser> pinUsers = promotionService.selectPinUser(pinUser);
            try {
                for (PinUser p : pinUsers) {
                    Order order = new Order();
                    order.setPinActiveId(activityId);
                    order.setUserId(p.getUserId());
                    List<Order> orders = cartService.getPinOrder(order);
                    if (orders.size() > 0) {
                        Refund refund = new Refund();
                        refund.setAmount(order.getOrderAmount());
                        refund.setOrderId(order.getOrderId());
                        refund.setPayBackFee(order.getPayTotal());
                        refund.setReason("拼团失败,自动退款");
                        refund.setRefundType("pin");

                        PinSku pinsku = new PinSku();
                        pinsku =promotionService.getPinSkuById(pinActivity.getPinId());

                        refund.setSkuId(pinsku.getInvId());
                        refund.setSplitOrderId(order.getOrderSplitId());
                        refund.setUserId(pinUser.getUserId());
                        if (cartService.insertRefund(refund)){
                            Map<String, String> params = JDPay.payBackParams(refund, null, null);
                            StringBuilder sb = new StringBuilder();
                            params.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
                            ws.url("https://cbe.wangyin.com/cashier/refund").setContentType("application/x-www-form-urlencoded").post(sb.toString()).map(wsResponse -> {
                                JsonNode response = wsResponse.asJson();
                                Logger.info("京东退款返回数据JSON: " + response.toString());
                                Refund re = new Refund();
                                re.setId(response.get("out_trade_no").asLong());
                                re.setOrderId(response.get("return_params").asLong());
                                re.setPgCode(response.get("response_code").asText());
                                re.setPgMessage(response.get("response_message").asText());
                                re.setPgTradeNo(response.get("trade_no").asText());
                                re.setState(response.get("is_success").asText());

                                if (cartService.updateRefund(re)) {
                                    if (re.getState().equals("Y")) {
                                        Logger.info(pinUser.getUserId()+"用户拼购退款成功");
                                    } else {
                                        Logger.error(pinUser.getUserId()+"用户拼购退款失败");
                                    }
                                }
                                return  wsResponse.asJson();
                            });
                        }
                    }
                }
            } catch (Exception e) {
                Logger.error("拼购失败自动退款出现错误: "+e.getMessage());
                e.printStackTrace();
            }
        }).matchAny(s -> Logger.error("PublicCouponActor received messages not matched: {}", s.toString())).build());
    }
}