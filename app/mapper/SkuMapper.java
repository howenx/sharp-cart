package mapper;

import domain.Carriage;
import domain.Sku;
import domain.SysParameter;

/**
 * 商品库查询,更新
 * Created by howen on 15/11/22.
 */
public interface SkuMapper {

    Sku getInv(Sku sku) throws Exception;

    Integer updateInv(Sku sku) throws Exception;

    Carriage getCarriage(Carriage carriage) throws Exception;

    SysParameter getSysParameter(SysParameter sysParameter) throws Exception;

}
