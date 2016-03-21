package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.JDPay;
import domain.Order;
import domain.Refund;
import play.Logger;
import play.libs.ws.WSClient;
import service.CartService;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * 退款Actor
 * Created by howen on 16/2/24.
 */
public class RefundActor extends AbstractActor {

    @Inject
    public RefundActor(CartService cartService, WSClient ws) {

        receive(ReceiveBuilder.match(Refund.class, refund -> {

            //必传,orderId,reason,refundType,userId,skuId

            Order order = new Order();
            order.setOrderId(refund.getOrderId());
            List<Order> orderList = cartService.getOrder(order);
            if (orderList.size() > 0) {
                order = orderList.get(0);

                refund.setAmount(order.getOrderAmount());
                refund.setPayBackFee(order.getPayTotal());
                refund.setSplitOrderId(order.getOrderSplitId());
                refund.setUserId(order.getUserId());

                if (cartService.insertRefund(refund)) {
                    Map<String, String> params = JDPay.payBackParams(refund, null, null);
                    StringBuilder sb = new StringBuilder();
                    params.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
                    ws.url("https://cbe.wangyin.com/cashier/refund").setContentType("application/x-www-form-urlencoded").post(sb.toString()).thenApply(wsResponse -> {
                        JsonNode response = wsResponse.asJson();
                        Logger.info("京东退款返回数据JSON: " + response.toString());
                        Refund re = new Refund();
                        re.setId(response.get("out_trade_no").asLong());
                        re.setPgCode(response.get("response_code").asText());
                        re.setPgMessage(response.get("response_message").asText());
                        re.setPgTradeNo(response.get("trade_no").asText());
                        re.setState(response.get("is_success").asText());
                        try {
                            if (cartService.updateRefund(re)) {
                                if (re.getState().equals("Y")) {
                                    Order order1 = new Order();
                                    order1.setOrderId(refund.getOrderId());
                                    order1.setOrderStatus("T");
                                    cartService.updateOrder(order1);
                                    Logger.info(refund.getUserId() + "退款成功");
                                } else {
                                    Logger.error(refund.getUserId() + "退款失败");
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        return wsResponse.asJson();
                    });
                }

            }
        }).matchAny(s -> Logger.error("RefundActor received messages not matched: {}", s.toString())).build());
    }
}