package middle;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import controllers.Application;
import controllers.OrderCtrl;
import domain.*;
import play.Logger;
import play.libs.Json;
import service.CartService;
import service.IdService;
import service.PromotionService;
import service.SkuService;
import util.CalCountDown;
import util.ComUtil;
import util.SysParCom;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单中间层
 * Created by howen on 16/2/4.
 */
public class OrderMid {

    @Inject
    private SkuService skuService;

    @Inject
    private CartService cartService;

    @Inject
    private IdService idService;

    @Inject
    private PromotionService promotionService;

    @Inject
    @Named("subOrderActor")
    private ActorRef orderSplitActor;

    @Inject
    private ComUtil comUtil;

    @Inject
    private Application application;

    @Inject
    private OrderCtrl orderCtrl;

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
            settleVo.setCoupons(calCoupon(userId, settleVo,settleOrderDTO));
        }
        return settleVo;
    }


    //获取用户地址
    private Address selectAddress(Long addressId, Long userId) throws Exception {

        Address address = new Address();
        address.setUserId(userId); //加人判断
        //如果没有用户地址ID,那么就查找用户默认地址,否则就去查找用户指定的地址
        if (addressId != null && addressId != 0) {
            address.setAddId(addressId);
        } else {
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
        List<Long> itemIdList = new ArrayList<>();//用于保存该笔订单的所有itemId
        List<Long> invIdList = new ArrayList<>();//用于保存该笔订单的所有invId

        for (SettleDTO settleDTO : settleDTOList) {

            //计算单个海关费用
            SettleFeeVo settleFeeVo = calCustomsFee(settleDTO, address, userId);

            if (settleFeeVo.getSkuTypeList() != null) skuTypeList.addAll(settleFeeVo.getSkuTypeList());
            if (null!=settleFeeVo.getItemIdList()) itemIdList.addAll(settleFeeVo.getItemIdList());
            if (null!=settleFeeVo.getInvIdList()) invIdList.addAll(settleFeeVo.getInvIdList());

            if (settleFeeVo.getMessageCode() != null) {
                settleVo.setMessageCode(settleFeeVo.getMessageCode());
                return settleVo;
            } else {
                singleCustoms.add(settleFeeVo);
                //总订单产生的费用统计
                shipFee = shipFee.add(settleFeeVo.getShipSingleCustomsFee());
                postalFee = postalFee.add(settleFeeVo.getFactPortalFeeSingleCustoms());
                totalFee = totalFee.add(settleFeeVo.getSingleCustomsSumFee());
//                totalPayFee = totalPayFee.add(settleFeeVo.getSingleCustomsSumPayFee());
            }
        }

        settleVo.setSingleCustoms(singleCustoms);
        settleVo.setShipFee(shipFee);
        settleVo.setPortalFee(postalFee);
        settleVo.setTotalFee(totalFee);

        settleVo.setAddress(address);
        settleVo.setUserId(userId);
        settleVo.setFreeShipLimit(new BigDecimal(SysParCom.FREE_SHIP));
        settleVo.setPostalStandard(SysParCom.POSTAL_STANDARD);
        settleVo.setSkuTypeList(skuTypeList);
        settleVo.setItemIdList(itemIdList);
        settleVo.setInvIdList(invIdList);

        //此订单的实际邮费统计
        if (settleVo.getTotalFee().compareTo(new BigDecimal(SysParCom.FREE_SHIP)) > 0) {
            settleVo.setFactShipFee(BigDecimal.ZERO);
        } else settleVo.setFactShipFee(settleVo.getShipFee());

        //此订单的产生的实际行邮税
        if (settleVo.getPortalFee().compareTo(new BigDecimal(SysParCom.POSTAL_STANDARD)) <= 0) {
            settleVo.setFactPortalFee(BigDecimal.ZERO);
        } else settleVo.setFactPortalFee(settleVo.getPortalFee());

        //总支付费用
        settleVo.setTotalPayFee(settleVo.getFactShipFee().add(settleVo.getFactPortalFee()).add(totalFee));

        return settleVo;
    }


    //计算每个报关单位下的所有费用
    private SettleFeeVo calCustomsFee(SettleDTO settleDTO, Address address, Long userId) throws Exception {

        SettleFeeVo settleFeeVo = new SettleFeeVo();


        //行邮税
        BigDecimal postalFeeSingle = BigDecimal.ZERO;

        //总计sku费用
        BigDecimal totalFeeSingle = BigDecimal.ZERO;

        //总计支付费用
        BigDecimal totalPayFeeSingle;

        List<String> skuTypeList = new ArrayList<>();//用于保存该笔订单的所有sku的类型
        List<Long> itemIdList = new ArrayList<>();//用于保存该笔订单的所有itemId
        List<Long> invIdList = new ArrayList<>();//用于保存该笔订单的所有invId

        Boolean orPinRestrict = false;//用于标识用户购买的拼购商品是否超出限购数量


        Integer totalWeight = 0;//计算所有商品的总重量单位毫克

        for (CartDto cartDto : settleDTO.getCartDtos()) {

            SkuVo skuVo = new SkuVo();
            skuVo.setSkuType(cartDto.getSkuType());
            skuVo.setSkuTypeId(cartDto.getSkuTypeId());


            Optional<List<SkuVo>> skuOptional = Optional.ofNullable(skuService.getAllSkus(skuVo));
            if (skuOptional.isPresent() && skuOptional.get().size() > 0) { //可能为null,加入数量判断
                skuVo = skuOptional.get().get(0);
            } else {
                settleFeeVo.setMessageCode(Message.ErrorCode.SKU_DETAIL_NULL_EXCEPTION.getIndex());
                return settleFeeVo;
            }

            //先确定商品状态是正常,然后确定商品结算数量是否超出库存量
            if ("K".equals(skuVo.getState())) { //单个sku状态  'Y'--正常,'D'--下架,'N'--删除,'K'--售空，'P'--预售
                settleFeeVo.setMessageCode(Message.ErrorCode.SKU_DOWN.getIndex());
                return settleFeeVo;
            }else if ("P".equals(skuVo.getState())) { //单个sku状态  'Y'--正常,'D'--下架,'N'--删除,'K'--售空，'P'--预售
                settleFeeVo.setMessageCode(Message.ErrorCode.SKU_PRE_SALE.getIndex());
                return settleFeeVo;
            }else if (!skuVo.getState().equals("Y")) {
                settleFeeVo.setMessageCode(Message.ErrorCode.SKU_INVALID.getIndex());
                return settleFeeVo;
            } else if (!skuVo.getSkuTypeStatus().equals("Y")) {
                settleFeeVo.setMessageCode(Message.ErrorCode.SKU_DOWN.getIndex());
                return settleFeeVo;
            } else if (comUtil.isOutOfRestrictAmount(cartDto.getAmount(), skuVo)) {
                settleFeeVo.setMessageCode(Message.ErrorCode.PURCHASE_QUANTITY_LIMIT.getIndex());
                return settleFeeVo;
            } else if (cartDto.getAmount() > skuVo.getRestAmount()) {
                settleFeeVo.setMessageCode(Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex());
                return settleFeeVo;
            } else {
                //邮费
                if (address != null && address.getProvinceCode() != null) {
                    totalWeight += skuVo.getInvWeight();
                }

                skuTypeList.add(skuVo.getSkuType());
                itemIdList.add(skuVo.getItemId());
                invIdList.add(skuVo.getInvId());
                cartDto.setItemId(skuVo.getItemId()); //暂存的itemId
                cartDto.setInvId(skuVo.getInvId()); //暂存的invId

                if (cartDto.getSkuType().equals("pin")) {
                    //1.取消重复拼团相关的限制，即同一个用户在发起或参加一个拼团且拼团未成功时，应还可以参加或发起另一个拼团 2016-08-30
//                    PinActivity pinActivity = new PinActivity();
//                    pinActivity.setMasterUserId(userId);
//                    pinActivity.setPinId(cartDto.getSkuTypeId());
//                    pinActivity.setStatus("Y");
//                    List<PinActivity> pinActivities = promotionService.selectPinActivity(pinActivity);
//                    if (pinActivities.size() > 0) {
//                        settleFeeVo.setMessageCode(Message.ErrorCode.PURCHASE_PIN_SINGLE_ONE_TIME.getIndex());
//                    }

                    PinTieredPrice pinTieredPrice = new PinTieredPrice();
                    pinTieredPrice.setId(cartDto.getPinTieredPriceId());
                    pinTieredPrice.setPinId(cartDto.getSkuTypeId());
                    pinTieredPrice = promotionService.getTieredPriceById(pinTieredPrice);
                    BigDecimal feeSingle=pinTieredPrice.getPrice().multiply(new BigDecimal(cartDto.getAmount()));
                    totalFeeSingle = totalFeeSingle.add(feeSingle);
                    cartDto.setFeeSingle(feeSingle);
                    //单sku产生的行邮税
                    postalFeeSingle = postalFeeSingle.add(calculatePostalTax(skuVo.getPostalTaxRate(), pinTieredPrice.getPrice(), cartDto.getAmount()));


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
                } else {
                    if (cartDto.getSkuType().equals("vary")) {
                        Integer varyAmount = application.validateVary(skuVo.getSkuTypeId(), cartDto.getAmount());
                        if (varyAmount == null || varyAmount < 0) {
                            settleFeeVo.setMessageCode(Message.ErrorCode.VARY_OVER_LIMIT.getIndex());
                        }
                    }
                    BigDecimal feeSingle=skuVo.getSkuTypePrice().multiply(new BigDecimal(cartDto.getAmount()));
                    totalFeeSingle = totalFeeSingle.add(feeSingle);
                    postalFeeSingle = postalFeeSingle.add(calculatePostalTax(skuVo.getPostalTaxRate(), skuVo.getSkuTypePrice(), cartDto.getAmount()));
                    cartDto.setFeeSingle(feeSingle);//每个商品*数量的费用
                }
            }
        }

        BigDecimal shipFeeSingle = BigDecimal.ZERO;

        if (address != null && address.getProvinceCode() != null) {
            shipFeeSingle = calculateShipFee(address.getProvinceCode(), settleDTO.getInvArea(), totalWeight);
        }


        //每个海关的实际邮费统计
        if (totalFeeSingle.compareTo(new BigDecimal(SysParCom.FREE_SHIP)) > 0) {
            settleFeeVo.setFactSingleCustomsShipFee(BigDecimal.ZERO);
            settleFeeVo.setFreeShip(true);
        } else {
            settleFeeVo.setFreeShip(false);
            settleFeeVo.setFactSingleCustomsShipFee(shipFeeSingle);
        }

        //统计如果各个海关的实际关税,如果关税小于50元,则免税
        if (postalFeeSingle.compareTo(new BigDecimal(SysParCom.POSTAL_STANDARD)) <= 0) {
            settleFeeVo.setFactPortalFeeSingleCustoms(BigDecimal.ZERO);
        } else settleFeeVo.setFactPortalFeeSingleCustoms(postalFeeSingle);


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
        settleFeeVo.setItemIdList(itemIdList);
        settleFeeVo.setInvIdList(invIdList);

        //用户购买拼购商品限购数量超出
        if (orPinRestrict) {
            settleFeeVo.setMessageCode(Message.ErrorCode.PURCHASE_PIN_RESTRICT.getIndex());
            return settleFeeVo;
        }

        //如果存在单个海关的金额超过2000,返回   直邮不限制
        if (comUtil.isOutOfPostalLimit(settleDTO.getInvArea(), totalFeeSingle)) {
            settleFeeVo.setMessageCode(Message.ErrorCode.PURCHASE_QUANTITY_SUM_PRICE.getIndex());
            return settleFeeVo;
        }

        return settleFeeVo;
    }


    //计算邮费
    private BigDecimal calculateShipFee(String provinceCode, String invArea, Integer totalWeight) throws Exception {

        return BigDecimal.ZERO;

//        BigDecimal shipFee = BigDecimal.ZERO;
//        //取邮费
//        Carriage carriage = new Carriage();
//        carriage.setCityCode(provinceCode);
//        carriage.setStoreArea(invArea);
//
//        Optional<Carriage> carriageOptional = Optional.ofNullable(skuService.getCarriage(carriage));
//        if (carriageOptional.isPresent()) {
//            carriage = carriageOptional.get();
//            //规则:如果购买数量小于首件数量要求,则取首费,否则就整除续件数量+1,乘以续费再加首费
//            if (totalWeight <= carriage.getFirstNum()) {
//                shipFee = shipFee.add(carriage.getFirstFee());
//            } else {
//                Integer addWeight = 0;
//                if ((totalWeight - carriage.getFirstNum()) % carriage.getAddNum() > 0) {
//                    addWeight = (totalWeight - carriage.getFirstNum()) / carriage.getAddNum() + 1;
//                } else addWeight = (totalWeight - carriage.getFirstNum()) / carriage.getAddNum();
//                shipFee = shipFee.add(carriage.getFirstFee()).add(new BigDecimal(addWeight).multiply(carriage.getAddFee()));
//            }
//        }
//
//        return shipFee;
    }

    //计算行邮税
    private BigDecimal calculatePostalTax(String postalTaxRate, BigDecimal price, Integer amount) {

        return BigDecimal.ZERO;

//        if (postalTaxRate == null) {
//            return BigDecimal.ZERO;
//        }
//        BigDecimal postalFee = BigDecimal.ZERO;
//        //计算行邮税,行邮税加和
//        postalFee = postalFee.add(new BigDecimal(postalTaxRate).multiply(price).multiply(new BigDecimal(amount)).multiply(new BigDecimal(0.01)));
//        return postalFee;
    }


    //计算优惠券
    private List<CouponVo> calCoupon(Long userId, SettleVo settleVo,SettleOrderDTO settleOrderDTO) throws Exception {
        CouponVo couponVo = new CouponVo();
        couponVo.setUserId(userId);
        couponVo.setState("N");
        List<CouponVo> lists = cartService.getUserCoupon(couponVo);
        //优惠券,只列出当前满足条件优惠的优惠券,购买金额要大于限制金额且是未使用的,有效的
        return lists.stream().filter(s ->checkCouponCondition(s,settleVo,settleOrderDTO)).collect(Collectors.toList());
    }

    /**
     * 检测符合要求的优惠券
     * @param couponVo
     * @param settleVo
     * @param settleOrderDTO
     * @return
     */
    private boolean checkCouponCondition(CouponVo couponVo,SettleVo settleVo,SettleOrderDTO settleOrderDTO){
        if(couponVo.getLimitQuota().compareTo(settleVo.getTotalFee()) >0){ //购买金额要大于限制金额且是未使用的,有效的
            return false;
        }
        CouponMap temp=new CouponMap();
        temp.setCouponCateId(couponVo.getCoupCateId());
        List<CouponMap> couponMapList=cartService.getCouponMap(temp);
    //    Logger.info("===couponVo=="+couponVo);
     //   Logger.info("===settleOrderDTO=="+settleOrderDTO);
     //   Logger.info("==settleVo.getItemIdList()==="+settleVo.getItemIdList()+"==getInvIdList="+settleVo.getInvIdList());
        if(null!=couponMapList&&couponMapList.size()>0){
            for(CouponMap couponMap:couponMapList){
                //1.全场，2.商品，3.sku，4.拼购商品，5.商品二级类目，6.主题，7.商品一级类目
                //0表示任意商品，item_id，inv_id，pin_id，cate_id，theme_id，pcate_id
                List<Long> idList=new ArrayList<>();
                if(couponMap.getCateType()==1){
                    return true;
                }else if(couponMap.getCateType()==2){ //2.商品 item_id
                    if(null!=settleVo.getItemIdList()&&settleVo.getItemIdList().contains(couponMap.getCateTypeId())){
                        idList.add(couponMap.getCateTypeId());
                        //该部分的商品满足限额
                        if(checkLimitQuotaByItemId(settleOrderDTO,idList,couponVo.getLimitQuota())){
                            return true;
                        }else{
                            Logger.debug("该购物券不满足限额"+couponMap+",couponVo="+couponVo);
                        }
                    }
                }else if(couponMap.getCateType()==3){//3.sku item_id
                    if(null!=settleVo.getInvIdList()&&settleVo.getInvIdList().contains(couponMap.getCateTypeId())){
                        idList.add(couponMap.getCateTypeId());
                        //该部分的商品满足限额
                        if(checkLimitQuotaByInvId(settleOrderDTO,idList,couponVo.getLimitQuota())){
                            return true;
                        }else{
                            Logger.debug("该购物券不满足限额"+couponMap+",couponVo="+couponVo);
                        }
                    }
                }else if(couponMap.getCateType()==4){//4.拼购商品 pin_id
                    if(null!=settleOrderDTO.getPinActiveId()&&settleOrderDTO.getPinActiveId()>0&&settleOrderDTO.getPinActiveId().longValue()==couponMap.getCateTypeId()){
                        return true;
                    }
                }else if(couponMap.getCateType()==5){ //5.商品二级类目 cate_id
                   if(null!=settleVo.getItemIdList()){
                       for(Long itemId:settleVo.getItemIdList()){
                           Item item=new Item();
                           item.setId(itemId);
                           List<Item> itemList=skuService.getItemBy(item);
                           if(null!=itemList&&itemList.size()>0){
                               if(couponMap.getCateTypeId().longValue()==itemList.get(0).getCateId()){
                                   idList.add(itemList.get(0).getId());
                               }
                           }
                       }
                       //该部分的商品满足限额
                       if(checkLimitQuotaByItemId(settleOrderDTO,idList,couponVo.getLimitQuota())){
                           return true;
                       }else{
                           Logger.debug("该购物券不满足限额"+couponMap+",couponVo="+couponVo);
                       }
                   }
                }else if(couponMap.getCateType()==6){ //6.主题  theme_id
                    Theme theme= skuService.getThemeBy(couponMap.getCateTypeId());
                    if(null!=theme&&null!=theme.getThemeItem()&&null!=settleVo.getInvIdList()){
                        for(Long invId:settleVo.getInvIdList()){
                            if(theme.getThemeItem().contains(invId+"")){
                                idList.add(invId);
                                //该部分的商品满足限额
                                if(checkLimitQuotaByInvId(settleOrderDTO,idList,couponVo.getLimitQuota())){
                                    return true;
                                }else{
                                    Logger.debug("该购物券不满足限额"+couponMap+",couponVo="+couponVo);
                                }
                            }
                        }
                    }
                }else if(couponMap.getCateType()==7){ //7.商品一级类目  pcate_id
                    if(null!=settleVo.getItemIdList()){
                        //查询一级以及二级目录
                        Cates tempCates=new Cates();
                        tempCates.setPcateId(couponMap.getCateTypeId());
                        List<Cates> catesList=skuService.getCate(tempCates);
                        HashSet<Long> cateSet=new HashSet<>();
                        cateSet.add(couponMap.getCateTypeId());
                        if(null!=catesList&&catesList.size()>0){
                            for(Cates cates:catesList){
                                cateSet.add(cates.getCateId());
                            }
                        }

                        for(Long itemId:settleVo.getItemIdList()){
                            Item item=new Item();
                            item.setId(itemId);
                            List<Item> itemList=skuService.getItemBy(item);
                            if(null!=itemList&&itemList.size()>0){
                                if(cateSet.contains(itemList.get(0).getCateId())){
                                    idList.add(itemList.get(0).getId());//该分类对应的itemId
                                }
                            }
                        }
                        //该部分的商品满足限额
                        if(checkLimitQuotaByItemId(settleOrderDTO,idList,couponVo.getLimitQuota())){
                            return true;
                        }else{
                            Logger.debug("该购物券不满足限额"+couponMap+",couponVo="+couponVo);
                        }
                    }
                }

            }
            return false;
        }

        return true;
    }

    /**
     * 根据itemId判断商品是否满足限额
     * @param settleOrderDTO
     * @param itemIdList
     * @param limitQuota
     * @return
     */
    private boolean checkLimitQuotaByItemId(SettleOrderDTO settleOrderDTO,List<Long> itemIdList,BigDecimal limitQuota){
        if(null==itemIdList||itemIdList.size()<=0){
            return false;
        }
        BigDecimal total=new BigDecimal(0);
        for(SettleDTO settleDTO:settleOrderDTO.getSettleDTOs()){
            for(CartDto cartDto:settleDTO.getCartDtos()){
                if(null != cartDto.getItemId() && itemIdList.contains(cartDto.getItemId().longValue())){
                    total=total.add(cartDto.getFeeSingle());
                    //该部分的商品满足限额
                    if(limitQuota.compareTo(total)<=0){
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * 根据invId判断商品是否满足限额
     * @param settleOrderDTO
     * @param itemIdList
     * @param limitQuota
     * @return
     */
    private boolean checkLimitQuotaByInvId(SettleOrderDTO settleOrderDTO,List<Long> itemIdList,BigDecimal limitQuota){
        BigDecimal total=new BigDecimal(0);
        for(SettleDTO settleDTO:settleOrderDTO.getSettleDTOs()){
            for(CartDto cartDto:settleDTO.getCartDtos()){
                if(null != cartDto.getItemId() && itemIdList.contains(cartDto.getInvId().longValue())){
                    total=total.add(cartDto.getFeeSingle());
                    //该部分的商品满足限额
                    if(limitQuota.compareTo(total)<=0){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //计算优惠了多钱
    private BigDecimal calDiscount(Long userId, String couponId, SettleVo settleVo,SettleOrderDTO settleOrderDTO) throws Exception {
        BigDecimal discountFee = BigDecimal.ZERO;
        //优惠金额
        CouponVo couponVo = new CouponVo();
        couponVo.setUserId(userId);
        couponVo.setState("N");
        couponVo.setCoupId(couponId);
        Optional<List<CouponVo>> lists = Optional.ofNullable(cartService.getUserCoupon(couponVo));

        if (lists.isPresent()) {
            //优惠券,只列出当前满足条件优惠的优惠券,购买金额要大于限制金额且是未使用的,有效的
            if (lists.get().size() > 0 && checkCouponCondition(lists.get().get(0),settleVo,settleOrderDTO)) {
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
        if(null!=settleOrderDTO.getAdSource()&&!"".equals(settleOrderDTO.getAdSource())){
            Ad ad=new Ad();
            ad.setAdSource(settleOrderDTO.getAdSource()); //广告来源
            ad.setSubAdSource(settleOrderDTO.getSubAdSource());
            ad.setAdParam(settleOrderDTO.getAdParam());
            if(cartService.insertAd(ad)){
                order.setAdId(ad.getId());
            }
        }
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

        if (addressOptional.isPresent()&&null!=addressOptional.get()) {
            settleVo.setAddress(addressOptional.get());
        }else{
            Logger.error("创建订单地址异常settleOrderDTO="+settleOrderDTO+",userId="+userId);
            settleVo.setMessageCode(Message.ErrorCode.CREATE_ORDER_EXCEPTION.getIndex());
            return settleVo;
        }

        settleVo = calOrderFee(settleOrderDTO.getSettleDTOs(), userId, settleVo.getAddress());

        if (settleOrderDTO.getCouponId() != null && !settleOrderDTO.getCouponId().equals("")) {
            BigDecimal discount = calDiscount(userId, settleOrderDTO.getCouponId(), settleVo,settleOrderDTO);

            settleVo.setCouponId(settleOrderDTO.getCouponId());

            List<SettleFeeVo> settleFeeVoList = settleVo.getSingleCustoms();

            if(settleVo.getTotalFee().compareTo(discount.add(BigDecimal.ONE))<=0){   //至少1元
                discount = settleVo.getTotalFee().subtract(BigDecimal.ONE);
            }

            BigDecimal totalDiscountSingle = BigDecimal.ZERO;//用于计数除过最后一笔订单折扣后总计
            for (Integer i = 0; i < settleFeeVoList.size(); i++) {
                BigDecimal singleDiscount = settleFeeVoList.get(i).getSingleCustomsSumFee().divide(settleVo.getTotalFee(), BigDecimal.ROUND_DOWN).multiply(discount);
                if (i == settleFeeVoList.size() - 1) {
                    settleFeeVoList.get(i).setDiscountFeeSingleCustoms(discount.subtract(totalDiscountSingle));
                } else {
                    settleFeeVoList.get(i).setDiscountFeeSingleCustoms(singleDiscount);
                    totalDiscountSingle = singleDiscount.add(totalDiscountSingle);
                }
            }
            settleVo.setTotalPayFee(settleVo.getTotalFee().subtract(discount));
            settleVo.setDiscountFee(discount);
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


    public List<Map> getOrders(Order order) throws Exception {
        Optional<List<Order>> orderList = Optional.ofNullable(cartService.getOrderBy(order));

        //返回总数据
        List<Map> mapList = new ArrayList<>();

        try {
            if (orderList.isPresent() && orderList.get().size() > 0) {

                for (Order o : orderList.get()) {
                    Map<String, Object> map = new HashMap<>();
                    Map<String, Object> orderAddress = getOrderAddress(o);

                    if (orderAddress != null) map.putAll(orderAddress);

                    Map<String, Object> orderRefund = getOrderRefund(o);

                    if (orderRefund != null) map.putAll(orderRefund);

                    if (o.getOrderStatus().equals("I"))
                        o.setCountDown(CalCountDown.getTimeSubtract(o.getOrderCreateAt()));

                    //未支付订单
                    if (o.getOrderStatus().equals("I") || o.getOrderStatus().equals("C")) {
                        Map<String, Object> orderI = getIorders(o);

                        if (orderI != null) map.putAll(orderI);
                    } else {
                        Map<String, Object> orderSplit = getSplitOrders(o);

                        if (orderSplit != null) map.putAll(orderSplit);
                    }
                    mapList.add(map);
                }
                return mapList;
            } else return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.error(Throwables.getStackTraceAsString(ex));
            return null;
        }
    }


    private Map<String, Object> getIorders(Order order) throws Exception {

        //用于保存每个订单对应的明细list和地址信息
        Map<String, Object> map = new HashMap<>();

        OrderLine orderLine = new OrderLine();
        orderLine.setOrderId(order.getOrderId());

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
            skuDto.setInvImg(orderCtrl.getInvImg(orl.getSkuImg()));
            skuDto.setInvUrl(SysParCom.DEPLOY_URL + "/comm/detail/" + orl.getSkuType() + "/" + orl.getItemId() + "/" + orl.getSkuTypeId());

            skuDto.setItemColor(orl.getSkuColor());
            skuDto.setItemSize(orl.getSkuSize());
            skuDto.setSkuType(orl.getSkuType());
            skuDto.setSkuTypeId(orl.getSkuTypeId());
            skuDtoList.add(skuDto);
        }

        order.setOrderAmount(orderAmount);
        //组装每个订单对应的明细和地址
        map.put("order", order);
        map.put("sku", skuDtoList);

        return map;
    }

    private Map<String, Object> getSplitOrders(Order order) throws Exception {
        //用于保存每个订单对应的明细list和地址信息
        Map<String, Object> map = new HashMap<>();

        OrderSplit orderSplit = new OrderSplit();
        orderSplit.setOrderId(order.getOrderId());
        Optional<List<OrderSplit>> optionalOrderSplitList = Optional.ofNullable(cartService.selectOrderSplit(orderSplit));
        if (optionalOrderSplitList.isPresent() && optionalOrderSplitList.get().size() > 0) {
            for (OrderSplit osp : optionalOrderSplitList.get()) {
                Order orderS = new Order();
                orderS.setOrderId(osp.getOrderId());
                orderS.setOrderAmount(osp.getTotalAmount());
                orderS.setPayMethod(order.getPayMethod());
                orderS.setPayTotal(osp.getTotalPayFee());
                orderS.setTotalFee(osp.getTotalFee());
                orderS.setShipFee(osp.getShipFee());
                orderS.setPostalFee(osp.getPostalFee());
                orderS.setOrderCreateAt(order.getOrderCreateAt());
                orderS.setOrderStatus(order.getOrderStatus());

                OrderLine orderLine = new OrderLine();
                orderLine.setOrderId(order.getOrderId());
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

                    skuDto.setInvImg(orderCtrl.getInvImg(orl.getSkuImg()));
                    skuDto.setInvUrl(SysParCom.DEPLOY_URL + "/comm/detail/" + orl.getSkuType() + "/" + orl.getItemId() + "/" + orl.getSkuTypeId());

                    skuDto.setSkuType(orl.getSkuType());
                    skuDto.setSkuTypeId(orl.getSkuTypeId());
                    skuDto.setItemColor(orl.getSkuColor());
                    skuDto.setItemSize(orl.getSkuSize());

                    skuDtoList.add(skuDto);
                }

                if (order.getOrderStatus().equals("R")) {
                    String remark = remarkComplete(order);
                    if (remark != null) orderS.setRemark(remark);
                }

                map.put("order", orderS);
                map.put("sku", skuDtoList);
            }
            return map;
        } else return null;
    }


    private String remarkComplete(Order order) {
        OrderLine orderLine = new OrderLine();
        orderLine.setOrderId(order.getOrderId());
        try {
            List<OrderLine> orderLineList = cartService.selectOrderLine(orderLine);
            for (OrderLine ol : orderLineList) {
                Remark remark = new Remark();
                remark.setSkuType(ol.getSkuType());
                remark.setSkuTypeId(ol.getSkuTypeId());
                remark.setOrderId(order.getOrderId());
                remark.setUserId(order.getUserId());
                List<Remark> remarkList = cartService.selectRemark(remark);
                if (remarkList == null || remarkList.size() == 0) {
                    return "N";
                }
            }
            return "Y";

        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(Throwables.getStackTraceAsString(e));
            return null;
        }

    }

    private Map<String, Object> getOrderAddress(Order order) throws Exception {
        //用于保存每个订单对应的明细list和地址信息
        Map<String, Object> map = new HashMap<>();
        OrderAddress orderAddress = new OrderAddress();
        orderAddress.setOrderId(order.getOrderId());

        Optional<List<OrderAddress>> orderAddressOptional = Optional.ofNullable(cartService.selectOrderAddress(orderAddress));

        if (orderAddressOptional.isPresent() && orderAddressOptional.get().size() > 0) {
            //获取地址信息
            Address address = new Address();
            address.setDeliveryCity(orderAddressOptional.get().get(0).getDeliveryCity());
            address.setDeliveryDetail(orderAddressOptional.get().get(0).getDeliveryAddress());
            address.setIdCardNum(orderAddressOptional.get().get(0).getDeliveryCardNum());
            address.setName(orderAddressOptional.get().get(0).getDeliveryName());
            address.setTel(orderAddressOptional.get().get(0).getDeliveryTel());
            map.put("address", address);
            return map;
        } else return null;
    }

    private Map<String, Object> getOrderRefund(Order order) throws Exception {
        //用于保存每个订单对应的明细list和地址信息
        Map<String, Object> map = new HashMap<>();
        //查询是否存在退款信息
        Refund refund = new Refund();
        refund.setOrderId(order.getOrderId());
        Optional<List<Refund>> listRefundOptional = Optional.ofNullable(cartService.selectRefund(refund));
        if (listRefundOptional.isPresent() && listRefundOptional.get().size() > 0) {
            map.put("refund", listRefundOptional.get().get(0));
            return map;
        } else return null;
    }

}
