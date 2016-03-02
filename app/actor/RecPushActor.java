package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import controllers.PushCtrl;
import domain.PushMsg;
import play.Logger;

import javax.inject.Inject;

/**
 * 收到推送消息
 * Created by sibyl.sun on 16/3/1.
 */
public class RecPushActor extends AbstractActor {

    @Inject
    public RecPushActor(PushCtrl pushCtrl){
        receive(ReceiveBuilder.match(PushMsg.class, pushMsg -> {
            Logger.info("收到推送消息"+pushMsg);
            if("alias".equals(pushMsg.getAudience()))
            {
                pushCtrl.send_push_android_and_ios_alias(pushMsg.getAlert(),pushMsg.getTitle(),pushCtrl.getPushExtras(pushMsg.getUrl(),pushMsg.getTargetType()),
                        pushMsg.getAliasOrTag());
            }else if("tag".equals(pushMsg.getAudience()))
            {
                pushCtrl.send_push_android_and_ios_alias(pushMsg.getAlert(),pushMsg.getTitle(),pushCtrl.getPushExtras(pushMsg.getUrl(),pushMsg.getTargetType()),
                        pushMsg.getAliasOrTag());
            }else if("all".equals(pushMsg.getAudience()))
            {
                pushCtrl.send_push_android_and_ios_all(pushMsg.getAlert(),pushMsg.getTitle(),pushCtrl.getPushExtras(pushMsg.getUrl(),pushMsg.getTargetType()));
            }

        }).matchAny(s -> Logger.error("PushActor received messages not matched: {}", s.toString())).build());
    }
}
