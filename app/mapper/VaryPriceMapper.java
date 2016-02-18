package mapper;

import domain.VaryPrice;

import java.util.List;

/**
 * Created by Sunny Wu on 16/1/19.
 * kakao china.
 */
public interface VaryPriceMapper {

    Long insertVaryPrice(VaryPrice varyPrice);

    void updateVaryPrice(VaryPrice varyPrice);

    List<VaryPrice> getVaryPriceBy(VaryPrice varyPrice);

    List<VaryPrice> getAllVaryPrices();

}
