package modules;

import domain.SysParameter;
import play.Configuration;
import service.SkuService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 查询参数表中的参数项
 * Created by hao on 16/2/28.
 */
@Singleton
public class SysParCom {

    //行邮税收税标准
    public static String POSTAL_STANDARD;

    //海关规定购买单笔订单金额限制
    public static String POSTAL_LIMIT;

    //达到多少免除邮费
    public static String FREE_SHIP;

    //图片服务器url
    public static String IMAGE_URL;

    //发布服务器url
    public static String DEPLOY_URL;

    //shopping服务器url
    public static String SHOPPING_URL;

    //id服务器url
    public static String ID_URL;

    public static String PROMOTION_URL;

    public static String JD_SECRET;

    public static String JD_SELLER;

    public static Long COUNTDOWN_MILLISECONDS;

    public static Long PIN_MILLISECONDS;

    @Inject
    public SysParCom(SkuService skuService, Configuration configuration) {

        POSTAL_STANDARD = skuService.getSysParameter(new SysParameter(null, null, null, "POSTAL_STANDARD")).getParameterVal();

        POSTAL_LIMIT = skuService.getSysParameter(new SysParameter(null, null, null, "POSTAL_LIMIT")).getParameterVal();

        FREE_SHIP = skuService.getSysParameter(new SysParameter(null, null, null, "FREE_SHIP")).getParameterVal();


        IMAGE_URL = configuration.getString("image.server.url");


        DEPLOY_URL = configuration.getString("deploy.server.url");


        SHOPPING_URL = configuration.getString("shopping.server.url");


        ID_URL = configuration.getString("id.server.url");

        PROMOTION_URL = configuration.getString("promotion.server.url");

        JD_SECRET = configuration.getString("jd_secret");

        JD_SELLER = configuration.getString("jd_seller");

        COUNTDOWN_MILLISECONDS = Long.valueOf(configuration.getString("order.countdown.milliseconds"));

        PIN_MILLISECONDS = Long.valueOf(configuration.getString("pin.activity.milliseconds"));

    }

}
