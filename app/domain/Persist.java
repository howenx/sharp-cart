package domain;

import akka.actor.Cancellable;

import java.io.Serializable;
import java.util.Date;

/**
 * persist
 * Created by howen on 16/2/25.
 */

public class Persist implements Serializable {

    static final long serialVersionUID = 48L;

    private String actorPath;//actor path "/user/schedulerCancelOrderActor"
    private Object message;//actor接收消息
    private Date createAt;//创建schedule时间
    private Long delay;//schedule多久后执行
    private transient Cancellable cancellable;//schedule返回对象

    public Persist() {
    }

    public Persist(String actorPath, Object message, Date createAt, Long delay, Cancellable cancellable) {
        this.actorPath = actorPath;
        this.message = message;
        this.createAt = createAt;
        this.delay = delay;
        this.cancellable = cancellable;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getActorPath() {
        return actorPath;
    }

    public void setActorPath(String actorPath) {
        this.actorPath = actorPath;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public Cancellable getCancellable() {
        return cancellable;
    }

    public void setCancellable(Cancellable cancellable) {
        this.cancellable = cancellable;
    }

    @Override
    public String toString() {
        return "Persist{" +
                "actorPath='" + actorPath + '\'' +
                ", message=" + message +
                ", createAt=" + createAt +
                ", delay=" + delay +
                ", cancellable=" + cancellable +
                '}';
    }
}
