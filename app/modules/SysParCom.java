package modules;

import domain.SysParameter;
import play.Play;
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
    public static  String POSTAL_STANDARD;

    //海关规定购买单笔订单金额限制
    public static  String POSTAL_LIMIT;

    //达到多少免除邮费
    public static String FREE_SHIP;

    //图片服务器url
    public static final String IMAGE_URL = play.Play.application().configuration().getString("image.server.url");

    //发布服务器url
    public static final String DEPLOY_URL = play.Play.application().configuration().getString("deploy.server.url");

    //shopping服务器url
    public static final String SHOPPING_URL = play.Play.application().configuration().getString("shopping.server.url");

    //id服务器url
    public static final String ID_URL = play.Play.application().configuration().getString("id.server.url");

    public static final String IMG_PROCESS_URL = play.Play.application().configuration().getString("imgprocess.server.url");

    public static final String PIN_USER_PHOTO = Play.application().configuration().getString("oss.url");

    public static final String PROMOTION_URL = play.Play.application().configuration().getString("promotion.server.url");

    public static final String JD_SECRET = Play.application().configuration().getString("jd_secret");

    public static final String JD_SELLER = Play.application().configuration().getString("jd_seller");

    public static final Long COUNTDOWN_MILLISECONDS = Long.valueOf(Play.application().configuration().getString("order.countdown.milliseconds"));

    public static final Long PIN_MILLISECONDS = Long.valueOf(Play.application().configuration().getString("pin.activity.milliseconds"));

    @Inject
    public SysParCom(SkuService skuService) {

        POSTAL_STANDARD = skuService.getSysParameter(new SysParameter(null, null, null, "POSTAL_STANDARD")).getParameterVal();

        POSTAL_LIMIT = skuService.getSysParameter(new SysParameter(null, null, null, "POSTAL_LIMIT")).getParameterVal();

        FREE_SHIP = skuService.getSysParameter(new SysParameter(null, null, null, "FREE_SHIP")).getParameterVal();
    }

}
