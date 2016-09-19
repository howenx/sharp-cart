package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import static play.libs.Json.newObject;

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
                List<JsonNode> orderNodeList=new ArrayList<>();
                for(Order order:orderList){
                    orderNodeList.add(yiqifaOrder(order));
                }
                ObjectNode ordersNode=newObject();
                ordersNode.putPOJO("orders",orderNodeList);
                return ok(Json.toJson(ordersNode));
            }

        } catch (Exception e) {
            Logger.error("广告查询订单异常" + Throwables.getStackTraceAsString(e));
        }

        return ok("success");
    }

    private JsonNode yiqifaOrder(Order order){
        ObjectNode paramMap=newObject();
     //   LinkedHashMap<String,String> paramMap=new LinkedHashMap<>();
        paramMap.put("orderNo",order.getOrderId());//订单编号
        paramMap.put("campaignId",order.getSubAdSource());
        if(null!=order.getAdParam()&&!"".equals(order.getAdParam())){
            JsonNode adParamNode = Json.parse(order.getAdParam());
            if(adParamNode.has("wi")){
                paramMap.put("feedback",adParamNode.get("wi").asText());
            }
        }
        paramMap.put("orderTime",order.getOrderCreateAt());//下单时间
        paramMap.put("orderStatus",order.getOrderStatus()); //订单状态
        paramMap.put("paymentStatus",order.getOrderStatus());//支付状态
        paramMap.put("paymentType",order.getPayMethod()); //支付方式
        OrderLine temp=new OrderLine();
        temp.setOrderId(order.getOrderId());
        try {
            List<OrderLine> orderLineList=cartService.selectOrderLine(temp);
            if(null!=orderLineList&&orderLineList.size()>0){
                List<JsonNode> products=new ArrayList<>();
                for(OrderLine orderLine:orderLineList){
                    ObjectNode orderLineNode = newObject();
                    orderLineNode.put("productNo",orderLine.getSkuTypeId());
                    orderLineNode.put("name",orderLine.getSkuTitle());
                    orderLineNode.put("amount",orderLine.getAmount());
                    orderLineNode.put("price",orderLine.getPrice());
                    products.add(orderLineNode);
                }
                paramMap.putPOJO("products",products);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paramMap;
    }

}
