package controllers;

import com.google.inject.Singleton;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import util.Crypto;

import java.util.*;


/**
 * Created by handy on 15/12/16.
 * kakao china
 */
@Singleton
public class JDPay extends Controller {

    /**
     * GET 或者POST 请求
     * 需要的参数:
     * out_trade_no
     * trade_amount   整数, 按照分计算的,1表示1分, 100表示1元,取消
     * trade_subject 订单标题,就是商品的标题
     * sub_order_info 子订单内容
     * @return
     */
    public Result pay () {
        Map<String, String[]> req_map = request().queryString();
        Map<String,String[]> body_map = request().body().asFormUrlEncoded();
        Map<String, String> params = new HashMap<>();
        if(req_map != null) {
            req_map.forEach((k,v)->params.put(k,v[0]));
        }
        if(body_map != null)
         body_map.forEach((k, v) -> params.put(k, v[0]));

        //查询订单的金额, 标题, 子订单

        String seller = Play.application().configuration().getString("jd_seller");
        String secret = Play.application().configuration().getString("jd_secret");
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss");
        String req_date = f.print(new DateTime());
        String sign_type = "MD5";
        Long order_id = System.currentTimeMillis();
        int amount = 1;
        String trade_currency = "CNY";
        String settle_currency = "USD";
        String buyer_info = "{\"customer_type\": \"OUT_CUSTOMER_VALUE\", \"customer_code\":\"Louch2010\"}";
        String sub_order_info = "[{\"sub_order_no\": \"32132\", \"sub_order_amount\":\"1\", \"sub_order_name\":\"测试商品\"}]";
        String jeep_info = "";
        String return_params = "abc";
        String notify_url = Play.application().configuration().getString("jd_notify_url");
        String return_url = Play.application().configuration().getString("jd_return_url");
        String token = "";


        params.put("buyer_info","{\"customer_type\":\"OUT_CUSTOMER_VALUE\",\"customer_code\":\"Louch2010\"}");
        params.put("customer_no",seller);
        params.put("jeep_info","");
        params.put("notify_url",notify_url);
        params.put("out_trade_no","" + order_id);
        params.put("request_datetime",req_date);
        params.put("return_params",return_params);
        params.put("return_url",return_url);
        params.put("settle_currency",settle_currency);
        params.put("sub_order_info","[{\"sub_order_no\": \"32132\", \"sub_order_amount\":\"1\", \"sub_order_name\":\"测试商品\"}]");
        params.put("token","");
        params.put("trade_amount","" + amount);
        params.put("trade_currency",trade_currency);
        params.put("trade_subject","测试订单");
        params.put("sign_type",sign_type);

        params.put("sign_data",create_sign(params,secret));


        StringBuilder sbHtml = new StringBuilder();

        sbHtml.append("<html>\n" +
                "<head>\n" +
                "\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                "</head><form id=\"jdsubmit\" name=\"jdsubmit\" action=\"https://cbe.wangyin.com/cashier/mobile/payment\""
                + "_input_charset=utf-8" + "\" method=\"" + "POST" + "\">");

          params.forEach((k,v) -> sbHtml.append("<input type=\"hidden\" name=\"" + k + "\" value='" + v + "'/>"));




        //submit按钮控件请不要含有name属性
        sbHtml.append("<input type=\"submit\" value=\"" + "submit" + "\" style=\"display:none;\"></form>");
        sbHtml.append("<script>document.forms['jdsubmit'].submit();</script></html>");

        return ok(sbHtml.toString()).as("text/html");
    }

    public Result back() {
        Map<String,String[]> body_map = request().body().asFormUrlEncoded();
        Map<String, String> params = new HashMap<>();
        body_map.forEach((k, v) -> params.put(k, v[0]));
        String sign = params.get("sign_data");
        String secret = Play.application().configuration().getString("jd_secret");
        String _sign = create_sign(params,secret);
        if(!sign.equalsIgnoreCase(_sign)) {
            //error
            return ok("通知失败，签名失败！");
        }

        //update order status .

        return ok("SUCCESS");
    }

    public Result front() {
        Map<String,String[]> body_map = request().body().asFormUrlEncoded();
        Map<String, String> params = new HashMap<>();
        body_map.forEach((k, v) -> params.put(k, v[0]));
        String sign = params.get("sign_data");
        String secret = Play.application().configuration().getString("jd_secret");
        String _sign = create_sign(params,secret);
        if(!sign.equalsIgnoreCase(_sign)) {

            return ok("error page");

        }


        return ok("success page");
    }

    public static String create_sign(Map<String, String> params, String secret) {
        StringBuffer sb = new StringBuffer();
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            String value = params.get(key);
            if (key.equals("KEY") || key.equals("URL") || key.equals("sign_data") || key.equals("sign_type")) {
                continue;
            }
            if(sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s", key, value));
        }

        String pre_sign = sb.toString();
        Logger.debug(pre_sign);

        return Crypto.md5(pre_sign + secret);

    }
}
