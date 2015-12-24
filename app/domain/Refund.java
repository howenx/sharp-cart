package domain;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退款VO
 * Created by howen on 15/12/22.
 */
public class Refund implements Serializable{

    private Long id;
    private Long orderId;
    private BigDecimal payBackFee;
    private String reason;
    private String state;
    private String pgTradeNo;
    private String pgCode;
    private String pgMessage;

    public Refund() {
    }

    public Refund(Long id, Long orderId, BigDecimal payBackFee, String reason, String state, String pgTradeNo, String pgCode, String pgMessage) {
        this.id = id;
        this.orderId = orderId;
        this.payBackFee = payBackFee;
        this.reason = reason;
        this.state = state;
        this.pgTradeNo = pgTradeNo;
        this.pgCode = pgCode;
        this.pgMessage = pgMessage;
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

    @Override
    public String toString() {
        return "Refund{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", payBackFee=" + payBackFee +
                ", reason='" + reason + '\'' +
                ", state='" + state + '\'' +
                ", pgTradeNo='" + pgTradeNo + '\'' +
                ", pgCode='" + pgCode + '\'' +
                ", pgMessage='" + pgMessage + '\'' +
                '}';
    }
}
