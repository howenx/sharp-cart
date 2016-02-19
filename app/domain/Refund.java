package domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.security.Timestamp;
import java.util.Optional;

/**
 * 退款VO
 * Created by howen on 15/12/22.
 */
public class Refund implements Serializable{

    private Long        id;//主键
    private Long        orderId;//订单ID
    private Long        splitOrderId;//子订单ID
    private Long        skuId;//商品ID
    private BigDecimal  payBackFee;//退款金额
    private String      reason;//申请退款原因
    private String      state;//状态
    private String      pgTradeNo;//支付流水号
    private String      pgCode;//支付返回码
    private String      pgMessage;//支付返回消息

    private Timestamp   createAt;//创建时间
    private Timestamp   updateAt;//更新时间
    private Integer     amount;//申请退款数量
    private String      refundImg;//退款上传图片
    private String      contactName;//联系人姓名
    private String      contactTel;//联系人电话
    private String      expressCompany;//快递公司名称
    private String      expressCompCode;//快递公司编码
    private String      expressNum;//快递编号
    private String      rejectReason;//客服拒绝退款原因

    private Long        userId;//用户ID

    private String      refundType;//退款类型，pin：拼购自动退款，receive：收货后申请退款，deliver：发货前退款


    public Refund() {
    }

    public Refund(Long id, Long orderId, Long splitOrderId, Long skuId, BigDecimal payBackFee, String reason, String state, String pgTradeNo, String pgCode, String pgMessage, Timestamp createAt, Timestamp updateAt, Integer amount, String refundImg, String contactName, String contactTel, String expressCompany, String expressCompCode, String expressNum, String rejectReason, Long userId, String refundType) {
        this.id = id;
        this.orderId = orderId;
        this.splitOrderId = splitOrderId;
        this.skuId = skuId;
        this.payBackFee = payBackFee;
        this.reason = reason;
        this.state = state;
        this.pgTradeNo = pgTradeNo;
        this.pgCode = pgCode;
        this.pgMessage = pgMessage;
        this.createAt = createAt;
        this.updateAt = updateAt;
        this.amount = amount;
        this.refundImg = refundImg;
        this.contactName = contactName;
        this.contactTel = contactTel;
        this.expressCompany = expressCompany;
        this.expressCompCode = expressCompCode;
        this.expressNum = expressNum;
        this.rejectReason = rejectReason;
        this.userId = userId;
        this.refundType = refundType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getSplitOrderId() {
        return splitOrderId;
    }

    public void setSplitOrderId(Long splitOrderId) {
        this.splitOrderId = splitOrderId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public BigDecimal getPayBackFee() {
        return payBackFee;
    }

    public void setPayBackFee(BigDecimal payBackFee) {
        this.payBackFee = payBackFee;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPgTradeNo() {
        return pgTradeNo;
    }

    public void setPgTradeNo(String pgTradeNo) {
        this.pgTradeNo = pgTradeNo;
    }

    public String getPgCode() {
        return pgCode;
    }

    public void setPgCode(String pgCode) {
        this.pgCode = pgCode;
    }

    public String getPgMessage() {
        return pgMessage;
    }

    public void setPgMessage(String pgMessage) {
        this.pgMessage = pgMessage;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public Timestamp getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Timestamp updateAt) {
        this.updateAt = updateAt;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getRefundImg() {
        return refundImg;
    }

    public void setRefundImg(String refundImg) {
        this.refundImg = refundImg;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactTel() {
        return contactTel;
    }

    public void setContactTel(String contactTel) {
        this.contactTel = contactTel;
    }

    public String getExpressCompany() {
        return expressCompany;
    }

    public void setExpressCompany(String expressCompany) {
        this.expressCompany = expressCompany;
    }

    public String getExpressCompCode() {
        return expressCompCode;
    }

    public void setExpressCompCode(String expressCompCode) {
        this.expressCompCode = expressCompCode;
    }

    public String getExpressNum() {
        return expressNum;
    }

    public void setExpressNum(String expressNum) {
        this.expressNum = expressNum;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRefundType() {
        return refundType;
    }

    public void setRefundType(String refundType) {
        this.refundType = refundType;
    }

    @Override
    public String toString() {
        return "Refund{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", splitOrderId=" + splitOrderId +
                ", skuId=" + skuId +
                ", payBackFee=" + payBackFee +
                ", reason='" + reason + '\'' +
                ", state='" + state + '\'' +
                ", pgTradeNo='" + pgTradeNo + '\'' +
                ", pgCode='" + pgCode + '\'' +
                ", pgMessage='" + pgMessage + '\'' +
                ", createAt=" + createAt +
                ", updateAt=" + updateAt +
                ", amount=" + amount +
                ", refundImg='" + refundImg + '\'' +
                ", contactName='" + contactName + '\'' +
                ", contactTel='" + contactTel + '\'' +
                ", expressCompany='" + expressCompany + '\'' +
                ", expressCompCode='" + expressCompCode + '\'' +
                ", expressNum='" + expressNum + '\'' +
                ", rejectReason='" + rejectReason + '\'' +
                ", userId=" + userId +
                ", refundType='" + refundType + '\'' +
                '}';
    }
}
