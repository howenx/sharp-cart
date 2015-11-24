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
        ERROR("内部发生错误", 1001),
        SERVER_EXCEPTION("服务器异常", 1002),
        BAD_PARAMETER("参数不合法", 1003),
        BAD_USER_TOKEN("用户不存在", 1004),
        DATABASE_EXCEPTION("数据库操作异常", 1005);

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
