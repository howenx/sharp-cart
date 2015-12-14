package modules;

import actor.CalculateShipFeeActor;
import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;

/**
 * Akka Actor Module
 * Created by howen on 15/12/14.
 */
public class ActorModule extends AbstractModule implements AkkaGuiceSupport {
    @Override
    protected void configure() {
        bindActor(CalculateShipFeeActor.class, "shipFee-actor");
//        bindActorFactory(ConfiguredChildActor.class, ConfiguredChildActorProtocol.Factory.class);
    }
}
