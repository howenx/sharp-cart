package util;

import com.squareup.okhttp.MediaType;
import domain.SysParameter;
import play.Configuration;
import play.mvc.WebSocket;
import service.SkuService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

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

    //图片处理服务器URL
    public static String IMG_PROCESS_URL;

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

    public static String WEIXIN_APP_ID;
    public static String WEIXIN_MCH_ID;
    public static String WEIXIN_KEY;
    public static String WEIXIN_PAY_UNIFIEDORDER;
    public static String WEIXIN_PAY_ORDERQUERY;
    public static String WEIXIN_PAY_REFUND;
    public static String WEIXIN_SSL_PATH;
    public static String WEIXIN_SSL_PATH_APP;
    public static String WEIXIN_APP_ID_APP;
    public static String WEIXIN_MCH_ID_APP;
    public static String WEIXIN_KEY_APP;


    public static String ALIPAY_PAYMENT_TYPE;
    public static String ALIPAY_PARTNER;
    public static String ALIPAY_SELLER_ID;
    public static String ALIPAY_KEY;
    public static String ALIPAY_OPENAPI_GATEWAY;
    public static String ALIPAY_MAPI_GATEWAY;
    public static String ALIPAY_RSA_PRIVATE_KEY;
    public static String ALIPAY_RSA_PUBLIC_KEY;


    public static String EXPRESS_POST_URL;
    public static String EXPRESS_KEY;
    public static String EXPRESS_CUSTOMER;

    public static String REDIS_URL;
    public static String REDIS_PASSWORD;
    public static Integer REDIS_PORT;
    public static String REDIS_CHANNEL;

    //微信扫码支付
    public static Map<String,WebSocket.Out<String>> WEIXIN_SOCKET;

    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    //极光推送参数
    public static Long PUSH_TIME_TO_LIVE;
    public static String PUSH_APP_KEY;
    public static String PUSH_MASTER_SECRET;


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
        PIN_ADD_MSG = configuration.getString("pin_add_msg");
        M_PIN = configuration.getString("m.jump.pin");

        ERP_PUSH = configuration.getString("erp.order.push");

        EXPRESS_URL = configuration.getString("express.url");

        ONE_CENT_PAY = configuration.getBoolean("one.cent.pay");

        MNS_ENDPOINT = configuration.getString("mns.accountendpoint");
        MNS_KEY = configuration.getString("mns.accesskeyid");
        MNS_SECRET = configuration.getString("mns.accesskeysecret");

        WEIXIN_APP_ID = configuration.getString("weixin.app.id");
        WEIXIN_MCH_ID = configuration.getString("weixin.mch.id");
        WEIXIN_KEY = configuration.getString("weixin.key");
        WEIXIN_PAY_UNIFIEDORDER = configuration.getString("weixin.pay.unifiedorder");

        IMG_PROCESS_URL = configuration.getString("imgprocess.server.url");
        WEIXIN_PAY_ORDERQUERY = configuration.getString("weixin.pay.orderquery");
        WEIXIN_PAY_REFUND = configuration.getString("weixin.pay.refund");
        WEIXIN_SSL_PATH = configuration.getString("weixin.ssl.path");

        WEIXIN_APP_ID_APP = configuration.getString("weixin.app.idapp");
        WEIXIN_MCH_ID_APP = configuration.getString("weixin.mch.idapp");
        WEIXIN_KEY_APP = configuration.getString("weixin.keyapp");
        WEIXIN_SSL_PATH_APP=configuration.getString("weixin.ssl.pathapp");


        //支付宝
        ALIPAY_PAYMENT_TYPE = configuration.getString("alipay.payment.type");
        ALIPAY_PARTNER = configuration.getString("alipay.partner");
        ALIPAY_SELLER_ID = configuration.getString("alipay.seller.id");
        ALIPAY_KEY = configuration.getString("alipay.key");
        ALIPAY_OPENAPI_GATEWAY = configuration.getString("aalipay.openapi.gateway");
        ALIPAY_MAPI_GATEWAY = configuration.getString("alipay.mapi.gateway");
        ALIPAY_RSA_PRIVATE_KEY=configuration.getString("alipay.rsa.private.key");
        ALIPAY_RSA_PUBLIC_KEY=configuration.getString("alipay.rsa.public.key");



        EXPRESS_POST_URL = configuration.getString("express.post.url");
        EXPRESS_KEY = configuration.getString("express.key");
        EXPRESS_CUSTOMER = configuration.getString("express.customer");

        REDIS_URL = configuration.getString("redis.host");
        REDIS_PASSWORD = configuration.getString("redis.password");
        REDIS_PORT = configuration.getInt("redis.port");
        REDIS_CHANNEL = configuration.getString("redis.channel");

        PUSH_TIME_TO_LIVE=configuration.getLong("push.time.to.live");
        PUSH_APP_KEY=configuration.getString("push.app.key");
        PUSH_MASTER_SECRET=configuration.getString("push.master.secret");

        WEIXIN_SOCKET=new HashMap<>();

    }
}
