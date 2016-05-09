package controllers;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import util.CalCountDown;
import util.ExpressMD5;
import util.SysParCom;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;

import static akka.pattern.Patterns.ask;
import static play.libs.Json.newObject;
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
            Logger.error("settle: " + ex.getMessage());
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
            Logger.error("submitOrder " + ex.getMessage());
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
            Logger.error("server exception:" + ex.getMessage());
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

                        String sign = ExpressMD5.encode(obj.toString() + EXPRESS_KEY + EXPRESS_CUSTOMER);

                        return ws.url(EXPRESS_POST_URL).setQueryParameter("param", obj.toString()).setQueryParameter("sign", sign).setQueryParameter("customer", EXPRESS_CUSTOMER).post("").map(wsResponse -> {
                            JsonNode jsonNode = wsResponse.asJson();
                            Logger.error("快递100返回信息--->" + jsonNode.toString());
                            ((ObjectNode) jsonNode).put("expressName", expressName);
                            ((ObjectNode) jsonNode).put("expressNum", expressNum);
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
            Logger.error("server exception:" + ex.getMessage());
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
                            order.setOrderStatus("N");
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
                            order.setOrderStatus("N");
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
            Logger.error("server exception:" + ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    /**
     * 收藏
     *
     * @return Result
     */
    @Security.Authenticated(UserAuth.class)
    public Result submitCollect() {

        ObjectNode result = newObject();
        Optional<JsonNode> json = Optional.ofNullable(request().body().asJson());
        /*******取用户ID*********/
        Long userId = (Long) ctx().args.get("userId");
        try {
            if (json.isPresent() && json.get().size() > 0) {
                //客户端发过来的收藏数据
                CollectSubmitDTO collectSubmitDTO = mapper.readValue(json.get().toString(), mapper.getTypeFactory().constructType(CollectSubmitDTO.class));
                //用户收藏信息
                Collect collect = new Collect();
                collect.setUserId(userId);
                collect.setSkuId(collectSubmitDTO.getSkuId());
                collect.setSkuType(collectSubmitDTO.getSkuType());
                collect.setSkuTypeId(collectSubmitDTO.getSkuTypeId());
                //判断是否已经收藏
                Optional<List<Collect>> collectList = Optional.ofNullable(cartService.selectCollect(collect));
                if (!(collectList.isPresent() && collectList.get().size() > 0)) { //未收藏
                    collect = createCollect(userId, collectSubmitDTO);
                } else {
                    collect = collectList.get().get(0);
                }
                if (null != collect) {
                    result.putPOJO("collectId", collect.getCollectId());
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                    return ok(result);
                }
            }

        } catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
        return ok(result);
    }

    /**
     * 创建收藏数据
     *
     * @param userId
     * @param collectSubmitDTO
     * @return
     * @throws Exception
     */
    private Collect createCollect(Long userId, CollectSubmitDTO collectSubmitDTO) throws Exception {
        Collect collect = new Collect();
        collect.setUserId(userId);
        collect.setSkuId(collectSubmitDTO.getSkuId());
        collect.setSkuType(collectSubmitDTO.getSkuType());
        collect.setSkuTypeId(collectSubmitDTO.getSkuTypeId());
        if (cartService.insertCollect(collect)) {
            return collect;
        }
        return null;
    }

    /**
     * 删除收藏
     *
     * @param collectId
     * @return
     */
    @Security.Authenticated(UserAuth.class)
    public Result delCollect(Long collectId) {
        ObjectNode result = newObject();
        try {
            /*******取用户ID*********/
            Long userId = (Long) ctx().args.get("userId");
            Collect collect = new Collect();
            collect.setCollectId(collectId);
            collect.setUserId(userId);
            if (collectId > 0 && cartService.deleteCollect(collect)) {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(result);
            }

        } catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
        return ok(result);
    }

    /***
     * 获取所有收藏数据
     *
     * @return
     */
    @Security.Authenticated(UserAuth.class)
    public Result getCollect() {

        ObjectNode result = newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");
            Collect collect = new Collect();
            collect.setUserId(userId);
            List<CollectDto> collectDtoList = new ArrayList<CollectDto>();
            Optional<List<Collect>> collectList = Optional.ofNullable(cartService.selectCollect(collect));
            if (collectList.isPresent() && collectList.get().size() > 0) {
                for (Collect c : collectList.get()) {
                    CollectDto collectDto = new CollectDto();
                    collectDto.setCollectId(c.getCollectId());
                    collectDto.setCreateAt(c.getCreateAt());
                    collectDto.setSkuType(c.getSkuType());
                    collectDto.setSkuTypeId(c.getSkuTypeId());

                    Sku sku = new Sku();
                    sku.setId(c.getSkuId());
                    sku = skuService.getInv(sku);
                    if (null == sku) {
                        Logger.warn("collect sku not exist ,skuId=" + c.getSkuId());
                        continue;
                    }
                    CartSkuDto skuDto = new CartSkuDto();
                    skuDto.setSkuId(c.getSkuId());
                    skuDto.setSkuTitle(sku.getInvTitle());
                    skuDto.setAmount(sku.getAmount());
                    skuDto.setPrice(sku.getItemPrice());
                    skuDto.setInvUrl(SysParCom.DEPLOY_URL + "/comm/detail/" + c.getSkuType() + "/" + sku.getItemId() + "/" + c.getSkuTypeId());

                    //跳转地址
                    if ("pin".equals(c.getSkuType())) {
                        PinSku pinSku = promotionService.getPinSkuById(c.getSkuTypeId());
                        if (null == pinSku) {
                            Logger.warn("collect pin sku not exist ,pinSkuId=" + c.getSkuTypeId());
                            continue;
                        }
                        JsonNode jsonNode = Json.parse(pinSku.getFloorPrice()); //拼购取最低价
                        skuDto.setPrice(new BigDecimal(jsonNode.get("price").asText()));

                    }
                    skuDto.setInvImg(getInvImg(sku.getInvImg()));

                    skuDto.setItemColor(sku.getItemColor());
                    skuDto.setItemSize(sku.getItemSize());

                    collectDto.setCartSkuDto(skuDto);
                    collectDtoList.add(collectDto);

                }
            }
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            result.putPOJO("collectList", Json.toJson(collectDtoList));

            return ok(result);
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            Logger.error("server exception:", ex);
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
}
