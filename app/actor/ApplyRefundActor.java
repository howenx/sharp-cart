package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.JDPay;
import domain.Order;
import domain.Refund;
import util.SysParCom;
import play.Logger;
import play.libs.ws.WSClient;
import service.CartService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * 退款Actor
 * Created by howen on 16/2/24.
 */
public class ApplyRefundActor extends AbstractActor {

    @Inject
    public ApplyRefundActor(CartService cartService, WSClient ws,@Named("resumeInvActor") ActorRef resumeInvActor) {

        receive(ReceiveBuilder.match(Object.class, refund -> {

            if (refund instanceof Refund) {

                Logger.error("退款Refund--->" + ((Refund) refund).toString());

                //必传,orderId,reason,refundType,userId,skuId
                Order order = new Order();
                order.setOrderId(((Refund) refund).getOrderId());
                List<Order> orderList = cartService.getOrder(order);
                if (orderList.size() > 0) {
                    order = orderList.get(0);

                    Map<String, String> params = JDPay.payBackParams(((Refund) refund), null, null);
                    StringBuilder sb = new StringBuilder();
                    params.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
                    ws.url(SysParCom.JD_REFUND_URL).setContentType("application/x-www-form-urlencoded").post(sb.toString()).map(wsResponse -> {
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
                                Logger.info(((Refund) refund).getUserId() + "退款成功");
                                resumeInvActor.tell(((Refund) refund).getOrderId(),ActorRef.noSender());
                            } else {
                                Logger.error(((Refund) refund).getUserId() + "退款失败");
                            }
                        }
                        return wsResponse.asJson();
                    });
                }
            }
        }).matchAny(s -> Logger.error("RefundActor received messages not matched: {}", s.toString())).build());
    }
}