package middle;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import common.MsgTypeEnum;
import controllers.Application;
import controllers.MsgCtrl;
import controllers.PushCtrl;
import domain.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import service.CartService;
import service.IdService;
import service.PromotionService;
import util.Crypto;
import util.SysParCom;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * 京东支付中间层
 * Created by howen on 16/2/17.
 */
public class JDPayMid {

    @Inject
    private CartService cartService;

    @Inject
    private IdService idService;

    @Inject
    private PromotionService promotionService;

    @Inject
    @Named("refundActor")
    private ActorRef refundActor;

    @Inject
    private MsgCtrl msgCtrl;

    @Inject
    private PushCtrl pushCtrl;


    @Inject
    private Configuration configuration;

    @Inject
    private ActorSystem system;


    /**
     * 京东支付异步通知结果
     *
     * @param params 参数
     */
    public String asynPay(Map<String, String> params) {

        Order order = new Order();
        order.setOrderId(Long.valueOf(params.get("out_trade_no")));

        try {
            List<Order> orders = cartService.getOrder(order);

            if (orders.size() > 0) order = orders.get(0);

            //支付回调更新用户token
            if (params.containsKey("token") && params.containsKey("buyer_info") && Json.parse(params.get("buyer_info")).get("customer_code") != null) {
                Long userId = Long.valueOf(Json.parse(params.get("buyer_info")).get("customer_code").asText());
                IdPlus idPlus = new IdPlus();
                idPlus.setUserId(userId);
                Optional<IdPlus> idPlusOptional = Optional.ofNullable(idService.getIdPlus(idPlus));
                idPlus.setPayJdToken(params.get("token"));
                if (idPlusOptional.isPresent()) {
                    if (idService.updateIdPlus(idPlus)) {
                        Logger.info("京东支付成功回调更新用户Token:" + Json.toJson(idPlus));
                    } else {
                        Logger.info("asynPay更新用户token失败");
                    }
                } else {
                    if (idService.insertIdPlus(idPlus)) {
                        Logger.info("京东支付成功回调创建用户Token:" + Json.toJson(idPlus));
                    } else {
                        Logger.error("asynPay插入用户token失败," + order.getOrderId());
                    }
                }
            }

            if (order.getOrderStatus().equals("S") || order.getOrderStatus().equals("PS") || order.getOrderStatus().equals("PF") || order.getOrderStatus().equals("F")) {
                return "success";
            } else {
                order.setOrderStatus("S");
                order.setErrorStr(params.get("trade_status"));
                order.setPgTradeNo(params.get("trade_no"));
                order.setPayMethod("JD");

                if (cartService.updateOrder(order)) {

                    Logger.info("京东支付回调订单更新订单信息: " + Json.toJson(order));
//                    system.actorSelection(ERP_PUSH).tell(order.getOrderId(), ActorRef.noSender());
//                    Logger.info("调用ERP推送订单:" + order.getOrderId());

                    Logger.info("京东支付后端回调返回成功," + order.getOrderId());
                    return "success";

                } else {
                    Logger.error("asynPay更新订单状态失败," + order.getOrderId());
                    return "error";
                }
            }
        } catch (Exception e) {
            Logger.error("asynPay支付回调订单出错: " + Throwables.getStackTraceAsString(e));
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * 支付宝和微信支付异步通知
     *
     * @param orderParams orderParams
     * @return String
     */
    public String asynPay(Order orderParams) {
        Order order = new Order();
        order.setOrderId(orderParams.getOrderId());

        try {
            List<Order> orders = cartService.getOrder(order);

            if (orders.size() > 0) order = orders.get(0);

            if (order.getOrderStatus().equals("S") || order.getOrderStatus().equals("PS") || order.getOrderStatus().equals("PF") || order.getOrderStatus().equals("F")) {
                return "success";
            } else {
                order.setOrderStatus("S");
                order.setPayMethod(orderParams.getPayMethod());
                if (null != orderParams.getPayMethodSub()) {
                    order.setPayMethodSub(orderParams.getPayMethodSub());
                }
                order.setErrorStr(orderParams.getErrorStr());
                order.setPgTradeNo(orderParams.getPgTradeNo());

                if (cartService.updateOrder(order)) {
                    Logger.info("异步通知,支付回调订单更新订单信息: " + Json.toJson(order));
//                    system.actorSelection(ERP_PUSH).tell(order.getOrderId(), ActorRef.noSender());
//                    Logger.info("调用ERP推送订单:" + order.getOrderId());

                    Logger.info("异步通知,支付后端回调返回成功," + order.getOrderId());
                    return "success";

                } else {
                    Logger.error("asynPayWeixin更新订单状态失败," + order.getOrderId());
                    return "error";
                }
            }
        } catch (Exception e) {
            Logger.error("asynPayWeixin支付回调订单出错: " + Throwables.getStackTraceAsString(e));
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * 拼购支付成功后做相应处理
     *
     * @param order order
     * @return String
     * @throws Exception
     */
    public String pinActivityDeal(Order order) throws Exception {
        //如果是拼购,而且是团长发起的拼购活动,那么在支付成功后是需要创建拼购活动
        if (order.getOrderType() != null && order.getOrderType() == 2 && !order.getOrderStatus().equals("PS") && !order.getOrderStatus().equals("PF") && !order.getOrderStatus().equals("F") && !order.getOrderStatus().equals("T")) { //1:正常购买订单，2：拼购订单
            order.setOrderStatus("PS");
            if (order.getPinActiveId() == null) {

                OrderLine orderLine = new OrderLine();
                orderLine.setOrderId(order.getOrderId());
                orderLine.setSkuType("pin");

                List<OrderLine> orderLines = cartService.selectOrderLine(orderLine);

                if (orderLines.size() > 0) orderLine = orderLines.get(0);

                PinTieredPrice pinTieredPrice = new PinTieredPrice();
                pinTieredPrice.setId(orderLine.getPinTieredPriceId());
                pinTieredPrice = promotionService.getTieredPriceById(pinTieredPrice);


                PinActivity activity = new PinActivity();
                activity.setJoinPersons(1);
                activity.setMasterUserId(order.getUserId());
                activity.setPersonNum(pinTieredPrice.getPeopleNum());
                activity.setPinPrice(pinTieredPrice.getPrice());
                activity.setPinId(orderLine.getSkuTypeId());
                activity.setStatus("Y");
                activity.setEndAt(new Timestamp(new Date().getTime() + SysParCom.PIN_MILLISECONDS));
                activity.setPinTieredId(pinTieredPrice.getId());

                if (promotionService.insertPinActivity(activity)) {
                    order.setPinActiveId(activity.getPinActiveId());
                    PinUser pinUser = new PinUser();
                    pinUser.setOrMaster(true);
                    pinUser.setOrRobot(false);
                    pinUser.setPinActiveId(activity.getPinActiveId());
                    pinUser.setUserId(order.getUserId());
                    pinUser.setUserIp(order.getOrderIp());
                    pinUser.setUserImg(idService.getID(order.getUserId()).getPhotoUrl());
                    promotionService.insertPinUser(pinUser);
                }
                cartService.updateOrder(order);
                return "success";

            } else {
                PinActivity activity = promotionService.selectPinActivityById(order.getPinActiveId());
                if (!activity.getStatus().equals("Y")) {//如果拼购活动已经失败或者拼购活动已经结束
                    order.setPinActiveId(activity.getPinActiveId());
                    order.setOrderStatus("F");
                    PinUser pinUser = new PinUser();
                    pinUser.setOrMaster(false);
                    pinUser.setOrRobot(false);
                    pinUser.setPinActiveId(activity.getPinActiveId());
                    pinUser.setUserId(order.getUserId());
                    pinUser.setUserIp(order.getOrderIp());
                    pinUser.setUserImg(idService.getID(order.getUserId()).getPhotoUrl());
                    promotionService.insertPinUser(pinUser);

                    Refund refund = new Refund();
                    refund.setAmount(order.getOrderAmount());
                    refund.setOrderId(order.getOrderId());
                    refund.setPayBackFee(order.getPayTotal());
                    refund.setReason("拼团支付失败退款");
                    refund.setRefundType("pin");

                    //自动退款
                    refundActor.tell(refund, ActorRef.noSender());

                    //更新订单
                    cartService.updateOrder(order);
                    return "error";

                } else {
                    activity.setJoinPersons(activity.getJoinPersons() + 1);
                    if (activity.getJoinPersons().equals(activity.getPersonNum())) {//成团
                        activity.setStatus("C");
                        order.setOrderStatus("S");
                        Order order1 = new Order();
                        order1.setPinActiveId(activity.getPinActiveId());
                        List<Order> orders1 = cartService.getPinOrder(order1);
                        for (Order order2 : orders1) {
                            order2.setOrderStatus("S");
                            cartService.updateOrder(order2);
                        }
                    }
                    if (promotionService.updatePinActivity(activity)) {

                        order.setPinActiveId(activity.getPinActiveId());
                        PinUser pinUser = new PinUser();
                        pinUser.setOrMaster(false);
                        pinUser.setOrRobot(false);
                        pinUser.setPinActiveId(activity.getPinActiveId());
                        pinUser.setUserId(order.getUserId());
                        pinUser.setUserIp(order.getOrderIp());
                        pinUser.setUserImg(idService.getID(order.getUserId()).getPhotoUrl());
                        promotionService.insertPinUser(pinUser);
                    }
                    cartService.updateOrder(order);
                    return "success";
                }
            }
        } else return "success";
    }


    /**
     * 京东支付退款异步通知
     *
     * @param params Map
     * @return String
     */
    public String asynRefund(Map<String, String> params) {
        Refund re = new Refund();
        re.setId(Long.valueOf(params.get("return_params")));
        re.setOrderId(Long.valueOf(params.get("out_trade_no")));
        re.setPgCode(params.get("trade_status"));
        re.setPgMessage(params.get("trade_subject"));
        re.setPgTradeNo(params.get("trade_no"));
        if (params.get("trade_status").equals("ACSU"))
            re.setState("Y");
        else re.setState("N");

        try {
            if (cartService.updateRefund(re)) {
                return "success";
            } else {
                Logger.error("refundBack update exception");
                return "error";
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("refundBack update exception " + Throwables.getStackTraceAsString(e));
            return "error";
        }
    }


    /**
     * 拼团成功消息推送
     *
     * @param activity activity
     */
    public void pinPushMsg(PinActivity activity, String message, Long pinUserId) {
        PinSku pinSku = promotionService.getPinSkuById(activity.getPinId());

        JsonNode js_invImg = Json.parse(pinSku.getPinImg());
        if (js_invImg.has("url")) {
            PinUser pinUser = new PinUser();
            pinUser.setPinActiveId(activity.getPinActiveId());
            List<PinUser> pinUsers = promotionService.selectPinUser(pinUser);
            for (PinUser p : pinUsers) {
                if (pinUserId == null || !pinUserId.equals(p.getId())) {
                    //发消息
                    msgCtrl.addMsgRec(p.getUserId(), MsgTypeEnum.Goods, message, pinSku.getPinTitle(), js_invImg.get("url").asText(), "/promotion/pin/activity/" + activity.getPinActiveId(), "V");
                    //推送消息
                    Map<String, String> map = new HashMap<>();
                    map.put("targetType", "V");
                    map.put("url", SysParCom.PROMOTION_URL + "/promotion/pin/activity/" + activity.getPinActiveId());
                    pushCtrl.send_push_android_and_ios_alias(message, null, SysParCom.PUSH_TIME_TO_LIVE, map, p.getUserId().toString());
                }
            }
        }
    }

    /**
     * 获取京东订单的报关参数
     *
     * @param splitId 子订单ID
     * @return map
     */

    public Map<String, String> getCustomsBasicInfo(Long splitId) {

        OrderSplit ordersplit = new OrderSplit();
        ordersplit.setOrderId(splitId);

        try {
            List<OrderSplit> orders = cartService.selectOrderSplit(ordersplit);
            if (orders.size() > 0) ordersplit = orders.get(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.error(Throwables.getStackTraceAsString(ex));
        }

        Map<String, String> params = new HashMap<>();

        DateTimeFormatter f = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss");
        String req_date = f.print(new DateTime());
        String sign_type = "MD5";

        params.put("customer_no", SysParCom.JD_SELLER);
        params.put("request_datetime", req_date);
        params.put("sign_type", sign_type);
        params.put("custom", ordersplit.getCbeCode());

        params.put("tax_fee", ordersplit.getPostalFee().multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString());

        params.put("goods_fee", ordersplit.getTotalPayFee().multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString());

        params.put("freight", ordersplit.getShipFee().multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString());

        //tax_fee       //税款金额，单位：分，默认为0分
        //goods_fee     //货款金额，单位：分，默认与子订单支付时金额相同
        //freight       //运费金额，单位：分，默认为0分
        //other_fee     //其它费用金额，单位：分，默认为0分
        //biz_type      //业务类型，重庆海关报送时必填

        Map<String, String> customs = Application.mapper.convertValue(configuration.getObject(ordersplit.getCbeCode()), Application.mapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        if (customs.size() > 0) params.putAll(customs);

        params.put("sign_data", Crypto.create_sign(params, SysParCom.JD_SECRET));

        return params;
    }


    public Map<String, String> getCustomsQueryInfo(Long splitId) {

        OrderSplit ordersplit = new OrderSplit();
        ordersplit.setOrderId(splitId);

        try {
            List<OrderSplit> orders = cartService.selectOrderSplit(ordersplit);
            if (orders.size() > 0) ordersplit = orders.get(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.error(Throwables.getStackTraceAsString(ex));
        }

        Map<String, String> params = new HashMap<>();

        DateTimeFormatter f = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss");
        String req_date = f.print(new DateTime());
        String sign_type = "MD5";

        params.put("customer_no", SysParCom.JD_SELLER);
        params.put("request_datetime", req_date);
        params.put("sign_type", sign_type);
        params.put("out_trade_no", ordersplit.getOrderId().toString());

        params.put("sub_order_no", ordersplit.getSplitId().toString());

        params.put("sub_out_trade_no", ordersplit.getSubPgTradeNo());

        params.put("sign_data", Crypto.create_sign(params, SysParCom.JD_SECRET));

        return params;
    }
}
