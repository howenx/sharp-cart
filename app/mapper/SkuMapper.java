package mapper;

import domain.*;

import java.util.List;

/**
 * 商品库查询,更新
 * Created by howen on 15/11/22.
 */
public interface SkuMapper {

    Sku getInv(Sku sku) throws Exception;

    Integer updateInv(Sku sku) throws Exception;

    Carriage getCarriage(Carriage carriage) throws Exception;

    SysParameter getSysParameter(SysParameter sysParameter) throws Exception;

    List<SkuVo> getAllSkus(SkuVo skuVo);

    List<VersionVo> getVersioning(VersionVo versionVo);

}
