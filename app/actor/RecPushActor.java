package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import controllers.PushCtrl;
import domain.PushMsg;
import play.Logger;
import util.SysParCom;

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
            Long timeToLive= SysParCom.PUSH_TIME_TO_LIVE;
            if(null!=pushMsg.getTimeToLive()){
                timeToLive=pushMsg.getTimeToLive();
            }
            if("alias".equals(pushMsg.getAudience()))
            {
                pushCtrl.send_push_android_and_ios_alias(pushMsg.getAlert(),pushMsg.getTitle(),timeToLive,pushCtrl.getPushExtras(pushMsg.getUrl(),pushMsg.getTargetType()),
                        pushMsg.getAliasOrTag());
            }else if("tag".equals(pushMsg.getAudience()))
            {
                pushCtrl.send_push_android_and_ios_tag(pushMsg.getAlert(),pushMsg.getTitle(),timeToLive,pushCtrl.getPushExtras(pushMsg.getUrl(),pushMsg.getTargetType()),
                        pushMsg.getAliasOrTag());
            }else if("all".equals(pushMsg.getAudience()))
            {
                pushCtrl.send_push_android_and_ios_all(pushMsg.getAlert(),pushMsg.getTitle(),timeToLive,pushCtrl.getPushExtras(pushMsg.getUrl(),pushMsg.getTargetType()));
            }

        }).matchAny(s -> Logger.error("PushActor received messages not matched: {}", s.toString())).build());
    }
}
