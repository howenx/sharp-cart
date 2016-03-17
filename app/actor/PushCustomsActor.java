package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import domain.Order;
import domain.OrderSplit;
import middle.JDPayMid;
import modules.NewScheduler;
import modules.SysParCom;
import play.Logger;
import play.libs.ws.WSClient;
import scala.concurrent.duration.Duration;
import service.CartService;
import util.Crypto;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 京东支付报关
 * Created by howen on 16/03/08.
 */

public class PushCustomsActor extends AbstractActor {

    @Inject
    public PushCustomsActor(NewScheduler newScheduler, WSClient ws, JDPayMid jdPayMid, CartService cartService, @Named("queryCustomStatusActor") ActorRef queryCustomStatusActor) {
        receive(ReceiveBuilder.match(Long.class, orderId -> {
            try {
                Order order = new Order();
                order.setOrderId(orderId);
                List<Order> orderList = cartService.getOrder(order);
                if (orderList.size() > 0) {
                    order = orderList.get(0);
                    OrderSplit orderSplit = new OrderSplit();
                    orderSplit.setOrderId(orderId);
                    List<OrderSplit> orderSplits = cartService.selectOrderSplit(orderSplit);
                    if (orderSplits.size() > 0) {
                        for (OrderSplit os : orderSplits) {
                            StringBuilder sb = new StringBuilder();
                            jdPayMid.getCustomsBasicInfo(os.getSplitId()).forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));

                            ws.url(SysParCom.JD_PUSH_URL).setContentType("application/x-www-form-urlencoded").post(sb.toString()).map(wsResponse -> {
                                JsonNode response = wsResponse.asJson();
                                Logger.info("京东海关报送返回JSON: " + response.toString());

                                String sign_data = response.get("sign_data").asText();

                                Map<String, String> params = controllers.Application.mapper.convertValue(response, controllers.Application.mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class));

                                String _sign = Crypto.create_sign(params, SysParCom.JD_SECRET);
                                if (!sign_data.equalsIgnoreCase(_sign)) {
                                    Logger.info("京东海关报送返回签名校验失败");
                                } else {
                                    os.setPayResponseCode(params.get("response_code"));
                                    os.setPayResponseMsg(params.get("response_message"));
                                    os.setSubPgTradeNo(params.get("sub_out_trade_no"));
                                    os.setState(params.get("is_success"));
                                    cartService.updateOrderSplit(os);
                                    if (params.get("is_success").equals("Y")) {
                                        Map<String,String> map = new HashMap<>();
                                        map.put("orderId",orderId.toString());
                                        map.put("actorPath",queryCustomStatusActor.path().toString());

                                        newScheduler.schedule(Duration.create(5000, TimeUnit.MILLISECONDS),Duration.create(SysParCom.JD_QUERY_DELAY, TimeUnit.MILLISECONDS),queryCustomStatusActor,map);
                                    }
                                }
                                return null;
                            });
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.error("Connection error. Should retry later. ", ex);
                ex.printStackTrace();
            }
        }).matchAny(s -> Logger.error("PushActor received messages not matched: {}", s.toString())).build());
    }
}
