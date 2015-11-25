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
import service.CartService;
import service.SkuService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class Application extends Controller {

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


    @Security.Authenticated(UserAuth.class)
    public Result cart() {
        JsonNode json = request().body().asJson();
        ObjectNode result = Json.newObject();
        try {
            List<CartDto> cartDtoList = mapper.readValue(json.toString(), mapper.getTypeFactory().constructCollectionType(List.class, CartDto.class));
            Long userId = (Long) ctx().args.get("userId");

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
                    cart.setStatus(cartDto.getState());
                }

                //cartId为0,insert,否则update
                if (cartDto.getCartId() == 0) {
                    cartService.addCart(cart);
                } else {
                    cart.setCartId(cartDto.getCartId());
                    cartService.updateCart(cart);
                }

            }

            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            result.putPOJO("cartList", Json.toJson(cartAll(userId)));
            Logger.error("返回数据" + result.toString());
            return ok(result);

        } catch (Exception ex) {
            Logger.error("server exception:" + ex.toString());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    @Security.Authenticated(UserAuth.class)
    public Result cartList() {
        ObjectNode result = Json.newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            result.putPOJO("cartList", Json.toJson(cartAll(userId)));
            Logger.error("返回数据" + result.toString());
            return ok(result);
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.toString());
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
            Cart cartu = new Cart();
            cartu.setCartId(cartId);
            cartu.setStatus("N");
            cartService.updateCart(cartu);

            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            result.putPOJO("cartList", Json.toJson(cartAll(userId)));
            Logger.error("返回数据" + result.toString());
            return ok(result);
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.toString());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    private List<CartListDto> cartAll(Long userId) throws Exception {

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
            cartList.setCartDelUrl(DEPLOY_URL + "/client/cart/del/" + cart.getCartId());
            cartList.setInvTitle(sku.getInvTitle());
            cartListDto.add(cartList);
        }
        return cartListDto;
    }
}
