package controllers;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import com.squareup.okhttp.*;
import domain.*;
import filters.UserAuth;
import middle.OrderMid;
import org.apache.commons.io.FileUtils;
import play.Logger;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.*;
import service.CartService;
import service.PromotionService;
import service.SkuService;
import sun.security.provider.MD5;
import util.CalCountDown;
import util.Crypto;
import util.ExpressMD5;
import util.SysParCom;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;

import static akka.pattern.Patterns.ask;
import static java.nio.charset.StandardCharsets.UTF_8;
import static play.libs.Json.newObject;
import static play.libs.Json.toJson;
import static util.SysParCom.*;

/**
 * 订单相关,提交订单,优惠券
 * Created by howen on 15/12/1.
 */

public class OrderCtrl extends Controller {

    @Inject
    private SkuService skuService;

    @Inject
    private CartService cartService;

    @Inject
    private PromotionService promotionService;

    @Inject
    @Named("cancelOrderActor")
    private ActorRef cancelOrderActor;

    @Inject
    @Named("uploadImagesActor")
    private ActorRef uploadImagesActor;

    @Inject
    private OrderMid orderMid;

    @Inject
    private WSClient ws;

    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * 请求结算页面
     *
     * @return result
     */
    @Security.Authenticated(UserAuth.class)
    public Result settle() {

        ObjectNode result = newObject();

        Optional<JsonNode> json = Optional.ofNullable(request().body().asJson());
        //   Logger.info("====settle=="+json);

        try {
            Long userId = (Long) ctx().args.get("userId");
            if (json.isPresent() && json.get().size() > 0) {

                SettleOrderDTO settleOrderDTO = mapper.convertValue(json.get(), SettleOrderDTO.class);
                SettleVo settleVo = orderMid.OrderSettle(settleOrderDTO, userId);
                if (settleVo.getMessageCode() != null) {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(settleVo.getMessageCode()), settleVo.getMessageCode())));
                    return ok(result);
                }
                result.putPOJO("settle", Json.toJson(settleVo));
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(result);
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
                return ok(result);
            }
        } catch (Exception ex) {
            Logger.error("settle: " + Throwables.getStackTraceAsString(ex));
            ex.printStackTrace();
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ERROR.getIndex()), Message.ErrorCode.ERROR.getIndex())));
            return ok(result);
        }
    }

    @Security.Authenticated(UserAuth.class)
    public Result submitOrder() {

        ObjectNode result = newObject();
        Optional<JsonNode> json = Optional.ofNullable(request().body().asJson());
        try {
            /*******取用户ID*********/
            Long userId = (Long) ctx().args.get("userId");

            if (json.isPresent() && json.get().size() > 0) {

                SettleOrderDTO settleOrderDTO = mapper.convertValue(json.get(), SettleOrderDTO.class);
                settleOrderDTO.setClientIp(request().remoteAddress());

                SettleVo settleVo = orderMid.submitOrder(userId, settleOrderDTO);
                if (settleVo.getMessageCode() != null) {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(settleVo.getMessageCode()), settleVo.getMessageCode())));
                    return ok(result);
                } else {
                    result.putPOJO("orderId", settleVo.getOrderId());
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                    return ok(result);
                }
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
                return ok(result);
            }
        } catch (Exception ex) {
            Logger.error("submitOrder " + Throwables.getStackTraceAsString(ex));
            ex.printStackTrace();
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ERROR.getIndex()), Message.ErrorCode.ERROR.getIndex())));
            return ok(result);
        }
    }

    /**
     * 购物券List
     *
     * @return result
     */
    @Security.Authenticated(UserAuth.class)
    public Result couponsList() {

        ObjectNode result = newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");

            CouponVo couponVo = new CouponVo();
            couponVo.setUserId(userId);
            couponVo.setState("N");
            cartService.updateCouponInvalid(couponVo);
            couponVo.setState("");
            List<CouponVo> lists = cartService.getUserCouponAll(couponVo);

            result.putPOJO("coupons", Json.toJson(lists));
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            return ok(result);

        } catch (Exception ex) {
            Logger.error(ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
            return ok(result);
        }
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @return promise
     */
    public F.Promise<Result> cancelOrder(Long orderId) {
        ObjectNode result = newObject();
        return F.Promise.wrap(ask(cancelOrderActor, orderId, 3000)
        ).map(response -> {
            Logger.info("取消订单:" + orderId);
            if (((Integer) response) == 200) {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(result);
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ERROR.getIndex()), Message.ErrorCode.ERROR.getIndex())));
                return ok(result);
            }
        });
    }

    /**
     * 用户查询所有订单接口
     *
     * @return 返回所有订单数据
     */
    @Security.Authenticated(UserAuth.class)
    public Result shoppingOrder(Long orderId) {
        ObjectNode result = newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");
            Order order = new Order();
            if (orderId != 0L) {
                order.setOrderId(orderId);
            }
            order.setUserId(userId);
            //返回总数据
            List<Map> mapList = orderMid.getOrders(order);

            if (mapList != null && mapList.size() > 0) {
                result.putPOJO("orderList", Json.toJson(mapList));
            }
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            return ok(result);
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("server exception:" + Throwables.getStackTraceAsString(ex));
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    /**
     * 查看物流信息
     * @param orderId orderId
     * @return Result
     */
    @Security.Authenticated(UserAuth.class)
    public F.Promise<Result> express(Long orderId) {
        ObjectNode result = newObject();
        Order order = new Order();
        order.setOrderId(orderId);
        try {
            Optional<List<Order>> orderList = Optional.ofNullable(cartService.getOrderBy(order));

            if (orderList.isPresent() && orderList.get().size() > 0) {
                order = orderList.get().get(0);
                if (order.getOrderStatus().equals("D") || order.getOrderStatus().equals("R")) {
                    OrderSplit orderSplit = new OrderSplit();
                    orderSplit.setOrderId(order.getOrderId());
                    Optional<List<OrderSplit>> optionalOrderSplitList = Optional.ofNullable(cartService.selectOrderSplit(orderSplit));
                    if (optionalOrderSplitList.isPresent() && optionalOrderSplitList.get().size() > 0) {
                        orderSplit = optionalOrderSplitList.get().get(0);
                        Logger.info("快递编号: " + orderSplit.getExpressNum() + "\n快递名称: " + orderSplit.getExpressNm());
                        final String expressName = orderSplit.getExpressNm();
                        final String expressNum = orderSplit.getExpressNum();


                        ObjectNode obj = Json.newObject();
                        obj.put("com", orderSplit.getExpressCode());
                        obj.put("num", orderSplit.getExpressNum());

                        JsonNode weisheng=null;
                        if(null!=expressNum&&!"0".equals(expressNum)){
                            weisheng=weishengOrderTrack(expressNum);
                         //   weisheng=weishengOrderTrack("806843734566");
                        }


                        String sign = ExpressMD5.encode(obj.toString() + EXPRESS_KEY + EXPRESS_CUSTOMER);

                        final JsonNode finalWeisheng = weisheng;
                        return ws.url(EXPRESS_POST_URL).setQueryParameter("param", obj.toString()).setQueryParameter("sign", sign).setQueryParameter("customer", EXPRESS_CUSTOMER).post("").map(wsResponse -> {
                            JsonNode jsonNode = wsResponse.asJson();
                            Logger.error("快递100返回信息--->" + jsonNode.toString());
                            ((ObjectNode) jsonNode).put("expressName", expressName);
                            ((ObjectNode) jsonNode).put("expressNum", expressNum);
                            if(null!=finalWeisheng){
                                ((ObjectNode) jsonNode).putPOJO("weisheng", finalWeisheng);
                            }

                            return ok(jsonNode);
                        });
                    } else {
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.DATA_NOT_EXISTS.getIndex()), Message.ErrorCode.DATA_NOT_EXISTS.getIndex())));
                        return F.Promise.promise(() -> ok(result));
                    }
                } else {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ORDER_NOT_DELIVERY.getIndex()), Message.ErrorCode.ORDER_NOT_DELIVERY.getIndex())));
                    return F.Promise.promise(() -> ok(result));
                }
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ORDER_NOT_EXISTS.getIndex()), Message.ErrorCode.ORDER_NOT_EXISTS.getIndex())));
                return F.Promise.promise(() -> ok(result));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("server exception:" + Throwables.getStackTraceAsString(ex));
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return F.Promise.promise(() -> ok(result));
        }
    }

    /**
     * 校验订单状态 ,前端点击去支付时
     *
     * @param orderId 订单ID
     * @return 返回消息
     */
    @Security.Authenticated(UserAuth.class)
    public Result verifyOrder(Long orderId) {
        ObjectNode result = newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");
            if (orderId != 0) {
                Order order = new Order();
                order.setOrderId(orderId);
                order.setUserId(userId);
                Optional<List<Order>> listOptional = Optional.ofNullable(cartService.getOrderBy(order));
                if (listOptional.isPresent() && listOptional.get().size() > 0) {
                    order = cartService.getOrderBy(order).get(0);
                    Optional<Long> longOptional = Optional.ofNullable(CalCountDown.getTimeSubtract(order.getOrderCreateAt()));
                    if (longOptional.isPresent() && longOptional.get() < 0) {
                        cancelOrderActor.tell(orderId, null);
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ORDER_CANCEL_AUTO.getIndex()), Message.ErrorCode.ORDER_CANCEL_AUTO.getIndex())));
                        return ok(result);
                    } else {
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                        return ok(result);
                    }
                } else {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
                    return ok(result);
                }
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
                return ok(result);
            }
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    /**
     * 删除订单
     *
     * @param orderId 订单ID
     * @return 消息
     */
    @Security.Authenticated(UserAuth.class)
    public Result delOrder(Long orderId) {
        ObjectNode result = newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");
            if (orderId != 0) {
                Order order = new Order();
                order.setOrderId(orderId);
                order.setUserId(userId);
                Optional<List<Order>> listOptional = Optional.ofNullable(cartService.getOrderBy(order));
                if (listOptional.isPresent() && listOptional.get().size() > 0) {
                    order = cartService.getOrderBy(order).get(0);

                    switch (order.getOrderStatus()) {
                        case "I":
                            cancelOrderActor.tell(orderId, null);
                            order.setOrDel(true);
                            if (cartService.updateOrder(order)) {
                                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                                return ok(result);
                            } else {
                                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.DATABASE_EXCEPTION.getIndex()), Message.ErrorCode.DATABASE_EXCEPTION.getIndex())));
                                return ok(result);
                            }
                        case "S":
                            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ORDER_DEL.getIndex()), Message.ErrorCode.ORDER_DEL.getIndex())));
                            return ok(result);
                        case "D":
                            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ORDER_DEL.getIndex()), Message.ErrorCode.ORDER_DEL.getIndex())));
                            return ok(result);
                        default:
                            order.setOrDel(true);
                            if (cartService.updateOrder(order)) {
                                Logger.info("删除订单ID: " + order.getOrderId());
                                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                                return ok(result);
                            } else {
                                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.DATABASE_EXCEPTION.getIndex()), Message.ErrorCode.DATABASE_EXCEPTION.getIndex())));
                                return ok(result);
                            }
                    }
                } else {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
                    return ok(result);
                }
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
                return ok(result);
            }
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    /**
     * 退货申请
     *
     * @return result
     */
    @Security.Authenticated(UserAuth.class)
    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = 50 * 1024 * 1024)
    public Result refundApply() {

        ObjectNode result = newObject();

        Long userId = (Long) ctx().args.get("userId");

        Http.MultipartFormData body = request().body().asMultipartFormData();

        Logger.error("请求数据--->\n" + request().body());

        Form<Refund> userForm = Form.form(Refund.class).bindFromRequest();

        if (userForm.hasErrors()) {
            Logger.error("校验错误: " + userForm.errorsAsJson().toString());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
            return ok(result);
        } else {
            try {
                Refund refund = userForm.get();

                if (refund.getOrderId() != null && refund.getSkuId() != null) {
                    OrderLine orderLine = new OrderLine();
                    orderLine.setOrderId(refund.getOrderId());
                    orderLine.setSkuId(refund.getSkuId());
                    List<OrderLine> orderLines = cartService.selectOrderLine(orderLine);
                    if (orderLines.size() > 0) orderLine = orderLines.get(0);
                    refund.setPayBackFee(orderLine.getPrice().multiply(new BigDecimal(refund.getAmount())).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
                refund.setUserId(userId);
                List<Http.MultipartFormData.FilePart> fileParts = body.getFiles();

                Boolean flag = cartService.insertRefund(refund);

                if (!fileParts.isEmpty() && flag) {
                    Map<String, Object> mapActor = new HashMap<>();
                    List<byte[]> files = new ArrayList<>();
                    mapActor.put("refundId", refund.getId());
                    for (Http.MultipartFormData.FilePart filePart : fileParts) {
                        if (!"image/jpeg".equalsIgnoreCase(filePart.getContentType()) && !"image/png".equalsIgnoreCase(filePart.getContentType())) {
                            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FILE_TYPE_NOT_SUPPORTED.getIndex()), Message.ErrorCode.FILE_TYPE_NOT_SUPPORTED.getIndex())));
                            return badRequest(result);
                        } else {
                            files.add(FileUtils.readFileToByteArray(filePart.getFile()));
                        }
                    }
                    mapActor.put("files", files);
                    mapActor.put("url", SysParCom.IMAGE_URL);
                    uploadImagesActor.tell(mapActor, ActorRef.noSender());
                }

                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(result);

            } catch (Exception ex) {
                Logger.error("server exception:" + ex.getMessage());
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
                return ok(result);
            }
        }
    }


    /**
     * 确认收货
     *
     * @param orderId orderId
     * @return Result
     */
    @Security.Authenticated(UserAuth.class)
    public Result confirmDelivery(Long orderId) {
        ObjectNode result = newObject();
        Order order = new Order();
        order.setOrderId(orderId);
        Long userId = (Long) ctx().args.get("userId");
        try {
            Optional<List<Order>> orderList = Optional.ofNullable(cartService.getOrderBy(order));

            if (orderList.isPresent() && orderList.get().size() > 0) {
                order = orderList.get().get(0);
                if (order.getOrderStatus().equals("D")) {
                    order.setOrderStatus("R");
                    order.setUserId(userId);
                    if (cartService.updateOrder(order)) {
                        Logger.info("确认收货成功:" + userId);
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                        return ok(result);
                    } else {
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.CONFIRM_DELIVERY_FAIL.getIndex()), Message.ErrorCode.CONFIRM_DELIVERY_FAIL.getIndex())));
                        return ok(result);
                    }
                } else {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ORDER_NOT_DELIVERY.getIndex()), Message.ErrorCode.ORDER_NOT_DELIVERY.getIndex())));
                    return ok(result);
                }
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ORDER_NOT_EXISTS.getIndex()), Message.ErrorCode.ORDER_NOT_EXISTS.getIndex())));
                return ok(result);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("server exception:" + Throwables.getStackTraceAsString(ex));
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    /**
     * 转换图片,拼接URL前缀
     *
     * @param invImg invImg
     * @return invImg
     */
    public String getInvImg(String invImg) {
        //SKU图片
        if (invImg.contains("url")) {
            JsonNode jsonNode_InvImg = Json.parse(invImg);
            if (jsonNode_InvImg.has("url")) {
                ((ObjectNode) jsonNode_InvImg).put("url", SysParCom.IMAGE_URL + jsonNode_InvImg.get("url").asText());
                return Json.stringify(jsonNode_InvImg);
            } else return SysParCom.IMAGE_URL + invImg;
        } else return SysParCom.IMAGE_URL + invImg;
    }


    /**
     * 商户获取物流追踪信息接口
     * @param expressNo 威盛快递单号
     */
    private JsonNode weishengOrderTrack(String expressNo){
        WeiSheng weiSheng=new WeiSheng();
        weiSheng.setExpressNo(expressNo);
        List<WeiSheng> weiShengList=skuService.getWeiSheng(weiSheng);

        if(null!=weiShengList&&weiShengList.size()>0){
            ObjectNode  requestJson= newObject();
            requestJson.put("appname",WEISHENG_APP_NAME);
            requestJson.put("appid",WEISHENG_APP_ID);
            requestJson.put("TrackingID",weiShengList.get(0).getTrackingId());

            String EData=Json.toJson(requestJson).toString();
            String SignMsg= Crypto.md5(EData+WEISHENG_KEY);
            String msg="";
            try {
                msg= URLEncoder.encode(EData,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            msg="EData="+msg+"&SignMsg="+SignMsg;

//            Logger.error("-发送内容-->"+msg);

            //创建一个OkHttpClient对象
            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody formBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"),msg);
            //创建一个请求对象
            Request request = new Request.Builder().url(WEISHENG_ORDER_TRACK_URL).post(formBody).build();
            //发送请求获取响应
            try {
                Response response=okHttpClient.newCall(request).execute();
                //判断请求是否成功
                if(response.isSuccessful()){
                    //打印服务端返回结果
                    JsonNode jsonNode=Json.parse(new String(response.body().bytes(), UTF_8));
                    Logger.info("威盛物流返回信息--->" + jsonNode);
                    return jsonNode;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Logger.error("威盛订单TrackingID不存在,expressNo="+expressNo);
        }

        return null;




    }

}
