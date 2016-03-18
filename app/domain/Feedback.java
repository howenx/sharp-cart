package domain;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 意见反馈
 * Created by sibyl.sun on 16/3/18.
 */
public class Feedback implements Serializable {
    private Long id;
    private Long userId;  //用户id
    private String content; //内容
    private Timestamp createAt; //创建时间

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }
}
