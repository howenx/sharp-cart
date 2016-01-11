package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.*;
import filters.UserAuth;
import net.spy.memcached.MemcachedClient;
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
import java.util.stream.Collectors;

public class Application extends Controller {

    @Inject
    private SkuService skuService;

    @Inject
    private CartService cartService;

    @Inject
    private IdService idService;

    @Inject
    private MemcachedClient cache;

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
        Boolean S_FLAG = false;

        List<Long> sCartIds = new ArrayList<>();

        ObjectNode result = Json.newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");

            Boolean flag_restrict =false;//标志是否是超出限购数量
            Boolean flag_rest  = false;//标志是否超出最大库存量

            if (json.isPresent() && json.get().size() > 0) {
                List<CartDto> cartDtoList = mapper.readValue(json.get().toString(), mapper.getTypeFactory().constructCollectionType(List.class, CartDto.class));

                for (CartDto cartDto : cartDtoList) {

                    Cart cart = new Cart();
                    cart.setSkuId(cartDto.getSkuId());
                    cart.setUserId(userId);

                    Sku sku = new Sku();
                    sku.setId(cartDto.getSkuId());
                    sku = skuService.getInv(sku);

                    cart.setItemId(sku.getItemId());
                    cart.setSkuTitle(sku.getInvTitle());

                    cart.setSkuImg(sku.getInvImg());

                    //先确定商品状态是正常,否则直接存为失效商品
                    if (!sku.getState().equals("Y")) {
                        cart.setStatus("S");
                        S_FLAG = true;

                    } else {
                        cart.setStatus(cartDto.getState());
                    }

                    if (sku.getRestrictAmount() != 0 && sku.getRestrictAmount() < cartDto.getAmount()) {
                        cart.setAmount(sku.getRestrictAmount());
                        flag_restrict =true;
                    } else cart.setAmount(cartDto.getAmount());


                    //判断是否超过库存余量,超过库存余量直接给最大库存余量
                    if (cart.getAmount() > sku.getRestAmount()) {
                        cart.setAmount(sku.getRestAmount());
                        flag_rest = true;
                    }

                    if (cartDto.getCartId() == 0) {
                        if (cart.getStatus().equals("I") || cart.getStatus().equals("G")) {

                            List<Cart> carts = cartService.getCartByUserSku(cart);
                            //cartId为0,有两种情况,1种情况是,当购物车中没有出现同一个userId,skuId,状态为I,G的商品时候才去insert,否则是update
                            if (carts.size() > 0) {
                                cart.setCartId(carts.get(0).getCartId());//获取到登录状态下中已经存在的购物车ID,然后update
                                cart.setAmount(cart.getAmount() + carts.get(0).getAmount());//购买数量累加
                                if (cart.getAmount() > sku.getRestrictAmount() && sku.getRestrictAmount() != 0) {
                                    cart.setAmount(sku.getRestrictAmount());
                                    flag_restrict =true;
                                } else if (cart.getAmount() > sku.getRestAmount()) {
                                    cart.setAmount(sku.getRestAmount());
                                    flag_rest = true;
                                }
                                cartService.updateCart(cart);
                                if (cart.getStatus().equals("S")) sCartIds.add(cart.getCartId());

                            } else {
                                //cartId为0,有两种情况,1种情况是,当购物车中没有出现同一个userId,skuId,状态为I,G的商品时候才去insert
                                cartService.addCart(cart);
                                if (cart.getStatus().equals("S")) sCartIds.add(cart.getCartId());
                            }
                        }
                    } else {
                        cart.setCartId(cartDto.getCartId());
                        cartService.updateCart(cart);
                    }
                }
            }

            if (cartAllMap(userId).isPresent()) {
                result.putPOJO("cartList", Json.toJson(cartAllMap(userId).get()));
                if (flag_restrict){
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.PURCHASE_QUANTITY_LIMIT.getIndex()), Message.ErrorCode.PURCHASE_QUANTITY_LIMIT.getIndex())));
                }else if (flag_rest){
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex()), Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex())));
                }else result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            } else
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.CART_LIST_NULL_EXCEPTION.getIndex()), Message.ErrorCode.CART_LIST_NULL_EXCEPTION.getIndex())));

            if (S_FLAG) {
                for (Long s : sCartIds) {
                    Cart cart = new Cart();
                    cart.setCartId(s);
                    cart.setStatus("N");
                    cartService.updateCart(cart);
                    Logger.info("失效购物车ID: " + cart.getCartId());
                }
            }
            return ok(result);

        } catch (Exception ex) {
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
            if (cartAllMap(userId).isPresent()) {
                result.putPOJO("cartList", Json.toJson(cartAllMap(userId).get()));
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            } else
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.CART_LIST_NULL_EXCEPTION.getIndex()), Message.ErrorCode.CART_LIST_NULL_EXCEPTION.getIndex())));

            return ok(result);
        } catch (Exception ex) {
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

            Long userId = (Long) ctx().args.get("userId");
            Cart cartu = new Cart();
            cartu.setCartId(cartId);
            cartu.setStatus("N");
            cartService.updateCart(cartu);

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

        //行邮税收税标准
        final String POSTAL_STANDARD = skuService.getSysParameter(new SysParameter(null, null, null, "POSTAL_STANDARD")).getParameterVal();

        //海关规定购买单笔订单金额限制
        final String POSTAL_LIMIT = skuService.getSysParameter(new SysParameter(null, null, null, "POSTAL_LIMIT")).getParameterVal();

        //达到多少免除邮费
        final String FREE_SHIP = skuService.getSysParameter(new SysParameter(null, null, null, "FREE_SHIP")).getParameterVal();

        JsonNode json = request().body().asJson();
        ObjectNode result = Json.newObject();
        List<CartListDto> cartListDto = new ArrayList<>();
        try {
            List<CartDto> cartDtoList = mapper.readValue(json.toString(), mapper.getTypeFactory().constructCollectionType(List.class, CartDto.class));

            for (CartDto cartDto : cartDtoList) {

                Sku sku = new Sku();
                sku.setId(cartDto.getSkuId());
                sku = skuService.getInv(sku);

                //返回数据组装
                CartListDto cartList = new CartListDto();

                cartList.setCartId(cartDto.getCartId());
                cartList.setSkuId(cartDto.getSkuId());

                cartList.setItemColor(sku.getItemColor());
                cartList.setItemSize(sku.getItemSize());
                cartList.setItemPrice(sku.getItemPrice());


                //先确定商品状态是正常,否则直接存为失效商品
                if (!sku.getState().equals("Y")) {
                    cartList.setState("S");
                } else {
                    cartList.setState(cartDto.getState());
                }

                //判断是否超过限购数量
                if (sku.getRestrictAmount() != 0 && sku.getRestrictAmount() <= cartDto.getAmount()) {
                    cartList.setAmount(sku.getRestrictAmount());
                } else cartList.setAmount(cartDto.getAmount());

                //判断是否超过库存余量
                if (cartDto.getAmount() > sku.getRestAmount()) {
                    cartList.setAmount(sku.getRestAmount());
                }

                cartList.setInvArea(sku.getInvArea());
                cartList.setInvAreaNm(sku.getInvAreaNm());
                cartList.setRestrictAmount(sku.getRestrictAmount());
                cartList.setRestAmount(sku.getRestAmount());

                if (sku.getInvImg().contains("url")){
                    JsonNode jsonNode  = Json.parse(sku.getInvImg());
                    if (jsonNode.has("url")){
                        cartList.setInvImg(IMAGE_URL + jsonNode.get("url").asText());
                    }
                }else cartList.setInvImg(IMAGE_URL +sku.getInvImg());


                cartList.setInvUrl(DEPLOY_URL + "/comm/detail/" + sku.getItemId() + "/" + sku.getId());
                cartList.setInvUrlAndroid(DEPLOY_URL + "/comm/detail/web/" + sku.getItemId() + "/" + sku.getId());
                cartList.setInvCustoms(sku.getInvCustoms());
                cartList.setPostalTaxRate(sku.getPostalTaxRate());

                if (cartDto.getCartId() == 0) {
                    cartList.setCartDelUrl("");
                } else cartList.setCartDelUrl(DEPLOY_URL + "/client/cart/del/" + cartDto.getCartId());
                cartList.setInvTitle(sku.getInvTitle());
                cartListDto.add(cartList);

            }

            Map<String, List<CartListDto>> cartListMap = cartListDto
                    .stream()
                    .collect(Collectors.groupingBy(CartListDto::getInvArea));

            List<Map<String, Object>> list = new ArrayList<>();

            cartListMap
                    .forEach((invArea, p) -> {
                        Map<String, Object> mapResult = new HashMap<>();
                        mapResult.put("invCustoms",p.get(0).getInvCustoms());
                        mapResult.put("invArea", invArea);
                        mapResult.put("invAreaNm", p.get(0).getInvAreaNm());
                        mapResult.put("carts", p);
                        mapResult.put("postalStandard", POSTAL_STANDARD);
                        mapResult.put("postalLimit", POSTAL_LIMIT);
                        mapResult.put("freeShip", FREE_SHIP);
                        list.add(mapResult);
                    });

            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            result.putPOJO("cartList", Json.toJson(list));
            return ok(result);

        } catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    private Optional<List<Map<String, Object>>> cartAllMap(Long userId) throws Exception {

        //行邮税收税标准
        final String POSTAL_STANDARD = skuService.getSysParameter(new SysParameter(null, null, null, "POSTAL_STANDARD")).getParameterVal();

        //海关规定购买单笔订单金额限制
        final String POSTAL_LIMIT = skuService.getSysParameter(new SysParameter(null, null, null, "POSTAL_LIMIT")).getParameterVal();

        //达到多少免除邮费
        final String FREE_SHIP = skuService.getSysParameter(new SysParameter(null, null, null, "FREE_SHIP")).getParameterVal();

        List<CartListDto> cartListDto = new ArrayList<>();

        Cart c = new Cart();
        c.setUserId(userId);

        Optional<List<Cart>> listOptional = Optional.ofNullable(cartService.getCarts(c));

        if (listOptional.isPresent()) {
            //返回数据组装,根据用户id查询出所有可显示的购物车数据
            List<Cart> listCart = listOptional.get();

            for (Cart cart : listCart) {

                Sku sku = new Sku();
                sku.setId(cart.getSkuId());
                Optional<Sku> skuOptional = Optional.ofNullable(skuService.getInv(sku));
                if (skuOptional.isPresent()) {
                    sku = skuOptional.get();

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
                    cartList.setInvArea(sku.getInvArea());
                    cartList.setInvAreaNm(sku.getInvAreaNm());
                    cartList.setRestrictAmount(sku.getRestrictAmount());
                    cartList.setRestAmount(sku.getRestAmount());

                    if (sku.getInvImg().contains("url")){
                        JsonNode jsonNode  = Json.parse(sku.getInvImg());
                        if (jsonNode.has("url")){
                            cartList.setInvImg(IMAGE_URL + jsonNode.get("url").asText());
                        }
                    }else cartList.setInvImg(IMAGE_URL + sku.getInvImg());
                    cartList.setInvUrl(DEPLOY_URL + "/comm/detail/" + sku.getItemId() + "/" + sku.getId());
                    cartList.setInvUrlAndroid(DEPLOY_URL + "/comm/detail/web/" + sku.getItemId() + "/" + sku.getId());
                    cartList.setCartDelUrl(SHOPPING_URL + "/client/cart/del/" + cart.getCartId());
                    cartList.setInvTitle(sku.getInvTitle());
                    cartList.setCreateAt(cart.getCreateAt());
                    cartList.setInvCustoms(sku.getInvCustoms());
                    cartList.setPostalTaxRate(sku.getPostalTaxRate());
                    cartList.setPostalStandard(sku.getPostalStandard());
                    cartListDto.add(cartList);
                }
            }
            if (cartListDto.size() > 0) {

                Map<String, List<CartListDto>> cartListMap = cartListDto
                        .stream()
                        .collect(Collectors.groupingBy(CartListDto::getInvArea));

                List<Map<String, Object>> list = new ArrayList<>();

                cartListMap
                        .forEach((invArea, p) -> {
                            Map<String, Object> mapResult = new HashMap<>();
                            mapResult.put("invCustoms", p.get(0).getInvCustoms());
                            mapResult.put("invArea", invArea);
                            mapResult.put("invAreaNm", p.get(0).getInvAreaNm());
                            mapResult.put("carts", p);
                            mapResult.put("postalStandard", POSTAL_STANDARD);
                            mapResult.put("postalLimit", POSTAL_LIMIT);
                            mapResult.put("freeShip", FREE_SHIP);
                            list.add(mapResult);
                        });
                return Optional.of(list);
            } else return Optional.empty();
        } else return Optional.empty();
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
            Logger.error("server exception:" + ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }
}
