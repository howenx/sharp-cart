package domain;

import akka.actor.ActorRef;
import akka.actor.Cancellable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

/**
 * persist
 * Created by howen on 16/2/25.
 */
public class Persist implements Serializable{

    private byte[] cancellable;
    private String actorNm;
    private byte[] receiver;
    private Object message;
    private Date createAt;
    private Long interval;


    public Persist() {
    }

    public Persist(byte[] cancellable, String actorNm, byte[] receiver, Object message, Date createAt, Long interval) {
        this.cancellable = cancellable;
        this.actorNm = actorNm;
        this.receiver = receiver;
        this.message = message;
        this.createAt = createAt;
        this.interval = interval;
    }

    public byte[] getCancellable() {
        return cancellable;
    }

    public void setCancellable(byte[] cancellable) {
        this.cancellable = cancellable;
    }

    public String getActorNm() {
        return actorNm;
    }

    public void setActorNm(String actorNm) {
        this.actorNm = actorNm;
    }

    public byte[] getReceiver() {
        return receiver;
    }

    public void setReceiver(byte[] receiver) {
        this.receiver = receiver;
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

    public Long getInterval() {
        return interval;
    }

    public void setInterval(Long interval) {
        this.interval = interval;
    }

    @Override
    public String toString() {
        return "Persist{" +
                "cancellable=" + Arrays.toString(cancellable) +
                ", actorNm='" + actorNm + '\'' +
                ", receiver=" + Arrays.toString(receiver) +
                ", message=" + message +
                ", createAt=" + createAt +
                ", interval=" + interval +
                '}';
    }
}
