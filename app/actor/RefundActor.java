package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import common.WeiXinTradeType;
import controllers.JDPay;
import controllers.WeiXinCtrl;
import domain.Order;
import domain.Refund;
import play.Logger;
import play.libs.ws.WSClient;
import service.CartService;
import util.SysParCom;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 退款Actor
 * Created by howen on 16/2/24.
 */
public class RefundActor extends AbstractActor {

    @Inject
    public RefundActor(CartService cartService, WSClient ws, WeiXinCtrl weiXinCtrl) {

        receive(ReceiveBuilder.match(Object.class, refund -> {

            if (refund instanceof Refund) {

                //必传,orderId,reason,refundType,userId,skuId
                Order order = new Order();
                order.setOrderId(((Refund) refund).getOrderId());
                List<Order> orderList = cartService.getOrder(order);
                if (orderList.size() > 0) {
                    order = orderList.get(0);

                    ((Refund) refund).setAmount(order.getOrderAmount());
                    ((Refund) refund).setPayBackFee(order.getPayTotal());
                    ((Refund) refund).setSplitOrderId(order.getOrderSplitId());
                    ((Refund) refund).setUserId(order.getUserId());
                    List<Refund> refunds =cartService.selectRefund(((Refund) refund));
                    if (refunds.size()>0){
                        if (cartService.updateRefund(((Refund) refund))) {
                            if (order.getPayMethod().equals("JD")) {
                                jdPayRefund(cartService, ws, (Refund) refund);
                            } else if (order.getPayMethod().equals("WEIXIN")) {
                                weixinPayRefund(cartService, (Refund) refund, weiXinCtrl);
                            }
                        }
                    }else{
                        if (cartService.insertRefund(((Refund) refund))) {
                            if (order.getPayMethod().equals("JD")) {
                                jdPayRefund(cartService, ws, (Refund) refund);
                            } else if (order.getPayMethod().equals("WEIXIN")) {
                                weixinPayRefund(cartService, (Refund) refund, weiXinCtrl);
                            }
                        }
                    }
                }
            }
        }).matchAny(s -> Logger.error("RefundActor received messages not matched: {}", s.toString())).build());
    }

    private void jdPayRefund(CartService cartService, WSClient ws, Refund refund) {
        Map<String, String> params = JDPay.payBackParams(refund, null, null);
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
                    Order order1 = new Order();
                    order1.setOrderId(refund.getOrderId());
                    order1.setOrderStatus("T");
                    cartService.updateOrder(order1);
                    Logger.info(refund.getUserId() + "退款成功");
                } else {
                    Logger.error(refund.getUserId() + "退款失败");
                }
            }
            return wsResponse.asJson();
        });
    }

    private void weixinPayRefund(CartService cartService, Refund refund, WeiXinCtrl weiXinCtrl) {

        Order order = new Order();
        order.setOrderId(refund.getOrderId());
        Optional<List<Order>> listOptional = null;
        try {
            listOptional = Optional.ofNullable(cartService.getOrder(order));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (listOptional.isPresent() && listOptional.get().size() == 1) {
            order = listOptional.get().get(0);
            String xmlContent = weiXinCtrl.getRefundParams(order);
            if (xmlContent != null) {
                String result = weiXinCtrl.refundConnect(SysParCom.WEIXIN_PAY_REFUND, xmlContent, WeiXinTradeType.getWeiXinTradeType(order.getPayMethodSub())); //接口提供所有微信支付订单的查询
                Logger.info("微信支付退款发送内容\n" + xmlContent + "\n返回内容" + result);

                if (Objects.equals("", result) || null == result) {
                    Logger.error(refund.getUserId() + "微信退款返回结果为空");
                }
                try {
                    Map<String, String> resultMap = weiXinCtrl.xmlToMap(result);

                    Refund re = new Refund();

                    if (null == resultMap || resultMap.size() <= 0) {
                        Logger.error(refund.getUserId() + "微信退款返回结果为空");
                    } else if (!"SUCCESS".equals(resultMap.get("return_code"))) { //返回状态码  SUCCESS/FAIL 此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
                        Logger.error(refund.getUserId() + "微信退款失败,返回状态码:" + resultMap.get("return_code"));
                    } else {
                        String out_refund_no=resultMap.get("out_refund_no");
                        re.setOrderId(Long.valueOf(out_refund_no));
                        re.setPgCode(resultMap.get("result_code"));
                        re.setPgMessage(resultMap.get("return_msg"));
                        re.setPgTradeNo(resultMap.get("refund_id"));//微信退款单号
                        if (resultMap.get("result_code").equals("SUCCESS")) {
                            re.setState("Y");
                            Order order1 = new Order();
                            order1.setOrderId(refund.getOrderId());
                            order1.setOrderStatus("T");
                            cartService.updateOrder(order1);
                            Logger.error(refund.getUserId() + "微信退款成功,返回业务结果码:" + resultMap.get("result_code")+",refund="+re);
                        } else {
                            Logger.error(refund.getUserId() + "微信退款失败,返回业务结果码:" + resultMap.get("result_code")+",refund="+re);
                            re.setState("N");
                        }
                        cartService.updateRefund(re);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Logger.error("微信退款出现异常," + Throwables.getStackTraceAsString(ex));
                }
            }

        }

    }
}