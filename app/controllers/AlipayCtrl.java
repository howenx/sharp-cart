package controllers;

import alipay.AlipayConfig;
import domain.Order;
import play.Logger;
import play.mvc.Controller;
import util.SysParCom;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static util.SysParCom.ONE_CENT_PAY;

/**
 * Created by sibyl.sun on 16/4/25.
 */
public class AlipayCtrl extends Controller {

    public Map<String,String>  getAlipayParams(Order order){
        Map<String,String> map=new HashMap<>();

        /**
         * service	接口名称	String	接口名称。	不可空	alipay.wap.create.direct.pay.by.user
         partner	合作者身份ID	String(16)	签约的支付宝账号对应的支付宝唯一用户号。以2088开头的16位纯数字组成。	不可空	2088111111111194
         _input_charset	参数编码字符集	String	商户网站使用的编码格式，仅支持utf-8。	不可空	utf-8
         sign_type	签名方式	String	DSA、RSA、MD5三个值可选，必须大写。	不可空	RSA
         sign	签名	String	请参见签名。	不可空	AqDeHSqY%2BwcYy0bTSAaVoyTGTYOOkXm6KEKlJ6LIaefDOdX%2F3adfalkdfjaldkfjaldlGrkVJNqcL5Lf2%2BX2SGH4jPl9E5PbsAgFq0LQGT4kvhTdcOGqaOcjYRt3TScJnoFn%2B3biV3P2%2FiBuRTdVuOgivkkjG%2BNDLKTDAgTxDNM%3D
         notify_url	服务器异步通知页面路径	String(190)	支付宝服务器主动通知商户网站里指定的页面http路径。	可空	http://hlcb.uz.com/notify-web/TradePayNotify
         return_url	页面跳转同步通知页面路径	String(200)	支付宝处理完请求后，当前页面自动跳转到商户网站里指定页面的http路径。	可空	alipays://platformapi/startApp?appId=10000011
         out_trade_no	商户网站唯一订单号	String(64)	支付宝合作商户网站唯一订单号。	不可空	70501111111S001111119
         subject	商品名称	String(256)	商品的标题/交易标题/订单标题/订单关键字等。该参数最长为128个汉字。	不可空	大乐透
         total_fee	交易金额	String	该笔订单的资金总额，单位为RMB-Yuan。取值范围为[0.01，100000000.00]，精确到小数点后两位。	不可空	9.00
         seller_id	卖家支付宝用户号	String(16)	卖家支付宝账号对应的支付宝唯一用户号。以2088开头的纯16位数字。	不可空	2088111111116894
         payment_type	支付类型	String(4)	支付类型。仅支持：1（商品购买）。	不可空	1
         show_url	商品展示网址	String(400)	收银台页面上，商品展示的超链接。	不可空	http://www.taobao.com/product/113714.html
         body	商品描述	String(1000)	对一笔交易的具体描述信息。如果是多种商品，请将商品描述字符串累加传给body。	可空	测试
         it_b_pay	超时时间	String	设置未付款交易的超时时间，一旦超时，该笔交易就会自动被关闭。
         取值范围：1m～15d。
         m-分钟，h-小时，d-天，1c-当天（1c-当天的情况下，无论交易何时创建，都在0点关闭）。
         该参数数值不接受小数点，如1.5h，可转换为90m。
         当用户输入支付密码、点击确认付款后（即创建支付宝交易后）开始计时。
         支持绝对超时时间，格式为yyyy-MM-dd HH:mm。	可空	3837m
         extern_token	手机支付宝token	String	接入极简版wap收银台时支持。当商户请求是来自手机支付宝，在手机支付宝登录后，有生成登录信息token时，使用该参数传入token将可以实现信任登录收银台，不需要再次登录。注意：登录后用户还是有入口可以切换账户，不能使用该参数锁定用户。	可空	appopenBb64d181d0146481ab6a762c00714cC27
         otherfee	航旅订单其它费用	Number	航旅订单中除去票面价之外的费用，单位为RMB-Yuan。取值范围为[0.01,100000000.00]，精确到小数点后两位。	可空	200
         airticket	航旅订单金额	String(1,64)	航旅订单金额描述，由四项或两项构成，各项之间由“|”分隔，每项包含金额与描述，金额与描述间用“^”分隔，票面价之外的价格之和必须与otherfee相等。	可空	800^票面价|50^机建费|120^燃油费|30^航意险
         或
         800^票面价|50^其他
         rn_check	是否发起实名校验	String(1)	T：发起实名校验；
         F：不发起实名校验。	可空	T
         buyer_cert_no	买家证件号码	String	买家证件号码（需要与支付宝实名认证时所填写的证件号码一致）。
         说明： 当scene=ZJCZTJF 的情况下，才会校验buyer_cert_no字段。	可空	329829197809290921
         buyer_real_name	买家真实姓名	String	买家真实姓名。
         说明： 当scene=ZJCZTJF 的情况下，才会校验buyer_real_name字段。	可空	张三
         scene	收单场景	String	收单场景。如需使用该字段，需向支付宝申请开通，否则传入无效。	可空	ZJCZTJF
         */


        Long orderId=order.getOrderId();
        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = orderId+"";

        //订单名称，必填
        String subject = new String("韩秘美-订单编号" + orderId);

        //付款金额，必填
        String total_fee = "";

        if (ONE_CENT_PAY) {
            total_fee="1";
        } else {
            total_fee=order.getPayTotal().multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_DOWN).toPlainString();
        }

        //收银台页面上，商品展示的超链接，必填
        String show_url = "https://style.hanmimei.com/";

        //商品描述，可空
        String body = new String("韩秘美-订单编号商品描述" + orderId); //URLEncoder.encode

        //把请求参数打包成数组
        Map<String, String> sParaTemp = new HashMap<String, String>();
        sParaTemp.put("service", SysParCom.ALIPAY_SERVICE);
        sParaTemp.put("partner", SysParCom.ALIPAY_PARTNER);
        sParaTemp.put("seller_id", SysParCom.ALIPAY_SELLER_ID);
        sParaTemp.put("_input_charset", AlipayConfig.input_charset);
        sParaTemp.put("payment_type", SysParCom.ALIPAY_PAYMENT_TYPE);
        sParaTemp.put("notify_url", SysParCom.ALIPAY_NOTITY_URL);
        sParaTemp.put("return_url", SysParCom.ALIPAY_RETURN_URL);
        sParaTemp.put("out_trade_no", out_trade_no);
        sParaTemp.put("subject", subject);
        sParaTemp.put("total_fee", total_fee);
        sParaTemp.put("show_url", show_url);
        sParaTemp.put("body", body);
        Logger.error("====支付宝参数=="+sParaTemp.toString());
        return map;
    }

}
