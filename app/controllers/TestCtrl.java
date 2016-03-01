package controllers;

import actor.MsgRecActor;
import actor.PushActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import play.mvc.Controller;
import play.mvc.Result;
import service.MsgService;

import javax.inject.Inject;

/**
 * Created by sibyl.sun on 16/3/1.
 */
public class TestCtrl extends Controller {
    @Inject
    private ActorSystem system;

    private MsgService msgService;
    @Inject
    public TestCtrl(MsgService msgService){
        this.msgService=msgService;
    }

    public Result test(){

        ActorRef msgActor=system.actorOf(Props.create(MsgRecActor.class,msgService), "msg");
        System.out.println("Started MsgRecActor,path="+msgActor.path());

        ActorRef pushActor=system.actorOf(Props.create(PushActor.class), "push");
        System.out.println("Started PushActor,path="+pushActor.path());


        return ok("success");
    }
}
