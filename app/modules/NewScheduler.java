package modules;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import com.google.common.base.Throwables;
import domain.Persist;
import play.Logger;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 可以自行持久化的AKKA scheduler
 * Created by hao on 16/2/27.
 */
public class NewScheduler {

    @Inject
    private ActorSystem system;

    @Inject
    private LevelFactory levelFactory;

    @Inject
    @Named("delScheduleActor")
    private ActorRef delScheduleActor;

    /**
     * 持久化只调用一次的schedule
     * @param delay 多久后执行
     * @param receiver actor
     * @param message 消息
     * @return CancelScheduler
     */
    public CancelScheduler scheduleOnce(FiniteDuration delay, ActorRef receiver, Object message) {

        Cancellable cancellable = system.scheduler().scheduleOnce(delay, receiver, message, system.dispatcher(), ActorRef.noSender());

        //调用delScheduleActor去在schedule执行完10s后删除已经持久化的schedule
        Cancellable del = system.scheduler().scheduleOnce(Duration.create(delay.toSeconds() + 5, TimeUnit.SECONDS), delScheduleActor, message, system.dispatcher(), ActorRef.noSender());

        try {
            if (levelFactory.map.containsKey(message)) {
                Persist p = levelFactory.map.get(message);
                p.getCancellable().cancel();
//                Logger.error("取消以前的所有的同一个message的schedule "+persist.getCancellable().cancel());
                levelFactory.map.remove(message);
            }
            if (levelFactory.get(message) != null) {
                levelFactory.delete(message);
            }
            if (levelFactory.delMap.containsKey(message)){
                Cancellable delCancellable = levelFactory.delMap.get(message);
                delCancellable.cancel();
//                Logger.error("用于删除schedule所启动的schedule "+delCancellable.cancel());
                levelFactory.delMap.remove(message);
            }

            Persist persist = new Persist();
            persist.setActorPath(receiver.path().toString());
            persist.setCancellable(cancellable);
            persist.setCreateAt(new Date());
            persist.setDelay(delay.toMillis());
            persist.setMessage(message);
            persist.setType("scheduleOnce");

            levelFactory.map.put(message, persist);//HashMap存储

            levelFactory.put(message, persist);//leveldb存储

            levelFactory.delMap.put(message,del);

            Logger.info("创建scheduleOnce: "+message.toString()+", 于"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(new Date().getTime()+delay.toMillis()))+" 时执行");

        } catch (Exception ex) {
            cancellable.cancel();
            del.cancel();
            ex.printStackTrace();
            Logger.error(Throwables.getStackTraceAsString(ex));
            return null;
        }

        return new CancelScheduler(cancellable, message, levelFactory);

    }


    public CancelScheduler schedule(FiniteDuration initialDelay,FiniteDuration delay, ActorRef receiver, Object message) {

        Cancellable cancellable = system.scheduler().schedule(initialDelay,delay, receiver, message, system.dispatcher(), ActorRef.noSender());

        try {
            if (levelFactory.map.containsKey(message)) {
                Persist p = levelFactory.map.get(message);
                p.getCancellable().cancel();
                levelFactory.map.remove(message);
            }
            if (levelFactory.get(message) != null) {
                levelFactory.delete(message);
            }
            if (levelFactory.delMap.containsKey(message)){
                Cancellable delCancellable = levelFactory.delMap.get(message);
                delCancellable.cancel();
                levelFactory.delMap.remove(message);
            }

            Persist persist = new Persist();
            persist.setActorPath(receiver.path().toString());
            persist.setCancellable(cancellable);
            persist.setCreateAt(new Date());
            persist.setDelay(delay.toMillis());
            persist.setMessage(message);
            persist.setType("schedule");
            persist.setInitialDelay(initialDelay.toMillis());

            levelFactory.map.put(message, persist);//HashMap存储

            levelFactory.put(message, persist);//leveldb存储

            Logger.info("创建schedule: "+message.toString()+", 每隔 "+delay.toHours()+" 小时执行一次");

        } catch (Exception ex) {
            cancellable.cancel();
            ex.printStackTrace();
            Logger.error(Throwables.getStackTraceAsString(ex));
            return null;
        }

        return new CancelScheduler(cancellable, message, levelFactory);

    }

}

class CancelScheduler {

    private Cancellable cancellable;
    private Object message;
    private LevelFactory levelFactory;


    public Boolean cancel() {


        try {
            if (levelFactory.map.containsKey(message)) {
                levelFactory.map.remove(message);
            }
            if (levelFactory.get(message) != null) {
                levelFactory.delete(message);
            }
            return cancellable.cancel();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    public Boolean isCancelled() {
        return cancellable.isCancelled();
    }

    public CancelScheduler() {
    }

    public CancelScheduler(Cancellable cancellable, Object message, LevelFactory levelFactory) {
        this.cancellable = cancellable;
        this.message = message;
        this.levelFactory = levelFactory;
    }

    public Cancellable getCancellable() {
        return cancellable;
    }

    public void setCancellable(Cancellable cancellable) {
        this.cancellable = cancellable;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public LevelFactory getLevelFactory() {
        return levelFactory;
    }

    public void setLevelFactory(LevelFactory levelFactory) {
        this.levelFactory = levelFactory;
    }

    @Override
    public String toString() {
        return "CancelScheduler{" +
                "cancellable=" + cancellable +
                ", message=" + message +
                ", levelFactory=" + levelFactory +
                '}';
    }
}