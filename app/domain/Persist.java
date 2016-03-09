package domain;

import akka.actor.Cancellable;

import java.io.Serializable;
import java.util.Date;

/**
 * persist
 * Created by howen on 16/2/25.
 */

public class Persist implements Serializable {

    static final long serialVersionUID = 51L;

    private String actorPath;//actor path "/user/schedulerCancelOrderActor"
    private Object message;//actor接收消息
    private Date createAt;//创建schedule时间
    private Long delay;//schedule多久后执行
    private transient Cancellable cancellable;//schedule返回对象

    private String type;//类型,schedule,scheduleOnce

    private Long initialDelay;//类型为schedule的初始化延迟

    public Persist() {
    }

    public Persist(String actorPath, Object message, Date createAt, Long delay, Cancellable cancellable, String type, Long initialDelay) {
        this.actorPath = actorPath;
        this.message = message;
        this.createAt = createAt;
        this.delay = delay;
        this.cancellable = cancellable;
        this.type = type;
        this.initialDelay = initialDelay;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(Long initialDelay) {
        this.initialDelay = initialDelay;
    }

    @Override
    public String toString() {
        return "Persist{" +
                "actorPath='" + actorPath + '\'' +
                ", message=" + message +
                ", createAt=" + createAt +
                ", delay=" + delay +
                ", cancellable=" + cancellable +
                ", type='" + type + '\'' +
                ", initialDelay=" + initialDelay +
                '}';
    }
}
