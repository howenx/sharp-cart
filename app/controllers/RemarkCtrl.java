package controllers;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.*;
import filters.UserAuth;
import org.apache.commons.io.FileUtils;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.*;
import redis.clients.jedis.Jedis;
import service.CartService;
import util.SysParCom;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static play.libs.Json.newObject;

/**
 * 评价controller
 * Created by howen on 16/4/26.
 */
public class RemarkCtrl extends Controller {

    @Inject
    private CartService cartService;

    @Inject
    @Named("uploadImagesActor")
    private ActorRef uploadImagesActor;

    @Inject
    private OrderCtrl orderCtrl;

    private static ObjectMapper mapper = new ObjectMapper();

    @Inject
    private Jedis jedis;

    /**
     * 插入评价信息
     *
     * @return result
     */
    @Security.Authenticated(UserAuth.class)
    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = 50 * 1024 * 1024)
    public Result submitRemark() {

        ObjectNode result = newObject();

        Long userId = (Long) ctx().args.get("userId");

        Http.MultipartFormData body = request().body().asMultipartFormData();

        Logger.error("请求数据--->\n" + request().body());

        Form<Remark> userForm = Form.form(Remark.class).bindFromRequest();

        if (userForm.hasErrors()) {
            Logger.error("校验错误: " + userForm.errorsAsJson().toString());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
            return ok(result);
        } else {
            try {
                Remark remark = userForm.get();
                remark.setUserId(userId);
                List<Http.MultipartFormData.FilePart> fileParts = body.getFiles();

                Remark remarkExist = new Remark();
                remarkExist.setUserId(userId);
                remarkExist.setSkuType(remark.getSkuType());
                remarkExist.setSkuTypeId(remark.getSkuTypeId());
                remarkExist.setOrderId(remark.getOrderId());

                List<Remark> remarkList = cartService.selectRemark(remarkExist);

                if (remarkList.size() > 0) {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.REMARK_EXISTS.getIndex()), Message.ErrorCode.REMARK_EXISTS.getIndex())));
                    return ok(result);
                } else {
                    if (cartService.insertRemark(remark)) {
                        if (!fileParts.isEmpty() && fileParts.size() < 6) {
                            Map<String, Object> mapActor = new HashMap<>();
                            List<byte[]> files = new ArrayList<>();
                            mapActor.put("remarkId", remark.getId());
                            for (Http.MultipartFormData.FilePart filePart : fileParts) {
                                if (!"image/jpeg".equalsIgnoreCase(filePart.getContentType()) && !"image/png".equalsIgnoreCase(filePart.getContentType())) {
                                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FILE_TYPE_NOT_SUPPORTED.getIndex()), Message.ErrorCode.FILE_TYPE_NOT_SUPPORTED.getIndex())));
                                    return ok(result);
                                } else {
                                    files.add(FileUtils.readFileToByteArray(filePart.getFile()));
                                }
                            }
                            mapActor.put("files", files);
                            mapActor.put("url", SysParCom.IMG_PROCESS_URL);
                            uploadImagesActor.tell(mapActor, ActorRef.noSender());
                        } else if (fileParts.size() > 5) {
                            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.UPLOAD_PICTURE_SIZES_OVER_LIMIT.getIndex()), Message.ErrorCode.UPLOAD_PICTURE_SIZES_OVER_LIMIT.getIndex())));
                            return ok(result);
                        }
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                        return ok(result);
                    } else {
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE.getIndex()), Message.ErrorCode.FAILURE.getIndex())));
                        return ok(result);
                    }
                }
            } catch (Exception ex) {
                Logger.error("server exception:" + ex.getMessage());
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
                return ok(result);
            }
        }
    }

    @Security.Authenticated(UserAuth.class)
    public Result orderRemarkList(Long orderId) {
        ObjectNode result = newObject();

        Long userId = (Long) ctx().args.get("userId");

        try {
            Order order = new Order();
            order.setOrderId(orderId);
            order.setUserId(userId);
            List<Order> orders = cartService.getOrder(order);
            if (orders != null && orders.size() == 1) {
                order = orders.get(0);
                if (order.getOrderStatus().equals("R")) {//订单状态为已收货状态
                    OrderLine orderLine = new OrderLine();
                    orderLine.setOrderId(orderId);
                    List<OrderLine> orderLines = cartService.selectOrderLine(orderLine);

                    List<Map<String, Object>> resultList = new ArrayList<>();

                    for (OrderLine orl : orderLines) {
                        Map<String, Object> map = new HashMap<>();

                        Remark remark = new Remark();
                        remark.setUserId(userId);
                        remark.setOrderId(orl.getOrderId());
                        remark.setSkuType(orl.getSkuType());
                        remark.setSkuTypeId(orl.getSkuTypeId());
                        List<Remark> remarkList = cartService.selectRemark(remark);
                        if (remarkList != null && remarkList.size() == 1) {
                            remark = remarkList.get(0);
                            if (remark.getPicture() != null) {
                                List<String> remarkPics = mapper.readValue(remark.getPicture(), mapper.getTypeFactory().constructCollectionType(List.class, String.class));
                                remarkPics = remarkPics.stream().map(pic -> SysParCom.IMAGE_URL + pic).collect(Collectors.toList());
                                remark.setPicture(Json.toJson(remarkPics).toString());
                            }
                            map.put("comment", remark);
                        }
                        CartSkuDto skuDto = new CartSkuDto();

                        //返回评价晒单页面商品信息
                        skuDto.setSkuId(orl.getSkuId());
                        skuDto.setAmount(orl.getAmount());
                        skuDto.setPrice(orl.getPrice());
                        skuDto.setSkuTitle(orl.getSkuTitle());


                        skuDto.setInvImg(orderCtrl.getInvImg(orl.getSkuImg()));
                        skuDto.setInvUrl(SysParCom.DEPLOY_URL + "/comm/detail/" + orl.getSkuType() + "/" + orl.getItemId() + "/" + orl.getSkuTypeId());

                        skuDto.setSkuType(orl.getSkuType());
                        skuDto.setSkuTypeId(orl.getSkuTypeId());
                        skuDto.setItemColor(orl.getSkuColor());
                        skuDto.setItemSize(orl.getSkuSize());
                        skuDto.setOrderId(orderId);
                        map.put("orderLine", skuDto);
                        resultList.add(map);
                    }
                    result.putPOJO("orderRemark", Json.toJson(resultList));
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                    return ok(result);
                } else {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ORDER_STATUS_EXCEPTION.getIndex()), Message.ErrorCode.ORDER_STATUS_EXCEPTION.getIndex())));
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
}
