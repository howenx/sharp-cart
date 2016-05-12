package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.google.inject.Singleton;
import domain.*;
import filters.UserAuth;
import middle.JDPayMid;
import modules.NewScheduler;
import net.spy.memcached.MemcachedClient;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.Logger;
import play.Play;
import play.data.Form;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import scala.concurrent.duration.FiniteDuration;
import service.CartService;
import service.IdService;
import service.PromotionService;
import util.CalCountDown;
import util.Crypto;
import util.SysParCom;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.concurrent.TimeUnit.HOURS;
import static util.SysParCom.*;

/**
 * Created by handy on 15/12/16.
 * kakao china
 */
@Singleton
public class JDPay extends Controller {

    private CartService cartService;

    private IdService idService;

    private ActorRef cancelOrderActor;

    private ActorRef pinFailActor;

    private PromotionService promotionService;

    @Inject
    private JDPayMid jdPayMid;

    @Inject
    WSClient ws;

    @Inject
    ActorSystem system;

    @Inject
    NewScheduler newScheduler;

    @Inject
    private MemcachedClient cache;


    @Inject
    AlipayCtrl alipayCtrl;

    @Inject
    public JDPay(CartService cartService, IdService idService, PromotionService promotionService, @Named("cancelOrderActor") ActorRef cancelOrderActor, @Named("pinFailActor") ActorRef pinFailActor) {
        this.cartService = cartService;
        this.idService = idService;
        this.cancelOrderActor = cancelOrderActor;
        this.promotionService = promotionService;
        this.pinFailActor = pinFailActor;
    }

    @Security.Authenticated(UserAuth.class)
    public Result cashDesk(Long orderId, String paySrc) {

        Map<String, String> params_failed = new HashMap<>();
        params_failed.put("m_index", M_INDEX);

        try {
            Long userId = (Long) ctx().args.get("userId");
            Order order = new Order();
            order.setOrderId(orderId);
            order.setUserId(userId);
            Optional<List<Order>> listOptional = Optional.ofNullable(cartService.getOrder(order));
            if (listOptional.isPresent() && listOptional.get().size() > 0) {
                order = listOptional.get().get(0);
                Optional<Long> longOptional = Optional.ofNullable(CalCountDown.getTimeSubtract(order.getOrderCreateAt()));
                if (longOptional.isPresent() && longOptional.get() < 0) {
                    cancelOrderActor.tell(orderId, null);
                    Logger.error("order timeout:" + order.getOrderId());
                    return ok(views.html.jdpayfailed.render(params_failed));
                } else {
                    SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 格式化时间
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(d.parse(order.getOrderCreateAt()));
                    Map<String, String> params = getParams(calendar.getTimeInMillis(), request().queryString(), request().body().asFormUrlEncoded(), userId, orderId);
                    String userType = "JD";
                    if (ctx().request().getHeader("User-Agent").contains("MicroMessenger")) {
                        userType = "WEIXIN"; //微信公众号支付
                    }
                    String token = (String) ctx().flash().get("id-token");

                    String securityCode = orderSecurityCode(orderId + "", token);

                    return ok(views.html.cashdesk.render(params, paySrc, userType, token, securityCode));
                }
            } else return ok(views.html.jdpayfailed.render(params_failed));
        } catch (Exception ex) {
            Logger.error("settle: " + ex.getMessage());
            ex.printStackTrace();
            return ok(views.html.jdpayfailed.render(params_failed));
        }
    }

    /**
     * 订单加密
     *
     * @param orderId
     * @param token
     * @return
     */
    public String orderSecurityCode(String orderId, String token) {
        Map<String, String> map = new TreeMap<>();
        map.put("orderId", orderId);
        map.put("token", token);
        try {
            return Crypto.getSignature(map, "HMM");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
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
    private Map<String, String> getParams(Long timeInMillis, Map<String, String[]> req_map, Map<String, String[]> body_map, Long userId, Long orderId) throws Exception {
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
            params.put("orderCreateAt", String.valueOf(timeInMillis));
            params.put("url", JD_PAY_URL);
            params.put("m_index", M_INDEX);
            params.put("m_orders", M_ORDERS);
            params.put("sign_data", Crypto.create_sign(params, SysParCom.JD_SECRET));
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
        Optional<List<Order>> listOptional = Optional.ofNullable(cartService.getOrder(order));

        IdPlus idPlus = new IdPlus();
        idPlus.setUserId(userId);
        Optional<IdPlus> idPlusOptional = Optional.ofNullable(idService.getIdPlus(idPlus));

        if (listOptional.isPresent() && listOptional.get().size() > 0) {
            order = listOptional.get().get(0);
            map.put("out_trade_no", orderId.toString());
            map.put("return_params", orderId.toString());//成功支付,或者查询时候,返回订单编号
            map.put("trade_subject", "韩秘美-订单编号" + orderId);
            if (ONE_CENT_PAY) {
                map.put("trade_amount", "1");
            } else {
                map.put("trade_amount", order.getPayTotal().multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_DOWN).toPlainString());
            }

            Logger.error("总订单金额为——-->" + map.get("trade_amount"));


            //自用字断
            map.put("all_fee", order.getPayTotal().stripTrailingZeros().toPlainString());
            if (idPlusOptional.isPresent() && idPlusOptional.get().getPayJdToken() != null) {
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

            BigDecimal allSubFee = BigDecimal.ZERO;

            if (orderSplitList.isPresent()) {
                List<Map<String, String>> subInfo = new ArrayList<>();
                for (int i = 0; i < orderSplitList.get().size(); i++) {
                    OrderSplit orderSp = orderSplitList.get().get(i);


                    Map<String, String> subOrderMap = new HashMap<>();
                    OrderLine orderLine = new OrderLine();
                    orderLine.setOrderId(orderId);
                    orderLine.setSplitId(orderSp.getSplitId());
                    subOrderMap.put("sub_order_no", orderSp.getSplitId().toString());
                    subOrderMap.put("sub_order_name", "韩秘美-子订单号" + orderSp.getSplitId());

                    if (ONE_CENT_PAY) {
                        subOrderMap.put("sub_order_amount", String.valueOf(1));
                    } else {
                        subOrderMap.put("sub_order_amount", orderSp.getTotalPayFee().multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_DOWN).toPlainString());
                        if (i == orderSplitList.get().size() - 1) {
                            subOrderMap.put("sub_order_amount", order.getPayTotal().subtract(allSubFee).multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_DOWN).toPlainString());
                        }

                        allSubFee = allSubFee.add(orderSp.getTotalPayFee());
                    }

                    Logger.error("子订单金额为——-->" + subOrderMap.get("sub_order_amount"));

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

        params.put("customer_no", SysParCom.JD_SELLER);
        params.put("notify_url", SysParCom.SHOPPING_URL + "/client/pay/jd/back");
        params.put("request_datetime", req_date);
        params.put("return_url", SysParCom.SHOPPING_URL + "/client/pay/jd/front");
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
            //异步通知支付接口
            if (params.containsKey("trade_class") && params.get("trade_class").equals("SALE")) {
                if (params.containsKey("out_trade_no") && params.containsKey("trade_no") && params.containsKey("trade_status") && params.get("trade_status").equals("FINI")) {
                    Logger.info("京东支付异步通知数据: " + params.toString());
                    if (jdPayMid.asynPay(params).equals("success")) {
                        Order order = new Order();
                        order.setOrderId(Long.valueOf(params.get("out_trade_no")));
                        try {
                            List<Order> orders = cartService.getOrder(order);
                            if (orders.size() > 0) {
                                order = orders.get(0);
                                if (order.getOrderType() != null && order.getOrderType() == 2) { //1:正常购买订单，2：拼购订单
                                    if (dealPinActivity(params, order) == null) {
                                        Logger.error("************京东支付异步通知 拼购订单返回处理结果为空************," + order.getOrderId());
                                        return ok("error");
                                    } else {
                                        Logger.error("************京东支付异步通知 拼购订单返回成功************," + order.getOrderId());
                                        return ok("success");
                                    }
                                } else {
                                    Logger.error("************京东支付异步通知 普通订单返回成功************," + order.getOrderId());
                                    return ok("success");
                                }
                            } else {
                                Logger.error("************京东支付异步通知 订单未找到************," + order.getOrderId());
                                return ok("error");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return ok("error");
                        }
                    } else {
                        Logger.error("************京东支付异步通知 异步方法调用返回失败************," + params.get("out_trade_no"));
                        return ok("error");
                    }
                } else {
                    Logger.error("支付回调参数校验失败或者支付状态有误: " + params.toString());
                    return ok("error");
                }
            }
            //异步通知退款接口
            else if (params.containsKey("trade_class") && params.get("trade_class").equals("REFD")) {
                Logger.info("京东退款异步通知数据: " + params.toString());
                if (params.containsKey("out_trade_no") && params.containsKey("trade_no") && params.containsKey("trade_status") && params.get("trade_status").equals("ACSU")) {
                    return ok(jdPayMid.asynRefund(params));
                } else {
                    Logger.error("退款回调参数校验失败或者退款状态有误: " + params.toString());
                    return ok("error");
                }
            } else {
                Logger.error("京东交易回传参数有误交易类型无法识别: " + params.toString());
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
        params.put("m_index", M_INDEX);
        params.put("m_orders", M_ORDERS);

        if (!sign.equalsIgnoreCase(_sign)) {
            return ok(views.html.jdpayfailed.render(params));
        } else {
            if (params.containsKey("out_trade_no") && params.containsKey("token") && params.containsKey("trade_no") && params.containsKey("trade_status") && params.get("trade_status").equals("FINI")) {
                //需要先判断订单状态,如果是S状态就去调用否则不能2次调用,而且如果订单是被更新为PF状态,那么就需要前段返回到拼购失败页面
                if (jdPayMid.asynPay(params).equals("success")) {
                    Order order = new Order();
                    order.setOrderId(Long.valueOf(params.get("out_trade_no")));

                    try {
                        List<Order> orders = cartService.getOrder(order);
                        if (orders.size() > 0) {
                            order = orders.get(0);
                            if (order.getOrderType() != null && order.getOrderType() == 2) { //1:正常购买订单，2：拼购订单
                                if (dealPinActivity(params, order) == null)
                                    return ok(views.html.jdpayfailed.render(params));
                                else return ok(views.html.pin.render(params));
                            } else return ok(views.html.jdpaysuccess.render(params));
                        } else return ok(views.html.jdpayfailed.render(params));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ok(views.html.jdpayfailed.render(params));
                    }
                } else return ok(views.html.jdpayfailed.render(params));
            } else return ok(views.html.jdpayfailed.render(params));
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
    public static Map<String, String> payBackParams(Refund refund, Map<String, String[]> req_map, Map<String, String[]> body_map) {
        Map<String, String> params = new HashMap<>();
        if (req_map != null) {
            req_map.forEach((k, v) -> params.put(k, v[0]));
        }
        if (body_map != null)
            body_map.forEach((k, v) -> params.put(k, v[0]));

        params.put("out_trade_no", refund.getId().toString());
        params.put("original_out_trade_no", refund.getOrderId().toString());

        if (ONE_CENT_PAY) {
            params.put("trade_amount", "1");
        } else
            params.put("trade_amount", refund.getPayBackFee().multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString());


        params.put("trade_subject", refund.getReason());
        params.put("return_params", refund.getId().toString());
        getBasicInfo().forEach(params::put);
        params.put("sign_data", Crypto.create_sign(params, SysParCom.JD_SECRET));
        return params;
    }


    public Map<String, String> dealPinActivity(Map<String, String> params, Order order) throws Exception {
        Boolean newCreatePin = false;
        List<PinUser> pinUsers;
        PinUser pinUser = new PinUser();
        pinUser.setUserId(order.getUserId());

        if (order.getPinActiveId() != null) {
            pinUser.setPinActiveId(order.getPinActiveId());
            pinUsers = promotionService.selectPinUser(pinUser);
            if (pinUsers.size() == 0) {
                newCreatePin = true;
                if (!jdPayMid.pinActivityDeal(order).equals("success"))
                    return null;
                pinUser.setPinActiveId(order.getPinActiveId());
                pinUsers = promotionService.selectPinUser(pinUser);
            }
        } else {
            newCreatePin = true;
            if (!jdPayMid.pinActivityDeal(order).equals("success"))
                return null;
            pinUser.setPinActiveId(order.getPinActiveId());
            pinUsers = promotionService.selectPinUser(pinUser);
        }

        PinActivity activity = promotionService.selectPinActivityById(order.getPinActiveId());

        if (newCreatePin) {
            if (activity.getJoinPersons().equals(activity.getPersonNum())) {
                jdPayMid.pinPushMsg(activity, PIN_SUCCESS_MSG, null);
            }
            if (pinUsers.size() > 0) {
                pinUser = pinUsers.get(0);
                if (pinUser.isOrMaster()) {
                    newScheduler.scheduleOnce(FiniteDuration.create(24, HOURS), pinFailActor, order.getPinActiveId());
                } else if (activity.getJoinPersons() < activity.getPersonNum()) {
                    jdPayMid.pinPushMsg(activity, PIN_ADD_MSG, pinUser.getId());
                }
            }
        }

        if (pinUsers.size() > 0) {
            pinUser = pinUsers.get(0);

            if (pinUser.isOrMaster()) {
                params.put("pinActivity", SysParCom.PROMOTION_URL + "/promotion/pin/activity/pay/" + order.getPinActiveId() + "/1");
                params.put("m_pinActivity", M_PIN + order.getPinActiveId() + "/1");
            } else {
                params.put("pinActivity", SysParCom.PROMOTION_URL + "/promotion/pin/activity/pay/" + order.getPinActiveId() + "/2");
                params.put("m_pinActivity", M_PIN + order.getPinActiveId() + "/2");
            }
        }
        return params;
    }

    public Result redirectCash() {
        Form<RedirectCash> redirectCashForm = Form.form(RedirectCash.class).bindFromRequest();
        Map<String, String> params_failed = new HashMap<>();
        params_failed.put("m_index", M_INDEX);
        if (redirectCashForm.hasErrors()) {
            return ok(views.html.jdpayfailed.render(params_failed));
        } else {
            RedirectCash redirectCash = redirectCashForm.get();
            flash().put("id-token", redirectCash.getToken());
            return redirect("/client/pay/order/get/" + redirectCash.getOrderId() + "/" + redirectCash.getPaySrc());
        }
    }

}
