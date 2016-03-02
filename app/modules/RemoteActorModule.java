package modules;

import actor.MsgRecActor;
import actor.RecPushActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import controllers.PushCtrl;
import play.Logger;
import service.MsgService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by sibyl.sun on 16/3/1.
 */
@Singleton
public class RemoteActorModule {
//    @Inject
//    public ActorSystem system;
//    @Inject
//    private MsgService msgService;
//    @Inject
//    private PushCtrl pushCtrl;

    /**接收消息*/
    private  ActorRef msgRecActor;
    /**接收推送*/
    private ActorRef pushRecActor;
    @Inject
    public RemoteActorModule(ActorSystem system,MsgService msgService,PushCtrl pushCtrl) {
        msgRecActor=system.actorOf(Props.create(MsgRecActor.class,msgService), "msg");
        System.out.println("Started MsgRecActor,path="+msgRecActor.path());

        pushRecActor=system.actorOf(Props.create(RecPushActor.class,pushCtrl), "push");
        System.out.println("Started PushRecActor,path="+pushRecActor.path());
    }
}
