package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.Message;
import domain.Order;
import domain.PinUser;
import middle.JDPayMid;
import util.SysParCom;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import net.spy.memcached.MemcachedClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import service.CartService;
import service.PromotionService;
import util.Crypto;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;

import static util.SysParCom.M_INDEX;
import static util.SysParCom.M_ORDERS;
import static util.SysParCom.ONE_CENT_PAY;
import static util.SysParCom.*;
import static play.libs.Json.newObject;
import static play.libs.Json.toJson;

/**
 * 微信支付
 * Created by sibyl.sun on 16/4/21.
 */
public class WeiXinCtrl extends Controller {

    private CartService cartService;
    private PromotionService promotionService;


    @Inject
    private MemcachedClient cache;

    @Inject
    private JDPayMid jdPayMid;

    @Inject
    private JDPay jdPay;

    @Inject
    public WeiXinCtrl(CartService cartService,PromotionService promotionService) {
        this.cartService = cartService;
        this.promotionService=promotionService;
    }

    public String getPayUnifiedorderParams(Order order,String tradeType) throws Exception {

        TreeMap<String,String> paramMap=new TreeMap<>();

        Long orderId=order.getOrderId();

        paramMap.put("appid",SysParCom.WEIXIN_APP_ID); //应用ID
        paramMap.put("mch_id",SysParCom.WEIXIN_MCH_ID);
        paramMap.put("nonce_str",UUID.randomUUID().toString().replaceAll("-", ""));
        paramMap.put("body","韩秘美-订单编号" + orderId); //URLEncoder.encode
        paramMap.put("notify_url",SysParCom.SHOPPING_URL+"/client/weixin/pay/back");
        paramMap.put("out_trade_no",orderId.toString());
        paramMap.put("spbill_create_ip","127.0.0.1");
        paramMap.put("trade_type",tradeType);
        paramMap.put("attach",orderId.toString());//附加数据，在查询API和支付通知中原样返回，该字段主要用于商户携带订单的自定义数据

        if (ONE_CENT_PAY) {
            paramMap.put("total_fee","1");
        } else {
            paramMap.put("total_fee",order.getPayTotal().multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_DOWN).toPlainString());
        }

        String sign=getWeiXinSign(paramMap);
        paramMap.put("sign",sign);

        return mapToXml(paramMap);
    }
    public String mapToXml(Map<String,String> paramMap){
        //生成xml
        StringBuffer sb=new StringBuffer();
        sb.append("<xml>");
        for(Map.Entry<String,String> entry:paramMap.entrySet()){
            sb.append("<"+entry.getKey()+">"+entry.getValue()+"</"+entry.getKey()+">");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    /**
     * 获取微信签名
     * @param paramMap
     * @return
     */
    private String getWeiXinSign(TreeMap<String,String> paramMap){
        StringBuffer stringA=new StringBuffer();
        for(Map.Entry<String,String> entry:paramMap.entrySet()){
            if (entry.getValue() == null || entry.getValue().equals("") || entry.getKey().equalsIgnoreCase("sign")) {
                continue;
            }
            if(stringA.length()>0){
                stringA.append("&"+entry.getKey()+"="+entry.getValue());
            }else{
                stringA.append(entry.getKey()+"="+entry.getValue());
            }
        }
        String key=SysParCom.WEIXIN_KEY; //秘钥
        String stringSignTemp=stringA+"&key="+key;
        Logger.info("=stringSignTemp==="+stringSignTemp);
        String sign= Crypto.md5(stringSignTemp).toUpperCase();//生成签名
        Logger.info("=sign==="+sign);
        return sign;

    }

    /**
     * 获取deeplink
     * @param prepayId
     * @param tradeType
     * @return
     * @throws UnsupportedEncodingException
     */

    public String getWeiXinDeeplink(String prepayId,String tradeType) throws UnsupportedEncodingException {
        TreeMap<String,String> paramMap=new TreeMap<>();
        paramMap.put("appid",SysParCom.WEIXIN_APP_ID);
        paramMap.put("noncestr",UUID.randomUUID().toString().replaceAll("-", ""));
        paramMap.put("package",tradeType);
        paramMap.put("prepayid",prepayId);
        paramMap.put("timestamp",System.currentTimeMillis()+"");

        String sign=getWeiXinSign(paramMap);
        paramMap.put("sign",sign);

        StringBuffer stringA=new StringBuffer();
        for(Map.Entry<String,String> entry:paramMap.entrySet()){
            if(stringA.length()>0){
                stringA.append("&"+entry.getKey()+"="+ URLEncoder.encode(entry.getValue(),"UTF-8"));
            }else{
                stringA.append(entry.getKey()+"="+URLEncoder.encode(entry.getValue(),"UTF-8"));
            }
        }

        String string2=URLEncoder.encode(stringA.toString(),"UTF-8");
        return "weixin：//wap/pay?"+string2;



    }

    /**
     * 微信统一下单
     * 商户系统先调用该接口在微信支付服务后台生成预支付交易单，返回正确的预支付交易回话标识后再在APP里面调起支付。
     * @param orderId
     * @return
     */
    public Result payUnifiedorder(String tradeType,Long orderId){
        ObjectNode objectNode = newObject();
       // Long userId = (Long) ctx().args.get("userId");
       // userId=1000242L;
        try{
            Order order = new Order();
            order.setOrderId(orderId);
            Optional<List<Order>> listOptional = Optional.ofNullable(cartService.getOrder(order));
            if (!listOptional.isPresent() || listOptional.get().size() <= 0) {
                objectNode.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE.getIndex()), Message.ErrorCode.FAILURE.getIndex())));
                return ok(objectNode);
            }

            order=listOptional.get().get(0);

            //获取响应内容
            try{

                String sendStr = getPayUnifiedorderParams(order,tradeType);

                String result=httpConnect(SysParCom.WEIXIN_PAY_UNIFIEDORDER,sendStr);
                Logger.info("微信统一下单请求内容\n"+sendStr+"返回内容\n"+result);


                Map<String,String> resultMap=xmlToMap(result);
                if(null==resultMap||resultMap.size()<=0){
                    objectNode.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE.getIndex()), Message.ErrorCode.FAILURE.getIndex())));
                    return ok(objectNode);
                }

                if(!"SUCCESS".equals(resultMap.get("return_code"))){ //返回状态码  SUCCESS/FAIL 此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
                    objectNode.putPOJO("message", Json.toJson(new domain.Message(resultMap.get("return_msg"), domain.Message.ErrorCode.FAILURE.getIndex())));
                    return ok(objectNode);
                }
                if(!"SUCCESS".equals(resultMap.get("result_code"))){ //业务结果
                    objectNode.putPOJO("message", Json.toJson(new domain.Message(resultMap.get("err_code_des"), domain.Message.ErrorCode.FAILURE.getIndex())));
                    return ok(objectNode);
                }


                if("NATIVE".equals(tradeType)){ //扫码支付
                    String code_url=resultMap.get("code_url");
                    if(null==code_url||"".equals(code_url)){
                        objectNode.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE.getIndex()), Message.ErrorCode.FAILURE.getIndex())));
                        return ok(objectNode);
                    }
                    //生成二维码图片
                    ByteArrayOutputStream qrOut = createQrGen(code_url);

                    String qrCodeUrl=UUID.randomUUID().toString().replaceAll("-", "");
                    cache.add(qrCodeUrl,2*60*60,qrOut.toByteArray());
                    objectNode.put("qr_code_url",qrCodeUrl); //二维码地址
                    order.setQrCodeAt(new Timestamp(System.currentTimeMillis()));
                    order.setQrCodeUrl(qrCodeUrl);
                    cartService.updateOrder(order);


                }else{

                    String prepay_id=resultMap.get("prepay_id");
                    objectNode.put("trade_type",resultMap.get("trade_type")); //交易类型
                    objectNode.put("prepay_id",prepay_id); //预支付交易会话标识
                    objectNode.put("deeplink",getWeiXinDeeplink(prepay_id,tradeType)); //deeplink
                }
                objectNode.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(objectNode);

            }catch(IOException e){
                e.printStackTrace();
            }catch(Exception e){
                e.printStackTrace();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        objectNode.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE.getIndex()), Message.ErrorCode.FAILURE.getIndex())));
        return ok(objectNode);

    }


    /**
     * XML转MAP
     * @param xmlContent
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private Map<String,String> xmlToMap(String xmlContent) throws IOException, SAXException, ParserConfigurationException {
        Map<String,String> resultMap=new HashMap<>();
        if("".equals(xmlContent)||null==xmlContent){
            return resultMap;
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        Element root = doc.getDocumentElement();
        NodeList books = root.getChildNodes();
        if (books != null) {
            for (int i = 0; i < books.getLength(); i++) {
                Node book = books.item(i);
                resultMap.put(book.getNodeName(), book.getFirstChild().getNodeValue());
                Logger.error("节点=" + book.getNodeName() + "\ttext="+ book.getFirstChild().getNodeValue());
            }
        }
        return resultMap;
    }

    /**
     * 支付回调测试
     * @return
     */
    public Result payBackendNotifyTest(Long orderId){
        Logger.info("==payBackendNotifyTest==="+orderId);
        TreeMap<String,String> params=new TreeMap<>();
        String test="<xml>\n" +
                "  <appid><![CDATA[wx2421b1c4370ec43b]]></appid>\n" +
                "  <attach><![CDATA[支付测试]]></attach>\n" +
                "  <bank_type><![CDATA[CFT]]></bank_type>\n" +
                "  <fee_type><![CDATA[CNY]]></fee_type>\n" +
                "  <is_subscribe><![CDATA[Y]]></is_subscribe>\n" +
                "  <mch_id><![CDATA[10000100]]></mch_id>\n" +
                "  <nonce_str><![CDATA[5d2b6c2a8db53831f7eda20af46e531c]]></nonce_str>\n" +
                "  <openid><![CDATA[oUpF8uMEb4qRXf22hE3X68TekukE]]></openid>\n" +
                "  <out_trade_no><![CDATA["+orderId+"]]></out_trade_no>\n" +
                "  <result_code><![CDATA[SUCCESS]]></result_code>\n" +
                "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "  <sign><![CDATA[B552ED6B279343CB493C5DD0D78AB241]]></sign>\n" +
                "  <sub_mch_id><![CDATA[10000100]]></sub_mch_id>\n" +
                "  <time_end><![CDATA[20140903131540]]></time_end>\n" +
                "  <total_fee>1</total_fee>\n" +
                "  <trade_type><![CDATA[JSAPI]]></trade_type>\n" +
                "  <transaction_id><![CDATA[1004400740201409030005092168]]></transaction_id>\n" +
                "</xml>";
        try {
            params=new TreeMap<>(xmlToMap(test));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        Logger.error("========"+params.toString()+"======"+toJson(params));

        if(null==params.get("return_code")) {
            Logger.error("微信支付回调,返回内容为空");
            return ok(weixinNotifyResponse("FAIL","param is null"));
        }
        if(!"SUCCESS".equals(params.get("return_code"))){ //失败
            Logger.error("微信支付回调return_code="+params.get("return_code")+",return_msg="+params.get("return_msg"));
            return ok(weixinNotifyResponse("FAIL","return_code"));
        }

//        String weixinSign=params.get("sign"); //微信发来的签名
//        String sign=getWeiXinSign(params);//我方签名
//        if(!weixinSign.equals(sign)){
//            Logger.error("微信支付回调,签名不一致我方="+sign+",微信="+weixinSign);
//            return ok(weixinNotifyResponse("FAIL","SIGN ERROR"));
//        }

        if(!"SUCCESS".equals(params.get("result_code"))){ //支付失败,返回支付失败的界面 TODO ...
            Logger.error("微信支付回调result_code="+params.get("result_code")+",err_code="+params.get("err_code")+",err_code_des="+params.get("err_code_des"));
            return ok(weixinNotifyResponse("FAIL","")); //TODO
        }

        if("SUCCESS".equals(params.get("result_code"))) {

            Order order = new Order();
            order.setOrderId(Long.valueOf(params.get("out_trade_no")));
            order.setPayMethod("WEIXIN");
            order.setErrorStr(params.get("err_code"));
            order.setPgTradeNo(params.get("transaction_id"));

            if (jdPayMid.asynPay(order).equals("success")) {
                Logger.error("################微信支付异步通知 异步方法调用返回成功################,"+params.get("out_trade_no"));
                try {
                    List<Order> orders = cartService.getOrder(order);
                    if (orders.size() > 0) {
                        order = orders.get(0);
                        if (order.getOrderType() != null && order.getOrderType() == 2) { //1:正常购买订单，2：拼购订单
                            if (jdPay.dealPinActivity(params, order) == null) {
                                Logger.error("################微信支付异步通知 拼购订单返回处理结果为空################,"+order.getOrderId());
                                return ok(weixinNotifyResponse("FAIL","order deal fail"));
                            }
                            else {
                                Logger.error("################微信支付异步通知 拼购订单返回成功################,"+order.getOrderId());
                                return ok(weixinNotifyResponse("SUCCESS","OK"));
                            }
                        } else {
                            Logger.error("################微信支付异步通知 普通订单返回成功################,"+order.getOrderId());
                            return ok(weixinNotifyResponse("SUCCESS","OK"));
                        }
                    } else {
                        Logger.error("################微信支付异步通知 订单未找到################,"+order.getOrderId());
                        return ok(weixinNotifyResponse("FAIL","order not found"));
                    }
                } catch (Exception e) {
                    Logger.error("################微信支付异步通知 出现异常################,"+order.getOrderId());
                    e.printStackTrace();
                    return ok(weixinNotifyResponse("FAIL",e.getMessage()));
                }
            }else {
                Logger.error("################微信支付异步通知 异步方法调用返回失败################,"+params.get("out_trade_no"));
                return ok(weixinNotifyResponse("FAIL","asynPayWeixin error"));
            }
        }
        return ok(weixinNotifyResponse("SUCCESS","OK"));
    }

    /**
     * 支付回调
     * @return
     */
    public Result payBackendNotify(){
        TreeMap<String,String> params=new TreeMap<>();
        Document content= request().body().asXml();
        Logger.info("微信支付回调返回\n"+content);
        Element root = content.getDocumentElement();
        NodeList books = root.getChildNodes();
        if (books != null) {
            for (int i = 0; i < books.getLength(); i++) {
                Node book = books.item(i);
                params.put(book.getNodeName(), book.getFirstChild().getNodeValue());
                Logger.error("===payBackendNotify==节点=" + book.getNodeName() + "\ttext="+ book.getFirstChild().getNodeValue());
            }
        }


        if(null==params.get("return_code")) {
            Logger.error("微信支付回调,返回内容为空");
            return ok(weixinNotifyResponse("FAIL","param is null"));
        }
        if(!"SUCCESS".equals(params.get("return_code"))){ //失败
            Logger.error("微信支付回调return_code="+params.get("return_code")+",return_msg="+params.get("return_msg"));
            return ok(weixinNotifyResponse("FAIL","return_code"));
        }

        String weixinSign=params.get("sign"); //微信发来的签名
        String sign=getWeiXinSign(params);//我方签名
        if(!weixinSign.equals(sign)){
            Logger.error("微信支付回调,签名不一致我方="+sign+",微信="+weixinSign);
            return ok(weixinNotifyResponse("FAIL","SIGN ERROR"));
        }

        if(!"SUCCESS".equals(params.get("result_code"))){ //支付失败,返回支付失败的界面 TODO ...
            Logger.error("微信支付回调result_code="+params.get("result_code")+",err_code="+params.get("err_code")+",err_code_des="+params.get("err_code_des"));
            return ok(weixinNotifyResponse("FAIL","")); //TODO
        }

        if("SUCCESS".equals(params.get("result_code"))) {

            Order order = new Order();
            order.setOrderId(Long.valueOf(params.get("out_trade_no")));
            order.setPayMethod("WEIXIN");
            order.setErrorStr(params.get("err_code"));
            order.setPgTradeNo(params.get("transaction_id"));

            if (jdPayMid.asynPay(order).equals("success")) {
                try {
                    List<Order> orders = cartService.getOrder(order);
                    if (orders.size() > 0) {
                        order = orders.get(0);
                        if (order.getOrderType() != null && order.getOrderType() == 2) { //1:正常购买订单，2：拼购订单
                            if (jdPay.dealPinActivity(params, order) == null) {
                                Logger.error("################微信支付异步通知 拼购订单返回处理结果为空################,"+order.getOrderId());
                                return ok(weixinNotifyResponse("FAIL","order deal fail"));
                            }
                            else {
                                Logger.error("################微信支付异步通知 拼购订单返回成功################,"+order.getOrderId());
                                return ok(weixinNotifyResponse("SUCCESS","OK"));
                            }
                        } else {
                            Logger.error("################微信支付异步通知 普通订单返回成功################,"+order.getOrderId());
                            return ok(weixinNotifyResponse("SUCCESS","OK"));
                        }
                    } else {
                        Logger.error("################微信支付异步通知 订单未找到################,"+order.getOrderId());
                        return ok(weixinNotifyResponse("FAIL","order not found"));
                    }
                } catch (Exception e) {
                    Logger.error("################微信支付异步通知 出现异常################,"+order.getOrderId());
                    e.printStackTrace();
                    return ok(weixinNotifyResponse("FAIL",e.getMessage()));
                }
            }else {
                Logger.error("################微信支付异步通知 异步方法调用返回失败################,"+params.get("out_trade_no"));
                return ok(weixinNotifyResponse("FAIL","asynPayWeixin error"));
            }
        }
        return ok(weixinNotifyResponse("SUCCESS","OK"));
    }

    /**
     * 微信通知返回信息
     * @param return_code
     * @param return_msg
     * @return
     */
    private String weixinNotifyResponse(String return_code,String return_msg){
        //生成xml
        StringBuffer sb=new StringBuffer();
        sb.append("<xml>");
        sb.append("<return_code><![CDATA["+return_code+"]]></return_code>");
        sb.append("<return_msg><![CDATA["+return_msg+"]]></return_msg>");
        sb.append("</xml>");
        return sb.toString();
    }



    /***
     * 生成二维码数据流
     * @param url
     * @return
     * @throws IOException
     */
    public static ByteArrayOutputStream createQrGen(String url) throws IOException {
        //如果有中文，可使用withCharset("UTF-8")方法
        //设置二维码url链接，图片宽度250*250，JPG类型
        return QRCode.from(url).withSize(250, 250).to(ImageType.JPG).stream();
    }

    public Result getQRCode(String qrCodeUrl){
        byte[] qr=(byte[]) cache.get(qrCodeUrl);
        return ok(qr);
    }

    /**
     * HTTP的请求URL
     * @param requestUrl
     * @return
     */
    public String httpConnect(String requestUrl,String sendStr){

        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestProperty("Content-Type", "text/xml;charset=utf-8");
            //HTTP的POST方式进行数据传输
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            OutputStream dataOut = conn.getOutputStream();
            dataOut.write(sendStr.getBytes());
            dataOut.flush();
            dataOut.close();
            //获取响应内容
            conn.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            String line = "";
            StringBuffer buffer = new StringBuffer(1024);
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            in.close();

            return buffer.toString();

        }catch (Exception e){
            Logger.error(e.getMessage());

        }
        return "";
    }

    /**
     * 微信支付订单查询
     * @param orderId
     * @return
     */
    public Result payOrderquery(Long orderId){
        ObjectNode objectNode = newObject();
        //I:初始化即未支付状态
        Map<String,String> returnMap=new HashMap<>();
        returnMap.put("m_index", M_INDEX);
        returnMap.put("m_orders", M_ORDERS);
        try {
            Order order = new Order();
            order.setOrderId(orderId);
            Optional<List<Order>> listOptional = Optional.ofNullable(cartService.getOrder(order));
            if (!listOptional.isPresent() || listOptional.get().size() <= 0) {
                return ok(views.html.jdpayfailed.render(returnMap));
            }
            order=listOptional.get().get(0);
            Logger.info("==order==="+order);
            if (order.getOrderStatus().equals("S") || order.getOrderStatus().equals("PS")){ //订单状态是支付成功
                return weixinPaySucessRedirect(order,returnMap);
            }
            if(!order.getOrderStatus().equals("I")) { //订单状态不是初始状态
                return ok(views.html.jdpayfailed.render(returnMap));
            }
            //I:初始化即未支付状态
            TreeMap<String,String> paramMap=new TreeMap<>();
            paramMap.put("appid",SysParCom.WEIXIN_APP_ID); //应用ID
            paramMap.put("mch_id",SysParCom.WEIXIN_MCH_ID);
            paramMap.put("out_trade_no",orderId+"");
            paramMap.put("nonce_str",UUID.randomUUID().toString().replaceAll("-", ""));
            String sign=getWeiXinSign(paramMap);
            paramMap.put("sign",sign);
            String xmlContent=mapToXml(paramMap);

                try{
                    String result=httpConnect(SysParCom.WEIXIN_PAY_ORDERQUERY,xmlContent); //接口提供所有微信支付订单的查询
                    Logger.info("微信支付订单查询发送内容\n"+xmlContent+"\n返回内容"+result);
                    if(""==result||null==result){
                        return ok(views.html.jdpayfailed.render(returnMap));
                    }
                    Map<String,String> resultMap=xmlToMap(result);
                    if(null==resultMap||resultMap.size()<=0){
                        return ok(views.html.jdpayfailed.render(returnMap));
                    }
                    if(!"SUCCESS".equals(resultMap.get("return_code"))){ //返回状态码  SUCCESS/FAIL 此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
                        return ok(views.html.jdpayfailed.render(returnMap));
                    }
                    if(!"SUCCESS".equals(resultMap.get("result_code"))){ //业务结果
                        return ok(views.html.jdpayfailed.render(returnMap));
                    }

                    if("SUCCESS".equals(resultMap.get("trade_state"))){ //交易状态支付成功
                        return weixinPaySucessRedirect(order,returnMap);
                    }

                }catch (Exception e){
                    Logger.error(e.getMessage());
                }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ok(views.html.jdpayfailed.render(returnMap));

    }

    /**
     * 支付成功后的跳转
     * @param order
     * @param returnMap
     * @return
     */
    private Result weixinPaySucessRedirect(Order order, Map<String,String> returnMap){
        if (order.getOrderType() != null && order.getOrderType() == 2) { //1:正常购买订单，2：拼购订单
            PinUser pinUser = new PinUser();
            pinUser.setUserId(order.getUserId());
            if (order.getPinActiveId() != null) { //TODO ...
                pinUser.setPinActiveId(order.getPinActiveId());
                List<PinUser> pinUsers = promotionService.selectPinUser(pinUser);
                if (pinUsers.size() > 0) {
                    pinUser = pinUsers.get(0);
                    if (pinUser.isOrMaster()) {
                        returnMap.put("pinActivity", SysParCom.PROMOTION_URL + "/promotion/pin/activity/pay/" + order.getPinActiveId() + "/1");
                        returnMap.put("m_pinActivity", M_PIN + order.getPinActiveId() + "/1");
                    } else {
                        returnMap.put("pinActivity", SysParCom.PROMOTION_URL + "/promotion/pin/activity/pay/" + order.getPinActiveId() + "/2");
                        returnMap.put("m_pinActivity", M_PIN + order.getPinActiveId() + "/2");
                    }
                }
            }
            return  ok(views.html.pin.render(returnMap));
        } else {
            return ok(views.html.jdpaysuccess.render(returnMap));
        }
    }

    /**
     * 微信支付退款
     * @param orderId
     * @return
     */
    public Result payRefund(Long orderId){
        ObjectNode objectNode = newObject();
        try {
            Order order = new Order();
            order.setOrderId(orderId);
            Optional<List<Order>> listOptional = Optional.ofNullable(cartService.getOrder(order));
            if (!listOptional.isPresent() || listOptional.get().size() <= 0) {
                objectNode.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE.getIndex()), Message.ErrorCode.FAILURE.getIndex())));
                return ok(objectNode);
            }
            order = listOptional.get().get(0);

            TreeMap<String, String> paramMap = new TreeMap<>();
            paramMap.put("appid", SysParCom.WEIXIN_APP_ID); //应用ID
            paramMap.put("mch_id", SysParCom.WEIXIN_MCH_ID);
            paramMap.put("nonce_str", UUID.randomUUID().toString().replaceAll("-", ""));
            paramMap.put("out_trade_no", orderId + "");
            paramMap.put("out_refund_no", orderId + ""); //商户系统内部的退款单号，商户系统内部唯一，同一退款单号多次请求只退一笔
            Integer totalFee=order.getTotalFee().multiply(new BigDecimal(100)).intValue();
            paramMap.put("total_fee", totalFee+""); //订单总金额，单位为分，只能为整数，详见支付金额
            paramMap.put("refund_fee",totalFee+""); //退款总金额，订单总金额，单位为分，只能为整数，详见支付金额
            paramMap.put("refund_fee_type", "CNY");
            paramMap.put("op_user_id", SysParCom.WEIXIN_MCH_ID);//操作员帐号, 默认为商户号

            String sign = getWeiXinSign(paramMap);
            paramMap.put("sign", sign);
            String xmlContent = mapToXml(paramMap);
            try{
                String result=httpConnect(SysParCom.WEIXIN_PAY_REFUND,xmlContent); //接口提供所有微信支付订单的查询
                Logger.info("微信支付退款发送内容\n"+xmlContent+"\n返回内容"+result);
                if(""==result||null==result){
                    objectNode.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE.getIndex()), Message.ErrorCode.FAILURE.getIndex())));
                    return ok(objectNode);
                }
                Map<String,String> resultMap=xmlToMap(result);
                if(null==resultMap||resultMap.size()<=0){
                    objectNode.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE.getIndex()), Message.ErrorCode.FAILURE.getIndex())));
                    return ok(objectNode);
                }
                if(!"SUCCESS".equals(resultMap.get("return_code"))){ //返回状态码  SUCCESS/FAIL 此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
                    objectNode.putPOJO("message", Json.toJson(new domain.Message(resultMap.get("return_msg"), domain.Message.ErrorCode.FAILURE.getIndex())));
                    return ok(objectNode);
                }
                if(!"SUCCESS".equals(resultMap.get("result_code"))){ //业务结果
                    objectNode.putPOJO("message", Json.toJson(new domain.Message(resultMap.get("err_code_des"), domain.Message.ErrorCode.FAILURE.getIndex())));
                    return ok(objectNode);
                }

                //退款成功//TODO 退款逻辑
                objectNode.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(objectNode);

            }catch (Exception e){
                Logger.error(e.getMessage());
            }

        }catch(Exception e){
            Logger.error(e.getMessage());
        }
        objectNode.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE.getIndex()), Message.ErrorCode.FAILURE.getIndex())));
        return ok(objectNode);
    }



}
