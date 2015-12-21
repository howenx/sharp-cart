package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;
import domain.*;
import filters.UserAuth;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.Logger;
import play.Play;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import service.CartService;
import service.IdService;
import util.Crypto;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;


/**
 * Created by handy on 15/12/16.
 * kakao china
 */
@Singleton
public class JDPay extends Controller {

    private CartService cartService;

    private IdService idService;

    //shopping服务器url
    private static final String SHOPPING_URL = play.Play.application().configuration().getString("shopping.server.url");

    private static final String JD_SECRET = Play.application().configuration().getString("jd_secret");

    private static final String JD_SELLER = Play.application().configuration().getString("jd_seller");

    static final ObjectNode result = Json.newObject();

    @Inject
    public JDPay(CartService cartService, IdService idService) {
        this.cartService = cartService;
        this.idService = idService;
    }

    /**
     * 支付调用
     * @param orderId 订单ID
     * @return 跳转到京东支付页面
     */
    @Security.Authenticated(UserAuth.class)
    public Result payOrderWeb(Long orderId){

        try {
            Map<String, String> params = getParams(request().queryString(),request().body().asFormUrlEncoded(),(Long) ctx().args.get("userId"),orderId);
            return ok(views.html.cashdesk.render(params));
        } catch (Exception ex) {
            Logger.error("settle: " + ex.getMessage());
            ex.printStackTrace();
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ERROR.getIndex()), Message.ErrorCode.ERROR.getIndex())));
            return ok(result);
        }
    }

    /**
     * 获取签名参数
     * @param req_map 请求参数
     * @param body_map  请求参数
     * @param userId    用户ID
     * @param orderId   订单ID
     * @return  map
     * @throws Exception
     */
    private Map<String, String> getParams(Map<String, String[]> req_map,Map<String, String[]> body_map,Long userId, Long orderId) throws Exception{
        Map<String, String> params = new HashMap<>();
        if (req_map != null) {
            req_map.forEach((k, v) -> params.put(k, v[0]));
        }
        if (body_map != null)
            body_map.forEach((k, v) -> params.put(k, v[0]));

        Optional<Map<String,String>> optionalOrderInfo = Optional.ofNullable(getOrderInfo(userId,orderId));

        if (optionalOrderInfo.isPresent()){
            optionalOrderInfo.get().forEach(params::put);
            getBasicInfo().forEach(params::put);
            params.put("sign_data", Crypto.create_sign(params, JD_SECRET));
            return params;
        }else return null;
    }


    /***
     * 支付订单参数配置
     * @param userId 用户ID
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
                map.put("sub_order_info",Json.stringify(Json.toJson(subInfo)));
            }
            return map;
        }else return null;
    }

    private static Map<String, String> getBasicInfo(){
        Map<String, String> params = new HashMap<>();

        DateTimeFormatter f = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss");
        String req_date = f.print(new DateTime());
        String sign_type = "MD5";
        String trade_currency = "CNY";
        String settle_currency = "USD";

        params.put("customer_no", JD_SELLER);
        params.put("notify_url", SHOPPING_URL+"/client/pay/jd/back");
        params.put("request_datetime", req_date);
        params.put("return_url",  SHOPPING_URL+"/client/pay/jd/front");
        params.put("settle_currency", settle_currency);
        params.put("trade_currency", trade_currency);
        params.put("sign_type", sign_type);
        return params;
    }

    public Result payBackendNotify() {
        Map<String, String[]> body_map = request().body().asFormUrlEncoded();
        Map<String, String> params = new HashMap<>();
        body_map.forEach((k, v) -> params.put(k, v[0]));
        String sign = params.get("sign_data");
        String secret = Play.application().configuration().getString("jd_secret");

        String _sign = Crypto.create_sign(params,secret);
        if(!sign.equalsIgnoreCase(_sign)) {
            //error
            return ok("通知失败，签名失败！");
        }

        //update order status .

        return ok("SUCCESS");
    }

    public Result payFrontNotify() {
        Map<String, String[]> body_map = request().body().asFormUrlEncoded();
        Map<String, String> params = new HashMap<>();
        body_map.forEach((k, v) -> params.put(k, v[0]));
        String sign = params.get("sign_data");
        String secret = Play.application().configuration().getString("jd_secret");
        String _sign = Crypto.create_sign(params,secret);
        Logger.error("支付成功返回数据: "+Json.toJson(params));

        if (!sign.equalsIgnoreCase(_sign)) {
            return ok("error page");
        }else {
            if (params.containsKey("out_trade_no") && params.containsKey("token") && params.containsKey("trade_no") && params.containsKey("trade_status") && params.get("trade_status").equals("FINI")){
                Order order = new Order();
                order.setOrderId(Long.valueOf(params.get("out_trade_no")));
                order.setOrderStatus("S");
                order.setErrorStr(params.get("trade_status"));
                order.setPgTradeNo(params.get("trade_no"));
                try {
                    if (cartService.updateOrder(order)) Logger.debug("支付回调订单更新payFrontNotify: "+Json.toJson(order));
                    Long userId = Long.valueOf(Json.parse(params.get("buyer_info")).get("customer_code").asText());
                    IdPlus idPlus = new IdPlus();
                    idPlus.setUserId(userId);
                    idPlus.setPayJdToken(params.get("token"));
                    if (idService.updateIdPlus(idPlus)) Logger.debug("支付成功回调更新用户Token payFrontNotify:"+Json.toJson(idPlus));
                } catch (Exception e) {
                    Logger.error("支付回调订单更新出错payFrontNotify: "+e.getMessage());
                    e.printStackTrace();
                }
                return ok("success page");
            }else return ok("success page");
        }
    }
}
