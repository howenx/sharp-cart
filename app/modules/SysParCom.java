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


    public static Long PIN_MILLISECONDS;

    public static String JD_PUSH_URL;
    public static String JD_QUERY_URL;
    public static Long JD_QUERY_DELAY;
    public static Long ORDER_OVER_TIME;

    public static String JD_PAY_URL;

    public static String JD_REFUND_URL;

    public static String M_INDEX;

    public static String M_ORDERS;

    public static String PIN_SUCCESS_MSG;
    public static String PIN_ADD_MSG;
    public static String M_PIN;
    public static String ERP_PUSH;
    public static String EXPRESS_URL;
    public static Boolean ONE_CENT_PAY;

    public static String MNS_ENDPOINT;
    public static String MNS_KEY;
    public static String MNS_SECRET;

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


        PIN_MILLISECONDS = configuration.getLong("pin.over.time");


        JD_PUSH_URL = configuration.getString("jd_push_url");

        JD_QUERY_URL = configuration.getString("jd_query_url");

        JD_QUERY_DELAY = configuration.getLong("jd.query.customs.status");

        ORDER_OVER_TIME = configuration.getLong("order.over.time");

        JD_PAY_URL = configuration.getString("jd_pay_url");
        JD_REFUND_URL = configuration.getString("jd_refund_url");

        M_INDEX = configuration.getString("m.jump.home");
        M_ORDERS = configuration.getString("m.jump.orders");
        JD_PAY_URL = configuration.getString("jd_pay_url");
        JD_REFUND_URL = configuration.getString("jd_refund_url");

        PIN_SUCCESS_MSG = configuration.getString("pin_success_msg");
        PIN_ADD_MSG  = configuration.getString("pin_add_msg");
        M_PIN = configuration.getString("m.jump.pin");

        ERP_PUSH = configuration.getString("erp.order.push");

        EXPRESS_URL=configuration.getString("express.url");

        ONE_CENT_PAY = configuration.getBoolean("one.cent.pay");

        MNS_ENDPOINT= configuration.getString("mns.accountendpoint");
        MNS_KEY= configuration.getString("mns.accesskeyid");
        MNS_SECRET= configuration.getString("mns.accesskeysecret");
    }

}
