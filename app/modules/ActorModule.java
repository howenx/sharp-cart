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
        bindActor(OrderAddressActor.class, "orderAddressActor");
        bindActor(OrderLineActor.class, "orderLineActor");
        bindActor(ClearCartActor.class,"clearCartActor");
        bindActor(PublicFreeShipActor.class,"publicFreeShipActor");
        bindActor(ReduceInvActor.class,"reduceInvActor");
        bindActor(CancelOrderActor.class,"cancelOrderActor");
        bindActor(PublicCouponActor.class,"publicCouponActor");
        bindActor(CustomStatus.class,"custom_status");
        bindActor(PushCustoms.class,"push_customs");
        bindActor(SchedulerCancelOrderActor.class,"schedulerCancelOrderActor");
        bindActor(UploadImagesActor.class,"uploadImagesActor");
        bindActor(PinFailActor.class, "pinFailActor");
        bindActor(PushActor.class,"pushActor");
        bindActor(RefundActor.class,"refundActor");
        bindActor(MsgRecActor.class,"msgActor");
        bindActor(SchedulerCleanMsgActor.class,"schedulerCleanMsgActor");
    }
}
