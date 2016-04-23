package service;

import domain.*;

import java.util.List;

/**
 * sku service
 * Created by howen on 15/11/24.
 */
public interface SkuService {

    Sku getInv(Sku sku) throws Exception;

    Boolean updateInv(Sku sku) throws Exception;

    Carriage getCarriage(Carriage carriage) throws Exception;

    SysParameter getSysParameter(SysParameter sysParameter);

    List<VaryPrice> getVaryPriceBy(VaryPrice varyPrice);

    SubjectPrice getSbjPriceById(Long id);

    List<SkuVo> getAllSkus(SkuVo skuVo);

    Integer updateVaryPrice(VaryPrice varyPrice);

    List<VersionVo> getVersioning(VersionVo versionVo);

}
