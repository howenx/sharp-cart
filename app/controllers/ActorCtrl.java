package controllers;

import actor.MsgRecActor;
import actor.PushActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import play.mvc.Controller;

/**
 * Created by sibyl.sun on 16/2/26.
 */
public class ActorCtrl extends Controller{

    final static ActorSystem system = ActorSystem.create("ShoppingSystem",
            ConfigFactory.load(("shopping")));

    public void initActor(){
        //添加actor
        system.actorOf(Props.create(PushActor.class), "push");
        system.actorOf(Props.create(MsgRecActor.class),"msg");

    }


}
