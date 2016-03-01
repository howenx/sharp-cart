package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import play.Logger;
import service.MsgService;

import javax.inject.Inject;

/**
 * 定期清理过期的全体系统消息以及用户已经删除的消息
 * Created by sibyl.sun on 16/2/25.
 */
public class SchedulerCleanMsgActor extends AbstractActor {
    @Inject
    public SchedulerCleanMsgActor(MsgService msgService){
        receive(ReceiveBuilder.match(Long.class,l->{
            //顺序不可换
            msgService.cleanMsg();  //定期清理过期的系统消息
            msgService.cleanMsgRec();//定期清理用户已经删除的消息
            //  context().system().scheduler().scheduleOnce(FiniteDuration.create(5, TimeUnit.MILLISECONDS),self(),settleVo,context().dispatcher(), ActorRef.noSender());
        }).matchAny(s -> {
            Logger.error("CancelOrderActor received messages not matched: {}", s.toString());
            unhandled(s);
        }).build());

    }
}
