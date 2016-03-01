package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
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
    public MsgRecActor(MsgService msgService){
        receive(ReceiveBuilder.match(Object.class,msg->{
           // Logger.info("========MsgRecActor======");
            if (msg instanceof Msg){ //收到全体系统消息
                Logger.info("=====收到全体系统消息"+(Msg)msg);
                
               // msgService.insertMsg((Msg)msg);
            }else if(msg instanceof MsgRec){ //收到指定用户的消息
                Logger.info("=====收到指定用户的消息"+(MsgRec)msg);
                msgService.insertMsgRec((MsgRec)msg);
            }

        }).matchAny(s -> Logger.error("MsgRecActor received messages not matched: {}", s.toString())).build());

    }
}
