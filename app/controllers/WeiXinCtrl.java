package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.Message;
import domain.Order;
import modules.SysParCom;
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
import java.util.*;

import static modules.SysParCom.ONE_CENT_PAY;
import static play.libs.Json.newObject;

/**
 * 微信支付
 * Created by sibyl.sun on 16/4/21.
 */
public class WeiXinCtrl extends Controller {

    private CartService cartService;

    private IdService idService;

    private PromotionService promotionService;

    @Inject
    public WeiXinCtrl(CartService cartService, IdService idService, PromotionService promotionService) {
        this.cartService = cartService;
        this.idService = idService;
        this.promotionService = promotionService;
    }

    public String getPayUnifiedorderParams(Long userId, Order order,String tradeType) throws Exception {
        /**
         * <xml>
         <appid>wx2421b1c4370ec43b</appid>
         <attach>支付测试</attach>
         <body>APP支付测试</body>
         <mch_id>10000100</mch_id>
         <nonce_str>1add1a30ac87aa2db72f57a2375d8fec</nonce_str>
         <notify_url>http://wxpay.weixin.qq.com/pub_v2/pay/notify.v2.php</notify_url>
         <out_trade_no>1415659990</out_trade_no>
         <spbill_create_ip>14.23.150.211</spbill_create_ip>
         <total_fee>1</total_fee>
         <trade_type>APP</trade_type>
         <sign>0CB01533B8C1EF103065174F50BCA001</sign>
         </xml>
         */

        TreeMap<String,String> paramMap=new TreeMap<>();

        Long orderId=order.getOrderId();

        paramMap.put("appid",SysParCom.WEIXIN_APP_ID); //应用ID
        paramMap.put("mch_id",SysParCom.WEIXIN_MCH_ID);
        paramMap.put("nonce_str",UUID.randomUUID().toString().replaceAll("-", ""));
        paramMap.put("body","韩秘美-订单编号" + orderId);
        paramMap.put("notify_url",SysParCom.SHOPPING_URL+"/client/pay/jd/back");
        paramMap.put("out_trade_no",orderId.toString());
        paramMap.put("spbill_create_ip",order.getOrderIp());
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


    public String getWeiXinDeeplink(String prepayId,String tradeType) throws UnsupportedEncodingException {
        /**
         * 公众账号ID	appid	是	String(32)	wx8888888888888888	微信分配的公众账号ID
         随机字符串	noncestr	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	随机字符串，不长于32位。推荐随机数生成算法
         订单详情扩展字符串	package	是	String(32)	WAP	扩展字段，固定填写WAP
         预支付交易会话标识	prepayid	是	String(64)	wx201410272009395522657a690389285100	微信统一下单接口返回的预支付回话标识，用于后续接口调用中使用，该值有效期为2小时
         签名	sign	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	签名，详见签名生成算法
         时间戳	timestamp	是	String(32)	1414561699
         当前的时间，其他详见时间戳规则


         生成deeplink 的步骤如下：
         步骤1：按URL 格式组装参数, $value 部分进行URL 编码，生成string1：
         string1 ： key1=Urlencode($value1)&key2=Urlencode($value2、&...
         步骤2：对string1 作整体的Urlencode，生成string2：
         String2=Urlencode(string1);
         步骤3：拼接前缀，生成最终deeplink

         举例如下：
         String1：
         appid=wxf5b5e87a6a0fde94&noncestr=123&package=WAP&prepayid=wx20141210163048
         0281750c890475924233&sign=53D411FB74FE0B0C79CC94F2AB0E2333&timestamp=1417511263
         再对整个string1 做一次URLEncode
         string2：
         appid%3Dwxf5b5e87a6a0fde94%26noncestr%3D123%26package%3DWAP%26prepayid%3Dw
         x201412101630480281750c890475924233%26sign%3D53D411FB74FE0B0C79CC94F2AB0E2
         333%26timestamp%3D1417511263
         再加上协议头weixin：//wap/pay? 得到最后的deeplink
         weixin：//wap/pay?appid%3Dwxf5b5e87a6a0fde94%26noncestr%3D123%26package%3DW
         AP%26prepayid%3Dwx201412101630480281750c890475924233%26sign%3D53D411FB74FE0
         B0C79CC94F2AB0E2333%26timestamp%3D1417511263
         */
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
        Long userId = (Long) ctx().args.get("userId");
        userId=1000242L;
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
            String sendStr = getPayUnifiedorderParams(userId,order,"APP");
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


                objectNode.putPOJO("trade_type",resultMap.get("trade_type")); //交易类型
                objectNode.putPOJO("prepay_id",resultMap.get("prepay_id")); //预支付交易会话标识


                objectNode.putPOJO("deeplink",getWeiXinDeeplink(resultMap.get("prepay_id"),tradeType)); //deeplink

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


}
