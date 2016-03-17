package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import service.MsgService;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by sibyl.sun on 16/3/1.
 */
public class TestCtrl extends Controller {
    @Inject
    private ActorSystem system;

    private MsgService msgService;
    @Inject
    private PushCtrl pushCtrl;

    @Inject
    @Named("pinFailActor")
    private ActorRef pinFailActor;
    @Inject
    public TestCtrl(MsgService msgService){
        this.msgService=msgService;
    }

    public Result test(){

//        ActorRef msgActor=system.actorOf(Props.create(MsgRecActor.class,msgService), "msg");
//        System.out.println("Started MsgRecActor,path="+msgActor.path());
//
//        ActorRef pushRecActor=system.actorOf(Props.create(RecPushActor.class,pushCtrl), "push");
//        System.out.println("Started PushActor,path="+pushRecActor.path());

        Logger.error("路径:"+pinFailActor.path());
        return ok("success");
    }
}
