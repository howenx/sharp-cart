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
