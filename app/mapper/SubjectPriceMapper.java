package mapper;

import domain.SubjectPrice;

import java.util.List;

/**
 *
 * Created by tiffany on 16/2/4.
 */
public interface SubjectPriceMapper {
    /**
     * 从主题添加多元化价格   Added by Tiffany Zhu 2016.02.04
     * @param subjectPrice
     */
    void sbjPriceAdd(SubjectPrice subjectPrice);

    /**
     * 更新多元化价格      Added by Tiffany Zhu 2016.02.04
     * @param subjectPrice
     */
    void sbjPriceUpd(SubjectPrice subjectPrice);

    /**
     * 删除多元化价格      Added by Tiffany Zhu 2016.02.04
     * @param id
     */
    void sbjPriceDelById(Long id);

    /**
     * 通过主题ID删除多元化价格      Added by Tiffany Zhu 2016.02.05
     * @param id
     */
    void sbjPriceDelByThemeId(Long id);

    /**
     * 获取多元化价格      Added by Tiffany Zhu 2016.02.05
     * @param subjectPrice
     * @return
     */
    SubjectPrice getSbjPrice(SubjectPrice subjectPrice);

    /**
     * 通过主题ID获取多元化价格    Added by Tiffany Zhu 2016.02.05
     * @param ThemeId
     * @return
     */
    List<SubjectPrice> getSbjPriceByThemeId(Long ThemeId);

    /**
     *通过多元化价格ID获取多元化价格信息    Added by Tiffany Zhu 2016.02.06
     * @param id
     * @return
     */
    SubjectPrice getSbjPriceById(Long id);
}
