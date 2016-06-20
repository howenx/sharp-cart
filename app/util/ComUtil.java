package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.Sku;
import domain.SkuVo;
import play.libs.Json;

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

    /**
     * 转换图片,拼接URL前缀
     *
     * @param invImg invImg
     * @return invImg
     */
    public String getInvImg(String invImg) {
        //SKU图片
        if (invImg.contains("url")) {
            JsonNode jsonNode_InvImg = Json.parse(invImg);
            if (jsonNode_InvImg.has("url")) {
                ((ObjectNode) jsonNode_InvImg).put("url", SysParCom.IMAGE_URL + jsonNode_InvImg.get("url").asText());
                return Json.stringify(jsonNode_InvImg);
            } else return SysParCom.IMAGE_URL + invImg;
        } else return SysParCom.IMAGE_URL + invImg;
    }

}
