package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.*;
import filters.UserAuth;
import middle.CartMid;
import net.spy.memcached.MemcachedClient;
import play.Logger;
import play.api.libs.Codecs;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import scala.concurrent.duration.Duration;
import service.CartService;
import service.IdService;
import service.SkuService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Application extends Controller {

    @Inject
    private SkuService skuService;

    @Inject
    private CartService cartService;

    @Inject
    private IdService idService;

    @Inject
    private MemcachedClient cache;

    @Inject
    private CartMid cartMid;

    @Inject
    private ActorSystem system;

    @Inject
    @Named("schedulerCancelOrderActor")
    private ActorRef schedulerCancelOrderActor;

    //图片服务器url
    public static final String IMAGE_URL = play.Play.application().configuration().getString("image.server.url");

    //发布服务器url
    public static final String DEPLOY_URL = play.Play.application().configuration().getString("deploy.server.url");

    //shopping服务器url
    public static final String SHOPPING_URL = play.Play.application().configuration().getString("shopping.server.url");

    //id服务器url
    public static final String ID_URL = play.Play.application().configuration().getString("id.server.url");

    //将Json串转换成List
    final static ObjectMapper mapper = new ObjectMapper();


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
     * @return string
     */
    public Result getFirstApp(String cipher){
        if (Codecs.md5("hmm-100901".getBytes()).equals(cipher)){

            Cancellable cl = system.scheduler()
                    .schedule(Duration.Zero(),
                            Duration.create(2, TimeUnit.SECONDS),
                            schedulerCancelOrderActor,
                            77701021L,
                            system.dispatcher(),
                            null);

            system.scheduler().scheduleOnce(Duration.create(6, TimeUnit.SECONDS), () -> {
                Logger.error("取消定时任务:----> "+cl.cancel());
            },system.dispatcher());

        }
        return ok("success");
    }
}
