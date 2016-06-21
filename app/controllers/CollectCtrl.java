package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.*;
import filters.UserAuth;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import redis.clients.jedis.Jedis;
import service.CartService;
import service.PromotionService;
import service.SkuService;
import util.ComUtil;
import util.SysParCom;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static play.libs.Json.newObject;
import static play.libs.Json.toJson;

/**
 * 收藏   redis中存储方式是<key,Map<串,1>>
 * Created by sibyl.sun on 16/6/20.
 */
public class CollectCtrl  extends Controller {

    @Inject
    private SkuService skuService;

    @Inject
    private CartService cartService;

    @Inject
    private PromotionService promotionService;

    @Inject
    private Jedis jedis;


    @Inject
    private ComUtil comUtil;


    private static ObjectMapper mapper = new ObjectMapper();


//    /**
//     * 收藏
//     *
//     * @return Result
//     */
//    @Security.Authenticated(UserAuth.class)
//    public Result submitCollect() {
//
//        ObjectNode result = newObject();
//        Optional<JsonNode> json = Optional.ofNullable(request().body().asJson());
//        /*******取用户ID*********/
//        Long userId = (Long) ctx().args.get("userId");
//        try {
//            if (json.isPresent() && json.get().size() > 0) {
//                //客户端发过来的收藏数据
//                CollectSubmitDTO collectSubmitDTO = mapper.readValue(json.get().toString(), mapper.getTypeFactory().constructType(CollectSubmitDTO.class));
//                //用户收藏信息
//                Collect collect = new Collect();
//                collect.setUserId(userId);
//                collect.setSkuId(collectSubmitDTO.getSkuId());
//                collect.setSkuType(collectSubmitDTO.getSkuType());
//                collect.setSkuTypeId(collectSubmitDTO.getSkuTypeId());
//                //判断是否已经收藏
//                Optional<List<Collect>> collectList = Optional.ofNullable(cartService.selectCollect(collect));
//                if (!(collectList.isPresent() && collectList.get().size() > 0)) { //未收藏
//                    collect = createCollect(userId, collectSubmitDTO);
//                } else {
//                    collect = collectList.get().get(0);
//                }
//                if (null != collect) {
//                    result.putPOJO("collectId", collect.getCollectId());
//                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
//                    return ok(result);
//                }
//            }
//
//        } catch (Exception ex) {
//            Logger.error("server exception:" + ex.getMessage());
//            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
//            return ok(result);
//        }
//        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
//        return ok(result);
//    }
//
//
//    /**
//     * 创建收藏数据
//     *
//     * @param userId
//     * @param collectSubmitDTO
//     * @return
//     * @throws Exception
//     */
//    private Collect createCollect(Long userId, CollectSubmitDTO collectSubmitDTO) throws Exception {
//        Collect collect = new Collect();
//        collect.setUserId(userId);
//        collect.setSkuId(collectSubmitDTO.getSkuId());
//        collect.setSkuType(collectSubmitDTO.getSkuType());
//        collect.setSkuTypeId(collectSubmitDTO.getSkuTypeId());
//        if (cartService.insertCollect(collect)) {
//            return collect;
//        }
//        return null;
//    }
//
//    /**
//     * 删除收藏
//     *
//     * @param collectId
//     * @return
//     */
//    @Security.Authenticated(UserAuth.class)
//    public Result delCollect(Long collectId) {
//        ObjectNode result = newObject();
//        try {
//            /*******取用户ID*********/
//            Long userId = (Long) ctx().args.get("userId");
//            Collect collect = new Collect();
//            collect.setCollectId(collectId);
//            collect.setUserId(userId);
//            if (collectId > 0 && cartService.deleteCollect(collect)) {
//                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
//                return ok(result);
//            }
//
//        } catch (Exception ex) {
//            Logger.error("server exception:" + ex.getMessage());
//            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
//            return ok(result);
//        }
//        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
//        return ok(result);
//    }
//
//    /***
//     * 获取所有收藏数据
//     *
//     * @return
//     */
//    @Security.Authenticated(UserAuth.class)
//    public Result getCollect() {
//
//        ObjectNode result = newObject();
//        try {
//            Long userId = (Long) ctx().args.get("userId");
//            Collect collect = new Collect();
//            collect.setUserId(userId);
//            List<CollectDto> collectDtoList = new ArrayList<CollectDto>();
//            Optional<List<Collect>> collectList = Optional.ofNullable(cartService.selectCollect(collect));
//            if (collectList.isPresent() && collectList.get().size() > 0) {
//                for (Collect c : collectList.get()) {
//                    CollectDto collectDto = new CollectDto();
//                    collectDto.setCollectId(c.getCollectId());
//                    collectDto.setCreateAt(c.getCreateAt());
//                    collectDto.setSkuType(c.getSkuType());
//                    collectDto.setSkuTypeId(c.getSkuTypeId());
//
//                    Sku sku = new Sku();
//                    sku.setId(c.getSkuId());
//                    sku = skuService.getInv(sku);
//                    if (null == sku) {
//                        Logger.warn("collect sku not exist ,skuId=" + c.getSkuId());
//                        continue;
//                    }
//                    CartSkuDto skuDto = new CartSkuDto();
//                    skuDto.setSkuId(c.getSkuId());
//                    skuDto.setSkuTitle(sku.getInvTitle());
//                    skuDto.setAmount(sku.getAmount());
//                    skuDto.setPrice(sku.getItemPrice());
//                    skuDto.setInvUrl(SysParCom.DEPLOY_URL + "/comm/detail/" + c.getSkuType() + "/" + sku.getItemId() + "/" + c.getSkuTypeId());
//
//                    //跳转地址
//                    if ("pin".equals(c.getSkuType())) {
//                        PinSku pinSku = promotionService.getPinSkuById(c.getSkuTypeId());
//                        if (null == pinSku) {
//                            Logger.warn("collect pin sku not exist ,pinSkuId=" + c.getSkuTypeId());
//                            continue;
//                        }
//                        JsonNode jsonNode = Json.parse(pinSku.getFloorPrice()); //拼购取最低价
//                        skuDto.setPrice(new BigDecimal(jsonNode.get("price").asText()));
//
//                    }
//                    skuDto.setInvImg(getInvImg(sku.getInvImg()));
//
//                    skuDto.setItemColor(sku.getItemColor());
//                    skuDto.setItemSize(sku.getItemSize());
//
//                    collectDto.setCartSkuDto(skuDto);
//                    collectDtoList.add(collectDto);
//
//                }
//            }
//            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
//            result.putPOJO("collectList", Json.toJson(collectDtoList));
//
//            return ok(result);
//        } catch (Exception ex) {
//            Logger.error("server exception:" + ex.getMessage());
//            Logger.error("server exception:", ex);
//            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
//            return ok(result);
//        }
//    }

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
                String userCollectKey=getUserCollectKey(userId);
                Long collectId=makeCollectId();
                Collect collect=new Collect();
                collect.setCollectId(collectId);
                collect.setUserId(userId);
                collect.setCreateAt(new Timestamp(System.currentTimeMillis()));
                collect.setSkuId(collectSubmitDTO.getSkuId());
                collect.setSkuType(collectSubmitDTO.getSkuType());
                collect.setSkuTypeId(collectSubmitDTO.getSkuTypeId());
                //添加商品收藏数据到redis
                jedis.hsetnx(userCollectKey,collectId+"",toJson(collect).toString());

                result.putPOJO("collectId", collectId);
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
            String userCollectKey=getUserCollectKey(userId);
            //redis删除收藏
            jedis.hdel(userCollectKey,collectId+"");
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            return ok(result);
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
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
            String userCollectKey=getUserCollectKey(userId);
            List<String> collectList=jedis.hvals(userCollectKey);
            List<CollectDto> collectDtoList = new ArrayList<CollectDto>();
            if (null!=collectList&&!collectList.isEmpty()) {
                Long skuId=0L,skuTypeId=0L;String skuType="";
                Collect collect;
                for (String str:collectList) {
                    collect=Json.fromJson(Json.parse(str),Collect.class);
                    skuId=collect.getSkuId();
                    skuType=collect.getSkuType();
                    skuTypeId=collect.getSkuTypeId();

                    CollectDto collectDto = new CollectDto();
                    collectDto.setCollectId(collect.getCollectId());
                    collectDto.setCreateAt(collect.getCreateAt());
                    collectDto.setSkuType(skuType);
                    collectDto.setSkuTypeId(skuTypeId);

                    Sku sku = new Sku();
                    sku.setId(skuId);
                    sku = skuService.getInv(sku);
                    if (null == sku) {
                        Logger.warn("collect sku not exist ,skuId=" + skuId);
                        continue;
                    }
                    CartSkuDto skuDto = new CartSkuDto();
                    skuDto.setSkuId(skuId);
                    skuDto.setSkuTitle(sku.getInvTitle());
                    skuDto.setAmount(sku.getAmount());
                    skuDto.setPrice(sku.getItemPrice());
                    skuDto.setInvUrl(SysParCom.DEPLOY_URL + "/comm/detail/" + skuType + "/" + sku.getItemId() + "/" + skuTypeId);

                    //跳转地址
                    if ("pin".equals(skuType)) {
                        PinSku pinSku = promotionService.getPinSkuById(skuTypeId);
                        if (null == pinSku) {
                            Logger.warn("collect pin sku not exist ,pinSkuId=" + skuTypeId);
                            continue;
                        }
                        JsonNode jsonNode = Json.parse(pinSku.getFloorPrice()); //拼购取最低价
                        skuDto.setPrice(new BigDecimal(jsonNode.get("price").asText()));

                    }
                    skuDto.setInvImg(comUtil.getInvImg(sku.getInvImg()));

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
     * 获取用户收藏的键值
     * @param userId
     * @return
     */
    private String getUserCollectKey(Long userId){
        return "collect-"+userId;
    }
    /**
     * 收藏的collectId
     * @return
     */
    private Long makeCollectId(){
        String time=System.currentTimeMillis()+"";
        return Long.valueOf(time.substring(4,time.length()));
    }
}
