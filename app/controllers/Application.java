package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.*;
import net.spy.memcached.MemcachedClient;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import service.CartService;
import service.SkuService;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

public class Application extends Controller {

    @Inject
    private MemcachedClient cache;

    @Inject
    private SkuService skuService;

    @Inject
    private CartService cartService;

    //图片服务器url
    public static final String IMAGE_URL = play.Play.application().configuration().getString("image.server.url");

    //发布服务器url
    public static final String DEPLOY_URL = play.Play.application().configuration().getString("deploy.server.url");

    //将Json串转换成List
    final static ObjectMapper mapper = new ObjectMapper();

    public Result getKey() {

        return ok(Encryption.getInstance().getPublicKey());
    }

    public Result getToken() {

        return ok(Encryption.handleEncrypt(Encryption.getInstance().getPrivateKey(), "123123"));
    }

    static class WrappedException extends RuntimeException {
        Throwable cause;

        WrappedException(Throwable cause) {
            this.cause = cause;
        }
    }

    static WrappedException throwWrapped(Throwable t) {
        throw new WrappedException(t);
    }

    public Result cart() {
        JsonNode json = request().body().asJson();
        ObjectNode result = Json.newObject();
        try {
            List<CartDto> cartDtoList = mapper.readValue(json.toString(), mapper.getTypeFactory().constructCollectionType(List.class, CartDto.class));
            Long userId = Long.valueOf(Json.parse(cache.get(request().getHeader("id-token")).toString()).findValue("id").asText());

            List<CartListDto> cartListDto = new ArrayList<>();

            for (CartDto cartDto : cartDtoList) {

                Cart cart = new Cart();
                cart.setSkuId(cartDto.getSkuId());
                cart.setUserId(userId);
                cart.setAmount(cartDto.getAmount());

                Sku sku = new Sku();
                sku.setId(cartDto.getSkuId());
                sku = skuService.getInv(sku);

                cart.setItemId(sku.getItemId());
                cart.setSkuTitle(sku.getInvTitle());

                //先确定商品状态是正常,否则直接存为失效商品
                if (!sku.getState().equals("Y")) {
                    cart.setStatus("S");
                } else {
                    if (cartDto.getState().equals("Y")) cart.setStatus("G");
                }

                //cartId为0,那么
                if (cartDto.getCartId() == 0) {
                    cartService.addCart(cart);
                } else {
                    cart.setCartId(cartDto.getCartId());
                }

            }
            //返回数据组装,根据用户id查询出所有可显示的购物车数据
            List<Cart> listCart = cartService.getCarts(userId);

            for (Cart cart : listCart) {

                Sku sku = new Sku();
                sku.setId(cart.getSkuId());
                sku = skuService.getInv(sku);

                //返回数据组装
                CartListDto cartList = new CartListDto();
                cartList.setCartId(cart.getCartId());
                cartList.setSkuId(cart.getSkuId());
                cartList.setAmount(cart.getAmount());
                cartList.setItemColor(sku.getItemColor());
                cartList.setItemSize(sku.getItemSize());
                cartList.setItemPrice(sku.getItemPrice());
                cartList.setState(cart.getStatus());
                cartList.setShipFee(sku.getShipFee());
                cartList.setInvArea(sku.getInvArea());
                cartList.setRestrictAmount(sku.getRestrictAmount());
                cartList.setRestAmount(sku.getRestAmount());
                cartList.setInvImg(IMAGE_URL + sku.getInvImg());
                cartList.setInvUrl(DEPLOY_URL + "/comm/detail/" + sku.getItemId() + "/" + sku.getId());
                cartList.setInvTitle(sku.getInvTitle());
                cartListDto.add(cartList);
            }
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            result.putPOJO("cartList", Json.toJson(cartListDto));
            Logger.error("返回数据" + result.toString());
            return ok(result);
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.toString());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    public Result cartList() {
        ObjectNode result = Json.newObject();
        try {
            Optional<String> token = Optional.of(cache.get(request().getHeader("id-token")).toString());

            if (token.isPresent()) {

                Long userId = Long.valueOf(Json.parse(token.get()).findValue("id").asText());

                List<CartListDto> cartListDto = new ArrayList<>();
                //返回数据组装,根据用户id查询出所有可显示的购物车数据

                List<Cart> listCart = cartService.getCarts(userId);

                for (Cart cart : listCart) {

                    Sku sku = new Sku();
                    sku.setId(cart.getSkuId());
                    sku = skuService.getInv(sku);

                    //返回数据组装
                    CartListDto cartList = new CartListDto();
                    cartList.setCartId(cart.getCartId());
                    cartList.setSkuId(cart.getSkuId());
                    cartList.setAmount(cart.getAmount());
                    cartList.setItemColor(sku.getItemColor());
                    cartList.setItemSize(sku.getItemSize());
                    cartList.setItemPrice(sku.getItemPrice());
                    cartList.setState(cart.getStatus());
                    cartList.setShipFee(sku.getShipFee());
                    cartList.setInvArea(sku.getInvArea());
                    cartList.setRestrictAmount(sku.getRestrictAmount());
                    cartList.setRestAmount(sku.getRestAmount());
                    cartList.setInvImg(IMAGE_URL + sku.getInvImg());
                    cartList.setInvUrl(DEPLOY_URL + "/comm/detail/" + sku.getItemId() + "/" + sku.getId());
                    cartList.setInvTitle(sku.getInvTitle());
                    cartListDto.add(cartList);
                }
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                result.putPOJO("cartList", Json.toJson(cartListDto));
                Logger.error("返回数据" + result.toString());
                return ok(result);
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_USER_TOKEN.getIndex()), Message.ErrorCode.BAD_USER_TOKEN.getIndex())));
                return ok(result);
            }
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.toString());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }

    }

    public Result cartUpdate() {
        JsonNode json = request().body().asJson();
        ObjectNode result = Json.newObject();
        Logger.error("客户端返回:" + json);
        try {
            Logger.error("测试用户信息:" + cache.get(request().getHeader("id-token")).toString());
            Long userId = Long.valueOf(Json.parse(cache.get(request().getHeader("id-token")).toString()).findValue("id").asText());
            List<Cart> list = cartService.getCarts(userId);
            List<Map> mapList = list.stream().map(c -> {
                Map<String, Object> map = new HashMap<>();
                map.put("cartId", c.getCartId());
                map.put("skuId", c.getSkuId());
                Sku sku = new Sku();
                sku.setId(c.getSkuId());
                try {
                    sku = skuService.getInv(sku);
                    //去库存表查看当前库存的状态和剩余量,如果剩余量为0或者状态不正常,就传客户端状态码为失效商品
                    if (sku.getRestAmount() != 0 && !sku.getState().equals("Y")) {
                        map.put("status", "I");
                    } else {
                        map.put("status", "S");
                        c.setStatus("S");
                        cartService.updateCart(c);
                    }
                } catch (Exception e) {
                    Logger.error("1005 更新购物车状态发生异常" + e.toString());
                }
                map.put("restAmount", sku.getRestAmount());
                map.put("invImg", sku.getInvImg());
                map.put("invTitle", sku.getInvTitle());
                map.put("itemId", sku.getItemId());
                map.put("price", sku.getItemPrice());//售价
                map.put("shipFee", sku.getShipFee());//邮费
                map.put("srcPrice", sku.getItemSrcPrice());//原价
                return map;
            }).collect(Collectors.toList());
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

}
