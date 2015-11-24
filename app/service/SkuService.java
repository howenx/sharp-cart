package service;

import domain.Sku;

/**
 * sku service
 * Created by howen on 15/11/24.
 */
public interface SkuService {

    Sku getInv(Sku sku) throws Exception;

    Integer updateInv(Sku sku) throws Exception;
}
