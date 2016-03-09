package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.*;
import filters.UserAuth;
import middle.CartMid;
import modules.LevelFactory;
import modules.NewScheduler;
import play.Logger;
import play.api.libs.Codecs;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import service.CartService;
import service.SkuService;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Application extends Controller {

    @Inject
    private SkuService skuService;

    @Inject
    private CartService cartService;

    @Inject
    private CartMid cartMid;

    @Inject
    private ActorSystem system;

    @Inject
    @Named("schedulerCancelOrderActor")
    private ActorRef schedulerCancelOrderActor;

    @Inject
    private NewScheduler newScheduler;

    @Inject
    private LevelFactory levelFactory;

    //将Json串转换成List
    public final static ObjectMapper mapper = new ObjectMapper();

    public static final Timeout TIMEOUT = new Timeout(100, TimeUnit.MILLISECONDS);


    /**
     * 用户登陆后同步所有购物车商品,以及详细页面点击Add时候掉用接口
     *
     * @return json
     */
    @Security.Authenticated(UserAuth.class)
    public Result cart() {

        ObjectNode result = Json.newObject();

        Optional<JsonNode> json = Optional.ofNullable(request().body().asJson());

        try {
            Long userId = (Long) ctx().args.get("userId");
            if (json.isPresent() && json.get().size() > 0) {
                List<CartDto> cartDtoList = mapper.convertValue(json.get(), mapper.getTypeFactory().constructCollectionType(List.class, CartDto.class));
                List<CartPar> cartPars = cartMid.createCart(userId, cartDtoList);
                result.putPOJO("cartList", Json.toJson(cartMid.getCarts(userId).get()));
                for (CartPar cp : cartPars) {
                    if (cp.getRestrictMessageCode() != null && cp.getRestrictMessageCode() == Message.ErrorCode.PURCHASE_QUANTITY_LIMIT.getIndex()) {
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(cp.getRestrictMessageCode()), cp.getRestrictMessageCode())));
                        return ok(result);
                    } else if (cp.getRestMessageCode() != null && cp.getRestrictMessageCode() == Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex()) {
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(cp.getRestMessageCode()), cp.getRestMessageCode())));
                        return ok(result);
                    }
                }
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(result);
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
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
     * 登录状态下获取用户的购物车列表
     *
     * @return json
     */
    @Security.Authenticated(UserAuth.class)
    public Result cartList() {
        ObjectNode result = Json.newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");
            Optional<List<CartItemDTO>> dtos = cartMid.getCarts(userId);
            if (dtos.isPresent()) {
//                Logger.error("购物车数据:\n"+Json.toJson(dtos.get()));
                result.putPOJO("cartList", Json.toJson(dtos.get()));
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            } else
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.CART_LIST_NULL_EXCEPTION.getIndex()), Message.ErrorCode.CART_LIST_NULL_EXCEPTION.getIndex())));
            return ok(result);
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.error("server exception:" + ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    /**
     * 删除购物车
     *
     * @param cartId 购物车主键
     * @return  所有购物车数据
     */
    @Security.Authenticated(UserAuth.class)
    public Result delCart(Long cartId) {
        ObjectNode result = Json.newObject();
        try {
            Cart cart = new Cart();
            cart.setCartId(cartId);
            cart.setStatus("N");
            cartService.updateCart(cart);

            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            return ok(result);
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }


    /**
     * 未校验用户情形下返回所有sku信息
     *
     * @return 购物车商品库存信息
     */
    public Result getCartSku() {

        ObjectNode result = Json.newObject();
        Optional<JsonNode> json = Optional.ofNullable(request().body().asJson());
        try {
            if (json.isPresent()) {
                List<CartDto> cartDtoList = mapper.readValue(json.get().toString(), mapper.getTypeFactory().constructCollectionType(List.class, CartDto.class));

                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                result.putPOJO("cartList", Json.toJson(cartMid.getCarts(cartDtoList).get()));
                return ok(result);
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
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
     * 登录与未登录状态下校验加入购物车数量是否超出或者商品是否失效
     *
     * @param skuId  库存ID
     * @param amount 数量
     * @return result
     */
    public Result verifySkuAmount(Long skuId, Integer amount) {
        ObjectNode result = Json.newObject();
        try {
            Sku sku = new Sku();
            sku.setId(skuId);
            Optional<Sku> skuOptional = Optional.ofNullable(skuService.getInv(sku));
            if (skuOptional.isPresent()) {
                sku = skuOptional.get();
                if (sku.getState().equals("S")) {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SKU_INVALID.getIndex()), Message.ErrorCode.SKU_INVALID.getIndex())));
                    return ok(result);
                } else if (sku.getRestrictAmount() != 0 && amount > sku.getRestrictAmount()) {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.PURCHASE_QUANTITY_LIMIT.getIndex()), Message.ErrorCode.PURCHASE_QUANTITY_LIMIT.getIndex())));
                    return ok(result);
                } else if (amount > sku.getRestAmount()) {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex()), Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex())));
                    return ok(result);
                } else {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                    return ok(result);
                }
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SKU_DETAIL_NULL_EXCEPTION.getIndex()), Message.ErrorCode.SKU_DETAIL_NULL_EXCEPTION.getIndex())));
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
     * 处理系统启动时候去做第一次请求,完成对定时任务的执行
     *
     * @return string
     */
    public Result getFirstApp(String cipher) {
        if (Codecs.md5("hmm-100901".getBytes()).equals(cipher)) {
            List<Persist> persists;
            try {
                persists = levelFactory.iterator();
                if (persists != null && persists.size() > 0) {
                    Logger.info("遍历所有持久化schedule---->\n" + persists);
                    for (Persist p : persists) {

                        ActorSelection sel = system.actorSelection(p.getActorPath());
                        Future<ActorRef> fut = sel.resolveOne(TIMEOUT);
                        ActorRef ref = Await.result(fut, TIMEOUT.duration());

                        if (p.getType().equals("scheduleOnce")){
                            Long time = p.getDelay() - (new Date().getTime() - p.getCreateAt().getTime());
                            Logger.info("重启后scheduleOnce执行时间---> " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(new Date().getTime()+time)));
                            if (time > 0) {
                                newScheduler.scheduleOnce(Duration.create(time, TimeUnit.MILLISECONDS), ref, p.getMessage());
                            } else {
                                levelFactory.delete(p.getMessage());
                                system.actorSelection(p.getActorPath()).tell(p.getMessage(), ActorRef.noSender());
                            }
                        }else if (p.getType().equals("schedule")){
                            newScheduler.schedule(Duration.create(p.getInitialDelay(), TimeUnit.MILLISECONDS),Duration.create(p.getDelay(), TimeUnit.MILLISECONDS), ref, p.getMessage());
                            Logger.info("重启后schedule执行---> 每隔 " + Duration.create(p.getDelay(), TimeUnit.MILLISECONDS).toHours()+" 小时执行一次");
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return notFound("error");
            }
            return ok("success");
        } else throw new NullPointerException(cipher);
    }

    public Result hello() {
        newScheduler.scheduleOnce(Duration.create(180, TimeUnit.SECONDS), schedulerCancelOrderActor, 77701093L);
        return ok("hello");
    }
}
