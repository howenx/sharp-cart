package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.*;
import modules.LevelFactory;
import org.apache.commons.beanutils.BeanUtils;
import play.Logger;
import play.Play;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import service.CartService;
import service.IdService;
import service.PromotionService;
import service.SkuService;
import util.CalCountDown;
import util.GenCouponCode;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.iq80.leveldb.DB;

import static play.libs.Json.newObject;

/**
 * 拼购
 * Created by howen on 16/2/17.
 */
public class PinCtrl extends Controller {

    private SkuService skuService;

    private CartService cartService;

    private IdService idService;

    private PromotionService promotionService;

    public static final String PIN_USER_PHOTO = Play.application().configuration().getString("oss.url");

    @Inject
    LevelFactory  levelFactory;

    @Inject
    public PinCtrl(SkuService skuService, CartService cartService, IdService idService, PromotionService promotionService) {
        this.cartService = cartService;
        this.idService = idService;
        this.skuService = skuService;
        this.promotionService = promotionService;

    }

    public Result testpin(){
        Map<String, String> params = new HashMap<>();
        params.put("pinActivity", JDPay.SHOPPING_URL+"/client/pin/activity/pay/"+223667);
        return ok(views.html.pin.render(params));
    }

    public Result pinActivity(Long activityId,Integer pay) {

        ObjectNode result = newObject();

        try {
            PinActivityDTO pinActivityDTO = new PinActivityDTO();

            PinActivity pinActivity = promotionService.selectPinActivityById(activityId);

            BeanUtils.copyProperties(pinActivityDTO,pinActivity);

            PinUser pinUser = new PinUser();
            pinUser.setPinActiveId(pinActivity.getPinActiveId());
            List<PinUser> pinUserList = promotionService.selectPinUser(pinUser);

            pinUserList=pinUserList.stream().map(p -> {
                p.setUserImg(PIN_USER_PHOTO+p.getUserImg());
                try {
                    ID userNm = idService.getID(p.getUserId());
                    if (userNm==null)
                    p.setUserNm(("HMM-"+ GenCouponCode.GetCode(4)).toLowerCase());
                    else p.setUserNm(idService.getID(p.getUserId()).getNickname());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return p;
            }).collect(Collectors.toList());

            pinActivityDTO.setPinUsers(pinUserList);

            if (pay==2) pinActivityDTO.setPay("new");
            else pinActivityDTO.setPay("normal");

            pinActivityDTO.setPinUrl(OrderCtrl.SHOPPING_URL+"/client/pin/activity/"+activityId);

            pinActivityDTO.setEndCountDown(CalCountDown.getEndTimeSubtract(pinActivityDTO.getEndAt()));

            PinSku pinSku = promotionService.getPinSkuById(pinActivityDTO.getPinId());


            Sku sku = new Sku();
            sku.setId(pinSku.getInvId());
            sku=skuService.getInv(sku);
            pinActivityDTO.setPinSkuUrl(OrderCtrl.DEPLOY_URL+"/comm/pin/detail/"+sku.getItemId()+"/"+sku.getId()+"/"+pinSku.getPinId());

            pinActivityDTO.setPinTitle(pinSku.getPinTitle());


            JsonNode js_invImg = Json.parse(pinSku.getPinImg());
            if (js_invImg.has("url")) {
                ((ObjectNode) js_invImg).put("url", Application.IMAGE_URL + js_invImg.get("url").asText());
            }
            pinActivityDTO.setPinImg(js_invImg.toString());

            result.putPOJO("activity", Json.toJson(pinActivityDTO));
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            return ok(result);
        }catch (Exception ex){
            Logger.error(ex.getMessage());
            ex.printStackTrace();
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ERROR.getIndex()), Message.ErrorCode.ERROR.getIndex())));
            return ok(result);
        }
    }

}
