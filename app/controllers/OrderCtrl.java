package controllers;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.*;
import filters.UserAuth;
import org.apache.commons.io.FileUtils;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.mvc.*;
import service.CartService;
import service.IdService;
import service.SkuService;
import util.CalCountDown;
import util.GenCouponCode;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static akka.pattern.Patterns.ask;
import static play.libs.Json.newObject;

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

    private ActorRef cancelOrderActor;

    private ActorRef uploadImagesActor;

    //行邮税收税标准
    static String POSTAL_STANDARD;

    //海关规定购买单笔订单金额限制
    static String POSTAL_LIMIT;

    //达到多少免除邮费
    static String FREE_SHIP;

    @Inject
    public OrderCtrl(SkuService skuService, CartService cartService, IdService idService, @Named("uploadImagesActor") ActorRef uploadImagesActor, @Named("subOrderActor") ActorRef orderSplitActor, @Named("cancelOrderActor") ActorRef cancelOrderActor) {
        this.cartService = cartService;
        this.idService = idService;
        this.orderSplitActor = orderSplitActor;
        this.skuService = skuService;
        this.cancelOrderActor = cancelOrderActor;
        this.uploadImagesActor = uploadImagesActor;

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

    static final String IMG_PROCESS_URL = play.Play.application().configuration().getString("imgprocess.server.url");

    //将Json串转换成List
    private static ObjectMapper mapper = new ObjectMapper();

//    static ObjectMapper newDefaultMapper() {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new Jdk8Module());
//        mapper.registerModule(new JSR310Module());
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
//
//        SimpleModule testModule = new SimpleModule("MyModule");
//        testModule.addSerializer(new StringUnicodeSerializer()); // assuming serializer declares correct class to bind to
//        mapper.registerModule(testModule);
//
//        Json.setObjectMapper(mapper);
//        return mapper;
//    }
//

    /**
     * 请求结算页面
     *
     * @return result
     */
    @Security.Authenticated(UserAuth.class)
    public Result settle() {

        ObjectNode result = newObject();

        Optional<JsonNode> json = Optional.ofNullable(request().body().asJson());

        Map<String, Object> resultMap = new HashMap<>();

        try {
            Long userId = (Long) ctx().args.get("userId");
            if (json.isPresent() && json.get().size() > 0) {

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
                Optional<Address> addressOptional = Optional.ofNullable(selectAddress(address));

                if (addressOptional.isPresent()) {
                    address = addressOptional.get();
                }
                resultMap.put("address", address);
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

                    singleCustomsFee.put("invAreaNm", s.get("invAreaNm").toString());

                    //如果存在单个海关的金额超过1000,返回
                    if (((BigDecimal) s.get("totalFeeSingle")).compareTo(new BigDecimal(POSTAL_LIMIT)) > 0) {
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.PURCHASE_QUANTITY_SUM_PRICE.getIndex()), Message.ErrorCode.PURCHASE_QUANTITY_SUM_PRICE.getIndex())));
                        return ok(result);
                    }
                    //每个海关的总费用统计
                    singleCustomsFee.put("singleCustomsSumFee", ((BigDecimal) s.get("totalFeeSingle")).stripTrailingZeros().toPlainString());

                    //每个海关购买的总数量
                    singleCustomsFee.put("singleCustomsSumAmount", ((Integer) s.get("totalAmount")));

                    //每个海关邮费统计
                    singleCustomsFee.put("shipSingleCustomsFee", ((BigDecimal) s.get("shipFeeSingle")).stripTrailingZeros().toPlainString());

                    //每个海关的实际邮费统计
                    if (((BigDecimal) s.get("shipFeeSingle")).compareTo(new BigDecimal(FREE_SHIP)) > 0) {
                        singleCustomsFee.put("factSingleCustomsShipFee", "0");//实际邮费
                    } else
                        singleCustomsFee.put("factSingleCustomsShipFee", ((BigDecimal) s.get("shipFeeSingle")).stripTrailingZeros().toPlainString());//每次计算出的邮费

                    //每个海关的关税统计
                    singleCustomsFee.put("portalSingleCustomsFee", ((BigDecimal) s.get("postalFeeSingle")).stripTrailingZeros().toPlainString());

                    //统计如果各个海关的实际关税,如果关税小于50元,则免税
                    if (((BigDecimal) s.get("postalFeeSingle")).compareTo(new BigDecimal(POSTAL_STANDARD)) <= 0)
                        singleCustomsFee.put("factPortalFeeSingleCustoms", "0");
                    else
                        singleCustomsFee.put("factPortalFeeSingleCustoms", ((BigDecimal) s.get("postalFeeSingle")).stripTrailingZeros().toPlainString());

                    returnFee.add(singleCustomsFee);
                }


                if (((BigDecimal) allFee.get("shipFee")).compareTo(new BigDecimal(FREE_SHIP)) > 0) {
                    resultMap.put("factShipFee", "0");//实际邮费
                } else
                    resultMap.put("factShipFee", ((BigDecimal) allFee.get("shipFee")).stripTrailingZeros().toPlainString());//每次计算出的邮费

                resultMap.put("shipFee", ((BigDecimal) allFee.get("shipFee")).stripTrailingZeros().toPlainString());
                resultMap.put("portalFee", ((BigDecimal) allFee.get("postalFee")).stripTrailingZeros().toPlainString());
                //统计如果各个海关的实际关税,如果关税小于50元,则免税
                if (((BigDecimal) allFee.get("postalFee")).compareTo(new BigDecimal(POSTAL_STANDARD)) <= 0)
                    resultMap.put("factPortalFee", "0");
                else
                    resultMap.put("factPortalFee", ((BigDecimal) allFee.get("postalFee")).stripTrailingZeros().toPlainString());

                //将各个海关下的费用统计返回
                resultMap.put("singleCustoms", returnFee);

                resultMap.put("postalStandard", POSTAL_STANDARD);

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

        ObjectNode result = newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");

            Logger.error(userId+"");

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

        ObjectNode result = newObject();
        Optional<JsonNode> json = Optional.ofNullable(request().body().asJson());
        try {
            /*******取用户ID*********/
            Long userId = (Long) ctx().args.get("userId");

            if (json.isPresent() && json.get().size() > 0) {

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

                List<Map<String, Object>> singleCustoms = (List<Map<String, Object>>) allFee.get("singleCustoms");

                if (settleOrderDTO.getCouponId() != null && !settleOrderDTO.getCouponId().equals("")) {
                    BigDecimal discount = calDiscount(userId, settleOrderDTO.getCouponId(), (BigDecimal) allFee.get("totalFee"));
                    allFee.put("discountFee", discount);
                    allFee.put("couponId", settleOrderDTO.getCouponId());
                    allFee.put("totalPayFee", ((BigDecimal) allFee.get("totalPayFee")).subtract(discount).setScale(2, BigDecimal.ROUND_HALF_UP));
                }

                //前端是立即购买还是结算页提交订单
                allFee.put("buyNow", settleOrderDTO.getBuyNow());

                //创建订单
                Order order = createOrder(settleOrderDTO, allFee, userId);

                if (order != null) {
                    //调用Actor创建子订单
                    allFee.put("orderId", order.getOrderId());
                    orderSplitActor.tell(allFee, ActorRef.noSender());
                    result.putPOJO("order", Json.toJson(order));
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                    return ok(result);
                } else {
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
        return postalFee;
    }

    //获取用户地址
    private Address selectAddress(Address address) throws Exception {
        Optional<Address> address_search = Optional.ofNullable(idService.getAddress(address));
        if (address_search.isPresent()) {
            address = address_search.get();
            JsonNode detailCity = Json.parse(address.getDeliveryCity());
            address.setProvinceCode(detailCity.get("province_code").asText());
            address.setDeliveryCity(detailCity.get("province").asText() + " " + detailCity.get("city").asText() + " " + detailCity.get("area").asText());
            return address;
        } else return null;
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
                if (address != null && address.getProvinceCode() != null) {
                    shipFeeSingle = shipFeeSingle.add(calculateShipFee(address.getProvinceCode(), sku.getCarriageModelCode(), cartDto.getAmount()));
                } else shipFeeSingle = BigDecimal.ZERO;

                //单sku产生的行邮税
                postalFeeSingle = postalFeeSingle.add(calculatePostalTax(sku.getPostalTaxRate(), sku.getItemPrice(), cartDto.getAmount()));

                //sku总计费用
                totalFeeSingle = totalFeeSingle.add(sku.getItemPrice().multiply(new BigDecimal(cartDto.getAmount())));

            }
        }

        //支付费用
        totalPayFeeSingle = shipFeeSingle.add(postalFeeSingle).add(totalFeeSingle);

        //海关名称
        map.put("invCustoms", settleDTO.getInvCustoms());
        map.put("invArea", settleDTO.getInvArea());
        map.put("invAreaNm", settleDTO.getInvAreaNm());
        map.put("shipFeeSingle", shipFeeSingle.setScale(2, BigDecimal.ROUND_HALF_UP));
        map.put("postalFeeSingle", postalFeeSingle.setScale(2, BigDecimal.ROUND_HALF_UP));
        map.put("totalFeeSingle", totalFeeSingle.setScale(2, BigDecimal.ROUND_HALF_UP));
        map.put("totalPayFeeSingle", totalPayFeeSingle.setScale(2, BigDecimal.ROUND_HALF_UP));
        map.put("totalAmount", settleDTO.getCartDtos().size());
        map.put("cartDtos", settleDTO.getCartDtos());

        //单个海关下是否达到某个消费值时候免邮
        if (totalFeeSingle.compareTo(new BigDecimal(FREE_SHIP)) > 0) {
            map.put("freeShip", true);
        } else map.put("freeShip", false);

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
            //计算单个海关费用
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
        allFee.put("shipFee", shipFee.setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros());
        allFee.put("postalFee", postalFee.setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros());
        allFee.put("totalFee", totalFee.setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros());
        allFee.put("totalPayFee", totalPayFee.setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros());
        allFee.put("address", address);
        allFee.put("userId", userId);
        allFee.put("freeShipLimit", new BigDecimal(FREE_SHIP).setScale(2, BigDecimal.ROUND_DOWN).stripTrailingZeros());
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
        if (allFee.containsKey("discountFee")) {
            order.setDiscount(((BigDecimal) allFee.get("discountFee")).setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        order.setOrderDesc(settleOrderDTO.getOrderDesc());
        order.setShipFee(((BigDecimal) allFee.get("shipFee")).setScale(2, BigDecimal.ROUND_HALF_UP));
        order.setPostalFee(((BigDecimal) allFee.get("postalFee")).setScale(2, BigDecimal.ROUND_HALF_UP));
        order.setTotalFee(((BigDecimal) allFee.get("totalFee")).setScale(2, BigDecimal.ROUND_HALF_UP));
        order.setOrderIp(settleOrderDTO.getClientIp());
        order.setClientType(settleOrderDTO.getClientType());
        if (cartService.insertOrder(order)) {
            Logger.error("创建订单ID: " + order.getOrderId());
            return order;
        } else return null;
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @return promise
     */
    public F.Promise<Result> cancelOrder(Long orderId) {
        ObjectNode result = newObject();
        return F.Promise.wrap(ask(cancelOrderActor, orderId, 3000)
        ).map(response -> {
            Logger.info("取消订单:" + orderId);
            if (((Integer) response) == 200) {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(result);
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ERROR.getIndex()), Message.ErrorCode.ERROR.getIndex())));
                return ok(result);
            }
        });
    }

    /**
     * 用户查询所有订单接口
     *
     * @return 返回所有订单数据
     */
    @Security.Authenticated(UserAuth.class)
    public Result shoppingOrder(Long orderId) {
        ObjectNode result = newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");
            Order order = new Order();
            if (orderId != 0) {
                order.setOrderId(orderId);
            }
            order.setUserId(userId);

            Optional<List<Order>> orderList = Optional.ofNullable(cartService.getOrderBy(order));

            //返回总数据
            List<Map> mapList = new ArrayList<>();

            if (orderList.isPresent()) {

                for (Order o : orderList.get()) {
                    //用于保存每个订单对应的明细list和地址信息
                    Map<String, Object> map = new HashMap<>();

                    OrderAddress orderAddress = new OrderAddress();
                    orderAddress.setOrderId(o.getOrderId());

                    Optional<List<OrderAddress>> orderAddressOptional = Optional.ofNullable(cartService.selectOrderAddress(orderAddress));

                    if (orderAddressOptional.isPresent()) {
                        //获取地址信息
                        Address address = new Address();
                        address.setDeliveryCity(orderAddressOptional.get().get(0).getDeliveryCity());
                        address.setDeliveryDetail(orderAddressOptional.get().get(0).getDeliveryAddress());
                        address.setIdCardNum(orderAddressOptional.get().get(0).getDeliveryCardNum());
                        address.setName(orderAddressOptional.get().get(0).getDeliveryName());
                        address.setTel(orderAddressOptional.get().get(0).getDeliveryTel());
                        map.put("address", address);

                    }
                    o.setCountDown(CalCountDown.getTimeSubtract(o.getOrderCreateAt()));

                    //未支付订单
                    if (o.getOrderStatus().equals("I") || o.getOrderStatus().equals("C")) {

                        OrderLine orderLine = new OrderLine();
                        orderLine.setOrderId(o.getOrderId());

                        List<OrderLine> orderLineList = cartService.selectOrderLine(orderLine);

                        //每个订单对应的商品明细
                        List<CartSkuDto> skuDtoList = new ArrayList<>();

                        Integer orderAmount = 0;

                        for (OrderLine orl : orderLineList) {
                            CartSkuDto skuDto = new CartSkuDto();

                            //组装返回的订单商品明细
                            skuDto.setSkuId(orl.getSkuId());
                            skuDto.setAmount(orl.getAmount());
                            orderAmount += skuDto.getAmount();
                            skuDto.setPrice(orl.getPrice());
                            skuDto.setSkuTitle(orl.getSkuTitle());
                            if (orl.getSkuImg().contains("url")) {
                                JsonNode jsonNode = Json.parse(orl.getSkuImg());
                                if (jsonNode.has("url")) {
                                    skuDto.setInvImg(IMAGE_URL + jsonNode.get("url").asText());
                                }
                            } else skuDto.setInvImg(IMAGE_URL + orl.getSkuImg());
                            skuDto.setInvUrl(DEPLOY_URL + "/comm/detail/" + orl.getItemId() + "/" + orl.getSkuId());
                            skuDto.setInvAndroidUrl(DEPLOY_URL + "/comm/detail/web/" + orl.getItemId() + "/" + orl.getSkuId());
                            skuDto.setItemColor(orl.getSkuColor());
                            skuDto.setItemSize(orl.getSkuSize());
                            skuDtoList.add(skuDto);
                        }

                        o.setOrderAmount(orderAmount);
                        //组装每个订单对应的明细和地址
                        map.put("order", o);
                        map.put("sku", skuDtoList);
                        mapList.add(map);
                    }
                    //否则以子订单来显示
                    else {
                        OrderSplit orderSplit = new OrderSplit();
                        orderSplit.setOrderId(o.getOrderId());
                        Optional<List<OrderSplit>> optionalOrderSplitList = Optional.ofNullable(cartService.selectOrderSplit(orderSplit));
                        if (optionalOrderSplitList.isPresent()) {
                            for (OrderSplit osp : optionalOrderSplitList.get()) {
                                Order orderS = new Order();
                                orderS.setOrderId(osp.getOrderId());
                                orderS.setOrderAmount(osp.getTotalAmount());
                                orderS.setPayMethod(o.getPayMethod());
                                orderS.setPayTotal(osp.getTotalPayFee());
                                orderS.setTotalFee(osp.getTotalFee());
                                orderS.setShipFee(osp.getShipFee());
                                orderS.setPostalFee(osp.getPostalFee());
                                orderS.setOrderCreateAt(o.getOrderCreateAt());
                                orderS.setOrderStatus(o.getOrderStatus());

                                OrderLine orderLine = new OrderLine();
                                orderLine.setOrderId(o.getOrderId());
                                orderS.setOrderSplitId(osp.getSplitId());
                                List<OrderLine> orderLineList = cartService.selectOrderLine(orderLine);

                                //每个订单对应的商品明细
                                List<CartSkuDto> skuDtoList = new ArrayList<>();

                                for (OrderLine orl : orderLineList) {
                                    CartSkuDto skuDto = new CartSkuDto();

                                    //组装返回的订单商品明细
                                    skuDto.setSkuId(orl.getSkuId());
                                    skuDto.setAmount(orl.getAmount());
                                    skuDto.setPrice(orl.getPrice());
                                    skuDto.setSkuTitle(orl.getSkuTitle());
                                    if (orl.getSkuImg().contains("url")) {
                                        JsonNode jsonNode = Json.parse(orl.getSkuImg());
                                        if (jsonNode.has("url")) {
                                            skuDto.setInvImg(IMAGE_URL + jsonNode.get("url").asText());
                                        }
                                    } else skuDto.setInvImg(IMAGE_URL + orl.getSkuImg());
                                    skuDto.setInvUrl(DEPLOY_URL + "/comm/detail/" + orl.getItemId() + "/" + orl.getSkuId());
                                    skuDto.setInvAndroidUrl(DEPLOY_URL + "/comm/detail/web/" + orl.getItemId() + "/" + orl.getSkuId());
                                    skuDto.setItemColor(orl.getSkuColor());
                                    skuDto.setItemSize(orl.getSkuSize());
                                    skuDtoList.add(skuDto);
                                }

                                map.put("order", orderS);
                                map.put("sku", skuDtoList);
                                mapList.add(map);
                            }
                        }
                    }
                }
            }


            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            result.putPOJO("orderList", Json.toJson(mapList));

            return ok(result);
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    /**
     * 校验订单状态 ,前端点击去支付时
     *
     * @param orderId 订单ID
     * @return 返回消息
     */
    @Security.Authenticated(UserAuth.class)
    public Result verifyOrder(Long orderId) {
        ObjectNode result = newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");
            if (orderId != 0) {
                Order order = new Order();
                order.setOrderId(orderId);
                order.setUserId(userId);
                Optional<List<Order>> listOptional = Optional.ofNullable(cartService.getOrderBy(order));
                if (listOptional.isPresent() && listOptional.get().size() > 0) {
                    order = cartService.getOrderBy(order).get(0);
                    Optional<Long> longOptional = Optional.ofNullable(CalCountDown.getTimeSubtract(order.getOrderCreateAt()));
                    if (longOptional.isPresent() && longOptional.get().compareTo(JDPay.COUNTDOWN_MILLISECONDS) > 0) {
                        cancelOrderActor.tell(orderId, null);
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ORDER_CANCEL_AUTO.getIndex()), Message.ErrorCode.ORDER_CANCEL_AUTO.getIndex())));
                        return ok(result);
                    } else {
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                        return ok(result);
                    }
                } else {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
                    return ok(result);
                }
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
                return ok(result);
            }
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    /**
     * 删除订单
     *
     * @param orderId 订单ID
     * @return 消息
     */
    @Security.Authenticated(UserAuth.class)
    public Result delOrder(Long orderId) {
        ObjectNode result = newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");
            if (orderId != 0) {
                Order order = new Order();
                order.setOrderId(orderId);
                order.setUserId(userId);
                Optional<List<Order>> listOptional = Optional.ofNullable(cartService.getOrderBy(order));
                if (listOptional.isPresent() && listOptional.get().size() > 0) {
                    order = cartService.getOrderBy(order).get(0);

                    switch (order.getOrderStatus()) {
                        case "I":
                            cancelOrderActor.tell(orderId, null);
                            order.setOrderStatus("N");
                            if (cartService.updateOrder(order)) {
                                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                                return ok(result);
                            } else {
                                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.DATABASE_EXCEPTION.getIndex()), Message.ErrorCode.DATABASE_EXCEPTION.getIndex())));
                                return ok(result);
                            }
                        case "S":
                            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ORDER_DEL.getIndex()), Message.ErrorCode.ORDER_DEL.getIndex())));
                            return ok(result);
                        case "D":
                            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ORDER_DEL.getIndex()), Message.ErrorCode.ORDER_DEL.getIndex())));
                            return ok(result);
                        default:
                            order.setOrderStatus("N");
                            if (cartService.updateOrder(order)) {
                                Logger.info("删除订单ID: " + order.getOrderId());
                                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                                return ok(result);
                            } else {
                                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.DATABASE_EXCEPTION.getIndex()), Message.ErrorCode.DATABASE_EXCEPTION.getIndex())));
                                return ok(result);
                            }
                    }
                } else {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
                    return ok(result);
                }
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
                return ok(result);
            }
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    /**
     * 退货申请
     *
     * @return result
     */
    @Security.Authenticated(UserAuth.class)
    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = 50 * 1024 * 1024)
    public Result refundApply() {

        ObjectNode result = newObject();

        Http.MultipartFormData body = request().body().asMultipartFormData();

        Map<String,String[]> stringMap = body.asFormUrlEncoded();
        Map<String,String> map = new HashMap<>();

        stringMap.forEach((k, v) -> map.put(k,v[0]));

        Optional<JsonNode> json = Optional.ofNullable(Json.toJson(map));

        Long userId = (Long) ctx().args.get("userId");
        try {
            if (json.isPresent() && json.get().size() > 0) {

                Refund refund = Json.fromJson(json.get(),Refund.class);
                if (refund.getOrderId()!=null && refund.getSkuId()!=null){
                    OrderLine orderLine = new OrderLine();
                    orderLine.setOrderId(refund.getOrderId());
                    orderLine.setSkuId(refund.getSkuId());
                    List<OrderLine> orderLines = cartService.selectOrderLine(orderLine);
                    if (orderLines.size()>0) orderLine = orderLines.get(0);
                    refund.setPayBackFee(orderLine.getPrice().multiply(new BigDecimal(refund.getAmount())).setScale(2,BigDecimal.ROUND_HALF_UP));
                }
                refund.setUserId(userId);
                List<Http.MultipartFormData.FilePart> fileParts = body.getFiles();

                Boolean flag = cartService.insertRefund(refund);

                if (!fileParts.isEmpty() && flag) {
                    Map<String,Object> mapActor = new HashMap<>();
                    List<byte[]> files = new ArrayList<>();
                    mapActor.put("refundId",refund.getId());
                    for (Http.MultipartFormData.FilePart filePart : fileParts) {
                        if (!"image/jpeg".equalsIgnoreCase(filePart.getContentType()) &&  !"image/png".equalsIgnoreCase(filePart.getContentType())) {
                            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FILE_TYPE_NOT_SUPPORTED.getIndex()), Message.ErrorCode.FILE_TYPE_NOT_SUPPORTED.getIndex())));
                            return badRequest(result);
                        }else{
                            files.add(FileUtils.readFileToByteArray(filePart.getFile()));
                        }
                    }
                    mapActor.put("files",files);
                    mapActor.put("url",IMG_PROCESS_URL);
                    uploadImagesActor.tell(mapActor, ActorRef.noSender());
                }

                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(result);

            }else{
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
                return ok(result);
            }

        } catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }
}
