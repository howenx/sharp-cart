package middle;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.Application;
import controllers.OrderCtrl;
import domain.*;
import net.spy.memcached.MemcachedClient;
import play.Logger;
import play.libs.Json;
import service.CartService;
import service.IdService;
import service.SkuService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 购物车中间层
 * Created by howen on 16/2/22.
 */
public class CartMid {

    @Inject
    private SkuService skuService;

    @Inject
    private CartService cartService;

    @Inject
    private IdService idService;

    @Inject
    private MemcachedClient cache;


    //创建用户购物车商品
    public List<CartPar> createCart(Long userId, List<CartDto> cartDtoList) throws Exception {

        List<CartPar> cartPars = new ArrayList<>();

        for (CartDto cartDto : cartDtoList) {
            CartPar cartPar = itemAddCart(cartDto, userId);
            if (cartPar != null) cartPars.add(cartPar);
        }
        return cartPars;
    }

    //获取购物车list
    public Optional<List<CartItemDTO>> getCarts(Long userId) throws Exception {

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

                    if (sku.getInvImg().contains("url")) {
                        JsonNode jsonNode = Json.parse(sku.getInvImg());
                        if (jsonNode.has("url")) {
                            cartList.setInvImg(Application.IMAGE_URL + jsonNode.get("url").asText());
                        }
                    } else cartList.setInvImg(Application.IMAGE_URL + sku.getInvImg());

                    switch (cart.getSkuType()) {
                        case "item":
                            cartList.setInvUrl(Application.DEPLOY_URL + "/comm/detail/" + sku.getItemId() + "/" + sku.getId());
                            break;
                        case "vary":
                            cartList.setInvUrl(Application.DEPLOY_URL + "/comm/detail/" + sku.getItemId() + "/" + sku.getId() + "/" + cart.getSkuTypeId());
                            break;
                        case "customize":
                            cartList.setInvUrl(Application.DEPLOY_URL + "/comm/subject/detail/" + sku.getItemId() + "/" + sku.getId() + "/" + cart.getSkuTypeId());
                            break;
                        case "pin":
                            cartList.setInvUrl(Application.DEPLOY_URL + "/comm/pin/detail/" + sku.getItemId() + "/" + sku.getId() + "/" + cart.getSkuTypeId());
                            break;
                    }

                    cartList.setCartDelUrl(Application.SHOPPING_URL + "/client/cart/del/" + cart.getCartId());
                    cartList.setInvTitle(sku.getInvTitle());
                    cartList.setCreateAt(cart.getCreateAt());
                    cartList.setInvCustoms(sku.getInvCustoms());
                    cartList.setPostalTaxRate(sku.getPostalTaxRate());
                    cartList.setPostalStandard(sku.getPostalStandard());
                    cartList.setSkuType(cart.getSkuType());
                    cartList.setSkuTypeId(cart.getSkuTypeId());
                    cartListDto.add(cartList);
                }
            }
            if (cartListDto.size() > 0) {

                Map<String, List<CartListDto>> cartListMap = cartListDto
                        .stream()
                        .collect(Collectors.groupingBy(CartListDto::getInvArea));

                List<CartItemDTO> list = new ArrayList<>();

                cartListMap
                        .forEach((invArea, p) -> {
                            CartItemDTO cartItemDTO = new CartItemDTO();
                            cartItemDTO.setInvCustoms(p.get(0).getInvCustoms());
                            cartItemDTO.setInvArea(invArea);
                            cartItemDTO.setInvAreaNm(p.get(0).getInvAreaNm());
                            cartItemDTO.setCarts(p);
                            cartItemDTO.setPostalStandard(OrderCtrl.POSTAL_STANDARD);
                            cartItemDTO.setPostalLimit(OrderCtrl.POSTAL_LIMIT);
                            cartItemDTO.setFreeShip(OrderCtrl.FREE_SHIP);
                            list.add(cartItemDTO);
                        });
                return Optional.of(list);
            } else return Optional.empty();
        } else return Optional.empty();
    }

    //更新失效商品
    private void invalidItem(List<CartPar> cartPars) throws Exception {
        for (CartPar s : cartPars) {
            Cart cart = new Cart();
            cart.setCartId(s.getsCartIds());
            cart.setStatus("N");
            cartService.updateCart(cart);
            Logger.info("失效购物车ID: " + cart.getCartId());
        }
    }


    private CartPar itemAddCart(CartDto cartDto, Long userId) throws Exception {

        CartPar cartPar = new CartPar();
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
        } else {
            cart.setStatus(cartDto.getState());
        }

        if (sku.getRestrictAmount() != 0 && sku.getRestrictAmount() < cartDto.getAmount()) {
            cart.setAmount(sku.getRestrictAmount());
            cartPar.setRestrictMessageCode(Message.ErrorCode.PURCHASE_QUANTITY_LIMIT.getIndex());
        } else {
            cartPar.setRestrictMessageCode(Message.ErrorCode.SUCCESS.getIndex());
            cart.setAmount(cartDto.getAmount());
        }


        //判断是否超过库存余量,超过库存余量直接给最大库存余量
        if (cart.getAmount() > sku.getRestAmount()) {
            cart.setAmount(sku.getRestAmount());
            cartPar.setRestMessageCode(Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex());
        }else {
            cartPar.setRestMessageCode(Message.ErrorCode.SUCCESS.getIndex());
        }

        //新增商品类型
        cart.setSkuType(cartDto.getSkuType());
        cart.setSkuTypeId(cartDto.getSkuTypeId());

        if (cartDto.getCartId() == 0) {
            if (cart.getStatus().equals("I") || cart.getStatus().equals("G")) {

                List<Cart> carts = cartService.getCartByUserSku(cart);
                //cartId为0,有两种情况,1种情况是,当购物车中没有出现同一个userId,skuId,skuType,skuTypeId 状态为I,G的商品时候才去insert,否则是update
                if (carts.size() > 0) {
                    cart.setCartId(carts.get(0).getCartId());//获取到登录状态下中已经存在的购物车ID,然后update
                    cart.setAmount(cart.getAmount() + carts.get(0).getAmount());//购买数量累加
                    if (cart.getAmount() > sku.getRestrictAmount() && sku.getRestrictAmount() != 0) {
                        cart.setAmount(sku.getRestrictAmount());
                    } else if (cart.getAmount() > sku.getRestAmount()) {
                        cart.setAmount(sku.getRestAmount());
                    }
                    cartService.updateCart(cart);
                    if (cart.getStatus().equals("S")) cartPar.setsCartIds(cartDto.getCartId());

                } else {
                    //cartId为0,有两种情况,1种情况是,当购物车中没有出现同一个userId,skuId,状态为I,G的商品时候才去insert
                    cartService.addCart(cart);
                    if (cart.getStatus().equals("S")) cartPar.setsCartIds(cartDto.getCartId());
                }
            }
        } else {
            cart.setCartId(cartDto.getCartId());
            cartService.updateCart(cart);
        }
        return cartPar;
    }

    public Optional<List<CartItemDTO>> getCarts(List<CartDto> cartDtoList) throws Exception {

        List<CartListDto> cartListDto = new ArrayList<>();

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
                    cartList.setInvImg(Application.IMAGE_URL + jsonNode.get("url").asText());
                }
            }else cartList.setInvImg(Application.IMAGE_URL +sku.getInvImg());


            switch (cartDto.getSkuType()) {
                case "item":
                    cartList.setInvUrl(Application.DEPLOY_URL + "/comm/detail/" + sku.getItemId() + "/" + sku.getId());
                    break;
                case "vary":
                    cartList.setInvUrl(Application.DEPLOY_URL + "/comm/detail/" + sku.getItemId() + "/" + sku.getId() + "/" + cartList.getSkuTypeId());
                    break;
                case "customize":
                    cartList.setInvUrl(Application.DEPLOY_URL + "/comm/subject/detail/" + sku.getItemId() + "/" + sku.getId() + "/" + cartList.getSkuTypeId());
                    break;
                case "pin":
                    cartList.setInvUrl(Application.DEPLOY_URL + "/comm/pin/detail/" + sku.getItemId() + "/" + sku.getId() + "/" + cartList.getSkuTypeId());
                    break;
            }

            cartList.setInvCustoms(sku.getInvCustoms());
            cartList.setPostalTaxRate(sku.getPostalTaxRate());

            if (cartDto.getCartId() == 0) {
                cartList.setCartDelUrl("");
            } else cartList.setCartDelUrl(Application.DEPLOY_URL + "/client/cart/del/" + cartDto.getCartId());
            cartList.setInvTitle(sku.getInvTitle());

            cartList.setSkuType(cartList.getSkuType());
            cartList.setSkuTypeId(cartList.getSkuTypeId());

            cartListDto.add(cartList);

        }

        Map<String, List<CartListDto>> cartListMap = cartListDto
                .stream()
                .collect(Collectors.groupingBy(CartListDto::getInvArea));

        List<CartItemDTO> list = new ArrayList<>();

        cartListMap
                .forEach((invArea, p) -> {
                    CartItemDTO cartItemDTO = new CartItemDTO();
                    cartItemDTO.setInvCustoms(p.get(0).getInvCustoms());
                    cartItemDTO.setInvArea(invArea);
                    cartItemDTO.setInvAreaNm(p.get(0).getInvAreaNm());
                    cartItemDTO.setCarts(p);
                    cartItemDTO.setPostalStandard(OrderCtrl.POSTAL_STANDARD);
                    cartItemDTO.setPostalLimit(OrderCtrl.POSTAL_LIMIT);
                    cartItemDTO.setFreeShip(OrderCtrl.FREE_SHIP);
                    list.add(cartItemDTO);
                });
        return Optional.of(list);
    }

}

