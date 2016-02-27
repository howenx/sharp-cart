package actor;

import akka.actor.AbstractActor;
import akka.actor.Cancellable;
import akka.japi.pf.ReceiveBuilder;
import domain.Persist;
import modules.LevelFactory;
import play.Logger;

import javax.inject.Inject;

/**
 * 用于删除持久化的schedule
 * Created by howen on 15/12/24.
 */
public class DelScheduleActor extends AbstractActor {

    @Inject
    public DelScheduleActor(LevelFactory levelFactory) {
        receive(ReceiveBuilder.match(Object.class, message -> {
            if (levelFactory.map.containsKey(message)) {
                Persist persist = levelFactory.map.get(message);
                if (!persist.getCancellable().isCancelled()) persist.getCancellable().cancel();
                levelFactory.map.remove(message);
            }
            if (levelFactory.get(message) != null) {
                levelFactory.delete(message);
            }
            if (levelFactory.delMap.containsKey(message)){
                Cancellable delCancellable = levelFactory.delMap.get(message);
                if (!delCancellable.isCancelled()) delCancellable.cancel();
                levelFactory.delMap.remove(message);
            }
            Logger.info("删除的持久化schedule---> " + message.toString());
        }).matchAny(s -> {
            Logger.error("DelScheduleActor received messages not matched: {}", s.toString());
            unhandled(s);
        }).build());
    }
}
