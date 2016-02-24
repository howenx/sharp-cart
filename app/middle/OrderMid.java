package middle;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.OrderCtrl;
import domain.*;
import play.Logger;
import play.libs.Json;
import service.CartService;
import service.IdService;
import service.PromotionService;
import service.SkuService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 订单中间层
 * Created by howen on 16/2/4.
 */
public class OrderMid {

    private SkuService skuService;

    private CartService cartService;

    private IdService idService;

    private PromotionService promotionService;

    private ActorRef orderSplitActor;

    public OrderMid(SkuService skuService, CartService cartService, IdService idService, PromotionService promotionService, ActorRef orderSplitActor) {
        this.cartService = cartService;
        this.idService = idService;
        this.skuService = skuService;
        this.promotionService = promotionService;
        this.orderSplitActor = orderSplitActor;
    }

    /**
     * 订单结算
     *
     * @param settleOrderDTO Dto
     * @param userId         用户id
     * @return SettleVo
     * @throws Exception
     */
    public SettleVo OrderSettle(SettleOrderDTO settleOrderDTO, Long userId) throws Exception {

        SettleVo settleVo = new SettleVo();

        //取用户地址
        Optional<Address> addressOptional = Optional.ofNullable(selectAddress(settleOrderDTO.getAddressId(), userId));

        if (addressOptional.isPresent()) {
            settleVo.setAddress(addressOptional.get());
        }

        settleVo = calOrderFee(settleOrderDTO.getSettleDTOs(), userId, settleVo.getAddress());

        if (settleVo.getMessageCode() != null) {
            return settleVo;
        } else if (!settleVo.getSkuTypeList().contains("pin")) {
            settleVo.setCoupons(calCoupon(userId, settleVo.getTotalFee()));
        }
        return settleVo;
    }


    //获取用户地址
    private Address selectAddress(Long addressId, Long userId) throws Exception {

        Address address = new Address();
        //如果没有用户地址ID,那么就查找用户默认地址,否则就去查找用户指定的地址
        if (addressId != null && addressId != 0) {
            address.setAddId(addressId);
        } else {
            address.setUserId(userId);
            address.setOrDefault(true);
        }

        Optional<Address> address_search = Optional.ofNullable(idService.getAddress(address));
        if (address_search.isPresent()) {
            address = address_search.get();
            JsonNode detailCity = Json.parse(address.getDeliveryCity());
            address.setProvinceCode(detailCity.get("province_code").asText());
            address.setDeliveryCity(detailCity.get("province").asText() + " " + detailCity.get("city").asText() + " " + detailCity.get("area").asText());
            return address;
        } else return null;
    }


    //计算一个订单产生的总费用
    private SettleVo calOrderFee(List<SettleDTO> settleDTOList, Long userId, Address address) throws Exception {

        SettleVo settleVo = new SettleVo();

        //运费
        BigDecimal shipFee = BigDecimal.ZERO;

        //行邮税
        BigDecimal postalFee = BigDecimal.ZERO;

        //总计sku费用
        BigDecimal totalFee = BigDecimal.ZERO;

        //总计支付费用
        BigDecimal totalPayFee = BigDecimal.ZERO;


        List<SettleFeeVo> singleCustoms = new ArrayList<>();

        List<String> skuTypeList = new ArrayList<>();//用于保存该笔订单的所有sku的类型

        for (SettleDTO settleDTO : settleDTOList) {

            //计算单个海关费用
            SettleFeeVo settleFeeVo = calCustomsFee(settleDTO, address, userId);

            if (settleFeeVo.getSkuTypeList() != null) skuTypeList.addAll(settleFeeVo.getSkuTypeList());

            if (settleFeeVo.getMessageCode() != null) {
                settleVo.setMessageCode(settleFeeVo.getMessageCode());
                return settleVo;
            } else {
                singleCustoms.add(settleFeeVo);
                //总订单产生的费用统计
                shipFee = shipFee.add(settleFeeVo.getShipSingleCustomsFee());
                postalFee = postalFee.add(settleFeeVo.getFactPortalFeeSingleCustoms());
                totalFee = totalFee.add(settleFeeVo.getSingleCustomsSumFee());
                totalPayFee = totalPayFee.add(settleFeeVo.getSingleCustomsSumPayFee());
            }
        }

        settleVo.setSingleCustoms(singleCustoms);
        settleVo.setShipFee(shipFee);
        settleVo.setPortalFee(postalFee);
        settleVo.setTotalFee(totalFee);
        settleVo.setTotalPayFee(totalPayFee);
        settleVo.setAddress(address);
        settleVo.setUserId(userId);
        settleVo.setFreeShipLimit(new BigDecimal(OrderCtrl.FREE_SHIP));
        settleVo.setPostalStandard(OrderCtrl.POSTAL_STANDARD);
        settleVo.setSkuTypeList(skuTypeList);

        //此订单的实际邮费统计
        if (settleVo.getShipFee().compareTo(new BigDecimal(OrderCtrl.FREE_SHIP)) > 0) {
            settleVo.setFactShipFee(BigDecimal.ZERO);
        } else settleVo.setFactShipFee(settleVo.getShipFee());

        //此订单的产生的实际行邮税
        if (settleVo.getPortalFee().compareTo(new BigDecimal(OrderCtrl.POSTAL_STANDARD)) <= 0) {
            settleVo.setFactPortalFee(BigDecimal.ZERO);
        } else settleVo.setFactPortalFee(settleVo.getPortalFee());

        return settleVo;
    }


    //计算每个报关单位下的所有费用
    private SettleFeeVo calCustomsFee(SettleDTO settleDTO, Address address, Long userId) throws Exception {

        SettleFeeVo settleFeeVo = new SettleFeeVo();

        //运费
        BigDecimal shipFeeSingle = BigDecimal.ZERO;

        //行邮税
        BigDecimal postalFeeSingle = BigDecimal.ZERO;

        //总计sku费用
        BigDecimal totalFeeSingle = BigDecimal.ZERO;

        //总计支付费用
        BigDecimal totalPayFeeSingle;

        List<String> skuTypeList = new ArrayList<>();//用于保存该笔订单的所有sku的类型

        Boolean orPinRestrict = false;//用于标识用户购买的拼购商品是否超出限购数量

        for (CartDto cartDto : settleDTO.getCartDtos()) {

            Sku sku = new Sku();
            sku.setId(cartDto.getSkuId());
            Optional<Sku> skuOptional = Optional.ofNullable(skuService.getInv(sku));

            if (skuOptional.isPresent()) {
                sku = skuOptional.get();
            } else {
                settleFeeVo.setMessageCode(Message.ErrorCode.SKU_DETAIL_NULL_EXCEPTION.getIndex());
                return settleFeeVo;
            }

            //先确定商品状态是正常,然后确定商品结算数量是否超出库存量
            if (!sku.getState().equals("Y")) {
                settleFeeVo.setMessageCode(Message.ErrorCode.SKU_INVALID.getIndex());
                return settleFeeVo;
            } else if (cartDto.getAmount() > sku.getRestrictAmount() && sku.getRestrictAmount() != 0) {
                settleFeeVo.setMessageCode(Message.ErrorCode.PURCHASE_QUANTITY_LIMIT.getIndex());
                return settleFeeVo;
            } else if (cartDto.getAmount() > sku.getRestAmount()) {
                settleFeeVo.setMessageCode(Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex());
                return settleFeeVo;
            } else {
                //邮费
                if (address != null && address.getProvinceCode() != null) {
                    shipFeeSingle = shipFeeSingle.add(calculateShipFee(address.getProvinceCode(), sku.getCarriageModelCode(), cartDto.getAmount()));
                } else shipFeeSingle = BigDecimal.ZERO;

                switch (cartDto.getSkuType()) {
                    case "item":
                        //sku总计费用
                        totalFeeSingle = totalFeeSingle.add(sku.getItemPrice().multiply(new BigDecimal(cartDto.getAmount())));
                        //单sku产生的行邮税
                        postalFeeSingle = postalFeeSingle.add(calculatePostalTax(sku.getPostalTaxRate(), sku.getItemPrice(), cartDto.getAmount()));
                        skuTypeList.add("item");
                        break;
                    case "vary":
                        VaryPrice varyPrice = new VaryPrice();
                        varyPrice.setId(cartDto.getSkuTypeId());
                        List<VaryPrice> varyPriceList = skuService.getVaryPriceBy(varyPrice);
                        if (varyPriceList.size() > 0) {
                            varyPrice = varyPriceList.get(0);
                        }
                        totalFeeSingle = totalFeeSingle.add(varyPrice.getPrice().multiply(new BigDecimal(cartDto.getAmount())));
                        //单sku产生的行邮税
                        postalFeeSingle = postalFeeSingle.add(calculatePostalTax(sku.getPostalTaxRate(), varyPrice.getPrice(), cartDto.getAmount()));
                        skuTypeList.add("vary");
                        break;
                    case "customize":
                        SubjectPrice subjectPrice = skuService.getSbjPriceById(cartDto.getSkuTypeId());
                        totalFeeSingle = totalFeeSingle.add(subjectPrice.getPrice().multiply(new BigDecimal(cartDto.getAmount())));
                        //单sku产生的行邮税
                        postalFeeSingle = postalFeeSingle.add(calculatePostalTax(sku.getPostalTaxRate(), subjectPrice.getPrice(), cartDto.getAmount()));
                        skuTypeList.add("customize");
                        break;
                    case "pin":
                        PinTieredPrice pinTieredPrice = new PinTieredPrice();
                        pinTieredPrice.setId(cartDto.getPinTieredPriceId());
                        pinTieredPrice.setPinId(cartDto.getSkuTypeId());
                        pinTieredPrice = promotionService.getTieredPriceById(pinTieredPrice);
                        totalFeeSingle = totalFeeSingle.add(pinTieredPrice.getPrice().multiply(new BigDecimal(cartDto.getAmount())));
                        //单sku产生的行邮税
                        postalFeeSingle = postalFeeSingle.add(calculatePostalTax(sku.getPostalTaxRate(), pinTieredPrice.getPrice(), cartDto.getAmount()));
                        skuTypeList.add("pin");

                        PinSku pinSku = promotionService.getPinSkuById(cartDto.getSkuTypeId());

                        //用户存在,需要验证用户是否符合限购策略
                        Order order = new Order();
                        order.setOrderType(2);//拼购订单
                        order.setUserId(userId);
                        List<Order> orders = cartService.getPinUserOrder(order);
                        if (orders.size() > 0) {
                            Integer userPin = 1;//拼购购买商品计数
                            for (Order os : orders) {
                                OrderLine orderLine = new OrderLine();
                                orderLine.setOrderId(os.getOrderId());
                                orderLine.setSkuType(cartDto.getSkuType());
                                orderLine.setSkuTypeId(cartDto.getSkuTypeId());
                                List<OrderLine> lines = cartService.selectOrderLine(orderLine);
                                userPin += lines.size();
                            }
                            if (userPin > pinSku.getRestrictAmount()) {
                                orPinRestrict = true;
                            }
                        }
                        break;
                }
            }
        }

        //每个海关的实际邮费统计
        if (shipFeeSingle.compareTo(new BigDecimal(OrderCtrl.FREE_SHIP)) > 0) {
            settleFeeVo.setFactSingleCustomsShipFee(BigDecimal.ZERO);
        } else settleFeeVo.setFactSingleCustomsShipFee(shipFeeSingle);

        //统计如果各个海关的实际关税,如果关税小于50元,则免税
        if (postalFeeSingle.compareTo(new BigDecimal(OrderCtrl.POSTAL_STANDARD)) <= 0) {
            settleFeeVo.setFactPortalFeeSingleCustoms(BigDecimal.ZERO);
        } else settleFeeVo.setFactPortalFeeSingleCustoms(postalFeeSingle);

        //单个海关下是否达到某个消费值时候免邮
        if (totalFeeSingle.compareTo(new BigDecimal(OrderCtrl.FREE_SHIP)) > 0) {
            settleFeeVo.setFreeShip(true);
        } else settleFeeVo.setFreeShip(false);

        //支付费用
        totalPayFeeSingle = shipFeeSingle.add(settleFeeVo.getFactPortalFeeSingleCustoms()).add(totalFeeSingle);

        //海关名称
        settleFeeVo.setInvCustoms(settleDTO.getInvCustoms());
        settleFeeVo.setInvArea(settleDTO.getInvArea());
        settleFeeVo.setInvAreaNm(settleDTO.getInvAreaNm());
        settleFeeVo.setShipSingleCustomsFee(shipFeeSingle);
        settleFeeVo.setPortalSingleCustomsFee(postalFeeSingle);
        settleFeeVo.setSingleCustomsSumFee(totalFeeSingle);
        settleFeeVo.setSingleCustomsSumPayFee(totalPayFeeSingle);
        settleFeeVo.setSingleCustomsSumAmount(settleDTO.getCartDtos().size());
        settleFeeVo.setCartDtos(settleDTO.getCartDtos());

        settleFeeVo.setSkuTypeList(skuTypeList);

        //用户购买拼购商品限购数量超出
        if (orPinRestrict) {
            settleFeeVo.setMessageCode(Message.ErrorCode.PURCHASE_PIN_RESTRICT.getIndex());
            return settleFeeVo;
        }

        //如果存在单个海关的金额超过1000,返回
        if (totalFeeSingle.compareTo(new BigDecimal(OrderCtrl.POSTAL_LIMIT)) > 0) {
            settleFeeVo.setMessageCode(Message.ErrorCode.PURCHASE_QUANTITY_SUM_PRICE.getIndex());
            return settleFeeVo;
        }

        return settleFeeVo;
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


    //计算优惠券
    private List<CouponVo> calCoupon(Long userId, BigDecimal totalFee) throws Exception {
        CouponVo couponVo = new CouponVo();
        couponVo.setUserId(userId);
        couponVo.setState("N");
        List<CouponVo> lists = cartService.getUserCoupon(couponVo);
        //优惠券,只列出当前满足条件优惠的优惠券,购买金额要大于限制金额且是未使用的,有效的
        return lists.stream().filter(s -> s.getLimitQuota().compareTo(totalFee) <= 0).collect(Collectors.toList());
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

    //创建订单
    private Order createOrder(SettleOrderDTO settleOrderDTO, SettleVo settleVo, Long userId) throws Exception {
        //创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setPayTotal(settleVo.getTotalPayFee());
        order.setPayMethod(settleOrderDTO.getPayMethod());
        order.setOrderIp(settleOrderDTO.getClientIp());
        if (settleVo.getDiscountFee() != null) {
            order.setDiscount(settleVo.getDiscountFee());
        }
        order.setOrderDesc(settleOrderDTO.getOrderDesc());
        order.setShipFee(settleVo.getShipFee());
        order.setPostalFee(settleVo.getPortalFee());
        order.setTotalFee(settleVo.getTotalFee());
        order.setOrderIp(settleOrderDTO.getClientIp());
        order.setClientType(settleOrderDTO.getClientType());


        if (settleVo.getSkuTypeList().contains("pin")) {
            order.setOrderType(2);//1:正常购买订单，2：拼购订单
            order.setOrderStatus("PI");
            if (settleOrderDTO.getPinActiveId() != null) {
                order.setPinActiveId(settleOrderDTO.getPinActiveId());
            }
        } else order.setOrderType(1);

        if (cartService.insertOrder(order)) {
            Logger.error("创建订单ID: " + order.getOrderId());
            return order;
        } else return null;
    }

    /**
     * 提交订单
     *
     * @param userId         用户ID
     * @param settleOrderDTO dto
     * @return SettleVo
     * @throws Exception
     */
    public SettleVo submitOrder(Long userId, SettleOrderDTO settleOrderDTO) throws Exception {
        SettleVo settleVo = new SettleVo();

        //取用户地址
        Optional<Address> addressOptional = Optional.ofNullable(selectAddress(settleOrderDTO.getAddressId(), userId));

        if (addressOptional.isPresent()) {
            settleVo.setAddress(addressOptional.get());
        }

        settleVo = calOrderFee(settleOrderDTO.getSettleDTOs(), userId, settleVo.getAddress());

        if (settleOrderDTO.getCouponId() != null && !settleOrderDTO.getCouponId().equals("")) {
            BigDecimal discount = calDiscount(userId, settleOrderDTO.getCouponId(), settleVo.getTotalFee());
            settleVo.setDiscountFee(discount);
            settleVo.setCouponId(settleOrderDTO.getCouponId());
            settleVo.setTotalPayFee(settleVo.getTotalFee().subtract(discount));
        }

        //前端是立即购买还是结算页提交订单
        settleVo.setBuyNow(settleOrderDTO.getBuyNow());

        if (settleVo.getMessageCode() != null) {
            return settleVo;
        }
        //创建订单
        Order order = createOrder(settleOrderDTO, settleVo, userId);
        if (order != null) {
            //调用Actor创建子订单
            settleVo.setOrderId(order.getOrderId());
            orderSplitActor.tell(settleVo, ActorRef.noSender());
        } else {
            settleVo.setMessageCode(Message.ErrorCode.CREATE_ORDER_EXCEPTION.getIndex());
        }
        return settleVo;
    }
}
