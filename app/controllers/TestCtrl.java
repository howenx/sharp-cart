package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squareup.okhttp.*;
import domain.Order;
import domain.OrderAddress;
import domain.OrderLine;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import service.CartService;
import util.Crypto;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static play.libs.Json.newObject;

/**
 * Created by sibyl.sun on 16/9/12.
 */
public class TestCtrl extends Controller {


    @Inject
    CartService cartService;
    public Result test(){

        Long orderId=50102340L;
        Order order=new Order();
        order.setOrderId(orderId);
        try {
            List<Order> orderList=cartService.getOrder(order);
            if(null!=orderList&&orderList.size()>0){
                order=orderList.get(0);
            }else{
                Logger.error("没有订单信息orderId="+orderId);
                //TODO ...
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //时间格式
        SimpleDateFormat simpleDateFormat1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMddHHmmss");
        String commitTime=simpleDateFormat.format(new Date());
        ObjectNode result = newObject();
        result.put("version","v1.9"); //网关版本
        result.put("commitTime",commitTime); //提交时间
        result.put("coName","北京东方爱怡斯科技有限公司"); //企业名称
        String coCode="NWXj232";
        result.put("coCode",coCode); //企业代码
        result.put("serialNumber",coCode+commitTime+"00001"); //流水号 规则为 coCode（7 位）+YYYYMMDDHHMMSS(14 位)+5 位流水号
        result.put("merchantOrderId",order.getOrderId()); //商户订单号
    //    result.put("assBillNo","9281736002"); //物流分运单号 物流ᨀ供的唯一分运单号 可不传
        try {
            result.put("orderCommitTime",simpleDateFormat.format(simpleDateFormat1.parse(order.getOrderCreateAt()))); //订单提交时间
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //TODO ...发件人相关信息
        result.put("senderName","王五");//发件人姓名
        result.put("senderTel","010-53678808");// 发件人电话
        result.put("SenderCompanyName","北京东方爱怡斯科技有限公司");//发件方公司名称
        result.put("senderAddr","北京望京洛娃大厦3层1304室");// 发件人地址
        result.put("senderZip","100000");//发件地邮编
        result.put("senderCity","北京");//发件地城市
        result.put("senderProvince","北京");// 发件地省/州名
        result.put("senderCountry","KOR");//发件地国家 韩国KOR



        result.put("cargoDescript","电器相关物品"); //订单商品信息简述 TODO ...
     //   result.put("allCargoTotalPrice",528.00);//全部购买商品合计总价
        result.put("allCargoTotalPrice",order.getPayTotal());//全部购买商品合计总价
        result.put("allCargoTotalTax",0);//全部购买商品行邮缴税总价
        result.put("expressPrice",0);//物流运费
        result.put("otherPrice",0);//其它费用


        //收件人信息
        OrderAddress orderAddress=new OrderAddress();
        orderAddress.setOrderId(orderId);
        try {
            List<OrderAddress> addressList=cartService.selectOrderAddress(orderAddress);
            if(null!=addressList&&addressList.size()>0){
                orderAddress=addressList.get(0);
            }else{
                Logger.error("没有订单地址信息orderId="+orderId);
                //TODO...
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        result.put("recPerson",orderAddress.getDeliveryName());// 收货人姓名
        result.put("recPhone",orderAddress.getDeliveryTel());// 收货人电话
        result.put("recCountry","中国");// 收货地国家
        result.put("recProvince", orderAddress.getDeliveryCity().split(" ")[0]);// 收货地省/州 TODO...
        result.put("recCity",orderAddress.getDeliveryCity().split(" ")[2]);// 收货地城市
        result.put("recAddress",orderAddress.getDeliveryAddress());// 收货地地址
    //    result.put("recZip","100000");// 收货地邮编 ,可为空

        result.put("serverType","S01"); //业务类型 S01：一般进口 S02：保税区进口
        result.put("custCode","2244");//海关关区代码
        result.put("operationCode","1");//操作编码 1：新增
//        result.put("customDeclCo","");//物流进境申报企业,可为空
//        result.put("spt","");//扩展字段 ,可为空

        OrderLine temp=new OrderLine();
        temp.setOrderId(orderId);
        List<OrderLine> orderLineList=null;
        try {
             orderLineList=cartService.selectOrderLine(temp);
            if(null==orderLineList||orderLineList.size()<=0){
                Logger.error("没有商品信息orderId="+orderId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<JsonNode> cargoeList=new ArrayList<>();
        for(OrderLine orderLine:orderLineList){
            ObjectNode cargoe = newObject();
            cargoe.put("cargoName","exo 明星logo款连帽卫衣");// 单项购买商品名
            cargoe.put("cargoCode","ABCDEFG12345");// 单项购买商品编号  电商商品备案时的编号 TODO ....
//            cargoe.put("cargoNum",1);// 单项购买商品数量
//            cargoe.put("cargoUnitPrice",528.00);// 单项购买商品单价
//            cargoe.put("cargoTotalPrice",528.00);//单项购买商品总价
            cargoe.put("cargoNum",orderLine.getAmount());// 单项购买商品数量
            cargoe.put("cargoUnitPrice",orderLine.getPrice());// 单项购买商品单价
            cargoe.put("cargoTotalPrice",orderLine.getPrice().multiply(BigDecimal.valueOf(orderLine.getAmount())));//单项购买商品总价
            cargoe.put("cargoTotalTax",0);// 单项购买商品行邮缴税总价
            cargoeList.add(cargoe);
        }
        result.putPOJO("cargoes",cargoeList);

        //支付信息
        if("ALIPAY".equals(order.getPayMethod())){
            result.put("payMethod","ALIPAY");//支付方式
            result.put("payMerchantCode","2088811744291968"); //企业支付编号 TODO ...
        }else if("WEIXIN".equals(order.getPayMethod())){
            result.put("payMethod","WEIXIN");//支付方式 TODO ...
         //   result.put("payMerchantName","深圳市腾讯计算机系统有限公司"); //企业支付名称  TODO ...
            result.put("payMerchantCode","1372832702"); //企业支付编号 TODO ...
        }else if("JD".equals(order.getPayMethod())){
            result.put("payMethod","JD");//支付方式 TODO ...
            result.put("payMerchantCode","23237662"); //企业支付编号 TODO ...
        }else {
            Logger.error("支付方式不存在"+order.getPayMethod());

        }
        result.put("payMerchantName","北京东方爱怡斯科技有限公司"); //企业支付名称

        result.put("payAmount",order.getPayTotal()); //支付总金额
        result.put("payCUR","CNY"); //付款币种
        result.put("payID",order.getPgTradeNo()); //支付交易号
        result.put("payTime",simpleDateFormat.format(order.getUpdatedAt())); //支付交易时间

        String EData= result.toString();

        String md5Str=EData+"J5844Lm654d0EWr41x40B1S9J4K7gP56";

        Logger.info("md5加密串"+md5Str);
        String SignMsg= Crypto.md5(md5Str).toUpperCase();

        String EDataEncodeStr="";
        try {
            EDataEncodeStr= URLEncoder.encode(EData,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String sendMsg="EData="+EDataEncodeStr+"&SignMsg="+SignMsg;

        Logger.info("EDataEncodeStr="+EDataEncodeStr+",SignMsg="+SignMsg);

        Logger.info("发送内容"+sendMsg);

        RequestBody formBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"),sendMsg);
        //创建一个OkHttpClient对象
        OkHttpClient client = new OkHttpClient();
        try {

            Request request =new Request.Builder()
                    .url("http://61.152.165.77:8081/cboi/order/orderlist.htm").post(formBody)
                    .build();
            client.setConnectTimeout(15, TimeUnit.SECONDS);

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String re=URLDecoder.decode(new String(response.body().bytes(),"utf-8"),"utf-8");
                //打印服务端返回结果
                JsonNode jsonNode= Json.parse(re);
                Logger.info("海关返回信息--->" + jsonNode);
                return ok(jsonNode);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ok("fail");
    }
}
