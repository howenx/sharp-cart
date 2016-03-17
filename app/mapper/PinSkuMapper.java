package mapper;

import domain.PinActivity;
import domain.PinSku;
import domain.PinTieredPrice;
import domain.PinUser;

import java.util.List;

/**
 *
 * Created by tiffany on 16/1/20.
 */
public interface PinSkuMapper {

    /**
     * 通过ID获取拼购    Added by Tiffany Zhu 2016.01.22
     *
     * @param pinId pinId
     * @return PinSku
     */
    PinSku getPinSkuById(Long pinId);

    List<PinTieredPrice> getTieredPriceByPinId(Long pinId);

    PinTieredPrice getTieredPriceById(PinTieredPrice pinTieredPrice);

    Integer insertPinActivity(PinActivity pinActivity);

    Integer updatePinActivity(PinActivity pinActivity);

    PinActivity selectPinActivityById(Long pinActivityId);

    Integer insertPinUser(PinUser pinUser);

    List<PinUser> selectPinUser(PinUser pinUser);

    List<PinActivity> selectPinActivity(PinActivity pinActivity);
}
