package middle;

import controllers.Application;
import controllers.OrderCtrl;
import domain.*;
import redis.clients.jedis.Jedis;
import util.RedisPool;
import util.SysParCom;
import service.CartService;
import service.SkuService;
import util.ComUtil;

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
    private OrderCtrl orderCtrl;

    @Inject
    private ComUtil comUtil;

    @Inject
    private Application application;


    /**
     * 创建购物车商品数据
     *
     * @param userId      userId
     * @param cartDtoList cartDtoList
     * @return List<CartPar>
     * @throws Exception
     */
    public List<CartPar> createCart(Long userId, List<CartDto> cartDtoList) throws Exception {

        List<CartPar> cartPars = new ArrayList<>();

        for (CartDto cartDto : cartDtoList) {
            CartPar cartPar = itemAddCart(cartDto, userId);
            if (cartPar != null) cartPars.add(cartPar);
        }
        return cartPars;
    }


    /**
     * 创建用户购物车单品数据
     *
     * @param cartDto cartDto
     * @param userId  userId
     * @return CartPar
     * @throws Exception
     */
    private CartPar itemAddCart(CartDto cartDto, Long userId) throws Exception {

        CartPar cartPar = new CartPar();
        Cart cart = new Cart();
        cart.setSkuId(cartDto.getSkuId());
        cart.setUserId(userId);

        SkuVo skuVo = new SkuVo();
        skuVo.setSkuTypeId(cartDto.getSkuTypeId());
        skuVo.setSkuType(cartDto.getSkuType());

        Optional<List<SkuVo>> skuVos = Optional.ofNullable(skuService.getAllSkus(skuVo));

        if (skuVos.isPresent() && skuVos.get().size() > 0) {
            skuVo = skuVos.get().get(0);

            cart.setItemId(skuVo.getItemId());
            cart.setSkuTitle(skuVo.getSkuTypeTitle());

            cart.setSkuImg(skuVo.getSkuTypeImg());

            //先确定商品状态是正常,否则直接存为失效商品
            if (!skuVo.getSkuTypeStatus().equals("Y")) {
                cart.setStatus("S");
            } else {
                cart.setStatus("I");
            }

            if (comUtil.isOutOfRestrictAmount(cartDto.getAmount(), skuVo)) {
                cart.setAmount(skuVo.getSkuTypeRestrictAmount());
                cartPar.setRestrictMessageCode(Message.ErrorCode.PURCHASE_QUANTITY_LIMIT.getIndex());
            } else {
                cartPar.setRestrictMessageCode(Message.ErrorCode.SUCCESS.getIndex());
                cart.setAmount(cartDto.getAmount());
            }


            //判断是否超过库存余量,超过库存余量直接给最大库存余量
            if (cart.getAmount() > skuVo.getRestAmount()) {
                cart.setAmount(skuVo.getRestAmount());
                cartPar.setRestMessageCode(Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex());
            } else {
                cartPar.setRestMessageCode(Message.ErrorCode.SUCCESS.getIndex());
            }

            //如果商品是vary则需要判断所卖出商品数量和当前限制卖出数量
            if (cartDto.getSkuType().equals("vary")) {
                Integer varyAmount = application.validateVary(skuVo.getSkuTypeId(), cartDto.getAmount());
                if (varyAmount == null) {
                    cart.setAmount(0);
                    cartPar.setRestMessageCode(Message.ErrorCode.VARY_OVER_LIMIT.getIndex());
                } else if (varyAmount < 0) {
                    cart.setAmount(cartDto.getAmount() + varyAmount);
                    cartPar.setRestMessageCode(Message.ErrorCode.VARY_OVER_LIMIT.getIndex());
                } else {
                    cartPar.setRestMessageCode(Message.ErrorCode.SUCCESS.getIndex());
                }
            }

            //新增商品类型
            cart.setSkuType(cartDto.getSkuType());
            cart.setSkuTypeId(cartDto.getSkuTypeId());

            if (cartDto.getCartId() == null || cartDto.getCartId() == 0) {
                if (cart.getStatus().equals("I") || cart.getStatus().equals("G")) {

                    List<Cart> carts = cartService.getCartByUserSku(cart);
                    //cartId为0,有两种情况,1种情况是,当购物车中没有出现同一个userId,skuId,skuType,skuTypeId 状态为I,G的商品时候才去insert,否则是update
                    if (carts.size() > 0) {
                        cart.setCartId(carts.get(0).getCartId());//获取到登录状态下中已经存在的购物车ID,然后update
                        cart.setAmount(cart.getAmount() + carts.get(0).getAmount());//购买数量累加
                        if (comUtil.isOutOfRestrictAmount(cart.getAmount(), skuVo)) {
                            cart.setAmount(skuVo.getSkuTypeRestrictAmount());
                            cartPar.setRestrictMessageCode(Message.ErrorCode.PURCHASE_QUANTITY_LIMIT.getIndex());
                        } else if (cart.getAmount() > skuVo.getRestAmount()) {
                            cartPar.setRestMessageCode(Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex());
                            cart.setAmount(skuVo.getRestAmount());
                        }
                        cartService.updateCart(cart);
                        if (cart.getStatus().equals("S")) cartPar.setsCartIds(cartDto.getCartId());

                    } else {
                        //cartId为0,有两种情况,1种情况是,当购物车中没有出现同一个userId,skuId,状态为I,G的商品时候才去insert
                        cartService.addCart(cart);
                        if (cart.getStatus().equals("S")) cartPar.setsCartIds(cartDto.getCartId());

                        //新增的购物车商品全部保存到redis勾选列表中
                        if (null != cart.getCartId() && 3 != cartDto.getCartSource() && cartDto.getOrCheck().equals("Y")) {
                            try (Jedis jedis = RedisPool.createPool().getResource()) {
                                jedis.sadd("cart-" + userId, cart.getCartId().toString());
                            }
                        }
                    }
                } else {
                    cartPar.setRestMessageCode(Message.ErrorCode.SKU_STATUS_ERROR.getIndex());
                }
            } else {
                cart.setCartId(cartDto.getCartId());
                if (cart.getStatus().equals("S")) {
                    try (Jedis jedis = RedisPool.createPool().getResource()) {
                        if (jedis.sismember("cart-" + userId, cart.getCartId().toString())) {
                            jedis.srem("cart-" + userId, cartDto.getCartId().toString());
                        }
                    }
                }
                cartService.updateCart(cart);
            }

            return cartPar;

        } else return null;


    }

    /**
     * 获取登录状态下购物车列表
     *
     * @param userId userId
     * @return Optional
     * @throws Exception
     */
    public Optional<List<CartItemDTO>> getCarts(Long userId) throws Exception {

        List<CartListDto> cartListDto = new ArrayList<>();

        Cart c = new Cart();
        c.setUserId(userId);

        Optional<List<Cart>> listOptional = Optional.ofNullable(cartService.getCarts(c));

        if (listOptional.isPresent() && listOptional.get().size() > 0) {
            //返回数据组装,根据用户id查询出所有可显示的购物车数据
            List<Cart> listCart = listOptional.get();

            for (Cart cart : listCart) {

                SkuVo skuVo = new SkuVo();
                skuVo.setSkuType(cart.getSkuType());
                skuVo.setSkuTypeId(cart.getSkuTypeId());

                Optional<List<SkuVo>> skuOptional = Optional.ofNullable(skuService.getAllSkus(skuVo));
                if (skuOptional.isPresent()) {
                    skuVo = skuOptional.get().get(0);

                    //先确定商品状态是正常,否则直接存为失效商品
                    if (!skuVo.getSkuTypeStatus().equals("Y")) {
                        cart.setStatus("S");
                    }

                    //返回数据组装
                    CartListDto cartList = new CartListDto();
                    cartList.setCartId(cart.getCartId());
                    cartList.setSkuId(cart.getSkuId());
                    cartList.setAmount(cart.getAmount());
                    cartList.setItemColor(skuVo.getItemColor());
                    cartList.setItemSize(skuVo.getItemSize());
                    cartList.setItemPrice(skuVo.getSkuTypePrice());
                    cartList.setState(cart.getStatus());
                    if (cart.getStatus().equals("S")) {
                        cart.setStatus("N");
                        cartService.UpdateCartBy(cart);
                    }
                    cartList.setInvArea(skuVo.getInvArea());
                    cartList.setInvAreaNm(skuVo.getInvAreaNm());
                    cartList.setRestrictAmount(skuVo.getSkuTypeRestrictAmount());
                    cartList.setRestAmount(skuVo.getRestAmount());

                    cartList.setInvImg(orderCtrl.getInvImg(skuVo.getSkuTypeImg()));

                    cartList.setInvUrl(SysParCom.DEPLOY_URL + "/comm/detail/" + cart.getSkuType() + "/" + skuVo.getItemId() + "/" + cart.getSkuTypeId());

                    cartList.setCartDelUrl(SysParCom.SHOPPING_URL + "/client/cart/del/" + cart.getCartId());
                    cartList.setInvTitle(skuVo.getSkuTypeTitle());
                    cartList.setCreateAt(cart.getCreateAt());
                    cartList.setInvCustoms(skuVo.getInvCustoms());
                    cartList.setPostalTaxRate(skuVo.getPostalTaxRate() == null ? "0" : skuVo.getPostalTaxRate());
                    cartList.setPostalStandard(skuVo.getPostalStandard());
                    cartList.setSkuType(cart.getSkuType());
                    cartList.setSkuTypeId(cart.getSkuTypeId());
                    try (Jedis jedis = RedisPool.createPool().getResource()) {
                        //判断redis里是否存有此cartId,有表示勾选,没有表示未勾选
                        if (jedis.sismember("cart-" + userId, cart.getCartId().toString())) {
                            cartList.setOrCheck("Y");
                        }
                    }

                    cartListDto.add(cartList);
                }
            }
            if (cartListDto.size() > 0) {

                Map<String, List<CartListDto>> cartListMap = cartListDto
                        .stream()
                        .collect(Collectors.groupingBy(CartListDto::getInvArea));

                return Optional.of(getCartListMap(cartListMap));
            } else return Optional.empty();
        } else return Optional.empty();
    }

    /**
     * 获取购物车列表数据,未登录
     *
     * @param cartDtoList cartDtoList
     * @return Optional
     * @throws Exception
     */
    public Optional<List<CartItemDTO>> getCarts(List<CartDto> cartDtoList) throws Exception {

        List<CartListDto> cartListDto = new ArrayList<>();

        for (CartDto cartDto : cartDtoList) {

            SkuVo skuVo = new SkuVo();
            skuVo.setSkuTypeId(cartDto.getSkuTypeId());
            skuVo.setSkuType(cartDto.getSkuType());

            Optional<List<SkuVo>> skuVos = Optional.ofNullable(skuService.getAllSkus(skuVo));

            if (skuVos.isPresent() && skuVos.get().size() > 0) {
                skuVo = skuVos.get().get(0);

                //返回数据组装
                CartListDto cartList = new CartListDto();

                cartList.setCartId(cartDto.getCartId());
                cartList.setSkuId(cartDto.getSkuId());

                cartList.setOrCheck(cartDto.getOrCheck());//勾选状态

                cartList.setItemColor(skuVo.getItemColor());
                cartList.setItemSize(skuVo.getItemSize());
                cartList.setItemPrice(skuVo.getSkuTypePrice());

                cartList.setSkuType(cartDto.getSkuType());
                cartList.setSkuTypeId(cartDto.getSkuTypeId());

                //先确定商品状态是正常,否则直接存为失效商品
                if (!skuVo.getSkuTypeStatus().equals("Y")) {
                    cartList.setState("S");
                } else {
                    cartList.setState(cartDto.getState());
                }

                //判断是否超过限购数量
                if (skuVo.getSkuTypeRestrictAmount() != 0 && skuVo.getSkuTypeRestrictAmount() <= cartDto.getAmount()) {
                    cartList.setAmount(skuVo.getSkuTypeRestrictAmount());
                } else cartList.setAmount(cartDto.getAmount());

                //判断是否超过库存余量
                if (cartDto.getAmount() > skuVo.getRestAmount()) {
                    cartList.setAmount(skuVo.getRestAmount());
                }

                //如果商品是vary则需要判断所卖出商品数量和当前限制卖出数量
                if (cartDto.getSkuType().equals("vary")) {
                    Integer varyAmount = application.validateVary(skuVo.getSkuTypeId(), cartDto.getAmount());
                    if (varyAmount == null) {
                        cartList.setAmount(0);
                    } else if (varyAmount < 0) {
                        cartList.setAmount(cartDto.getAmount() + varyAmount);
                    }
                }

                cartList.setInvArea(skuVo.getInvArea());
                cartList.setInvAreaNm(skuVo.getInvAreaNm());
                cartList.setRestrictAmount(skuVo.getSkuTypeRestrictAmount());
                cartList.setRestAmount(skuVo.getRestAmount());

                cartList.setInvImg(orderCtrl.getInvImg(skuVo.getSkuTypeImg()));

                cartList.setInvUrl(SysParCom.DEPLOY_URL + "/comm/detail/" + cartList.getSkuType() + "/" + skuVo.getItemId() + "/" + cartList.getSkuTypeId());


                cartList.setInvCustoms(skuVo.getInvCustoms());
                cartList.setPostalTaxRate(skuVo.getPostalTaxRate());

                if (cartDto.getCartId() == 0) {
                    cartList.setCartDelUrl("");
                } else cartList.setCartDelUrl(SysParCom.DEPLOY_URL + "/client/cart/del/" + cartDto.getCartId());
                cartList.setInvTitle(skuVo.getSkuTypeTitle());

                cartList.setSkuType(cartList.getSkuType());
                cartList.setSkuTypeId(cartList.getSkuTypeId());
                cartListDto.add(cartList);
            }
        }

        Map<String, List<CartListDto>> cartListMap = cartListDto
                .stream()
                .collect(Collectors.groupingBy(CartListDto::getInvArea));
        return Optional.of(getCartListMap(cartListMap));
    }


    /**
     * 获取购物车item
     *
     * @param map map
     * @return List
     */
    private List<CartItemDTO> getCartListMap(Map<String, List<CartListDto>> map) {
        List<CartItemDTO> list = new ArrayList<>();

        map.forEach((invArea, p) -> {
            CartItemDTO cartItemDTO = new CartItemDTO();
            cartItemDTO.setInvCustoms(p.get(0).getInvCustoms());
            cartItemDTO.setInvArea(invArea);
            cartItemDTO.setInvAreaNm(p.get(0).getInvAreaNm());
            cartItemDTO.setCarts(p);
            cartItemDTO.setPostalStandard(SysParCom.POSTAL_STANDARD);
            cartItemDTO.setPostalLimit(SysParCom.POSTAL_LIMIT);
            cartItemDTO.setFreeShip(SysParCom.FREE_SHIP);
            list.add(cartItemDTO);
        });
        return list;
    }

}

