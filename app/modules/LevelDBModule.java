package modules;

import com.google.inject.AbstractModule;
import com.typesafe.config.ConfigFactory;
import middle.CartMid;
import middle.JDPayMid;
import middle.OrderMid;
import play.Logger;
import redis.clients.jedis.Jedis;
import util.*;

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
        bind(MnsInit.class).asEagerSingleton();
        bind(LogUtil.class).asEagerSingleton();
        bind(RedisPool.class).asEagerSingleton();
        bind(LeveldbLoad.class).asEagerSingleton();
    }
}
