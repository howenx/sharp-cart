package service;

import domain.PinActivity;
import domain.PinSku;
import domain.PinTieredPrice;
import domain.PinUser;
import mapper.PinSkuMapper;

import javax.inject.Inject;
import java.util.List;

/**
 *
 * Created by howen on 16/1/25.
 */
public class PromotionServiceImpl implements PromotionService {

    @Inject
    private PinSkuMapper pinSkuMapper;

    @Override
    public PinSku getPinSkuById(Long pinId) {
        return pinSkuMapper.getPinSkuById(pinId);
    }

    @Override
    public List<PinTieredPrice> getTieredPriceByPinId(Long pinId) {
        return pinSkuMapper.getTieredPriceByPinId(pinId);
    }

    @Override
    public PinTieredPrice getTieredPriceById(PinTieredPrice pinTieredPrice) {
        return pinSkuMapper.getTieredPriceById(pinTieredPrice);
    }

    @Override
    public Boolean insertPinActivity(PinActivity pinActivity) {
        return pinSkuMapper.insertPinActivity(pinActivity)>0;
    }

    @Override
    public Boolean updatePinActivity(PinActivity pinActivity) {
        return pinSkuMapper.updatePinActivity(pinActivity)>0;
    }

    @Override
    public PinActivity selectPinActivityById(Long pinActivityId) {
        return pinSkuMapper.selectPinActivityById(pinActivityId);
    }

    @Override
    public Boolean insertPinUser(PinUser pinUser) {
        return pinSkuMapper.insertPinUser(pinUser)>0;
    }

    @Override
    public List<PinUser> selectPinUser(PinUser pinUser) {
        return pinSkuMapper.selectPinUser(pinUser);
    }

    @Override
    public List<PinActivity> selectPinActivity(PinActivity pinActivity) {
        return pinSkuMapper.selectPinActivity(pinActivity);
    }
}
