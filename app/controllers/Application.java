package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import domain.*;
import filters.UserAuth;
import middle.CartMid;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import service.CartService;
import service.SkuService;
import util.ComUtil;
import util.RedisPool;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class Application extends Controller {

    @Inject
    private SkuService skuService;

    @Inject
    private CartService cartService;

    @Inject
    private CartMid cartMid;


    @Inject
    private ComUtil comUtil;



    //将Json串转换成List
    public final static ObjectMapper mapper = new ObjectMapper();


    /**
     * 用户登陆后同步所有购物车商品,以及详细页面点击Add时候掉用接口,以及点击购物车列表中的加减时
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
                for (CartPar cp : cartPars) {
                    if (cp.getRestrictMessageCode() != null && cp.getRestrictMessageCode() == Message.ErrorCode.PURCHASE_QUANTITY_LIMIT.getIndex()) {
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(cp.getRestrictMessageCode()), cp.getRestrictMessageCode())));
                        return ok(result);
                    } else if (cp.getRestMessageCode() != null && cp.getRestrictMessageCode() == Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex()) {
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(cp.getRestMessageCode()), cp.getRestMessageCode())));
                        return ok(result);
                    } else if (cp.getRestMessageCode() != null) {//加入购物车商品有错误
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(cp.getRestMessageCode()), cp.getRestMessageCode())));
                        return ok(result);
                    }
                }

                Optional<List<CartItemDTO>> cartItemDTOList = cartMid.getCarts(userId);

                if (cartItemDTOList.isPresent() && cartItemDTOList.get().size() > 0) {
                    result.putPOJO("cartList", Json.toJson(cartItemDTOList.get()));
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                    return ok(result);
                } else {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.CART_LIST_NULL_EXCEPTION.getIndex()), Message.ErrorCode.CART_LIST_NULL_EXCEPTION.getIndex())));
                    return ok(result);
                }
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
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
            Logger.error("server exception:" + Throwables.getStackTraceAsString(ex));
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    /***
     * 登录状态下购物车商品勾选和取消勾选
     *
     * @return result
     */
    @Security.Authenticated(UserAuth.class)
    public Result cartCheck() {
        ObjectNode result = Json.newObject();
        try (Jedis jedis = RedisPool.createPool().getResource()) {
            Long userId = (Long) ctx().args.get("userId");
            Optional<JsonNode> json = Optional.ofNullable(request().body().asJson());

            if (json.isPresent()) {

                List<CartDto> cartDtoList = mapper.readValue(json.get().toString(), mapper.getTypeFactory().constructCollectionType(List.class, CartDto.class));
                cartDtoList.forEach(cartDto -> {
                    if (cartDto.getOrCheck().equals("N")) {
                        jedis.srem("cart-" + userId, cartDto.getCartId().toString());
                    } else if (cartDto.getOrCheck().equals("Y")) {
                        jedis.sadd("cart-" + userId, cartDto.getCartId().toString());
                    }
                });
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(result);
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
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
     * 删除购物车
     *
     * @param cartId 购物车主键
     * @return  所有购物车数据
     */
    @Security.Authenticated(UserAuth.class)
    public Result delCart(Long cartId) {
        ObjectNode result = Json.newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");
            Cart cart = new Cart();
            cart.setCartId(cartId);
            cart.setStatus("N");
            cartService.updateCart(cart);

            try (Jedis jedis = RedisPool.createPool().getResource()) {
                //判断redis里是否存有此cartId,有表示勾选,没有表示未勾选
                if (jedis.sismember("cart-" + userId, cart.getCartId().toString())) {
                    jedis.srem("cart-" + userId, cart.getCartId().toString());
                }
            }

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
            Logger.error("server exception:" + Throwables.getStackTraceAsString(ex));
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    /**
     * 未登录状态下校验加入购物车数量是否超出或者商品是否失效
     *
     * @return result
     */
    public Result verifySkuAmount() {

        ObjectNode result = Json.newObject();

        Optional<JsonNode> json = Optional.ofNullable(request().body().asJson());

        try {
            if (json.isPresent() && json.get().size() > 0) {
                CartDto cartDto = mapper.convertValue(json.get(), mapper.getTypeFactory().constructType(CartDto.class));
                SkuVo skuVo = new SkuVo();
                skuVo.setSkuTypeId(cartDto.getSkuTypeId());
                skuVo.setSkuType(cartDto.getSkuType());

                Optional<List<SkuVo>> skuVos = Optional.ofNullable(skuService.getAllSkus(skuVo));

                if (skuVos.isPresent()) {
                    skuVo = skuVos.get().get(0);

                    if (skuVo.getSkuTypeStatus().equals("S")) {
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SKU_INVALID.getIndex()), Message.ErrorCode.SKU_INVALID.getIndex())));
                        return ok(result);
                    } else if (comUtil.isOutOfRestrictAmount(cartDto.getAmount(), skuVo)) {
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.PURCHASE_QUANTITY_LIMIT.getIndex()), Message.ErrorCode.PURCHASE_QUANTITY_LIMIT.getIndex())));
                        return ok(result);
                    } else if (cartDto.getAmount() > skuVo.getRestAmount()) {
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex()), Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex())));
                        return ok(result);
                    } else if (cartDto.getSkuType().equals("vary")) {
                        Integer varyAmount = validateVary(skuVo.getSkuTypeId(), cartDto.getAmount());
                        if (varyAmount == null || varyAmount < 0) {
                            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.VARY_OVER_LIMIT.getIndex()), Message.ErrorCode.VARY_OVER_LIMIT.getIndex())));
                            return ok(result);
                        } else {
                            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                            return ok(result);
                        }
                    } else {
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                        return ok(result);
                    }
                } else {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.NOT_FOUND_SKU.getIndex()), Message.ErrorCode.NOT_FOUND_SKU.getIndex())));
                    return ok(result);
                }
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
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
     * 校验vary类型商品的卖出数量是否超出,超出返回true,否则返回false
     *
     * @param varyId varyId
     * @param amount amount
     * @return Boolean
     */
    public Integer validateVary(Long varyId, Integer amount) {
        VaryPrice varyPrice = new VaryPrice();
        varyPrice.setId(varyId);
        List<VaryPrice> varyPriceList = skuService.getVaryPriceBy(varyPrice);
        if (varyPriceList.size() > 0) {
            varyPrice = varyPriceList.get(0);
            return varyPrice.getLimitAmount() - (varyPrice.getSoldAmount() + amount);
        }
        return null;
    }
}
