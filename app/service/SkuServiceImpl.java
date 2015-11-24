package service;

import domain.Sku;
import mapper.SkuMapper;

import javax.inject.Inject;

/**
 * impl
 * Created by howen on 15/11/24.
 */
public class SkuServiceImpl implements SkuService{
    @Inject
    private SkuMapper skuMapper;

    @Override
    public Sku getInv(Sku sku) throws Exception{
        return skuMapper.getInv(sku);
    }

    @Override
    public Integer updateInv(Sku sku) throws Exception{
        return skuMapper.updateInv(sku);
    }
}
