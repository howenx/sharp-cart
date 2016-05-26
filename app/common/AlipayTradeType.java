package common;

/**
 * 支付宝交易类型
 * Created by sibyl.sun on 16/5/11.
 */
public enum AlipayTradeType {
    //即时到账
    DIRECT("DIRECT"),
    //手机网站支付
    WAP("WAP"),
    //app支付
    APP("APP");
    private String tradeType;

    private AlipayTradeType(String tradeType){
        this.tradeType=tradeType;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }


    public static AlipayTradeType getWeiXinTradeType(String tradeType){
        for(AlipayTradeType weiXinTradeType: AlipayTradeType.values()){
            if(weiXinTradeType.getTradeType().equals(tradeType)){
                return weiXinTradeType;
            }
        }
        return null;
    }
}
