package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import common.AlipayTradeType;
import domain.*;
import filters.UserAuth;
import middle.JDPayMid;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.codec.digest.DigestUtils;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.WebSocket;
import service.CartService;
import service.IdService;
import service.PromotionService;
import util.SysParCom;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.*;

import static play.libs.Json.newObject;
import static util.SysParCom.*;

/**
 * 支付宝支付
 * Created by sibyl.sun on 16/4/25.
 */
public class AlipayCtrl extends Controller {
    public static final String  SIGN_ALGORITHMS = "SHA1WithRSA";
    private CartService cartService;
    private PromotionService promotionService;
    private IdService idService;

    @Inject
    private MemcachedClient cache;

    @Inject
    private JDPayMid jdPayMid;

    @Inject
    private JDPay jdPay;
    @Inject
    private WeiXinCtrl weiXinCtrl;

    @Inject
    public AlipayCtrl(CartService cartService, PromotionService promotionService,IdService idService) {
        this.cartService = cartService;
        this.promotionService = promotionService;
        this.idService=idService;
    }

    /**
     * 支付宝请求参数
     * @param order
     * @param alipayTradeType
     * @return
     */
    public String getAlipayParamsUrl(Order order, AlipayTradeType alipayTradeType){
        Map<String, String> map=getAlipayParams(order,alipayTradeType);

        if(alipayTradeType==AlipayTradeType.APP){
            Logger.info("支付宝参数===>"+createLinkStringApp(map));
            return createLinkStringApp(map);
        }
        return createLinkString(map);
    }

    private Map<String, String> getAlipayParams(Order order, AlipayTradeType alipayTradeType){
        Long orderId=order.getOrderId();
        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = orderId+"";

        //付款金额，必填 该笔订单的资金总额，单位为RMB-Yuan。取值范围为[0.01，100000000.00]，精确到小数点后两位。
        String total_fee ;
        if (SysParCom.ONE_CENT_PAY) {
            total_fee="0.01";
        } else {
            total_fee=order.getPayTotal().toPlainString();
        }

        String detail="韩秘美-"+orderId;

        //把请求参数打包成数组
        TreeMap<String, String> sParaTemp = new TreeMap<>();
        if(alipayTradeType==AlipayTradeType.APP){
            sParaTemp.put("service", "mobile.securitypay.pay");
        }else if(alipayTradeType==AlipayTradeType.DIRECT){  //立即支付
            sParaTemp.put("service", "create_direct_pay_by_user");
        }
        sParaTemp.put("partner", SysParCom.ALIPAY_PARTNER);
        sParaTemp.put("seller_id", SysParCom.ALIPAY_SELLER_ID);
        sParaTemp.put("_input_charset", "utf-8");
        sParaTemp.put("payment_type", SysParCom.ALIPAY_PAYMENT_TYPE);
        sParaTemp.put("notify_url", SysParCom.SHOPPING_URL + "/client/alipay/pay/back");
        if(alipayTradeType==AlipayTradeType.DIRECT) {  //立即支付
            sParaTemp.put("return_url", SysParCom.SHOPPING_URL + "/client/alipay/pay/front");
            sParaTemp.put("show_url", M_ORDERS);
        }else if(alipayTradeType==AlipayTradeType.APP){
            sParaTemp.put("it_b_pay", "30m"); //超时时间
        }
        sParaTemp.put("out_trade_no", out_trade_no);
        sParaTemp.put("subject",detail);  //订单名称，必填
        sParaTemp.put("total_fee", total_fee);
        sParaTemp.put("body",detail);
        Map<String, String> map=buildRequestPara(sParaTemp,"RSA",alipayTradeType);
        String mySign= null;
        try {
            mySign = URLEncoder.encode(map.get("sign"),"utf-8");
            map.put("sign",mySign);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return map;
    }
    /**
     * 生成签名结果
     * @param sPara 要签名的数组
     * @return 签名结果字符串
     */
    public static String buildRequestMysign(Map<String, String> sPara,String key,String signType) {
        String prestr =createLinkString(sPara); //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        String mysign = sign(prestr, key, "utf-8",signType);
        return mysign;
    }
    /**
     * APP生成签名结果
     * @param sPara 要签名的数组
     * @return 签名结果字符串
     */
    public static String buildRequestMysignApp(Map<String, String> sPara,String key,String signType) {
        String prestr=createLinkStringApp(sPara);
        String mysign = sign(prestr, key, "utf-8",signType);
        Logger.info("======支付宝APP签名串\n"+prestr+"===>秘钥:\n"+key+",mysign=\n"+mysign);
        return mysign;
    }

    /**
     * 生成要请求给支付宝的参数数组
     * @param sParaTemp 请求前的参数数组
     * @return 要请求的参数数组
     */
    private static Map<String, String> buildRequestPara(Map<String, String> sParaTemp,String signType,AlipayTradeType alipayTradeType) {
        //除去数组中的空值和签名参数
        Map<String, String> sPara = paraFilter(sParaTemp);
        String key=encodeKey(signType);
        //生成签名结果
        String mysign = "";
        if(alipayTradeType==AlipayTradeType.APP){
            mysign=buildRequestMysignApp(sPara,key,signType);
        }else{
            mysign=buildRequestMysign(sPara,key,signType);
        }


        //签名结果与签名方式加入请求提交参数组中
        sPara.put("sign", mysign);
        sPara.put("sign_type", signType);
        return sPara;
    }
    /**
     * 除去数组中的空值和签名参数
     * @param sArray 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    public static Map<String, String> paraFilter(Map<String, String> sArray) {

        Map<String, String> result = new HashMap<String, String>();

        if (sArray == null || sArray.size() <= 0) {
            return result;
        }

        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")
                    || key.equalsIgnoreCase("sign_type")) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }

    /**
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String prestr = "";

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }

        return prestr;
    }
    /**
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkStringApp(Map<String, String> params) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String prestr = "";

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                prestr = prestr + key + "="+'"' + value+'"';
            } else {
                prestr = prestr + key + "="+'"' + value+'"' + "&";
            }
        }

        return prestr;
    }
    /**
     * 签名字符串
     * @param text 需要签名的字符串
     * @param key 密钥
     * @param input_charset 编码格式
     * @return 签名结果
     */
    public static String sign(String text, String key, String input_charset,String signType) {
        if(signType.equals("MD5")){
            text = text + key;
            return DigestUtils.md5Hex(getContentBytes(text, input_charset));
        }else if(signType.equals("RSA")){
            try
            {
                PKCS8EncodedKeySpec priPKCS8 	= new PKCS8EncodedKeySpec( Base64.getDecoder().decode(key) );
                KeyFactory keyf 				= KeyFactory.getInstance("RSA");
                PrivateKey priKey 				= keyf.generatePrivate(priPKCS8);

                java.security.Signature signature = java.security.Signature
                        .getInstance(SIGN_ALGORITHMS);

                signature.initSign(priKey);
                signature.update( text.getBytes(input_charset) );

                byte[] signed = signature.sign();

                return Base64.getEncoder().encodeToString(signed);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return null;

    }


    /**
     * 生成要请求给支付宝的参数数组
     * @param sParaTemp 请求前的参数数组
     * @return 要请求的参数数组
     */
    private boolean verifySign(Map<String, String> sParaTemp,String sign,String signType) {
        //除去数组中的空值和签名参数
        Map<String, String> sPara = paraFilter(sParaTemp);
        String key=decodeKey(signType);
        String prestr = createLinkString(sPara); //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        Logger.info("=verifySign=="+key);
        return verify(prestr,sign,key,"utf-8",signType);
    }

    /**
     * 验签名检查
     * @param content 待签名数据
     * @param sign 签名值
     * @param ali_public_key 支付宝公钥
     * @param input_charset 编码格式
     * @return 布尔值
     */
    private static boolean verify(String content, String sign, String ali_public_key, String input_charset,String signType)
    {
        if(signType.equals("MD5")){
            content = content + ali_public_key;
            return sign.equals(DigestUtils.md5Hex(getContentBytes(content, input_charset)));
        }else if(signType.equals("RSA")) {
            try {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                byte[] encodedKey = Base64.getDecoder().decode(ali_public_key);
                PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));


                java.security.Signature signature = java.security.Signature
                        .getInstance(SIGN_ALGORITHMS);

                signature.initVerify(pubKey);
                signature.update(content.getBytes(input_charset));

                boolean bverify = signature.verify(Base64.getDecoder().decode(sign));
                return bverify;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }
    /**
     * @param content
     * @param charset
     * @return
     * @throws UnsupportedEncodingException
     */
    private static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }

    /**
     * 加密需要的key
     * @param signType
     * @return
     */
    private static String encodeKey(String signType){
        String key=ALIPAY_KEY;
        if(signType.equals("RSA")){
            key=ALIPAY_RSA_PRIVATE_KEY;
        }
        return key;
    }

    /**
     * 解密需要的key
     * @param signType
     * @return
     */
    private static String decodeKey(String signType){
        String key=ALIPAY_KEY;
        if(signType.equals("RSA")){
            key=ALIPAY_RSA_PUBLIC_KEY;
        }
        return key;
    }


    /**
     *支付宝异步通知
     * @return
     */
    public Result payBackNotify(){
        Map<String, String[]> body_map = request().body().asFormUrlEncoded();
        Map<String, String> params = new HashMap<>();
//        params.put("discount","0.00");
//        params.put("payment_type","1");
//        params.put("subject","韩秘美订单50102328");
//        params.put("trade_no","2016052621001004360220362391");
//        params.put("buyer_email","13716311853");
//        params.put("gmt_create","2016-05-26 18:30:18");
//        params.put("notify_type","trade_status_sync");
//        params.put("quantity","1");
//        params.put("out_trade_no","50102328");
//        params.put("seller_id","2088811744291968");
//        params.put("notify_time","2016-05-26 18:30:25");
//        params.put("body","韩秘美订单50102328");
//        params.put("trade_status","TRADE_SUCCESS");
//        params.put("is_total_fee_adjust","N");
//        params.put("total_fee","0.01");
//        params.put("gmt_payment","2016-05-26 18:30:24");
//        params.put("seller_email","management@bjdacorp.com");
//        params.put("price","0.01");
//        params.put("buyer_id","2088802427704365");
//        params.put("notify_id","d75c103419a9844ddb6a0c5dcc38ce4is2");
//        params.put("use_coupon","N");
//        params.put("sign_type","RSA");
//        params.put("sign","GHQI7RLBvbI9gz2S6WxjQUc6J9emdx84DIstuRZGxY3t/kwLcQT4EEg/i7Bx3vu+F6TzDTw0asclNYNemOZCfLDMCWm+NBzFFWvPWK8RHN8E8w4Ne95GJ6piJjTRwNXPZrq+iNyOXLRwmHue/gw/VjMdulBwmNkKFCw5j3ldW2k=");

        body_map.forEach((k, v) -> params.put(k, v[0]));
        Logger.info("支付宝支付回调返回params="+params);

        if(verifySign(params,params.get("sign"),params.get("sign_type"))) { //验证签名
            String verifyAli=verifyFromAlipay(params.get("notify_id"));
            if(!"true".equals(verifyAli)){ //验证是否是支付宝发来的通知
                Logger.error("################支付宝支付异步通知 验证是否是支付宝发来的通知对不上################,notify_id=" + params.get("notify_id"));
                return ok("fail");

            }
            if(null!=params.get("out_trade_no")&& null!=params.get("total_fee")&&null!=params.get("trade_status")
                    &&("TRADE_SUCCESS".equals(params.get("trade_status"))||"TRADE_FINISHED".equals(params.get("trade_status")))){ //有订单号并且交易成功
                Long orderId = Long.valueOf(params.get("out_trade_no"));
                Order order = new Order();
                order.setOrderId(orderId);
                List<Order> orders = null;
                try {
                    orders = cartService.getOrder(order);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(null==orders||orders.size()<=0){
                    Logger.error("################支付宝支付异步通知 订单不存在################," + order.getOrderId());
                    return ok("fail");
                }
                //校验金额
                if(orders.get(0).getTotalFee().setScale(2,BigDecimal.ROUND_HALF_DOWN).compareTo(new BigDecimal(params.get("total_fee")).setScale(2,BigDecimal.ROUND_HALF_DOWN))!=0){
                    Logger.error("################支付宝支付异步通知 支付金额对于不上################,orderId=" + order.getOrderId()+",totalFee="+orders.get(0).getTotalFee()+",total_fee="+params.get("total_fee"));
                //    return ok("fail");
                }
                order.setPayMethod("ALIPAY");
                order.setPayMethodSub("ALIPAY");
                order.setErrorStr("");
                if(null!=params.get("trade_no")){
                    order.setPgTradeNo(params.get("trade_no"));
                }

                if (jdPayMid.asynPay(order).equals("success")) {
                    try {
                        orders = cartService.getOrder(order);
                        if (orders.size() > 0) {
                            order = orders.get(0);
                            if (order.getOrderType() != null && order.getOrderType() == 2) { //1:正常购买订单，2：拼购订单
                                if (jdPay.dealPinActivity(params, order) == null) {
                                    Logger.error("################支付宝支付异步通知 拼购订单返回处理结果为空################," + order.getOrderId());
                                    return ok("order deal fail");
                                } else {
                                    WebSocket.Out<String> out=WEIXIN_SOCKET.get(orderId+"");
                                    WEIXIN_SOCKET.remove(orderId+"");
                                    if(null!=out){ //扫码
                                        out.write("SUCCESS");
                                    }
                                    Logger.error("################支付宝支付异步通知 拼购订单返回成功################," + order.getOrderId());
                                    return ok("success");
                                }
                            } else {
                                WebSocket.Out<String> out=WEIXIN_SOCKET.get(orderId+"");
                                WEIXIN_SOCKET.remove(orderId+"");
                                if(null!=out){ //扫码
                                    out.write("SUCCESS");
                                }
                                Logger.error("################支付宝支付异步通知 普通订单返回成功################," + order.getOrderId());
                                return ok("success");
                            }
                        } else {
                            Logger.error("################支付宝支付异步通知 订单未找到################," + order.getOrderId());
                            return ok( "order not found");
                        }
                    } catch (Exception e) {
                        Logger.error("################支付宝支付异步通知 出现异常################," + order.getOrderId() +"\nerror:"+Throwables.getStackTraceAsString(e));
                        e.printStackTrace();
                        return ok("fail");
                    }
                } else {
                    Logger.error("################支付宝支付异步通知 异步方法调用返回失败################," + params.get("out_trade_no"));
                    return ok("asynPayAlipay error");
                }
            }
        }else{
            Logger.error("################支付宝支付异步通知 签名不一致################");
        }
        return ok("fail");

    }

    /**
     * 支付宝前端通知
     * @return
     */
    public Result payFrontNotify(){
        Map<String, String[]> body_map = request().queryString();
        Map<String, String> params = new HashMap<>();
        body_map.forEach((k, v) -> params.put(k, v[0]));
        Logger.info("支付宝前端通知\nparams="+params);
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("m_index", M_INDEX);
        returnMap.put("m_orders", M_ORDERS);
        if(!verifySign(params,params.get("sign"),params.get("sign_type"))) { //验证签名
            Logger.error("支付宝前端通知支付失败,签名对应不一致");
            return ok(views.html.jdpayfailed.render(returnMap));
        }
        try {
            if(null!=params.get("total_fee")){
                returnMap.put("all_fee",params.get("total_fee"));
            }
            if(null==params.get("is_success")||!"T".equals(params.get("is_success"))||null==params.get("out_trade_no")
                    ||(null!=params.get("trade_status")&&!(params.get("trade_status").equals("TRADE_FINISHED")||params.get("trade_status").equals("TRADE_SUCCESS")))){
                Logger.error("支付宝前端通知支付失败");
                return ok(views.html.jdpayfailed.render(returnMap));
            }
            Long orderId=Long.valueOf(params.get("out_trade_no"));
            Order order = new Order();
            order.setOrderId(orderId);
            Optional<List<Order>> listOptional = Optional.ofNullable(cartService.getOrder(order));
            if (!listOptional.isPresent() || listOptional.get().size() <= 0) {
                return ok(views.html.jdpayfailed.render(returnMap));
            }
            order = listOptional.get().get(0);

            return weiXinCtrl.weixinPaySucessRedirect(order,returnMap);

        }catch (Exception e){
            Logger.error(e.getMessage());
        }

        return ok(views.html.jdpayfailed.render(returnMap));

    }

//    /**
//     * 获取退款参数
//     * @param orderId
//     * @return
//     */
//    public String getRefundParamsAPI(Long orderId) {
//        try {
//            Order order = new Order();
//            order.setOrderId(orderId);
//            Optional<List<Order>> listOptional = Optional.ofNullable(cartService.getOrder(order));
//            if (listOptional.isPresent() && listOptional.get().size() == 1) {
//                order = listOptional.get().get(0);
//                TreeMap<String, String> sParaTemp = new TreeMap<>();
//                sParaTemp.put("app_id", SysParCom.ALIPAY_PARTNER);
//                sParaTemp.put("method","alipay.trade.refund");
//                sParaTemp.put("charset", "utf-8");
//                sParaTemp.put("timestamp",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//                sParaTemp.put("version","1.0");
//                sParaTemp.put("out_trade_no",order.getOrderId()+"");
//                sParaTemp.put("refund_amount",order.getPayTotal().toPlainString());
//                Map<String, String> map=buildRequestPara(sParaTemp,"RSA");
//                Logger.info("支付宝退款参数"+Json.toJson(map)+",ALIPAY_OPENAPI_GATEWAY="+ALIPAY_OPENAPI_GATEWAY);
//                //创建一个OkHttpClient对象
//                OkHttpClient okHttpClient = new OkHttpClient();
//                //创建一个RequestBody(参数1：数据类型 参数2传递的json串)
//                RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, Json.toJson(map).toString());
//                //创建一个请求对象
//                Request request = new Request.Builder().url(ALIPAY_OPENAPI_GATEWAY).post(requestBody).build();
//                //发送请求获取响应
//                try {
//                    Response response=okHttpClient.newCall(request).execute();
//                    //判断请求是否成功
//                    if(response.isSuccessful()){
//                        //打印服务端返回结果
//                        Logger.info("==退款返回结果==="+response.body().string());
//                        return response.body().string();
//
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            } else return null;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            Logger.error(Throwables.getStackTraceAsString(ex));
//            return null;
//        }
//        return null;
//    }

    /**
     * 支付宝退款异步通知
     * @return
     */
    public Result payRefundNotify(){
        Map<String, String[]> body_map = request().body().asFormUrlEncoded();
        Map<String, String> params = new HashMap<>();
//        params.put("sign", "RCBlGZP4fWUlzkmvrQmRfKvonkI2033VZ3+djOW0ws/KW9bRBjbNU0of/1p1EpWary6yUdVkR3wYzPZzF9jTDVCIbrCt8ySbFpKzT29f3r5AsciWrrfGAQZXxS5xQgAThYHh6GxjqcxnbZ4i7IsOfgdMDDk2uGJ5mGhG/J9zRrg=");
//        params.put("result_details","2016052521001004360218241676^0.01^SUCCESS");
//        params.put("notify_time","2016-05-25 18:23:09");
//        params.put("sign_type","RSA");
//        params.put("notify_type","batch_refund_notify");
//        params.put("notify_id","e8101c81cafd461d6aad631f90a36e0je9");
//        params.put("batch_no","2016052518185850102295");
//        params.put("success_num","1");
        body_map.forEach((k, v) -> params.put(k, v[0]));
        Logger.info("支付宝退款异步通知request().body()="+request().body()+",params="+params);
        if(verifySign(params,params.get("sign"),params.get("sign_type"))) { //验证签名
            String result_details=params.get("result_details");
            if(null!=result_details){
                String[] array=result_details.split(";");  //批量
                for(String refundInfo:array){
                    try {
                        String[] arr=refundInfo.split("^");
                        if(arr.length>=3){
                            Order order = new Order();
                            order.setPgTradeNo(arr[0]);
                            Optional<List<Order>> listOptional = null;
                            listOptional = Optional.ofNullable(cartService.getOrder(order));
                            if (listOptional.isPresent() && listOptional.get().size() == 1) {
                                    order=listOptional.get().get(0);
                                    Refund re = new Refund();
                                    re.setOrderId(order.getOrderId());
                                    re.setPgCode(arr[2]);
                                    re.setPgMessage("");
                                    re.setPgTradeNo(params.get("batch_no"));
                                    if (arr[2].startsWith("SUCCESS")) {
                                        Order order1 = new Order();
                                        order1.setOrderId(re.getOrderId());
                                        order1.setOrderStatus("T");
                                        cartService.updateOrder(order1);
                                        re.setState("Y");
                                        Logger.error(arr[0] + "支付宝退款成功,返回业务结果码:" + arr[2]+",Refund="+re);
                                    } else {
                                        Logger.error(arr[0] + "支付宝退款失败,返回业务结果码:" + arr[2]+",Refund="+re);
                                        re.setState("N");
                                    }
                                    cartService.updateRefund(re);
                            }else{
                                Logger.error(arr[0] + "支付宝退款失败,订单不存在" + arr[2]);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return ok("success");
            }

        }else{
            Logger.error("支付宝退款异步通知签名不一致");
        }
        return ok("fail");

    }

    /**
     * 获取退款参数
     * @param orderId
     * @return
     */
    public Map<String, String> getRefundParams(Long orderId) {
        Order order = new Order();
        order.setOrderId(orderId);
        Optional<List<Order>> listOptional = null;
        try {
            listOptional = Optional.ofNullable(cartService.getOrder(order));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (listOptional.isPresent() && listOptional.get().size() == 1) {
            order = listOptional.get().get(0);
            //把请求参数打包成数组
            Map<String, String> sParaTemp = new HashMap<String, String>();
            sParaTemp.put("service", "refund_fastpay_by_platform_pwd");
            sParaTemp.put("partner", SysParCom.ALIPAY_PARTNER);
            sParaTemp.put("_input_charset", "utf-8");
            sParaTemp.put("notify_url", SysParCom.SHOPPING_URL + "/client/alipay/pay/refund");
            sParaTemp.put("seller_user_id", SysParCom.ALIPAY_PARTNER);
            sParaTemp.put("refund_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            sParaTemp.put("batch_no", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + orderId);
            sParaTemp.put("batch_num", "1");
            //付款金额，必填 该笔订单的资金总额，单位为RMB-Yuan。取值范围为[0.01，100000000.00]，精确到小数点后两位。
            String total_fee ;
            if (SysParCom.ONE_CENT_PAY) {
                total_fee="0.01";
            } else {
                total_fee=order.getPayTotal().toPlainString();
            }

            String detail_data = order.getPgTradeNo() + "^" + total_fee + "^" + "HMM";
            sParaTemp.put("detail_data", detail_data);
            Map<String, String> map = buildRequestPara(sParaTemp, "RSA",AlipayTradeType.DIRECT);
            Logger.info("支付宝退款参数" + Json.toJson(map));
            return map;
        }
        return null;
    }

    /**
     * 获取退款参数
     * @param orderId
     * @return
     */
    public String alipayRefund(Long orderId) {
        try {

            Map<String, String> map=getRefundParams(orderId);
            Logger.info("支付宝退款参数\n"+createLinkString(map));

            //创建一个OkHttpClient对象
            OkHttpClient okHttpClient = new OkHttpClient();
            //创建一个RequestBody(参数1：数据类型 参数2传递的json串)
            RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, Json.toJson(map).toString());
            //创建一个请求对象
            Request request = new Request.Builder().url("https://mapi.alipay.com/gateway.do").post(requestBody).build();
            //发送请求获取响应
            try {
                Response response=okHttpClient.newCall(request).execute();
                //判断请求是否成功
                if(response.isSuccessful()){
                    //打印服务端返回结果
                    Logger.info("==退款返回结果==="+response.body().string());
                    return response.body().string();
                }
                else return null;

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.error(Throwables.getStackTraceAsString(ex));
            return null;
        }
        return null;
    }


    /**
     * 验证是否是支付宝发来的通知
     * @param notify_id
     * @return
     */
    private String verifyFromAlipay(String notify_id){
        /**
         * https://mapi.alipay.com/gateway.do?service=notify_verify&partner=2088002396712354&notify_id=RqPnCoPT3K9%252Fvwbh3I%252BFioE227%252BPfNMl8jwyZqMIiXQWxhOCmQ5MQO%252FWd93rvCB%252BaiGg
         */
        String url=ALIPAY_MAPI_GATEWAY+"?service=notify_verify&partner="+ALIPAY_PARTNER+"&notify_id="+notify_id;
        //创建一个OkHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //创建一个请求对象
        Request request = new Request.Builder().url(url).get().build();
        //发送请求获取响应
        try {
            Response response=okHttpClient.newCall(request).execute();
            //判断请求是否成功
            if(response.isSuccessful()){
                //打印服务端返回结果
                String notify_return=response.body().string();
                Logger.info("验证是否是支付宝发来的通知"+notify_return);
                return notify_return;
            }
            else return null;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * app支付跳转空白页
     * @return
     */
    public Result payApp(){
        Form<WeiXinApp> redirectCashForm = Form.form(WeiXinApp.class).bindFromRequest();
        Map<String, String> params_failed = new HashMap<>();
        params_failed.put("m_index", M_INDEX);
        if (redirectCashForm.hasErrors()) {
            return ok(views.html.jdpayfailed.render(params_failed));
        } else {
            Map<String, String[]> body_map = request().body().asFormUrlEncoded();
            Map<String, String> params = new HashMap<>();
            body_map.forEach((k, v) -> params.put(k, v[0]));
            Order order = new Order();
            order.setOrderId(Long.valueOf(params.get("orderId")));
            Optional<List<Order>> listOptional = null;
            try {
                listOptional = Optional.ofNullable(cartService.getOrder(order));
                if (listOptional.isPresent() && listOptional.get().size() > 0) {
                    order = listOptional.get().get(0);
                    String paramsUrl=getAlipayParamsUrl(order,AlipayTradeType.APP);
                    params.put("paramsUrl",paramsUrl);
                    params.put("paramsUrlIos",paramsUrl.replaceAll("%","%%"));
                    return ok(views.html.alipayapp.render(params)); //跳转页面
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ok(views.html.jdpayfailed.render(params_failed));
    }


    /**
     * 支付完成后订单状态查询
     *
     * @param orderId
     * @return
     */
    @Security.Authenticated(UserAuth.class)
    public Result payOrderquery(Long orderId) {
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("m_index", M_INDEX);
        returnMap.put("m_orders", M_ORDERS);
        try {
            Order order = new Order();
            order.setOrderId(orderId);
            Optional<List<Order>> listOptional = Optional.ofNullable(cartService.getOrder(order));
            if (!listOptional.isPresent() || listOptional.get().size() <= 0) {
                return ok(views.html.jdpayfailed.render(returnMap));
            }
            order = listOptional.get().get(0);
            if (order.getOrderStatus().equals("S") || order.getOrderStatus().equals("PS")) { //订单状态是支付成功
                return weiXinCtrl.weixinPaySucessRedirect(order, returnMap);
            }
            if (!order.getOrderStatus().equals("I")) { //订单状态不是初始状态
                return ok(views.html.jdpayfailed.render(returnMap));
            }
        } catch (Exception e) {
            Logger.error(Throwables.getStackTraceAsString(e));
        }
        return ok(views.html.jdpayfailed.render(returnMap));
    }

    /**
     * 跳转查询下单
     * @return
     */
    public Result redirectPayOrderquery() {
        Form<RedirectWeiXin> redirectCashForm = Form.form(RedirectWeiXin.class).bindFromRequest();
        if (redirectCashForm.hasErrors()) {
            ObjectNode objectNode = newObject();
            objectNode.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE.getIndex()), Message.ErrorCode.FAILURE.getIndex())));
            return ok(objectNode);
        } else {
            RedirectWeiXin redirectCash = redirectCashForm.get();
            flash().put("id-token", redirectCash.getToken());
            return redirect("/client/alipay/pay/orderquery/" + redirectCash.getOrderId());
        }
    }


}
