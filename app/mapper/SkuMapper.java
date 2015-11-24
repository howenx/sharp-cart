package mapper;

import domain.Sku;

/**
 * 商品库查询,更新
 * Created by howen on 15/11/22.
 */
public interface SkuMapper {

    Sku getInv(Sku sku) throws Exception;

    Integer updateInv(Sku sku) throws Exception;

}
