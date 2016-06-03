package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import common.MsgTypeEnum;
import controllers.MsgCtrl;
import domain.Msg;
import domain.MsgRec;
import play.Logger;
import service.MsgService;

import javax.inject.Inject;

/**
 * 收到消息
 * Created by sibyl.sun on 16/2/26.
 */
public class MsgRecActor extends AbstractActor {
    @Inject
    private MsgCtrl msgCtrl;
    @Inject
    public MsgRecActor(MsgService msgService){
        receive(ReceiveBuilder.match(Object.class,msg->{
            if (msg instanceof Msg){ //收到全体系统消息
                Msg m=(Msg)msg;
                Logger.info("=====收到全体系统消息"+(Msg)msg);
                msgCtrl.addSysMsg(MsgTypeEnum.getMsgTypeEnum(m.getMsgType()),m.getMsgTitle(),m.getMsgContent(),m.getMsgImg(),m.getMsgUrl(),m.getTargetType(),m.getEndAt());
            }else if(msg instanceof MsgRec){ //收到指定用户的消息
                Logger.info("=====收到指定用户的消息"+(MsgRec)msg);
                MsgRec m=(MsgRec)msg;
                msgCtrl.addMsgRec(m.getUserId(),MsgTypeEnum.getMsgTypeEnum(m.getMsgType()),m.getMsgTitle(),m.getMsgContent(),m.getMsgImg(),m.getMsgUrl(),m.getTargetType());
            }

        }).matchAny(s -> Logger.error("MsgRecActor received messages not matched: {}", s.toString())).build());

    }
}
