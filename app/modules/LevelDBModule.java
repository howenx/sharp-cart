package modules;

import com.google.inject.AbstractModule;
import middle.CartMid;
import middle.JDPayMid;
import middle.OrderMid;

/**
 * 启动leveldb
 * Created by howen on 16/2/19.
 */
public class LevelDBModule extends AbstractModule {

    protected void configure() {
        bind(CartMid.class);
        bind(JDPayMid.class);
        bind(LevelFactory.class).asEagerSingleton();
        bind(NewScheduler.class);
        bind(OrderMid.class);
        bind(SysParCom.class).asEagerSingleton();
        bind(RemoteActorModule.class).asEagerSingleton();
        bind(AppOnStart.class).asEagerSingleton();
    }
}
