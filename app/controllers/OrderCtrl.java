package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.Cart;
import domain.CartDto;
import domain.Message;
import domain.Sku;
import filters.UserAuth;
import net.spy.memcached.MemcachedClient;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import service.CartService;
import service.IdService;
import service.SkuService;

import javax.inject.Inject;
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
    public Result settle(){

        Optional<JsonNode> json = Optional.ofNullable(request().body().asJson());

        ObjectNode result = Json.newObject();
        try {
            Long userId = (Long) ctx().args.get("userId");
            if (json.isPresent() && json.get().size() > 0) {
                List<CartDto> cartDtoList = mapper.readValue(json.get().toString(), mapper.getTypeFactory().constructCollectionType(List.class, CartDto.class));
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
                        //计算用户的优惠券费用,邮费,关税
                        return null;
                    }
                }
                return null;
            }else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_PARAMETER.getIndex()), Message.ErrorCode.BAD_PARAMETER.getIndex())));
                return ok(result);
            }
        }catch (Exception ex){
            return null;
        }
    }
}
