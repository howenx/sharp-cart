package common;

/**
 * 消息类型的枚举
 * Created by sibyl.sun on 16/2/23.
 */
public enum MsgTypeEnum {
    System("system","系统消息"),
    Discount("discount","优惠消息"),
    Coupon("coupon","我的资产"),
    Logistics("logistics","物流通知"),
    Goods("goods","商品提醒");
    private String msgType;
    private String name;

    private MsgTypeEnum(String msgType,String name){
        this.msgType=msgType;
        this.name=name;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public static MsgTypeEnum getMsgTypeEnum(String tradeType){
        for(MsgTypeEnum msgTypeEnum:MsgTypeEnum.values()){
            if(msgTypeEnum.getMsgType().equals(tradeType)){
                return msgTypeEnum;
            }
        }
        return null;
    }
}
