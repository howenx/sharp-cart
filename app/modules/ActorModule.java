package modules;

import actor.*;
import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;

/**
 * Akka Actor Module
 * Created by howen on 15/12/14.
 */
public class ActorModule extends AbstractModule implements AkkaGuiceSupport {
    @Override
    protected void configure() {

        bindActor(OrderSplitActor.class, "subOrderActor");
        bindActor(OrderAddressActor.class, "orderShipActor");
        bindActor(OrderLineActor.class, "orderDetailActor");
        bindActor(ClearCartActor.class,"clearCartActor");
        bindActor(PublicFreeShipActor.class,"publicFreeShipActor");
        bindActor(ReduceInvActor.class,"reduceInvActor");
    }
}
