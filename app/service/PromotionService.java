package service;

import domain.PinActivity;
import domain.PinSku;
import domain.PinTieredPrice;
import domain.PinUser;

import java.util.List;

/**
 * For homepage theme list display function.
 * Created by howen on 15/10/26.
 */
public interface PromotionService {

    PinSku getPinSkuById(Long pinId);

    List<PinTieredPrice> getTieredPriceByPinId(Long pinId);

    PinTieredPrice getTieredPriceById(PinTieredPrice pinTieredPrice);


    Boolean insertPinActivity(PinActivity pinActivity);

    Boolean updatePinActivity(PinActivity pinActivity);

    PinActivity selectPinActivityById(Long pinActivityId);

    Boolean insertPinUser(PinUser pinUser);

    List<PinUser> selectPinUser(PinUser pinUser);

    List<PinActivity> selectPinActivity(PinActivity pinActivity);
}
