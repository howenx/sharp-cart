package domain;

import java.io.Serializable;

/**
 * 用于对客户端post或者get的返回消息代码
 * Created by howen on 15/11/19.
 */
public class Message implements Serializable{

    private String message;
    private Integer code;

    private Message() {
    }

    public Message(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", code=" + code +
                '}';
    }

    public enum ErrorCode{

        SUCCESS("成功", 200),
        FAILURE("失败", 400),
        FAILURE_REQUEST_ERROR("失败", 441),
        FAILURE_REQUEST_HANDLER_NOT_FOUND("失败", 442),
        FAILURE_BAD_REQUEST("失败", 443),

        ERROR("内部发生错误", 1001),
        SERVER_EXCEPTION("服务器异常", 1002),
        BAD_PARAMETER("参数不合法", 1003),
        BAD_USER_TOKEN("用户不存在", 1004),
        DATABASE_EXCEPTION("数据库操作异常", 1005),
        CREATE_ORDER_EXCEPTION("创建订单异常", 1006),
        REFUND_SUCCESS("退款成功",1007),
        REFUND_FAILED("退款失败",1008),
        ORDER_CANCEL_AUTO("未支付订单超过24小时,已被自动取消",1009),
        ORDER_DEL("交易未完成不能删除订单",1010),
        SLIDER_NULL_EXCEPTION("获取滚动条数据空", 1011),
        THEME_NULL_EXCEPTION("获取主题数据空", 1012),
        THEME_LIST_NULL_EXCEPTION("获取主题列表数据空", 1013),
        SKU_DETAIL_NULL_EXCEPTION("获取产品详情数据空", 1014),
        CART_LIST_NULL_EXCEPTION("获取购物车数据空", 1015),
        NOT_FOUND_SHIP_FEE_INFO("未找到邮费信息", 1016),


        SKU_AMOUNT_SHORTAGE("亲,此件商品库存不足了", 2001),
        SKU_INVALID("亲,您已经长时间未操作,此商品已经失效,建议您刷新购物车", 2002),



        PURCHASE_QUANTITY_LIMIT("亲,您购买数量超过我们的限制了", 3001),
        PURCHASE_QUANTITY_SUM_PRICE("海关规定单次报关物品价值不能超过1000元", 3002),
        

        PASSWORD_ERROR_TOO_MANY("密码错误次数过多", 4001),

        USERNAME_OR_PASSWORD_ERROR("用户名或密码有误", 4002),

        NOT_REGISTERED("用户未注册", 4003),

        INPUT_VERIFY_FAILED ("输入信息有误", 4004),

        PASSWORD_ERROR_LOCKED ("密码输错次数过多账户将被锁1小时,请1小时后再来登录", 4005),

        PASSWORD_ERROR_LOCKED_NOTIFY ("账户已被锁,请稍后再来登录", 4006),

        IMAGE_CODE_ERROR ("验证码校验失败", 4007),

        IMAGE_CODE_NULL ("验证码不能为空", 4008),

        USER_EXISTS ("此手机已经注册", 5001),

        SMS_CODE_ERROR ("短信验证码错误", 5002),

        PASSWORD_VERIFY_ERROR ("密码不符合规则", 5003),

        SECURITY_ERROR ("安全校验不通过", 5004),

        SEND_SMS_TOO_MANY ("发送验证码次数过多,请明天再试", 5005),

        FILE_TYPE_NOT_SUPPORTED("文件类型不支持",6001);

        // 成员变量
        private String name;
        private int index;

        // 构造方法
        private ErrorCode(String name, int index) {
            this.name = name;
            this.index = index;
        }

        // 普通方法
        public static String getName(int index) {
            for (ErrorCode c : ErrorCode.values()) {
                if (c.getIndex() == index) {
                    return c.name;
                }
            }
            return null;
        }
        // get set 方法
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }
}
