package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.*;
import net.spy.memcached.MemcachedClient;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import service.*;
import util.GenCouponCode;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    private Service service  = new ServiceImpl();

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


//    @Security.Authenticated(UserAuth.class)
    public Result settle(){

        Optional<JsonNode> json = Optional.ofNullable(request().body().asJson());

        ObjectNode result = Json.newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");
            if (json.isPresent() && json.get().size() > 0) {
                List<CartDto> cartDtoList = mapper.readValue(json.get().toString(), mapper.getTypeFactory().constructCollectionType(List.class, CartDto.class));

                //运费
                BigDecimal shipFee = new BigDecimal(0);
                //行邮税
                BigDecimal portalFee = new BigDecimal(0);




                for (CartDto cartDto : cartDtoList) {

                    Cart cart = new Cart();
                    cart.setSkuId(cartDto.getSkuId());
                    cart.setUserId(userId);
                    cart.setAmount(cartDto.getAmount());
                    cart.setCartId(cartDto.getCartId());

                    Sku sku = new Sku();
                    sku.setId(cartDto.getSkuId());
                    sku = skuService.getInv(sku);


                    //先确定商品状态是正常,然后确定商品结算数量是否超出库存量
                    if (!sku.getState().equals("Y")) {
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SKU_INVALID.getIndex()), Message.ErrorCode.SKU_INVALID.getIndex())));
                        return ok(result);
                    } else  if(cartDto.getAmount()>sku.getRestAmount()){
                        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex()), Message.ErrorCode.SKU_AMOUNT_SHORTAGE.getIndex())));
                        return ok(result);
                    }else{

                        //累加邮费
                        shipFee=shipFee.add(sku.getShipFee());
                        //计算行邮税,行邮税加和
                        portalFee =portalFee.add(new BigDecimal(sku.getPostalTaxRate()).multiply(sku.getItemPrice()).multiply(new BigDecimal(cart.getAmount())));

                    }
                }
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(result);
            }else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
                return ok(result);
            }
        }catch (Exception ex){
            return null;
        }
    }

    //发放优惠券
    private void publicCoupons() throws Exception{
        CouponVo couponVo = new CouponVo();
        couponVo.setUserId(((Integer)1000012).longValue());
        couponVo.setDenomination((BigDecimal.valueOf(20)));
        Date timeDate = new Date();
        Timestamp dateTime = new Timestamp(timeDate.getTime());
        couponVo.setStartAt(dateTime);
        couponVo.setEndAt(new Timestamp(timeDate.getTime()+10*24*60*60*1000));
        String coupId = GenCouponCode.GetCode(GenCouponCode.CouponClassCode.ACCESSORIES.getIndex(),8);
        Logger.error(coupId);
        couponVo.setCoupId(coupId);
        couponVo.setCateId(((Integer)GenCouponCode.CouponClassCode.ACCESSORIES.getIndex()).longValue());
        couponVo.setState("N");
        cartService.insertCoupon(couponVo);
    }
}
