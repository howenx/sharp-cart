package controllers;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;
import domain.*;
import filters.UserAuth;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.Logger;
import play.Play;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import service.CartService;
import service.IdService;
import util.CalCountDown;
import util.Crypto;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by handy on 15/12/16.
 * kakao china
 */
@Singleton
public class JDPay extends Controller {

    private CartService cartService;

    private IdService idService;

    private ActorRef cancelOrderActor;

    @Inject
    WSClient ws;

    //shopping服务器url
    private static final String SHOPPING_URL = play.Play.application().configuration().getString("shopping.server.url");

    private static final String JD_SECRET = Play.application().configuration().getString("jd_secret");

    private static final String JD_SELLER = Play.application().configuration().getString("jd_seller");

    public static final Long COUNTDOWN_MILLISECONDS=Long.valueOf(Play.application().configuration().getString("order.countdown.milliseconds"));

    static final ObjectNode result = Json.newObject();

    @Inject
    public JDPay(CartService cartService, IdService idService, @Named("cancelOrderActor") ActorRef cancelOrderActor) {
        this.cartService = cartService;
        this.idService = idService;
        this.cancelOrderActor = cancelOrderActor;
    }

    /**
     * 支付调用
     *
     * @param orderId 订单ID
     * @return 跳转到京东支付页面
     */
    @Security.Authenticated(UserAuth.class)
    public Result payOrderWeb(Long orderId) {

        try {
            Long userId = (Long) ctx().args.get("userId");

            Order order = new Order();
            order.setOrderId(orderId);
            order.setUserId(userId);
            Optional<List<Order>> listOptional = Optional.ofNullable(cartService.getOrderBy(order));
            if (listOptional.isPresent() && listOptional.get().size() > 0) {
                order = cartService.getOrderBy(order).get(0);
                Optional<Long> longOptional = Optional.ofNullable(CalCountDown.getTimeSubtract(order.getOrderCreateAt()));
                if (longOptional.isPresent() && longOptional.get().compareTo(COUNTDOWN_MILLISECONDS) > 0) {
                    cancelOrderActor.tell(orderId, null);
                    Logger.error("order timeout:"+order.getOrderId());
                    return ok(views.html.jdpayfailed.render());
                } else {
                    SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 格式化时间
                    Calendar calendar=Calendar.getInstance();
                    calendar.setTime(d.parse(order.getOrderCreateAt()));
                    Map<String, String> params = getParams(calendar.getTimeInMillis(),request().queryString(), request().body().asFormUrlEncoded(), userId, orderId);
                    Logger.info("someone paying: "+orderId);
                    return ok(views.html.cashdesk.render(params));
                }
            } else return ok(views.html.jdpayfailed.render());
        } catch (Exception ex) {
            Logger.error("settle: " + ex.getMessage());
            ex.printStackTrace();
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ERROR.getIndex()), Message.ErrorCode.ERROR.getIndex())));
            return ok(result);
        }
    }

    /**
     * 获取签名参数
     *
     * @param req_map  请求参数
     * @param body_map 请求参数
     * @param userId   用户ID
     * @param orderId  订单ID
     * @return map
     * @throws Exception
     */
    private Map<String, String> getParams(Long timeInMillis,Map<String, String[]> req_map, Map<String, String[]> body_map, Long userId, Long orderId) throws Exception {
        Map<String, String> params = new HashMap<>();
        if (req_map != null) {
            req_map.forEach((k, v) -> params.put(k, v[0]));
        }
        if (body_map != null)
            body_map.forEach((k, v) -> params.put(k, v[0]));

        Optional<Map<String, String>> optionalOrderInfo = Optional.ofNullable(getOrderInfo(userId, orderId));

        if (optionalOrderInfo.isPresent()) {
            optionalOrderInfo.get().forEach(params::put);
            getBasicInfo().forEach(params::put);
            params.put("orderCreateAt",String.valueOf(timeInMillis));
            params.put("sign_data", Crypto.create_sign(params, JD_SECRET));
            return params;
        } else return null;
    }


    /***
     * 支付订单参数配置
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @return map
     * @throws Exception
     */
    private Map<String, String> getOrderInfo(Long userId, Long orderId) throws Exception {
        Map<String, String> map = new HashMap<>();
        Order order = new Order();
        order.setOrderId(orderId);
        Optional<List<Order>> listOptional = Optional.ofNullable(cartService.getOrderBy(order));

        IdPlus idPlus = new IdPlus();
        idPlus.setUserId(userId);
        Optional<IdPlus> idPlusOptional = Optional.ofNullable(idService.getIdPlus(idPlus));

        if (listOptional.isPresent() && listOptional.get().size() > 0) {
            order = cartService.getOrderBy(order).get(0);
            map.put("out_trade_no", orderId.toString());
            map.put("return_params", orderId.toString());//成功支付,或者查询时候,返回订单编号
            map.put("trade_subject", "韩秘美-订单编号" + orderId);
//            map.put("trade_amount", order.getPayTotal().multiply(new BigDecimal(100)).setScale(0,BigDecimal.ROUND_DOWN).toPlainString());
            map.put("trade_amount", String.valueOf(1));
            //自用字断
            map.put("all_fee", order.getPayTotal().toPlainString());
            if (idPlusOptional.isPresent()) {
                map.put("token", idPlusOptional.get().getPayJdToken());
            }
            //buyer info json
            Map<String, String> buyerInfo = new HashMap<>();
            buyerInfo.put("customer_type", "OUT_CUSTOMER_VALUE");
            buyerInfo.put("customer_code", userId.toString());
            map.put("buyer_info", Json.stringify(Json.toJson(buyerInfo)));

            //sub_order_info
            OrderSplit orderSplit = new OrderSplit();
            orderSplit.setOrderId(orderId);
            Optional<List<OrderSplit>> orderSplitList = Optional.ofNullable(cartService.selectOrderSplit(orderSplit));
            if (orderSplitList.isPresent()) {
                List<Map<String, String>> subInfo = new ArrayList<>();
                for (OrderSplit orderSp : orderSplitList.get()) {
                    Map<String, String> subOrderMap = new HashMap<>();
                    OrderLine orderLine = new OrderLine();
                    orderLine.setOrderId(orderId);
                    orderLine.setSplitId(orderSp.getSplitId());
                    subOrderMap.put("sub_order_no", orderSp.getSplitId().toString());
                    subOrderMap.put("sub_order_name", "韩秘美-子订单号" + orderSp.getSplitId());
//                    subOrderMap.put("sub_order_amount",orderSp.getTotalPayFee().multiply(new BigDecimal(100)).setScale(0,BigDecimal.ROUND_DOWN).toPlainString());
                    subOrderMap.put("sub_order_amount", String.valueOf(1));

                    subInfo.add(subOrderMap);
                }
                map.put("sub_order_info", Json.stringify(Json.toJson(subInfo)));
            }
            return map;
        } else return null;
    }

    /**
     * 京东支付POST参数
     *
     * @return map
     */
    private static Map<String, String> getBasicInfo() {
        Map<String, String> params = new HashMap<>();

        DateTimeFormatter f = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss");
        String req_date = f.print(new DateTime());
        String sign_type = "MD5";
        String trade_currency = "CNY";
        String settle_currency = "USD";

        params.put("customer_no", JD_SELLER);
        params.put("notify_url", SHOPPING_URL + "/client/pay/jd/back");
        params.put("request_datetime", req_date);
        params.put("return_url", SHOPPING_URL + "/client/pay/jd/front");
        params.put("settle_currency", settle_currency);
        params.put("trade_currency", trade_currency);
        params.put("sign_type", sign_type);
        return params;
    }

    /**
     * 京东支付后端返回通知
     *
     * @return success
     */
    public Result payBackendNotify() {
        Map<String, String[]> body_map = request().body().asFormUrlEncoded();
        Map<String, String> params = new HashMap<>();
        body_map.forEach((k, v) -> params.put(k, v[0]));
        String sign = params.get("sign_data");
        String secret = Play.application().configuration().getString("jd_secret");

        String _sign = Crypto.create_sign(params, secret);
        if (!sign.equalsIgnoreCase(_sign)) {
            Logger.info("支付回调签名失败");
            return ok("error");
        } else {
            if (params.containsKey("out_trade_no") && params.containsKey("token") && params.containsKey("trade_no") && params.containsKey("trade_status") && params.get("trade_status").equals("FINI")) {
                Order order = new Order();
                order.setOrderId(Long.valueOf(params.get("out_trade_no")));
                order.setOrderStatus("S");
                order.setErrorStr(params.get("trade_status"));
                order.setPgTradeNo(params.get("trade_no"));
                try {
                    if (cartService.updateOrder(order)) Logger.error("支付回调订单更新payFrontNotify: " + Json.toJson(order));
                    Long userId = Long.valueOf(Json.parse(params.get("buyer_info")).get("customer_code").asText());
                    IdPlus idPlus = new IdPlus();
                    idPlus.setUserId(userId);
                    Optional<IdPlus> idPlusOptional = Optional.ofNullable(idService.getIdPlus(idPlus));
                    idPlus.setPayJdToken(params.get("token"));
                    if (idPlusOptional.isPresent()) {
                        if (idService.updateIdPlus(idPlus))
                            Logger.info("支付成功回调更新用户Token payFrontNotify:" + Json.toJson(idPlus));
                    } else {
                        if (idService.insertIdPlus(idPlus))
                            Logger.info("支付成功回调创建用户Token payFrontNotify:" + Json.toJson(idPlus));
                    }
                } catch (Exception e) {
                    Logger.error("支付回调订单更新出错payFrontNotify: " + e.getMessage());
                    e.printStackTrace();
                }
                return ok("SUCCESS");
            } else {
                Logger.error("支付回调参数校验失败");
                return ok("error");
            }
        }
    }

    /**
     * 京东支付前端返回通知接口
     *
     * @return success page
     */
    public Result payFrontNotify() {
        Map<String, String[]> body_map = request().body().asFormUrlEncoded();
        Map<String, String> params = new HashMap<>();
        body_map.forEach((k, v) -> params.put(k, v[0]));
        String sign = params.get("sign_data");
        String secret = Play.application().configuration().getString("jd_secret");
        String _sign = Crypto.create_sign(params, secret);
        Logger.info("支付成功返回数据: " + Json.toJson(params));
        if (!sign.equalsIgnoreCase(_sign)) {
            return ok(views.html.jdpayfailed.render());
        } else {
            if (params.containsKey("out_trade_no") && params.containsKey("token") && params.containsKey("trade_no") && params.containsKey("trade_status") && params.get("trade_status").equals("FINI")) {
                Order order = new Order();
                order.setOrderId(Long.valueOf(params.get("out_trade_no")));

                try {
                    Optional<List<Order>> listOptional = Optional.ofNullable(cartService.getOrderBy(order));
                    if (listOptional.isPresent() && listOptional.get().size()>0 && listOptional.get().get(0).getOrderStatus().equals("I")){
                        order.setOrderStatus("S");
                        order.setErrorStr(params.get("trade_status"));
                        order.setPgTradeNo(params.get("trade_no"));

                        if (cartService.updateOrder(order))  Logger.info("支付回调订单更新payFrontNotify: "+Json.toJson(order));
                        Long userId = Long.valueOf(Json.parse(params.get("buyer_info")).get("customer_code").asText());
                        IdPlus idPlus = new IdPlus();
                        idPlus.setUserId(userId);
                        Optional<IdPlus> idPlusOptional = Optional.ofNullable(idService.getIdPlus(idPlus));
                        idPlus.setPayJdToken(params.get("token"));
                        if (idPlusOptional.isPresent()){
                            if (idService.updateIdPlus(idPlus)) Logger.info("支付成功回调更新用户Token payFrontNotify:"+Json.toJson(idPlus));
                        }else{
                            if (idService.insertIdPlus(idPlus)) Logger.info("支付成功回调创建用户Token payFrontNotify:"+Json.toJson(idPlus));
                        }
                    }else Logger.info("支付前台回调成功,非前台更新订单状态:"+order.getOrderId());

                } catch (Exception e) {
                    Logger.error("支付回调订单更新出错payFrontNotify: "+e.getMessage());
                    e.printStackTrace();
                }
                return ok(views.html.jdpaysuccess.render(params));
            } else return ok(views.html.jdpayfailed.render());
        }
    }

    /**
     * 退款接口参数配置
     *
     * @param refund   refund
     * @param req_map  请求map
     * @param body_map body map
     * @return map
     */
    private Map<String, String> payBackParams(Refund refund, Map<String, String[]> req_map, Map<String, String[]> body_map) {
        Map<String, String> params = new HashMap<>();
        if (req_map != null) {
            req_map.forEach((k, v) -> params.put(k, v[0]));
        }
        if (body_map != null)
            body_map.forEach((k, v) -> params.put(k, v[0]));

        params.put("out_trade_no", refund.getId().toString());
        params.put("original_out_trade_no", refund.getOrderId().toString());
        params.put("trade_amount", refund.getPayBackFee().multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString());
        params.put("trade_subject", refund.getReason());
        params.put("return_params", refund.getOrderId().toString());
        getBasicInfo().forEach(params::put);
        params.put("sign_data", Crypto.create_sign(params, JD_SECRET));
        return params;
    }

    /**
     * 退款接口
     * @return 返回
     */
    public F.Promise<Result> payBack() {
        Form<Refund> refundForm = Form.form(Refund.class).bindFromRequest();
        try {
            Refund refund = refundForm.get();
            Optional<List<Refund>> refundOptional = Optional.ofNullable(cartService.selectRefund(refund));
            Boolean flags;
            if (refundOptional.isPresent() && refundOptional.get().size() > 0) {
                refund.setId(refundOptional.get().get(0).getId());
                flags = cartService.updateRefund(refund);
            } else flags = cartService.insertRefund(refund);
            if (flags) {
                Map<String, String> params = payBackParams(refund, request().queryString(), request().body().asFormUrlEncoded());
                StringBuilder sb = new StringBuilder();
                params.forEach((k, v) -> {
                    sb.append(k).append("=").append(v).append("&");
                });

                return ws.url("https://cbe.wangyin.com/cashier/refund").setContentType("application/x-www-form-urlencoded").post(sb.toString()).map(wsResponse -> {
                    JsonNode response = wsResponse.asJson();
                    Refund re = new Refund();
                    re.setId(response.get("out_trade_no").asLong());
                    re.setOrderId(response.get("return_params").asLong());
                    re.setPgCode(response.get("response_code").asText());
                    re.setPgMessage(response.get("response_message").asText());
                    re.setPgTradeNo(response.get("trade_no").asText());
                    re.setState(response.get("is_success").asText());

                    if (cartService.updateRefund(re)) {
                        if (re.getState().equals("Y")) {
                            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.REFUND_SUCCESS.getIndex()), Message.ErrorCode.REFUND_SUCCESS.getIndex())));
                            return ok(result);
                        } else {
                            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.REFUND_FAILED.getIndex()), Message.ErrorCode.REFUND_FAILED.getIndex())));
                            return ok(result);
                        }
                    } else {
                        Logger.error("payBack update exception");
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
                        return ok(result);
                    }
                });
            } else return F.Promise.promise(() -> ok("db insert error"));
        } catch (Exception e) {
            e.printStackTrace();
            return F.Promise.promise(() -> ok("error"));
        }
    }

    public Result payRefund() {
        return ok(views.html.payback.render());
    }


}
