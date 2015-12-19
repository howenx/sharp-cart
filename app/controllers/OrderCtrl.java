package controllers;

import akka.actor.ActorRef;
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
import util.GenCouponCode;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单相关,提交订单,优惠券
 * Created by howen on 15/12/1.
 */
@SuppressWarnings("unchecked")
@Singleton
public class OrderCtrl extends Controller {

    private SkuService skuService;

    private CartService cartService;

    private IdService idService;

    private ActorRef orderSplitActor;

    //行邮税收税标准
    static String POSTAL_STANDARD;

    //海关规定购买单笔订单金额限制
    static String POSTAL_LIMIT;

    //达到多少免除邮费
    static String FREE_SHIP;

    @Inject
    public OrderCtrl(SkuService skuService, CartService cartService, IdService idService, @Named("subOrderActor") ActorRef orderSplitActor) {
        this.cartService = cartService;
        this.idService = idService;
        this.orderSplitActor = orderSplitActor;
        this.skuService = skuService;
        //行邮税收税标准
        POSTAL_STANDARD = skuService.getSysParameter(new SysParameter(null, null, null, "POSTAL_STANDARD")).getParameterVal();

        //海关规定购买单笔订单金额限制
        POSTAL_LIMIT = skuService.getSysParameter(new SysParameter(null, null, null, "POSTAL_LIMIT")).getParameterVal();

        //达到多少免除邮费
        FREE_SHIP = skuService.getSysParameter(new SysParameter(null, null, null, "FREE_SHIP")).getParameterVal();

    }

    //图片服务器url
    static final String IMAGE_URL = play.Play.application().configuration().getString("image.server.url");

    //发布服务器url
    static final String DEPLOY_URL = play.Play.application().configuration().getString("deploy.server.url");

    //shopping服务器url
    static final String SHOPPING_URL = play.Play.application().configuration().getString("shopping.server.url");

    //id服务器url
    static final String ID_URL = play.Play.application().configuration().getString("id.server.url");

    //将Json串转换成List
    static final ObjectMapper mapper = new ObjectMapper();

    static final ObjectNode result = Json.newObject();

    @Security.Authenticated(UserAuth.class)
    public Result settle() {

        Optional<JsonNode> json = Optional.ofNullable(request().body().asJson());

        Map<String, Object> resultMap = new HashMap<>();

        try {

            Long userId = (Long) ctx().args.get("userId");
            if (json.isPresent() && json.get().size() > 0) {

                Logger.error("请求JSON : "+json.get().toString());

                SettleOrderDTO settleOrderDTO = mapper.readValue(json.get().toString(), mapper.getTypeFactory().constructType(SettleOrderDTO.class));

                /*******查询用户地址*************/
                Address address = new Address();
                //如果没有用户地址ID,那么就查找用户默认地址,否则就去查找用户指定的地址
                if (settleOrderDTO.getAddressId() != null && settleOrderDTO.getAddressId() != 0) {
                    address.setAddId(settleOrderDTO.getAddressId());
                } else {
                    address.setUserId(userId);
                    address.setOrDefault(true);
                }
                address = selectAddress(address);

                //计算所有费用
                Map<String, Object> allFee = calOrderFee(settleOrderDTO.getSettleDTOs(), userId, address);

                if (allFee.containsKey("message")) {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName((int) allFee.get("message")), (int) allFee.get("message"))));
                    return ok(result);
                }

                List<Map<String, Object>> singleCustoms = (List<Map<String, Object>>) allFee.get("singleCustoms");

                if (settleOrderDTO.getCouponId() != null && !settleOrderDTO.getCouponId().equals("")) {
                    allFee.put("discountFee", calDiscount(userId, settleOrderDTO.getCouponId(), (BigDecimal) allFee.get("totalFee")));
                }

                List<Map<String, Object>> returnFee = new ArrayList<>();

                for (Map<String, Object> s : singleCustoms) {

                    Map<String, Object> singleCustomsFee = new HashMap<>();
                    //海关名称
                    singleCustomsFee.put("invCustoms", s.get("invCustoms").toString());

                    singleCustomsFee.put("invArea", s.get("invArea").toString());

                    //如果存在单个海关的金额超过1000,返回
                    if (((BigDecimal) s.get("totalFeeSingle")).compareTo(new BigDecimal(POSTAL_LIMIT)) > 0) {
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.PURCHASE_QUANTITY_SUM_PRICE.getIndex()), Message.ErrorCode.PURCHASE_QUANTITY_SUM_PRICE.getIndex())));
                        return ok(result);
                    }
                    //每个海关的总费用统计
                    singleCustomsFee.put("singleCustomsSumFee", ((BigDecimal) s.get("totalFeeSingle")).toPlainString());

                    //每个海关购买的总数量
                    singleCustomsFee.put("singleCustomsSumAmount", ((Integer) s.get("totalAmount")));

                    //每个海关邮费统计
                    singleCustomsFee.put("shipSingleCustomsFee", ((BigDecimal) s.get("shipFeeSingle")).toPlainString());

                    //每个海关的实际邮费统计
                    if (((BigDecimal) s.get("shipFeeSingle")).compareTo(new BigDecimal(FREE_SHIP)) > 0) {
                        singleCustomsFee.put("factSingleCustomsShipFee", 0);//实际邮费
                    } else
                        singleCustomsFee.put("factSingleCustomsShipFee", ((BigDecimal) s.get("shipFeeSingle")).toPlainString());//每次计算出的邮费

                    //每个海关的关税统计
                    singleCustomsFee.put("portalSingleCustomsFee", ((BigDecimal) s.get("postalFeeSingle")).toPlainString());

                    //统计如果各个海关的实际关税,如果关税小于50元,则免税
                    if (((BigDecimal) s.get("postalFeeSingle")).compareTo(new BigDecimal(POSTAL_STANDARD)) <= 0)
                        singleCustomsFee.put("factPortalFeeSingleCustoms", 0);
                    else
                        singleCustomsFee.put("factPortalFeeSingleCustoms", ((BigDecimal) s.get("postalFeeSingle")).toPlainString());

                    returnFee.add(singleCustomsFee);
                }


                if (((BigDecimal) allFee.get("shipFee")).compareTo(new BigDecimal(FREE_SHIP)) > 0) {
                    resultMap.put("factShipFee", 0);//实际邮费
                } else
                    resultMap.put("factShipFee", ((BigDecimal) allFee.get("shipFee")).toPlainString());//每次计算出的邮费

                resultMap.put("shipFee", ((BigDecimal) allFee.get("shipFee")).toPlainString());
                resultMap.put("portalFee", ((BigDecimal) allFee.get("postalFee")).toPlainString());
                //统计如果各个海关的实际关税,如果关税小于50元,则免税
                if (((BigDecimal) allFee.get("postalFee")).compareTo(new BigDecimal(POSTAL_STANDARD)) <= 0)
                    resultMap.put("factPortalFee", 0);
                else resultMap.put("factPortalFee", ((BigDecimal) allFee.get("postalFee")).toPlainString());

                resultMap.put("address", address);

                //将各个海关下的费用统计返回
                resultMap.put("singleCustoms", returnFee);

                CouponVo couponVo = new CouponVo();
                couponVo.setUserId(userId);
                couponVo.setState("N");
                List<CouponVo> lists = cartService.getUserCoupon(couponVo);

                final BigDecimal sum = ((BigDecimal) allFee.get("totalFee"));

                //优惠券,只列出当前满足条件优惠的优惠券,购买金额要大于限制金额且是未使用的,有效的
                lists = lists.stream().filter(s -> s.getLimitQuota().compareTo(sum) <= 0).collect(Collectors.toList());

                resultMap.put("coupons", lists);

                result.putPOJO("settle", Json.toJson(resultMap));

                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));

                Logger.error("最终结果: " + result.toString());

                return ok(result);
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
                return ok(result);
            }
        } catch (Exception ex) {
            Logger.error("settle: " + ex.getMessage());
            ex.printStackTrace();
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ERROR.getIndex()), Message.ErrorCode.ERROR.getIndex())));
            return ok(result);
        }
    }

    //发放优惠券----->下订单时候,检查如果用户订单金额大于包邮金额,就发放一个999开头的优惠券,并且是已使用状态,当前时间下的
    public Result publicCoupons() throws Exception {
        CouponVo couponVo = new CouponVo();
        couponVo.setUserId(((Integer) 1000020).longValue());
        couponVo.setDenomination((BigDecimal.valueOf(50)));
        Calendar cal = Calendar.getInstance();
        couponVo.setStartAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime()));
        cal.add(Calendar.MONTH, 2);
        couponVo.setEndAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime()));
        String coupId = GenCouponCode.GetCode(GenCouponCode.CouponClassCode.ALL_FREE.getIndex(), 8);
        couponVo.setCoupId(coupId);
        couponVo.setCateId(((Integer) GenCouponCode.CouponClassCode.ALL_FREE.getIndex()).longValue());
        couponVo.setState("N");
        couponVo.setCateNm(GenCouponCode.CouponClassCode.ALL_FREE.getName());
        couponVo.setLimitQuota(BigDecimal.valueOf(500));
        cartService.insertCoupon(couponVo);
        return ok("Success");
    }

    /**
     * 购物券List
     *
     * @return result
     */
    @Security.Authenticated(UserAuth.class)
    public Result couponsList() {

        ObjectNode result = Json.newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");

            CouponVo couponVo = new CouponVo();
            couponVo.setUserId(userId);
            couponVo.setState("N");
            cartService.updateCouponInvalid(couponVo);
            couponVo.setState("");
            List<CouponVo> lists = cartService.getUserCouponAll(couponVo);

            result.putPOJO("coupons", Json.toJson(lists));
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            return ok(result);

        } catch (Exception ex) {
            Logger.error(ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
            return ok(result);
        }
    }

    @Security.Authenticated(UserAuth.class)
    public Result submitOrder() {

        Optional<JsonNode> json = Optional.ofNullable(request().body().asJson());
        try {
            /*******取用户ID*********/
            Long userId = (Long) ctx().args.get("userId");

            if (json.isPresent() && json.get().size() > 0) {

                Logger.error("请求JSON : "+json.get().toString());

                SettleOrderDTO settleOrderDTO = mapper.readValue(json.get().toString(), mapper.getTypeFactory().constructType(SettleOrderDTO.class));
                settleOrderDTO.setClientIp(request().remoteAddress());

                /*******查询用户地址*************/
                Address address = new Address();
                address.setAddId(settleOrderDTO.getAddressId());
                address = selectAddress(address);

                //计算所有费用
                Map<String, Object> allFee = calOrderFee(settleOrderDTO.getSettleDTOs(), userId, address);

                if (allFee.containsKey("message")) {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName((int) allFee.get("message")), (int) allFee.get("message"))));
                    return ok(result);
                }

                List<Map<String,Object>> singleCustoms =(List<Map<String,Object>>) allFee.get("singleCustoms");

                if (settleOrderDTO.getCouponId() != null && !settleOrderDTO.getCouponId().equals("")) {
                    BigDecimal discount =  calDiscount(userId, settleOrderDTO.getCouponId(), (BigDecimal) allFee.get("totalFee"));
                    allFee.put("discountFee", discount);
                    allFee.put("couponId",settleOrderDTO.getCouponId());
                    allFee.put("totalPayFee", ((BigDecimal)allFee.get("totalPayFee")).subtract(discount).setScale(2, BigDecimal.ROUND_HALF_UP));
                }

                //前端是立即购买还是结算页提交订单
                allFee.put("buyNow",settleOrderDTO.getBuyNow());

                Logger.error("所有费用: "+allFee.toString());

                //创建订单
                Order order = createOrder(settleOrderDTO, allFee, userId);

                if (order != null) {
                    //调用Actor创建子订单
                    allFee.put("orderId", order.getOrderId());
                    orderSplitActor.tell(allFee, ActorRef.noSender());
                    result.putPOJO("order", Json.toJson(order));
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                    return ok(result);
                }else {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.CREATE_ORDER_EXCEPTION.getIndex()), Message.ErrorCode.CREATE_ORDER_EXCEPTION.getIndex())));
                    return ok(result);
                }

            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
                return ok(result);
            }
        } catch (Exception ex) {
            Logger.error("submitOrder " + ex.getMessage());
            ex.printStackTrace();
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ERROR.getIndex()), Message.ErrorCode.ERROR.getIndex())));
            return ok(result);
        }
    }

    //计算邮费
    private BigDecimal calculateShipFee(String provinceCode, String carriageModelCode, Integer amount) throws Exception {
        BigDecimal shipFee = BigDecimal.ZERO;
        //取邮费
        Carriage carriage = new Carriage();
        carriage.setCityCode(provinceCode);
        carriage.setModelCode(carriageModelCode);
        Optional<Carriage> carriageOptional = Optional.ofNullable(skuService.getCarriage(carriage));
        if (carriageOptional.isPresent()) {
            carriage = carriageOptional.get();
            //规则:如果购买数量小于首件数量要求,则取首费,否则就整除续件数量+1,乘以续费再加首费
            if (amount <= carriage.getFirstNum()) {
                shipFee = shipFee.add(carriage.getFirstFee());
            } else {
                shipFee = shipFee.add(carriage.getFirstFee()).add(new BigDecimal((amount / carriage.getAddNum()) + 1).multiply(carriage.getAddFee()));
            }
        }
        return shipFee;
    }

    //计算行邮税
    private BigDecimal calculatePostalTax(String postalTaxRate, BigDecimal price, Integer amount) {
        BigDecimal postalFee = BigDecimal.ZERO;
        //计算行邮税,行邮税加和
        postalFee = postalFee.add(new BigDecimal(postalTaxRate).multiply(price).multiply(new BigDecimal(amount)).multiply(new BigDecimal(0.01)));

        //统计如果各个海关的实际关税,如果关税小于50(sku.getPostalStandard())元,则免税
        if (postalFee.compareTo(new BigDecimal(POSTAL_STANDARD)) > 0)       //公式应收税金 T=X*N*S/(1-S)
            return price.multiply(new BigDecimal(amount)).multiply(new BigDecimal(postalTaxRate).multiply(new BigDecimal(0.01))).divide(new BigDecimal(1).subtract(new BigDecimal(postalTaxRate).multiply(new BigDecimal(0.01))), 2, BigDecimal.ROUND_HALF_UP);
        else return BigDecimal.ZERO;
    }

    //获取用户地址
    private Address selectAddress(Address address) throws Exception {
        Optional<Address> address_search = Optional.ofNullable(idService.getAddress(address));
        if (address_search.isPresent()) {
            address = address_search.get();
            JsonNode detailCity = Json.parse(address.getDeliveryCity());
            address.setProvinceCode(detailCity.get("province_code").asText());
            address.setDeliveryCity(detailCity.get("province").asText() + " " + detailCity.get("city").asText() + " " + detailCity.get("area").asText());
        }
        return address;
    }

    //计算每个报关单位下的所有费用
    private Map<String, Object> calCustomsFee(SettleDTO settleDTO, Long userId, Address address) throws Exception {

        Map<String, Object> map = new HashMap<>();

        //运费
        BigDecimal shipFeeSingle = BigDecimal.ZERO;

        //行邮税
        BigDecimal postalFeeSingle = BigDecimal.ZERO;

        //总计sku费用
        BigDecimal totalFeeSingle = BigDecimal.ZERO;

        //总计支付费用
        BigDecimal totalPayFeeSingle = BigDecimal.ZERO;

        for (CartDto cartDto : settleDTO.getCartDtos()) {

            Sku sku = new Sku();
            sku.setId(cartDto.getSkuId());
            Optional<Sku> skuOptional = Optional.ofNullable(skuService.getInv(sku));

            if (skuOptional.isPresent()) {
                sku = skuOptional.get();
            } else {
                map.put("message", Message.ErrorCode.getName(Message.ErrorCode.SKU_DETAIL_NULL_EXCEPTION.getIndex()));
                return map;
            }

            //先确定商品状态是正常,然后确定商品结算数量是否超出库存量
            if (!sku.getState().equals("Y")) {
                map.put("message", Message.ErrorCode.SKU_INVALID.getIndex());
                return map;
            } else if (cartDto.getAmount() > sku.getRestrictAmount() && sku.getRestrictAmount() != 0) {
                map.put("message", Message.ErrorCode.PURCHASE_QUANTITY_LIMIT.getIndex());
                return map;
            } else if (cartDto.getAmount() > sku.getRestAmount()) {
                map.put("message", Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex());
                return map;
            } else {
                //邮费
                shipFeeSingle = shipFeeSingle.add(calculateShipFee(address.getProvinceCode(), sku.getCarriageModelCode(), cartDto.getAmount()));

                //单sku产生的行邮税
                postalFeeSingle = postalFeeSingle.add(calculatePostalTax(sku.getPostalTaxRate(), sku.getItemPrice(), cartDto.getAmount()));

                //sku总计费用
                totalFeeSingle = totalFeeSingle.add(sku.getItemPrice().multiply(new BigDecimal(cartDto.getAmount())));

                //支付费用
                totalPayFeeSingle = totalPayFeeSingle.add(shipFeeSingle).add(postalFeeSingle).add(totalFeeSingle);
            }
        }

        //海关名称
        map.put("invCustoms", settleDTO.getInvCustoms());
        map.put("invArea", settleDTO.getInvArea());
        map.put("shipFeeSingle", shipFeeSingle.setScale(2, BigDecimal.ROUND_HALF_UP));
        map.put("postalFeeSingle", postalFeeSingle.setScale(2, BigDecimal.ROUND_HALF_UP));
        map.put("totalFeeSingle", totalFeeSingle.setScale(2, BigDecimal.ROUND_HALF_UP));
        map.put("totalPayFeeSingle", totalPayFeeSingle.setScale(2, BigDecimal.ROUND_HALF_UP));
        map.put("totalAmount", settleDTO.getCartDtos().size());
        map.put("cartDtos", settleDTO.getCartDtos());

        //单个海关下是否达到某个消费值时候免邮
        if (totalFeeSingle.compareTo(new BigDecimal(FREE_SHIP)) > 0) {
            map.put("freeShip",true);
        }else map.put("freeShip",false);

        return map;
    }

    //计算优惠了多钱
    private BigDecimal calDiscount(Long userId, String couponId, BigDecimal totalFee) throws Exception {
        BigDecimal discountFee = BigDecimal.ZERO;
        //优惠金额
        CouponVo couponVo = new CouponVo();
        couponVo.setUserId(userId);
        couponVo.setState("N");
        couponVo.setCoupId(couponId);
        Optional<List<CouponVo>> lists = Optional.ofNullable(cartService.getUserCoupon(couponVo));

        if (lists.isPresent()) {
            //优惠券,只列出当前满足条件优惠的优惠券,购买金额要大于限制金额且是未使用的,有效的
            if (lists.get().size() > 0 && lists.get().get(0).getLimitQuota().compareTo(totalFee) <= 0) {
                discountFee = lists.get().get(0).getDenomination();
            }
        }
        return discountFee;
    }

    //计算一个订单产生的总费用
    private Map<String, Object> calOrderFee(List<SettleDTO> settleDTOList, Long userId, domain.Address address) throws Exception {

        Map<String, Object> allFee = new HashMap<>();

        //运费
        BigDecimal shipFee = BigDecimal.ZERO;

        //行邮税
        BigDecimal postalFee = BigDecimal.ZERO;

        //总计sku费用
        BigDecimal totalFee = BigDecimal.ZERO;

        //总计支付费用
        BigDecimal totalPayFee = BigDecimal.ZERO;


        List<Map<String, Object>> singleCustoms = new ArrayList<>();

        for (SettleDTO settleDTO : settleDTOList) {
            Map<String, Object> map = calCustomsFee(settleDTO, userId, address);
            if (map.containsKey("message")) {
                allFee.put("message", (int) map.get("message"));
                return allFee;
            } else {
                singleCustoms.add(map);
                //总订单产生的费用统计
                shipFee = shipFee.add((BigDecimal) map.get("shipFeeSingle"));
                postalFee = postalFee.add((BigDecimal) map.get("postalFeeSingle"));
                totalFee = totalFee.add((BigDecimal) map.get("totalFeeSingle"));
                totalPayFee = totalPayFee.add((BigDecimal) map.get("totalPayFeeSingle"));
            }
        }
        allFee.put("singleCustoms", singleCustoms);
        allFee.put("shipFee", shipFee.setScale(2, BigDecimal.ROUND_HALF_UP));
        allFee.put("postalFee", postalFee.setScale(2, BigDecimal.ROUND_HALF_UP));
        allFee.put("totalFee", totalFee.setScale(2, BigDecimal.ROUND_HALF_UP));
        allFee.put("totalPayFee", totalPayFee.setScale(2, BigDecimal.ROUND_HALF_UP));
        allFee.put("address", address);
        allFee.put("userId", userId);
        allFee.put("freeShipLimit", new BigDecimal(FREE_SHIP).setScale(2, BigDecimal.ROUND_DOWN));
        return allFee;
    }

    //创建订单
    private Order createOrder(SettleOrderDTO settleOrderDTO, Map<String, Object> allFee, Long userId) throws Exception {
        //创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setPayTotal(((BigDecimal) allFee.get("totalPayFee")).setScale(2, BigDecimal.ROUND_HALF_UP));
        order.setPayMethod(settleOrderDTO.getPayMethod());
        order.setOrderIp(settleOrderDTO.getClientIp());
        if (allFee.containsKey("discountFee")){
            order.setDiscount(((BigDecimal) allFee.get("discountFee")).setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        order.setOrderDesc(settleOrderDTO.getOrderDesc());
        order.setShipFee(((BigDecimal) allFee.get("shipFee")).setScale(2, BigDecimal.ROUND_HALF_UP));
        order.setPostalFee(((BigDecimal) allFee.get("postalFee")).setScale(2, BigDecimal.ROUND_HALF_UP));
        order.setTotalFee(((BigDecimal) allFee.get("totalFee")).setScale(2, BigDecimal.ROUND_HALF_UP));
        order.setOrderIp(settleOrderDTO.getClientIp());
        order.setClientType(settleOrderDTO.getClientType());
        if (cartService.insertOrder(order)) return order;
        else return null;
    }

    /**
     * 支付调用
     * @param orderId 订单ID
     * @return 跳转到京东支付页面
     */
    public Result payOrderWeb(Long orderId){
        return redirect("/jd/pay");
    }
}
