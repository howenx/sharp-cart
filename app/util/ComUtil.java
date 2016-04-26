package util;

import domain.Sku;
import domain.SkuVo;

import java.math.BigDecimal;

/**
 * Created by sibyl.sun on 16/4/6.
 */
public class ComUtil {

    //是否超出限购次数
    public boolean isOutOfRestrictAmount(Integer curAmount, SkuVo sku){
//        //直邮不限个数
//        if("K".equals(sku.getInvArea())){
//            return false;
//        }
        if(sku.getRestrictAmount() != 0 && sku.getSkuTypeRestrictAmount() < curAmount){
            return true;
        }
        return false;

    }

    //是否超出限购次数
    public boolean isOutOfRestrictAmount(Integer curAmount, Sku sku){
//        //直邮不限个数
//        if("K".equals(sku.getInvArea())){
//            return false;
//        }
        if(sku.getRestrictAmount() != 0 && sku.getRestrictAmount() < curAmount){
            return true;
        }
        return false;

    }

    //是否超出总额限制
    public boolean isOutOfPostalLimit(String invArea,BigDecimal curValue){
        //直邮不限总额
        if("K".equals(invArea)){
            return false;
        }
        if(curValue.compareTo(new BigDecimal(SysParCom.POSTAL_LIMIT)) > 0){
            return true;
        }
        return false;

    }

}
