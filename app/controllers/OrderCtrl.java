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
import play.libs.F;
import play.libs.Json;
import play.mvc.*;
import service.CartService;
import service.IdService;
import service.PromotionService;
import service.SkuService;
import util.CalCountDown;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.*;

import static akka.pattern.Patterns.ask;
import static play.libs.Json.newObject;

/**
 * 订单相关,提交订单,优惠券
 * Created by howen on 15/12/1.
 */
@Singleton
public class OrderCtrl extends Controller {

    private SkuService skuService;

    private CartService cartService;

    private IdService idService;

    private PromotionService promotionService;

    private ActorRef orderSplitActor;

    private ActorRef cancelOrderActor;

    private ActorRef uploadImagesActor;

    //行邮税收税标准
    public static String POSTAL_STANDARD;

    //海关规定购买单笔订单金额限制
    public static String POSTAL_LIMIT;

    //达到多少免除邮费
    public static String FREE_SHIP;

    private OrderMid orderMid;

    @Inject
    public OrderCtrl(SkuService skuService, CartService cartService, IdService idService, PromotionService promotionService, @Named("uploadImagesActor") ActorRef uploadImagesActor, @Named("subOrderActor") ActorRef orderSplitActor, @Named("cancelOrderActor") ActorRef cancelOrderActor) {
        this.cartService = cartService;
        this.idService = idService;
        this.orderSplitActor = orderSplitActor;
        this.skuService = skuService;
        this.cancelOrderActor = cancelOrderActor;
        this.uploadImagesActor = uploadImagesActor;
        this.promotionService = promotionService;

        //行邮税收税标准
        POSTAL_STANDARD = skuService.getSysParameter(new SysParameter(null, null, null, "POSTAL_STANDARD")).getParameterVal();

        //海关规定购买单笔订单金额限制
        POSTAL_LIMIT = skuService.getSysParameter(new SysParameter(null, null, null, "POSTAL_LIMIT")).getParameterVal();

        //达到多少免除邮费
        FREE_SHIP = skuService.getSysParameter(new SysParameter(null, null, null, "FREE_SHIP")).getParameterVal();

        orderMid = new OrderMid(skuService, cartService, idService, promotionService ,orderSplitActor);

    }

    //图片服务器url
    static final String IMAGE_URL = play.Play.application().configuration().getString("image.server.url");

    //发布服务器url
    static final String DEPLOY_URL = play.Play.application().configuration().getString("deploy.server.url");

    //shopping服务器url
    static final String SHOPPING_URL = play.Play.application().configuration().getString("shopping.server.url");

    //id服务器url
    static final String ID_URL = play.Play.application().configuration().getString("id.server.url");

    static final String IMG_PROCESS_URL = play.Play.application().configuration().getString("imgprocess.server.url");

    //将Json串转换成List
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
            if (orderId != 0) {
                order.setOrderId(orderId);
            }
            order.setUserId(userId);

            Optional<List<Order>> orderList = Optional.ofNullable(cartService.getOrderBy(order));

            //返回总数据
            List<Map> mapList = new ArrayList<>();

            if (orderList.isPresent()) {

                for (Order o : orderList.get()) {
                    //用于保存每个订单对应的明细list和地址信息
                    Map<String, Object> map = new HashMap<>();

                    OrderAddress orderAddress = new OrderAddress();
                    orderAddress.setOrderId(o.getOrderId());

                    Optional<List<OrderAddress>> orderAddressOptional = Optional.ofNullable(cartService.selectOrderAddress(orderAddress));

                    if (orderAddressOptional.isPresent()) {
                        //获取地址信息
                        Address address = new Address();
                        address.setDeliveryCity(orderAddressOptional.get().get(0).getDeliveryCity());
                        address.setDeliveryDetail(orderAddressOptional.get().get(0).getDeliveryAddress());
                        address.setIdCardNum(orderAddressOptional.get().get(0).getDeliveryCardNum());
                        address.setName(orderAddressOptional.get().get(0).getDeliveryName());
                        address.setTel(orderAddressOptional.get().get(0).getDeliveryTel());
                        map.put("address", address);

                    }
                    o.setCountDown(CalCountDown.getTimeSubtract(o.getOrderCreateAt()));

                    //未支付订单
                    if (o.getOrderStatus().equals("I") || o.getOrderStatus().equals("C")) {

                        OrderLine orderLine = new OrderLine();
                        orderLine.setOrderId(o.getOrderId());

                        List<OrderLine> orderLineList = cartService.selectOrderLine(orderLine);

                        //每个订单对应的商品明细
                        List<CartSkuDto> skuDtoList = new ArrayList<>();

                        Integer orderAmount = 0;

                        for (OrderLine orl : orderLineList) {
                            CartSkuDto skuDto = new CartSkuDto();

                            //组装返回的订单商品明细
                            skuDto.setSkuId(orl.getSkuId());
                            skuDto.setAmount(orl.getAmount());
                            orderAmount += skuDto.getAmount();
                            skuDto.setPrice(orl.getPrice());
                            skuDto.setSkuTitle(orl.getSkuTitle());
                            if (orl.getSkuImg().contains("url")) {
                                JsonNode jsonNode = Json.parse(orl.getSkuImg());
                                if (jsonNode.has("url")) {
                                    skuDto.setInvImg(IMAGE_URL + jsonNode.get("url").asText());
                                }
                            } else skuDto.setInvImg(IMAGE_URL + orl.getSkuImg());

                            switch (orl.getSkuType()) {
                                case "item":
                                    skuDto.setInvUrl(Application.DEPLOY_URL + "/comm/detail/" + orl.getItemId() + "/" + orl.getSkuId());
                                    break;
                                case "vary":
                                    skuDto.setInvUrl(Application.DEPLOY_URL + "/comm/detail/" + orl.getItemId() + "/" + orl.getSkuId() + "/" + orl.getSkuTypeId());
                                    break;
                                case "customize":
                                    skuDto.setInvUrl(Application.DEPLOY_URL + "/comm/subject/detail/" + orl.getItemId() + "/" + orl.getSkuId() + "/" + orl.getSkuTypeId());
                                    break;
                                case "pin":
                                    skuDto.setInvUrl(Application.DEPLOY_URL + "/comm/pin/detail/" + orl.getItemId() + "/" + orl.getSkuId() + "/" + orl.getSkuTypeId());
                                    break;
                            }

                            skuDto.setItemColor(orl.getSkuColor());
                            skuDto.setItemSize(orl.getSkuSize());
                            skuDtoList.add(skuDto);
                        }

                        o.setOrderAmount(orderAmount);
                        //组装每个订单对应的明细和地址
                        map.put("order", o);
                        map.put("sku", skuDtoList);
                        mapList.add(map);
                    }
                    //否则以子订单来显示
                    else {
                        OrderSplit orderSplit = new OrderSplit();
                        orderSplit.setOrderId(o.getOrderId());
                        Optional<List<OrderSplit>> optionalOrderSplitList = Optional.ofNullable(cartService.selectOrderSplit(orderSplit));
                        if (optionalOrderSplitList.isPresent()) {
                            for (OrderSplit osp : optionalOrderSplitList.get()) {
                                Order orderS = new Order();
                                orderS.setOrderId(osp.getOrderId());
                                orderS.setOrderAmount(osp.getTotalAmount());
                                orderS.setPayMethod(o.getPayMethod());
                                orderS.setPayTotal(osp.getTotalPayFee());
                                orderS.setTotalFee(osp.getTotalFee());
                                orderS.setShipFee(osp.getShipFee());
                                orderS.setPostalFee(osp.getPostalFee());
                                orderS.setOrderCreateAt(o.getOrderCreateAt());
                                orderS.setOrderStatus(o.getOrderStatus());

                                OrderLine orderLine = new OrderLine();
                                orderLine.setOrderId(o.getOrderId());
                                orderS.setOrderSplitId(osp.getSplitId());
                                List<OrderLine> orderLineList = cartService.selectOrderLine(orderLine);

                                //每个订单对应的商品明细
                                List<CartSkuDto> skuDtoList = new ArrayList<>();

                                for (OrderLine orl : orderLineList) {
                                    CartSkuDto skuDto = new CartSkuDto();

                                    //组装返回的订单商品明细
                                    skuDto.setSkuId(orl.getSkuId());
                                    skuDto.setAmount(orl.getAmount());
                                    skuDto.setPrice(orl.getPrice());
                                    skuDto.setSkuTitle(orl.getSkuTitle());
                                    if (orl.getSkuImg().contains("url")) {
                                        JsonNode jsonNode = Json.parse(orl.getSkuImg());
                                        if (jsonNode.has("url")) {
                                            skuDto.setInvImg(IMAGE_URL + jsonNode.get("url").asText());
                                        }
                                    } else skuDto.setInvImg(IMAGE_URL + orl.getSkuImg());

                                    switch (orl.getSkuType()) {
                                        case "item":
                                            skuDto.setInvUrl(Application.DEPLOY_URL + "/comm/detail/" + orl.getItemId() + "/" + orl.getSkuId());
                                            break;
                                        case "vary":
                                            skuDto.setInvUrl(Application.DEPLOY_URL + "/comm/detail/" + orl.getItemId() + "/" + orl.getSkuId() + "/" + orl.getSkuTypeId());
                                            break;
                                        case "customize":
                                            skuDto.setInvUrl(Application.DEPLOY_URL + "/comm/subject/detail/" + orl.getItemId() + "/" + orl.getSkuId() + "/" + orl.getSkuTypeId());
                                            break;
                                        case "pin":
                                            skuDto.setInvUrl(Application.DEPLOY_URL + "/comm/pin/detail/" + orl.getItemId() + "/" + orl.getSkuId() + "/" + orl.getSkuTypeId());
                                            break;
                                    }

                                    skuDto.setItemColor(orl.getSkuColor());
                                    skuDto.setItemSize(orl.getSkuSize());
                                    skuDtoList.add(skuDto);
                                }

                                map.put("order", orderS);
                                map.put("sku", skuDtoList);
                                mapList.add(map);
                            }
                        }
                    }
                }
            }
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            result.putPOJO("orderList", Json.toJson(mapList));

            return ok(result);
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("server exception:" + ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
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
                    if (longOptional.isPresent() && longOptional.get().compareTo(JDPay.COUNTDOWN_MILLISECONDS) > 0) {
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

        Http.MultipartFormData body = request().body().asMultipartFormData();

        Map<String, String[]> stringMap = body.asFormUrlEncoded();
        Map<String, String> map = new HashMap<>();

        stringMap.forEach((k, v) -> map.put(k, v[0]));

        Optional<JsonNode> json = Optional.ofNullable(Json.toJson(map));

        Long userId = (Long) ctx().args.get("userId");
        try {
            if (json.isPresent() && json.get().size() > 0) {

                Refund refund = Json.fromJson(json.get(), Refund.class);
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
                    mapActor.put("url", IMG_PROCESS_URL);
                    uploadImagesActor.tell(mapActor, ActorRef.noSender());
                }

                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(result);

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
     * 收藏
     * @return
     */
    @Security.Authenticated(UserAuth.class)
    public Result submitCollect(){

        ObjectNode result = newObject();
        Optional<JsonNode> json = Optional.ofNullable(request().body().asJson());
        /*******取用户ID*********/
        Long userId = (Long) ctx().args.get("userId");
        try{
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
            if (!(collectList.isPresent()&&collectList.get().size()>0)) { //未收藏
                collect = createCollect(userId,collectSubmitDTO);
            }else{
                Logger.info("========客户端userId="+userId+",发过来的收藏数据="+collectSubmitDTO+",collectId="+collect.getCollectId());
            }
            if(null!=collect){
                Logger.info("客户端userId="+userId+",发过来的收藏数据="+collectSubmitDTO+",collectId="+collect.getCollectId());
                result.putPOJO("collectId",collect.getCollectId());
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(result);
            }

        }catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
        return ok(result);
    }

    /**
     * 创建收藏数据
     * @param userId
     * @param collectSubmitDTO
     * @return
     * @throws Exception
     */
    private Collect createCollect(Long userId,CollectSubmitDTO collectSubmitDTO) throws Exception {
        Collect collect=new Collect();
        collect.setUserId(userId);
        collect.setSkuId(collectSubmitDTO.getSkuId());
        collect.setSkuType(collectSubmitDTO.getSkuType());
        collect.setSkuTypeId(collectSubmitDTO.getSkuTypeId());
        if(cartService.insertCollect(collect)){
            Logger.info("==============="+collect.getCollectId()+",===="+collect.getSkuId());
            return collect;
        }
        Logger.info("=====dssgsgsgs=========="+collect.getCollectId()+",===="+collect.getSkuId());
        return null;
    }

    /**
     * 删除收藏
     * @param collectId
     * @return
     */
    @Security.Authenticated(UserAuth.class)
    public Result delCollect(Long collectId){
        ObjectNode result = newObject();
        try{
            /*******取用户ID*********/
            Long userId = (Long) ctx().args.get("userId");
            Collect collect=new Collect();
            collect.setCollectId(collectId);
            collect.setUserId(userId);
            if(collectId>0&&cartService.deleteCollect(collect)){
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(result);
            }

        }catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
        return ok(result);
    }

    /***
     * 获取所有收藏数据
     * @return
     */
    @Security.Authenticated(UserAuth.class)
    public Result getCollect(){

        ObjectNode result = newObject();
        try{
            Long userId = (Long) ctx().args.get("userId");
            Collect collect=new Collect();
            collect.setUserId(userId);

            List<CollectDto> collectDtoList=new ArrayList<CollectDto>();
            Optional<List<Collect>> collectList = Optional.ofNullable(cartService.selectCollect(collect));
            if (collectList.isPresent()){
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

                    //跳转地址
                    if ("item".equals(c.getSkuType())) {
                        skuDto.setInvUrl(DEPLOY_URL + "/comm/detail/" + sku.getItemId() + "/" + sku.getId());
                        skuDto.setInvAndroidUrl(DEPLOY_URL + "/comm/detail/web/" + sku.getItemId() + "/" + sku.getId());
                    } else if ("pin".equals(c.getSkuType())) {
                        skuDto.setInvUrl(Application.DEPLOY_URL + "/comm/pin/detail/" + sku.getItemId() + "/" + sku.getId() + "/" + c.getSkuTypeId());
                        skuDto.setInvAndroidUrl(Application.DEPLOY_URL + "/comm/pin/detail/web/" + sku.getItemId() + "/" + sku.getId() + "/" + c.getSkuTypeId());
                        PinSku pinSku = promotionService.getPinSkuById(c.getSkuTypeId());
                        if(null==pinSku){
                            Logger.warn("collect pin sku not exist ,pinSkuId=" + c.getSkuTypeId());
                            continue;
                        }
                        JsonNode jsonNode=Json.parse(pinSku.getFloorPrice()); //拼购取最低价
                        skuDto.setPrice(new BigDecimal(jsonNode.get("price").asText()));

                    } else if ("vary".equals(c.getSkuType())) {
                        skuDto.setInvUrl(Application.DEPLOY_URL + "/comm/detail/" + sku.getItemId() + "/" + sku.getId() + "/" + c.getSkuTypeId());
                        skuDto.setInvAndroidUrl(Application.DEPLOY_URL + "/comm/detail/web/" + sku.getItemId() + "/" + sku.getId() + "/" + c.getSkuTypeId());
                    } else if ("customize".equals(c.getSkuType())) {
                        skuDto.setInvUrl(Application.DEPLOY_URL + "/comm/subject/detail/" + sku.getItemId() + "/" + sku.getId() + "/" + c.getSkuTypeId());
                        skuDto.setInvAndroidUrl(Application.DEPLOY_URL + "/comm/subject/detail/web/" + sku.getItemId() + "/" + sku.getId() + "/" + c.getSkuTypeId());
                    }

                    if (sku.getInvImg().contains("url")) {
                        JsonNode jsonNode = Json.parse(sku.getInvImg());
                        if (jsonNode.has("url")) {
                            skuDto.setInvImg(IMAGE_URL + jsonNode.get("url").asText());
                        }
                    }
                    else
                        skuDto.setInvImg(IMAGE_URL + sku.getInvImg());

                    skuDto.setItemColor(sku.getItemColor());
                    skuDto.setItemSize(sku.getItemSize());

                    collectDto.setCartSkuDto(skuDto);
                    collectDtoList.add(collectDto);

                }
            }
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            result.putPOJO("collectList", Json.toJson(collectDtoList));

            return ok(result);
        }catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            Logger.error("server exception:",ex);
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }
}
