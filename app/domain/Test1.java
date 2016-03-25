package domain;

import java.io.Serializable;

/**
 *
 * 测试
 * Created by howen on 16/3/24.
 */
public class Test1 implements Serializable{

    private String sign_type;
    private String trade_no;
    private String out_trade_no;
    private String out_trade_date;
    private String refund_amount;
    private String return_params;
    private String trade_pay_time;
    private String trade_subject;
    private String customer_no;
    private String trade_class;
    private String trade_pay_date;
    private String token;
    private String buyer_info;
    private String customer_type;
    private String customer_code;
    private String trade_currency;
    private String trade_amount;
    private String trade_status;
    private String sign_data;
    private String confirm_amount;
    private String notify_datetime;

    public Test1() {
    }

    public Test1(String sign_type, String trade_no, String out_trade_no, String out_trade_date, String refund_amount, String return_params, String trade_pay_time, String trade_subject, String customer_no, String trade_class, String trade_pay_date, String token, String buyer_info, String customer_type, String customer_code, String trade_currency, String trade_amount, String trade_status, String sign_data, String confirm_amount, String notify_datetime) {
        this.sign_type = sign_type;
        this.trade_no = trade_no;
        this.out_trade_no = out_trade_no;
        this.out_trade_date = out_trade_date;
        this.refund_amount = refund_amount;
        this.return_params = return_params;
        this.trade_pay_time = trade_pay_time;
        this.trade_subject = trade_subject;
        this.customer_no = customer_no;
        this.trade_class = trade_class;
        this.trade_pay_date = trade_pay_date;
        this.token = token;
        this.buyer_info = buyer_info;
        this.customer_type = customer_type;
        this.customer_code = customer_code;
        this.trade_currency = trade_currency;
        this.trade_amount = trade_amount;
        this.trade_status = trade_status;
        this.sign_data = sign_data;
        this.confirm_amount = confirm_amount;
        this.notify_datetime = notify_datetime;
    }

    public String getSign_type() {
        return sign_type;
    }

    public void setSign_type(String sign_type) {
        this.sign_type = sign_type;
    }

    public String getTrade_no() {
        return trade_no;
    }

    public void setTrade_no(String trade_no) {
        this.trade_no = trade_no;
    }

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public String getOut_trade_date() {
        return out_trade_date;
    }

    public void setOut_trade_date(String out_trade_date) {
        this.out_trade_date = out_trade_date;
    }

    public String getRefund_amount() {
        return refund_amount;
    }

    public void setRefund_amount(String refund_amount) {
        this.refund_amount = refund_amount;
    }

    public String getReturn_params() {
        return return_params;
    }

    public void setReturn_params(String return_params) {
        this.return_params = return_params;
    }

    public String getTrade_pay_time() {
        return trade_pay_time;
    }

    public void setTrade_pay_time(String trade_pay_time) {
        this.trade_pay_time = trade_pay_time;
    }

    public String getTrade_subject() {
        return trade_subject;
    }

    public void setTrade_subject(String trade_subject) {
        this.trade_subject = trade_subject;
    }

    public String getCustomer_no() {
        return customer_no;
    }

    public void setCustomer_no(String customer_no) {
        this.customer_no = customer_no;
    }

    public String getTrade_class() {
        return trade_class;
    }

    public void setTrade_class(String trade_class) {
        this.trade_class = trade_class;
    }

    public String getTrade_pay_date() {
        return trade_pay_date;
    }

    public void setTrade_pay_date(String trade_pay_date) {
        this.trade_pay_date = trade_pay_date;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getBuyer_info() {
        return buyer_info;
    }

    public void setBuyer_info(String buyer_info) {
        this.buyer_info = buyer_info;
    }

    public String getCustomer_type() {
        return customer_type;
    }

    public void setCustomer_type(String customer_type) {
        this.customer_type = customer_type;
    }

    public String getCustomer_code() {
        return customer_code;
    }

    public void setCustomer_code(String customer_code) {
        this.customer_code = customer_code;
    }

    public String getTrade_currency() {
        return trade_currency;
    }

    public void setTrade_currency(String trade_currency) {
        this.trade_currency = trade_currency;
    }

    public String getTrade_amount() {
        return trade_amount;
    }

    public void setTrade_amount(String trade_amount) {
        this.trade_amount = trade_amount;
    }

    public String getTrade_status() {
        return trade_status;
    }

    public void setTrade_status(String trade_status) {
        this.trade_status = trade_status;
    }

    public String getSign_data() {
        return sign_data;
    }

    public void setSign_data(String sign_data) {
        this.sign_data = sign_data;
    }

    public String getConfirm_amount() {
        return confirm_amount;
    }

    public void setConfirm_amount(String confirm_amount) {
        this.confirm_amount = confirm_amount;
    }

    public String getNotify_datetime() {
        return notify_datetime;
    }

    public void setNotify_datetime(String notify_datetime) {
        this.notify_datetime = notify_datetime;
    }


    @Override
    public String toString() {
        return "Test1{" +
                "sign_type='" + sign_type + '\'' +
                ", trade_no='" + trade_no + '\'' +
                ", out_trade_no='" + out_trade_no + '\'' +
                ", out_trade_date='" + out_trade_date + '\'' +
                ", refund_amount='" + refund_amount + '\'' +
                ", return_params='" + return_params + '\'' +
                ", trade_pay_time='" + trade_pay_time + '\'' +
                ", trade_subject='" + trade_subject + '\'' +
                ", customer_no='" + customer_no + '\'' +
                ", trade_class='" + trade_class + '\'' +
                ", trade_pay_date='" + trade_pay_date + '\'' +
                ", token='" + token + '\'' +
                ", buyer_info='" + buyer_info + '\'' +
                ", customer_type='" + customer_type + '\'' +
                ", customer_code='" + customer_code + '\'' +
                ", trade_currency='" + trade_currency + '\'' +
                ", trade_amount='" + trade_amount + '\'' +
                ", trade_status='" + trade_status + '\'' +
                ", sign_data='" + sign_data + '\'' +
                ", confirm_amount='" + confirm_amount + '\'' +
                ", notify_datetime='" + notify_datetime + '\'' +
                '}';
    }
}
