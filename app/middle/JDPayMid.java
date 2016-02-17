package middle;

import controllers.JDPay;
import domain.*;
import play.Logger;
import play.libs.Json;
import service.CartService;
import service.IdService;
import service.PromotionService;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 京东支付中间层
 * Created by howen on 16/2/17.
 */
public class JDPayMid {

    private CartService cartService;

    private IdService idService;

    private PromotionService promotionService;

    public JDPayMid(CartService cartService, IdService idService, PromotionService promotionService){
        this.cartService = cartService;
        this.idService = idService;
        this.promotionService = promotionService;
    }

    /**
     * 京东支付异步通知结果
     * @param params 参数
     */
    public String asynPay(Map<String, String> params){

        Order order = new Order();
        order.setOrderId(Long.valueOf(params.get("out_trade_no")));

        try {
            List<Order> orders = cartService.getOrderBy(order);

            if (orders.size()>0) order = orders.get(0);

            if (order.getOrderStatus().equals("S") || order.getOrderStatus().equals("PS")){
                return "success";
            }else {
                order.setOrderStatus("S");
                order.setErrorStr(params.get("trade_status"));
                order.setPgTradeNo(params.get("trade_no"));

                //如果是拼购,而且是团长发起的拼购活动,那么在支付成功后是需要创建拼购活动
                if (order.getOrderType()!=null && order.getOrderType()==2){ //1:正常购买订单，2：拼购订单
                    order.setOrderStatus("PS");
                    if (order.getPinActiveId()==null){

                        OrderLine orderLine = new OrderLine();
                        orderLine.setOrderId(order.getOrderId());
                        orderLine.setSkuType("pin");

                        List<OrderLine> orderLines = cartService.selectOrderLine(orderLine);

                        if (orderLines.size()>0) orderLine = orderLines.get(0);

                        PinTieredPrice pinTieredPrice = new PinTieredPrice();
                        pinTieredPrice.setId(orderLine.getPinTieredPriceId());
                        pinTieredPrice = promotionService.getTieredPriceById(pinTieredPrice);


                        PinActivity activity  = new PinActivity();
                        activity.setJoinPersons(1);
                        activity.setMasterUserId(order.getUserId());
                        activity.setPersonNum(pinTieredPrice.getPeopleNum());
                        activity.setPinPrice(pinTieredPrice.getPrice());
                        activity.setPinId(orderLine.getSkuTypeId());
                        activity.setStatus("Y");
                        activity.setEndAt(new Timestamp(new Date().getTime()+ JDPay.PIN_MILLISECONDS));
                        if (promotionService.insertPinActivity(activity)){
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
                    }else{
                        PinActivity activity  = new PinActivity();
                        activity.setPinActiveId(order.getPinActiveId());
                        activity.setJoinPersons(activity.getJoinPersons()+1);
                        if (promotionService.updatePinActivity(activity)){
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
                    }
                }

                if (cartService.updateOrder(order)){
                    Logger.info("京东支付回调订单更新payFrontNotify: " + Json.toJson(order));
                    if (params.containsKey("token")) {
                        Long userId = Long.valueOf(Json.parse(params.get("buyer_info")).get("customer_code").asText());
                        IdPlus idPlus = new IdPlus();
                        idPlus.setUserId(userId);
                        Optional<IdPlus> idPlusOptional = Optional.ofNullable(idService.getIdPlus(idPlus));
                        idPlus.setPayJdToken(params.get("token"));
                        if (idPlusOptional.isPresent()) {
                            if (idService.updateIdPlus(idPlus)){
                                Logger.info("京东支付成功回调更新用户Token payFrontNotify:" + Json.toJson(idPlus));
                                return "success";
                            }else return "error";
                        } else {
                            if (idService.insertIdPlus(idPlus)){
                                Logger.info("京东支付成功回调创建用户Token payFrontNotify:" + Json.toJson(idPlus));
                                return "success";
                            }else return "error";
                        }
                    }else return "error";
                }else return "error";
            }
        } catch (Exception e) {
            Logger.error("支付回调订单更新出错payFrontNotify: " + e.getMessage());
            e.printStackTrace();
            return "error";
        }
    }

    public String asynRefund(Map<String, String> params){
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
}
