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

    List<WeiSheng> getWeiSheng(WeiSheng weiSheng);

    Theme getThemeBy(Long themeId);

    List<Item> getItemBy(Item item);
    /**
     * 获取商品分类
     * @param cates
     * @return
     */
    List<Cates> getCate(Cates cates);

}
