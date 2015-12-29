package actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;

/**
 * Terminator Actor
 * Created by howen on 15/12/24.
 */
@SuppressWarnings("unchecked")
public class TerminatorActor extends AbstractLoggingActor {

    private final ActorRef ref;

    public TerminatorActor(ActorRef ref) {
        this.ref = ref;
        getContext().watch(ref);
        receive(ReceiveBuilder.
                match(TerminatorActor.class, t -> {
                    log().info("{} has terminated, shutting down system", ref.path());
                    context().stop(self());
                }).build());
    }
}