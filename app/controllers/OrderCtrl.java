package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.*;
import filters.UserAuth;
import net.spy.memcached.MemcachedClient;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import service.CartService;
import service.IdService;
import service.SkuService;
import util.GenCouponCode;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import static play.libs.F.Promise.promise;
/**
 * 订单相关,提交订单,优惠券
 * Created by howen on 15/12/1.
 */
public class OrderCtrl extends Controller {

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

    @Security.Authenticated(UserAuth.class)
    public Result settle() {

        //行邮税收税标准
        final String POSTAL_STANDARD = skuService.getSysParameter(new SysParameter(null, null, null, "POSTAL_STANDARD")).getParameterVal();

        //海关规定购买单笔订单金额限制
        final String POSTAL_LIMIT = skuService.getSysParameter(new SysParameter(null, null, null, "POSTAL_LIMIT")).getParameterVal();

        //达到多少免除邮费
        final String FREE_SHIP = skuService.getSysParameter(new SysParameter(null, null, null, "FREE_SHIP")).getParameterVal();

        Optional<JsonNode> json = Optional.ofNullable(request().body().asJson());

        ObjectNode result = Json.newObject();

        Map<String, Object> resultMap = new HashMap<>();

        try {
            Long userId = (Long) ctx().args.get("userId");
            if (json.isPresent() && json.get().size() > 0) {

                List<SettleDTO> settleDTOs = mapper.readValue(json.get().toString(), mapper.getTypeFactory().constructCollectionType(List.class, SettleDTO.class));

                //运费
                BigDecimal shipFee = new BigDecimal(0);
                //行邮税
                BigDecimal portalFee = new BigDecimal(0);
                //实际行邮税
                BigDecimal portalFeeFree = new BigDecimal(0);

                //总数量
                Integer skuAmount = 0;

                //总费用
                BigDecimal sumAmount = new BigDecimal(0);

                List<Map<String, Object>> returnFee = new ArrayList<>();

                //省份代码
                String province_code = "";
                //查询用户地址
                Address address = new Address();
                for (SettleDTO settleDTO : settleDTOs) {

                    //如果没有用户地址ID,那么就查找用户默认地址,否则就去查找用户指定的地址
                    if (settleDTO.getAddressId() != null && settleDTO.getAddressId() != 0) {
                        address.setAddId(settleDTO.getAddressId());
                    } else {
                        address.setUserId(userId);
                        address.setOrDefault(true);
                    }

                    Optional<Address> address_search = Optional.ofNullable(idService.getAddress(address));
                    if (address_search.isPresent()) {
                        address = address_search.get();
                        JsonNode detailCity = Json.parse(address.getDeliveryCity());

                        province_code = detailCity.get("province_code").asText();

                        address.setDeliveryCity(detailCity.get("province").asText() + " " + detailCity.get("city").asText() + " " + detailCity.get("area").asText());
                    }

                    Map<String, Object> map = new HashMap<>();
                    //针对每个海关的总费用
                    BigDecimal sumAmountSingleCustoms = new BigDecimal(0);
                    //针对每个海关的总数量
                    Integer skuAmountSingleCustoms = 0;

                    //运费
                    BigDecimal shipSingleCustomsFee = new BigDecimal(0);
                    //行邮税
                    BigDecimal portalSingleCustomsFee = new BigDecimal(0);

                    for (CartDto cartDto : settleDTO.getCartDtos()) {

                        Cart cart = new Cart();
                        cart.setSkuId(cartDto.getSkuId());
                        cart.setUserId(userId);
                        cart.setAmount(cartDto.getAmount());

                        Sku sku = new Sku();
                        sku.setId(cartDto.getSkuId());
                        Optional<Sku> skuOptional = Optional.ofNullable(skuService.getInv(sku));

                        if (skuOptional.isPresent()){
                            sku=skuOptional.get();
                        }else{
                            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SKU_DETAIL_NULL_EXCEPTION.getIndex()), Message.ErrorCode.SKU_DETAIL_NULL_EXCEPTION.getIndex())));
                            return ok(result);
                        }

                        //先确定商品状态是正常,然后确定商品结算数量是否超出库存量
                        if (!sku.getState().equals("Y")) {
                            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SKU_INVALID.getIndex()), Message.ErrorCode.SKU_INVALID.getIndex())));
                            return ok(result);
                        } else if (cartDto.getAmount() > sku.getRestrictAmount() && sku.getRestrictAmount()!=0) {
                            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.PURCHASE_QUANTITY_LIMIT.getIndex()), Message.ErrorCode.PURCHASE_QUANTITY_LIMIT.getIndex())));
                            return ok(result);
                        } else if (cartDto.getAmount() > sku.getRestAmount()) {
                            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex()), Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex())));
                            return ok(result);
                        } else {

                            //如果存在默认地址,取出默认地址下的邮费
                            if (!province_code.equals("")) {

                                //取邮费
                                Carriage carriage = new Carriage();
                                carriage.setCityCode(province_code);
                                carriage.setModelCode(sku.getCarriageModelCode());
                                Optional<Carriage> carriageOptional = Optional.ofNullable(skuService.getCarriage(carriage));
                                if (carriageOptional.isPresent()){
                                    carriage=carriageOptional.get();
                                    //规则:如果购买数量小于首件数量要求,则取首费,否则就整除续件数量+1,乘以续费再加首费
                                    if (cart.getAmount() <=carriage.getFirstNum()) {
                                        shipFee = shipFee.add(carriage.getFirstFee());
                                        shipSingleCustomsFee = shipSingleCustomsFee.add(carriage.getFirstFee());
                                    } else {
                                        shipFee = shipFee.add(carriage.getFirstFee()).add(new BigDecimal((cart.getAmount() / carriage.getAddNum()) + 1).multiply(carriage.getAddFee()));
                                        shipSingleCustomsFee = shipSingleCustomsFee.add(carriage.getFirstFee()).add(new BigDecimal((cart.getAmount() / carriage.getAddNum()) + 1).multiply(carriage.getAddFee()));
                                    }
                                }else{
                                    Logger.error("未找到相应邮费信息");
                                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.NOT_FOUND_SHIP_FEE_INFO.getIndex()), Message.ErrorCode.NOT_FOUND_SHIP_FEE_INFO.getIndex())));
                                    return ok(result);
                                }
                            }

                            //总费用
                            sumAmount = sumAmount.add(sku.getItemPrice().multiply(new BigDecimal(cart.getAmount())));

                            //针对每个海关的总费用
                            sumAmountSingleCustoms = sumAmountSingleCustoms.add(sku.getItemPrice().multiply(new BigDecimal(cart.getAmount())));

                            //总数量
                            skuAmount = +cart.getAmount();

                            //每个海关的总数量
                            skuAmountSingleCustoms = +cart.getAmount();

                            //计算行邮税,行邮税加和
                            portalFee = portalFee.add(new BigDecimal(sku.getPostalTaxRate()).multiply(sku.getItemPrice()).multiply(new BigDecimal(cart.getAmount())).multiply(new BigDecimal(0.01)));

                            //统计如果各个海关的实际关税,如果关税小于50(sku.getPostalStandard())元,则免税
                            if ((new BigDecimal(sku.getPostalTaxRate()).multiply(sku.getItemPrice()).multiply(new BigDecimal(cart.getAmount())).multiply(new BigDecimal(0.01))).compareTo(new BigDecimal(POSTAL_STANDARD)) > 0)
                                portalFeeFree = portalFeeFree.add(new BigDecimal(sku.getPostalTaxRate()).multiply(sku.getItemPrice()).multiply(new BigDecimal(cart.getAmount())).multiply(new BigDecimal(0.01)));

                            //单个海关行邮税
                            portalSingleCustomsFee = portalSingleCustomsFee.add(new BigDecimal(sku.getPostalTaxRate()).multiply(sku.getItemPrice()).multiply(new BigDecimal(cart.getAmount())).multiply(new BigDecimal(0.01)));
                        }
                    }

                    //海关名称
                    map.put("invCustoms", settleDTO.getInvCustoms());

                    map.put("invArea", settleDTO.getInvArea());

                    //如果存在单个海关的金额超过1000,返回
                    if (sumAmountSingleCustoms.compareTo(new BigDecimal(POSTAL_LIMIT)) > 0) {

                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.PURCHASE_QUANTITY_SUM_PRICE.getIndex()), Message.ErrorCode.PURCHASE_QUANTITY_SUM_PRICE.getIndex())));
                        return ok(result);
                    }
                    //每个海关的总费用统计
                    map.put("singleCustomsSumFee", sumAmountSingleCustoms.setScale(2, BigDecimal.ROUND_DOWN).toPlainString());

                    //每个海关购买的总数量
                    map.put("singleCustomsSumAmount", skuAmountSingleCustoms);

                    //每个海关邮费统计
                    map.put("shipSingleCustomsFee", shipSingleCustomsFee.setScale(2, BigDecimal.ROUND_DOWN).toPlainString());

                    //每个海关的实际邮费统计
                    if (sumAmount.compareTo(new BigDecimal(FREE_SHIP)) > 0) {
                        map.put("factSingleCustomsShipFee", 0);//实际邮费
                    } else
                        map.put("factSingleCustomsShipFee", shipSingleCustomsFee.setScale(2, BigDecimal.ROUND_DOWN).toPlainString());//每次计算出的邮费

                    //每个海关的关税统计
                    map.put("portalSingleCustomsFee", portalSingleCustomsFee.setScale(2, BigDecimal.ROUND_DOWN).toPlainString());

                    //统计如果各个海关的实际关税,如果关税小于50元,则免税
                    if (portalSingleCustomsFee.compareTo(new BigDecimal(POSTAL_STANDARD)) <= 0)
                        map.put("factPortalFeeSingleCustoms", 0);
                    else
                        map.put("factPortalFeeSingleCustoms", portalSingleCustomsFee.setScale(2, BigDecimal.ROUND_DOWN).toPlainString());

                    returnFee.add(map);
                }

                if (sumAmount.compareTo(new BigDecimal(FREE_SHIP)) > 0) {
                    resultMap.put("factShipFee", 0);//实际邮费
                } else
                    resultMap.put("factShipFee", shipFee.setScale(2, BigDecimal.ROUND_DOWN).toPlainString());//每次计算出的邮费
                resultMap.put("shipFee", shipFee.setScale(2, BigDecimal.ROUND_DOWN).toPlainString());
                resultMap.put("portalFee", portalFee.setScale(2, BigDecimal.ROUND_DOWN).toPlainString());
                //统计如果各个海关的实际关税,如果关税小于50元,则免税
                if (portalFeeFree.compareTo(new BigDecimal(POSTAL_STANDARD)) <= 0)
                    resultMap.put("factPortalFee", 0);
                else resultMap.put("factPortalFee", portalFeeFree.setScale(2, BigDecimal.ROUND_DOWN).toPlainString());
                resultMap.put("address", address);

                //将各个海关下的费用统计返回
                resultMap.put("singleCustoms", returnFee);

                CouponVo couponVo = new CouponVo();
                couponVo.setUserId(userId);
                couponVo.setState("N");
                List<CouponVo> lists = cartService.getUserCoupon(couponVo);

                final BigDecimal sum = sumAmount;

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
            Logger.error("settle: "+ex.getMessage());
            ex.printStackTrace();
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ERROR.getIndex()), Message.ErrorCode.ERROR.getIndex())));
            return ok(result);
        }
    }

    //发放优惠券----->下订单时候,检查如果用户订单金额大于包邮金额,就发放一个999开头的优惠券,并且是已使用状态,当前时间下的
    public Result publicCoupons() throws Exception {
        CouponVo couponVo = new CouponVo();
        couponVo.setUserId(((Integer) 1000038).longValue());
        couponVo.setDenomination((BigDecimal.valueOf(50)));
        Calendar cal = Calendar.getInstance();
        couponVo.setStartAt( new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime()));
        cal.add(Calendar.MONTH,2);
        couponVo.setEndAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime()));
        String coupId = GenCouponCode.GetCode(GenCouponCode.CouponClassCode.REGISTER_PUBLIC.getIndex(), 8);
        couponVo.setCoupId(coupId);
        couponVo.setCateId(((Integer) GenCouponCode.CouponClassCode.REGISTER_PUBLIC.getIndex()).longValue());
        couponVo.setState("N");
        couponVo.setCateNm(GenCouponCode.CouponClassCode.REGISTER_PUBLIC.getName());
        couponVo.setLimitQuota(BigDecimal.valueOf(0));
        cartService.insertCoupon(couponVo);
        return ok("Success");
    }

    /**
     * 购物券List
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

//    @Security.Authenticated(UserAuth.class)
    public F.Promise<Result> submitOrder() {
        return null;
    }

}
