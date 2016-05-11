package controllers;

import domain.Order;
import play.Logger;
import play.mvc.Controller;
import util.SysParCom;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付
 * Created by sibyl.sun on 16/4/25.
 */
public class AlipayCtrl extends Controller {

    public Map<String,String>  getAlipayParams(Order order){
        Map<String,String> map=new HashMap<>();

        Long orderId=order.getOrderId();
        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = orderId+"";

        //订单名称，必填
        String subject = new String("韩秘美-订单编号" + orderId);

        //付款金额，必填
        String total_fee = "";

        if (SysParCom.ONE_CENT_PAY) {
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
        sParaTemp.put("_input_charset", "utf-8");
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
