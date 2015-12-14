package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;

/**
 * 计算邮费Actor
 * Created by howen on 15/12/14.
 */
public class CalculateShipFeeActor extends AbstractActor {

        public static enum Msg {
            GREET, DONE;
        }

        public CalculateShipFeeActor() {
            receive(ReceiveBuilder.
                    matchEquals(Msg.GREET, m -> {
                        System.out.println("Hello World!");
                        sender().tell(Msg.DONE, self());
                    }).build());
        }

}
