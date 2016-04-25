package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.Message;
import domain.Order;
import middle.JDPayMid;
import modules.SysParCom;
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
import service.IdService;
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

import static modules.SysParCom.ONE_CENT_PAY;
import static play.libs.Json.newObject;

/**
 * 微信支付
 * Created by sibyl.sun on 16/4/21.
 */
public class WeiXinCtrl extends Controller {

    private CartService cartService;


    @Inject
    private MemcachedClient cache;

    @Inject
    private JDPayMid jdPayMid;

    @Inject
    private JDPay jdPay;

    @Inject
    public WeiXinCtrl(CartService cartService) {
        this.cartService = cartService;
    }

    public String getPayUnifiedorderParams(Order order,String tradeType) throws Exception {

        TreeMap<String,String> paramMap=new TreeMap<>();

        Long orderId=order.getOrderId();

        paramMap.put("appid",SysParCom.WEIXIN_APP_ID); //应用ID
        paramMap.put("mch_id",SysParCom.WEIXIN_MCH_ID);
        paramMap.put("nonce_str",UUID.randomUUID().toString().replaceAll("-", ""));
        paramMap.put("body","韩秘美-订单编号" + orderId); //URLEncoder.encode
        paramMap.put("notify_url",SysParCom.SHOPPING_URL+"/client/pay/weixin/back");
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

        //生成xml
        StringBuffer sb=new StringBuffer();
        sb.append("<xml>");
        for(Map.Entry<String,String> entry:paramMap.entrySet()){
            sb.append("<"+entry.getKey()+">"+entry.getValue()+"</"+entry.getKey()+">");
        }
        sb.append("</xml>");
        Logger.info("===getPayUnifiedorderParams===="+sb.toString());
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
            //HTTP的请求URL
            URL url = new URL(SysParCom.WEIXIN_PAY_UNIFIEDORDER);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestProperty("Content-Type", "text/xml;charset=utf-8");
            //HTTP的POST方式进行数据传输
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            OutputStream dataOut = conn.getOutputStream();
            String sendStr = getPayUnifiedorderParams(order,tradeType);
            dataOut.write(sendStr.getBytes());
            dataOut.flush();
            dataOut.close();
            //获取响应内容
            try{
                conn.connect();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
                //注意BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"GBK")); 这么写，是因为有时候网络传输过程中字符会被修改,如GB-2312，因为出现乱码,就需要将此处加入GBK。
                //loger.info("获取默认字符编码："+Charset.defaultCharset());
                String line = "";
                StringBuffer buffer = new StringBuffer(1024);
                while((line=in.readLine())!=null){
                    buffer.append(line);
                }
                in.close();

                String  result = buffer.toString();
                Logger.info("微信统一下单返回内容"+result);


                Map<String,String> resultMap=xmlToMap(result);
                if(null==resultMap||resultMap.size()<=0){
                    objectNode.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE.getIndex()), Message.ErrorCode.FAILURE.getIndex())));
                    return ok(objectNode);
                }

                if("FAIL".equals(resultMap.get("return_code"))){ //返回状态码  SUCCESS/FAIL 此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
                    objectNode.putPOJO("message", Json.toJson(new domain.Message(resultMap.get("return_msg"), domain.Message.ErrorCode.FAILURE.getIndex())));
                    return ok(objectNode);
                }
                if("FAIL".equals(resultMap.get("result_code"))){ //业务结果
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
        if("FAIL".equals(params.get("return_code"))){ //失败
            Logger.error("微信支付回调return_code="+params.get("return_code")+",return_msg="+params.get("return_msg"));
            return ok(weixinNotifyResponse("FAIL","return_code"));
        }

        String weixinSign=params.get("sign"); //微信发来的签名
        params.remove("sign");
        String sign=getWeiXinSign(params);//我方签名
        if(!weixinSign.equals(sign)){
            Logger.error("微信支付回调,签名不一致我方="+sign+",微信="+weixinSign);
            return ok(weixinNotifyResponse("FAIL","SIGN ERROR"));
        }

        if("FAIL".equals(params.get("result_code"))){ //支付失败,返回支付失败的界面 TODO ...
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
        sb.append(" <return_msg><![CDATA["+return_msg+"]]></return_msg>");
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




}
