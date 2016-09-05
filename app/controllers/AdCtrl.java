package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import domain.Order;
import domain.OrderLine;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import service.CartService;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 推广
 * Created by sibyl.sun on 16/9/2.
 */
public class AdCtrl extends Controller {

    @Inject
    CartService cartService;

    public Result adQueryOrder(String adSource,String subAdSource,String queryDate) {
        try {

            Logger.info("adSource="+adSource+",subAdSource="+subAdSource+",date="+queryDate);

            Order temp=new Order();
            temp.setAdSource(adSource);
            temp.setSubAdSource(subAdSource);
            if(null!=queryDate&&!"".equals(queryDate)){
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMdd");
                Date date=simpleDateFormat.parse(queryDate);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.MILLISECOND, 001);
                temp.setStartAt(new Timestamp(cal.getTimeInMillis())); //开始时间

                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.MILLISECOND, 999);
                temp.setEndAt(new Timestamp(cal.getTimeInMillis())); //结束时间
            }
            List<Order> orderList=cartService.getOrderByAd(temp);
            if(null!=orderList||orderList.size()>0){
                StringBuffer sb=new StringBuffer();
                sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<orders>\n");
                for(Order order:orderList){
                    sb.append(yiqifaOrder(order));
                }
                sb.append("</orders>");
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                XMLEncoder encoder = new XMLEncoder(baos);
//                encoder.writeObject(orderList.get(0));
//                encoder.flush();
//                encoder.close();
//                return ok(baos.toString());
                return ok(sb.toString());
            }

        } catch (Exception e) {
            Logger.error("广告查询订单异常" + Throwables.getStackTraceAsString(e));
        }

        return ok("success");
    }

    private String yiqifaOrder(Order order){
        LinkedHashMap<String,String> paramMap=new LinkedHashMap<>();
        if(null!=order.getAdParam()&&!"".equals(order.getAdParam())){
            JsonNode adParamNode = Json.parse(order.getAdParam());
            if(adParamNode.has("wi")){
                paramMap.put("wi",adParamNode.get("wi").asText());
            }
        }
        paramMap.put("order_time",order.getOrderCreateAt());//下单时间
        paramMap.put("order_no",order.getOrderId()+"");//订单编号
        OrderLine temp=new OrderLine();
        temp.setOrderId(order.getOrderId());
        /**pn	商品编号	pna商品名称 ct佣金类型 ta商品数量 pp商品单价*/
        StringBuffer pnsb=new StringBuffer(),pnasb=new StringBuffer(),ctsb=new StringBuffer(),tasb=new StringBuffer(),ppsb=new StringBuffer();
        try {
            List<OrderLine> orderLineList=cartService.selectOrderLine(temp);
            if(null!=orderLineList&&orderLineList.size()>0){
                for(OrderLine orderLine:orderLineList){
                    pnsb.append((pnsb.length()>0?"|":"")+orderLine.getSkuTypeId());
                    pnasb.append((pnasb.length()>0?"|":"")+orderLine.getSkuTitle());
                    ctsb.append((ctsb.length()>0?"|":"")+"basic"); //
                    tasb.append((tasb.length()>0?"|":"")+orderLine.getAmount());
                    ppsb.append((ppsb.length()>0?"|":"")+orderLine.getPrice());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        paramMap.put("prod_no",pnsb.toString());//商品编号
        paramMap.put("prod_name",pnasb.toString());//商品名称
        paramMap.put("amount",tasb.toString());//商品数量
        paramMap.put("price",ppsb.toString());//商品金额
        paramMap.put("order_status",order.getOrderStatus()); //订单状态
        paramMap.put("payment_status",order.getOrderStatus());//支付状态
        paramMap.put("payment_type",order.getPayMethod()); //支付方式
        paramMap.put("fare",0+"");//运费
        paramMap.put("favorable",order.getDiscount()+"");//优惠金额
//        paramMap.put("favorable_code","优惠券编号或积分卡卡号");//优惠券编号或积分卡卡号

        StringBuffer sb=new StringBuffer();
        sb.append("<order>");
        for(Map.Entry<String,String> entry:paramMap.entrySet()){
            sb.append("<"+entry.getKey()+">"+entry.getValue()+"</"+entry.getKey()+">\n");
        }

        sb.append("</order>\n");
        return sb.toString();
    }

}
