package middle;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import common.MsgTypeEnum;
import controllers.MsgCtrl;
import controllers.PushCtrl;
import domain.*;
import modules.SysParCom;
import net.spy.memcached.MemcachedClient;
import play.Logger;
import play.libs.Json;
import service.CartService;
import service.IdService;
import service.PromotionService;

import javax.inject.Inject;
import javax.inject.Named;
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
    private MemcachedClient cache;

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

            if (order.getOrderStatus().equals("S") || order.getOrderStatus().equals("PS") || order.getOrderStatus().equals("PF")|| order.getOrderStatus().equals("F")) {
                return "success";
            } else {
                order.setOrderStatus("S");
                order.setErrorStr(params.get("trade_status"));
                order.setPgTradeNo(params.get("trade_no"));

                if (cartService.updateOrder(order)) {
                    Logger.info("京东支付回调订单更新payFrontNotify: " + Json.toJson(order));
                    if (params.containsKey("token")) {
                        Long userId = Long.valueOf(Json.parse(params.get("buyer_info")).get("customer_code").asText());
                        IdPlus idPlus = new IdPlus();
                        idPlus.setUserId(userId);
                        Optional<IdPlus> idPlusOptional = Optional.ofNullable(idService.getIdPlus(idPlus));
                        idPlus.setPayJdToken(params.get("token"));
                        if (idPlusOptional.isPresent()) {
                            if (idService.updateIdPlus(idPlus)) {
                                Logger.info("京东支付成功回调更新用户Token payFrontNotify:" + Json.toJson(idPlus));
                                return "success";
                            } else return "error";
                        } else {
                            if (idService.insertIdPlus(idPlus)) {
                                Logger.info("京东支付成功回调创建用户Token payFrontNotify:" + Json.toJson(idPlus));
                                return "success";
                            } else return "error";
                        }
                    } else return "error";
                } else return "error";
            }
        } catch (Exception e) {
            Logger.error("支付回调订单更新出错payFrontNotify: " + e.getMessage());
            e.printStackTrace();
            return "error";
        }
    }

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
                    refundActor.tell(refund,ActorRef.noSender());

                    //更新订单
                    cartService.updateOrder(order);
                    return "error";

                }else{
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
            Logger.error("refundBack update exception " + e.getMessage());
            return "error";
        }
    }


    /**
     * 拼团成功消息推送
     * @param activity activity
     *
     */
    public void pinPushMsg(PinActivity activity){
        PinSku pinSku = promotionService.getPinSkuById(activity.getPinId());

        JsonNode js_invImg = Json.parse(pinSku.getPinImg());
        if (js_invImg.has("url")) {
            PinUser pinUser = new PinUser();
            pinUser.setPinActiveId(activity.getPinActiveId());
            List<PinUser> pinUsers = promotionService.selectPinUser(pinUser);
            for (PinUser p:pinUsers){
                //发消息
                msgCtrl.addMsgRec(p.getUserId(), MsgTypeEnum.Goods,"拼团成功啦,快去看看",pinSku.getPinTitle(),js_invImg.get("url").asText(),"/promotion/pin/activity/" + activity.getPinActiveId(),"V");
                //推送消息
                Map<String,String> map = new HashMap<>();
                map.put("targetType","V");
                map.put("url",SysParCom.PROMOTION_URL+"/promotion/pin/activity/" + activity.getPinActiveId());
                pushCtrl.send_push_android_and_ios_alias("拼团成功啦,快去看看",null,map,cache.get(p.getUserId().toString()).toString());
            }
        }
    }
}
