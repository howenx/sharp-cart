package common;

/**
 * 微信交易类型
 * Created by sibyl.sun on 16/5/11.
 */
public enum WeiXinTradeType {
    //公众号支付
    JSAPI("JSAPI","J"),
    //原生扫码支付
    NATIVE("NATIVE","N"),
    //app支付
    APP("APP","A");
    private String tradeType;
    //订单后缀 后面拼一位写死了
    private String orderSuffix;

    private WeiXinTradeType(String tradeType,String orderSuffix){
        this.tradeType=tradeType;
        this.orderSuffix=orderSuffix;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getOrderSuffix() {
        return orderSuffix;
    }

    public void setOrderSuffix(String orderSuffix) {
        this.orderSuffix = orderSuffix;
    }

    public static WeiXinTradeType getWeiXinTradeType(String tradeType){
        for(WeiXinTradeType weiXinTradeType:WeiXinTradeType.values()){
            if(weiXinTradeType.getTradeType().equals(tradeType)){
                return weiXinTradeType;
            }
        }
        return null;
    }
}
