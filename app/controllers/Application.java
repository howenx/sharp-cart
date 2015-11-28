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
import service.IdService;
import service.SkuService;

import javax.inject.Inject;
import java.util.*;

public class Application extends Controller {

    @Inject
    private SkuService skuService;

    @Inject
    private CartService cartService;

    @Inject
    private IdService idService;

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

        Optional<JsonNode> json = Optional.ofNullable(request().body().asJson());

        ObjectNode result = Json.newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");
            if (json.isPresent()) {
                List<CartDto> cartDtoList = mapper.readValue(json.get().toString(), mapper.getTypeFactory().constructCollectionType(List.class, CartDto.class));

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

                    if (cartDto.getCartId() == 0) {
                        if (cart.getStatus().equals("I") || cart.getStatus().equals("G")) {

                            List<Cart> carts = cartService.getCartByUserSku(cart);
                            //cartId为0,有两种情况,1种情况是,当购物车中没有出现同一个userId,skuId,状态为I,G的商品时候才去insert,否则是update
                            if (carts.size() > 0) {
                                cart.setCartId(carts.get(0).getCartId());//获取到登录状态下中已经存在的购物车ID,然后update
                                cart.setAmount(cart.getAmount() + carts.get(0).getAmount());//购买数量累加
                                cartService.updateCart(cart);
                            } else {
                                //cartId为0,有两种情况,1种情况是,当购物车中没有出现同一个userId,skuId,状态为I,G的商品时候才去insert
                                cartService.addCart(cart);
                            }
                        }
                    } else {
                        cart.setCartId(cartDto.getCartId());
                        cartService.updateCart(cart);
                    }
                }
            }
            Logger.error("用户的ID:" + userId);
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
     * 登录状态下获取用户的购物车列表
     *
     * @return json
     */
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
//            result.putPOJO("cartList", Json.toJson(cartAll(userId)));
            Logger.error("返回数据" + result.toString());
            return ok(result);
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.toString());
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
        JsonNode json = request().body().asJson();
        ObjectNode result = Json.newObject();
        List<CartListDto> cartListDto = new ArrayList<>();
        try {
            List<CartDto> cartDtoList = mapper.readValue(json.toString(), mapper.getTypeFactory().constructCollectionType(List.class, CartDto.class));

            Logger.error("又问问题:" + cartDtoList.toString());

            for (CartDto cartDto : cartDtoList) {

                Sku sku = new Sku();
                sku.setId(cartDto.getSkuId());
                sku = skuService.getInv(sku);

                //返回数据组装
                CartListDto cartList = new CartListDto();

                cartList.setCartId(cartDto.getCartId());
                cartList.setSkuId(cartDto.getSkuId());
                cartList.setAmount(cartDto.getAmount());
                cartList.setItemColor(sku.getItemColor());
                cartList.setItemSize(sku.getItemSize());
                cartList.setItemPrice(sku.getItemPrice());
                cartList.setState(cartDto.getState());
                cartList.setShipFee(sku.getShipFee());
                cartList.setInvArea(sku.getInvArea());
                cartList.setRestrictAmount(sku.getRestrictAmount());
                cartList.setRestAmount(sku.getRestAmount());
                cartList.setInvImg(IMAGE_URL + sku.getInvImg());
                cartList.setInvUrl(DEPLOY_URL + "/comm/detail/" + sku.getItemId() + "/" + sku.getId());

                if (cartDto.getCartId() == 0) {
                    cartList.setCartDelUrl("");
                } else cartList.setCartDelUrl(DEPLOY_URL + "/client/cart/del/" + cartDto.getCartId());
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

    /**
     * 用户查询订单接口
     *
     * @return 返回所有订单数据
     */
    @Security.Authenticated(UserAuth.class)
    public Result shoppingOrder() {
        ObjectNode result = Json.newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");
//            Long userId =  ((Integer)1000012).longValue();
            Order order = new Order();
            order.setUserId(userId);
            List<Order> orderList = cartService.getOrderBy(order);

            //返回总数据
            List<Map> mapList = new ArrayList<>();

            for (Order o : orderList) {

                //用于保存每个订单对应的明细list和地址信息
                Map<String, Object> map = new HashMap<>();

                //根据订单ID获取所有购物车内容
                Cart c = new Cart();
                c.setUserId(userId);
                c.setOrderId(o.getOrderId());
                List<Cart> carts = cartService.getCarts(c);

                //每个订单对应的商品明细
                List<CartSkuDto> skuDtoList = new ArrayList<>();

                for (Cart cart : carts) {
                    CartSkuDto skuDto = new CartSkuDto();

                    //获取每个库存信息
                    Sku sku = new Sku();


                    sku.setId(cart.getSkuId());
                    sku = skuService.getInv(sku);

                    Logger.error("测试库存单元:" + sku.toString());
                    //组装返回的订单商品明细
                    skuDto.setSkuId(sku.getId());
                    skuDto.setAmount(cart.getAmount());
                    skuDto.setPrice(cart.getPrice());
                    skuDto.setSkuTitle(sku.getInvTitle());
                    skuDto.setInvImg(IMAGE_URL + sku.getInvImg());
                    skuDto.setInvUrl(DEPLOY_URL + "/comm/detail/" + sku.getItemId() + "/" + sku.getId());
                    skuDto.setItemColor(sku.getItemColor());
                    skuDto.setItemSize(sku.getItemSize());

                    skuDtoList.add(skuDto);

                }

                //获取地址信息
                Address address = new Address();
                address.setAddId(o.getAddId());
                address = idService.getAddress(address);

                //组装每个订单对应的明细和地址
                map.put("order", o);
                map.put("sku", skuDtoList);
                map.put("address", address);

                mapList.add(map);
            }

            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            result.putPOJO("orderList", Json.toJson(mapList));
            Logger.error("返回数据" + result.toString());
            return ok(result);
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.toString());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    /**
     * 只供更改订单状态使用
     *
     * @return 返回更新是否成功
     */
    @Security.Authenticated(UserAuth.class)
    public Result updateOrderState() {
        JsonNode json = request().body().asJson();//{"orderId":1231231,"status":"C"}
        ObjectNode result = Json.newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");
            Order order = new Order();
            order.setUserId(userId);
            order.setOrderId(Long.valueOf(json.get("orderId").toString()));
            order.setOrderStatus(json.get("status").toString());
            if (cartService.updateOrder(order)) {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            } else
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.DATABASE_EXCEPTION.getIndex()), Message.ErrorCode.DATABASE_EXCEPTION.getIndex())));
            return ok(result);
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.toString());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    private List<CartListDto> cartAll(Long userId) throws Exception {

        List<CartListDto> cartListDto = new ArrayList<>();

        Cart c = new Cart();
        c.setUserId(userId);

        //返回数据组装,根据用户id查询出所有可显示的购物车数据
        List<Cart> listCart = cartService.getCarts(c);

        Logger.error("尼玛的:" + listCart);

        for (Cart cart : listCart) {

            Sku sku = new Sku();
            sku.setId(cart.getSkuId());
            sku = skuService.getInv(sku);

            //先确定商品状态是正常,否则直接存为失效商品
            if (!sku.getState().equals("Y")) {
                cart.setStatus("S");
            }

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
            cartList.setCartDelUrl(SHOPPING_URL + "/client/cart/del/" + cart.getCartId());
            cartList.setInvTitle(sku.getInvTitle());
            cartList.setCreateAt(cart.getCreateAt());
            cartListDto.add(cartList);
        }

//        Comparator<CartListDto> byCreateDate = new Comparator<CartListDto>() {
//            public int compare(CartListDto left, CartListDto right) {
//                if (left.getCreateAt().before(right.getCreateAt())) {
//                    return 1;
//                } else {
//                    return -1;
//                }
//            }
//        };
//        Logger.error("排序前的:" + listCart);
//        cartListDto = cartListDto
//                .parallelStream()
//                .sorted((e1, e2) -> e1.getCreateAt().compareTo(e2.getCreateAt()))
//                .sorted((e1, e2) -> e1.getSkuId().compareTo(e2.getSkuId()))
//                .collect(Collectors.toList());

//        Logger.error("排序后的:" + listCart);

        return cartListDto;
    }
}
