package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

import akka.actor.UntypedActor;
import akka.japi.pf.ReceiveBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import domain.Order;
import domain.OrderSplit;
import domain.Persist;
import middle.JDPayMid;
import modules.LevelFactory;
import modules.SysParCom;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.Logger;
import play.Play;
import play.libs.Akka;
import play.libs.ws.WS;
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
 * 京东支付报关查询报关状态
 * Created by howen on 16/03/08.
 */
public class QueryCustomStatusActor extends AbstractActor {

    @Inject
    public QueryCustomStatusActor(WSClient ws, JDPayMid jdPayMid, CartService cartService, LevelFactory levelFactory) {
        receive(ReceiveBuilder.match(Map.class, csmap -> {
            try {
                Order order = new Order();
                order.setOrderId(Long.valueOf(csmap.get("orderId").toString()));
                List<Order> orderList = cartService.getOrder(order);
                if (orderList.size() > 0) {
                    order = orderList.get(0);
                    OrderSplit orderSplit = new OrderSplit();
                    orderSplit.setOrderId(Long.valueOf(csmap.get("orderId").toString()));
                    List<OrderSplit> orderSplits = cartService.selectOrderSplit(orderSplit);
                    if (orderSplits.size() > 0) {
                        for (OrderSplit os : orderSplits) {
                            StringBuilder sb = new StringBuilder();
                            jdPayMid.getCustomsQueryInfo(os.getSplitId()).forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));

                            ws.url(SysParCom.JD_QUERY_URL).setContentType("application/x-www-form-urlencoded").post(sb.toString()).map(wsResponse -> {
                                JsonNode response = wsResponse.asJson();
                                Logger.info("京东海关报送状态查询返回JSON: " + response.toString());

                                String sign_data = response.get("sign_data").asText();

                                Map<String, String> params = controllers.Application.mapper.convertValue(response, controllers.Application.mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class));

                                String _sign = Crypto.create_sign(params, SysParCom.JD_SECRET);
                                if (!sign_data.equalsIgnoreCase(_sign)) {
                                    Logger.info("京东海关报送状态查询返回签名校验失败");
                                } else {
                                    if (params.get("is_success").equals("Y")){
                                        os.setPayCustomsReturnCode(params.get("custom_push_status"));//海送报送状态
                                        os.setPayCustomsReturnMsg(params.get("custom_push_status_desc"));//海关报送状态信息描述
                                        if (params.containsKey("insp_push_status") && params.get("insp_push_status")!=null) os.setPayInspReturnCode(params.get("insp_push_status"));//国检报送状态，对需要单独报送国检的海关有效
                                        if (params.containsKey("insp_push_status_desc")  && params.get("insp_push_status_desc")!=null) os.setPayInspReturnMsg(params.get("insp_push_status_desc"));//国检报送状态信息描述

                                        if (params.get("custom_push_status").equals("SUCCESS") || params.get("custom_push_status").equals("FAIL") || params.get("custom_push_status").equals("EXCEPTION")){

                                            if (params.containsKey("insp_push_status") && params.get("insp_push_status")!=null) {
                                                if (params.get("insp_push_status").equals("SUCCESS") || params.get("insp_push_status").equals("FAIL") || params.get("insp_push_status").equals("EXCEPTION")) {
                                                    if (levelFactory.map.containsKey(Long.valueOf(csmap.get("orderId").toString()))) {
                                                        Persist p = levelFactory.map.get(Long.valueOf(csmap.get("orderId").toString()));
                                                        p.getCancellable().cancel();
                                                        levelFactory.map.remove(Long.valueOf(csmap.get("orderId").toString()));
                                                    }
                                                    if (levelFactory.get(Long.valueOf(csmap.get("orderId").toString())) != null) {
                                                        levelFactory.delete(Long.valueOf(csmap.get("orderId").toString()));
                                                    }
                                                }
                                            }else {
                                                if (levelFactory.map.containsKey(Long.valueOf(csmap.get("orderId").toString()))) {
                                                    Persist p = levelFactory.map.get(Long.valueOf(csmap.get("orderId").toString()));
                                                    p.getCancellable().cancel();
                                                    levelFactory.map.remove(Long.valueOf(csmap.get("orderId").toString()));
                                                }
                                                if (levelFactory.get(Long.valueOf(csmap.get("orderId").toString())) != null) {
                                                    levelFactory.delete(Long.valueOf(csmap.get("orderId").toString()));
                                                }
                                            }
                                        }
                                        cartService.updateOrderSplit(os);
                                    }else {
                                        os.setPayResponseCode(params.get("response_code"));
                                        os.setPayResponseMsg(params.get("response_message"));
                                        os.setState(params.get("is_success"));
                                        cartService.updateOrderSplit(os);
                                        if (levelFactory.map.containsKey(Long.valueOf(csmap.get("orderId").toString()))) {
                                            Persist p = levelFactory.map.get(Long.valueOf(csmap.get("orderId").toString()));
                                            p.getCancellable().cancel();
                                            levelFactory.map.remove(Long.valueOf(csmap.get("orderId").toString()));
                                        }
                                        if (levelFactory.get(Long.valueOf(csmap.get("orderId").toString())) != null) {
                                            levelFactory.delete(Long.valueOf(csmap.get("orderId").toString()));
                                        }
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
