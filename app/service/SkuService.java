package service;

import domain.Carriage;
import domain.Sku;
import domain.SysParameter;

/**
 * sku service
 * Created by howen on 15/11/24.
 */
public interface SkuService {

    Sku getInv(Sku sku) throws Exception;

    Integer updateInv(Sku sku) throws Exception;

    Carriage getCarriage(Carriage carriage) throws Exception;

    SysParameter getSysParameter(SysParameter sysParameter);
}
