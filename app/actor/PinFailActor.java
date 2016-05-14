package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import controllers.JDPay;
import domain.*;
import play.Logger;
import play.libs.ws.WSClient;
import service.CartService;
import service.PromotionService;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Date;
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

            PinUser pinUser = new PinUser();
            pinUser.setPinActiveId(activityId);
            List<PinUser> pinUsers = promotionService.selectPinUser(pinUser);

            if (!pinActivity.getStatus().equals("Y")) {

            } else {
                //如果加入人数小于要求成团的人数就拼购失败
                if (pinActivity.getStatus().equals("Y") && pinUsers.size() < pinActivity.getPersonNum() && pinActivity.getEndAt().before(new Timestamp(new Date().getTime()))) {

                    pinActivity.setStatus("F");
                    promotionService.updatePinActivity(pinActivity);
                    try {
                        for (PinUser p : pinUsers) {
                            Order order = new Order();
                            order.setPinActiveId(activityId);
                            order.setUserId(p.getUserId());
                            List<Order> orders = cartService.getPinOrder(order);
                            if (orders.size() > 0) {
                                order = orders.get(0);
                                order.setOrderStatus("PF");
                                cartService.updateOrder(order);//更新订单状态为拼团失败状态

                                Refund refund = new Refund();
                                refund.setAmount(order.getOrderAmount());
                                refund.setOrderId(order.getOrderId());
                                refund.setPayBackFee(order.getPayTotal());
                                refund.setReason("拼团失败,自动退款");
                                refund.setRefundType("pin");

                                PinSku pinsku = new PinSku();
                                pinsku = promotionService.getPinSkuById(pinActivity.getPinId());

                                refund.setSkuId(pinsku.getInvId());
                                refund.setSplitOrderId(order.getOrderSplitId());
                                refund.setUserId(pinUser.getUserId());

                                if (cartService.insertRefund(refund)) {
                                    Map<String, String> params = JDPay.payBackParams(refund, null, null);
                                    StringBuilder sb = new StringBuilder();
                                    params.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
                                    ws.url("https://cbe.wangyin.com/cashier/refund").setContentType("application/x-www-form-urlencoded").post(sb.toString()).map(wsResponse -> {
                                        JsonNode response = wsResponse.asJson();
                                        Logger.info("京东退款返回数据JSON: " + response.toString());
                                        Refund re = new Refund();
                                        re.setId(response.get("out_trade_no").asLong());
                                        re.setPgCode(response.get("response_code").asText());
                                        re.setPgMessage(response.get("response_message").asText());
                                        re.setPgTradeNo(response.get("trade_no").asText());
                                        re.setState(response.get("is_success").asText());

                                        if (cartService.updateRefund(re)) {
                                            if (re.getState().equals("Y")) {
                                                Order order1 = new Order();
                                                order1.setOrderId(refund.getOrderId());
                                                order1.setOrderStatus("T");
                                                cartService.updateOrder(order1);
                                                Logger.info(p.getUserId() + "用户拼购退款成功");
                                            } else {
                                                Logger.error(p.getUserId() + "用户拼购退款失败");
                                            }
                                        }
                                        return wsResponse.asJson();
                                    });
                                }
                            }
                        }
                    } catch (Exception e) {
                        Logger.error("拼购失败自动退款出现错误: " + Throwables.getStackTraceAsString(e));
                        e.printStackTrace();
                    }
                }
            }


        }).matchAny(s -> Logger.error("PublicCouponActor received messages not matched: {}", s.toString())).build());
    }
}