package modules;

import com.google.inject.AbstractModule;
import middle.CartMid;
import middle.JDPayMid;
import play.Configuration;
import play.Environment;

/**
 * 启动leveldb
 * Created by howen on 16/2/19.
 */
public class LevelDBModule extends AbstractModule {

    private final Environment environment;
    private final Configuration configuration;

    public LevelDBModule(
            Environment environment,
            Configuration configuration) {
        this.environment = environment;
        this.configuration = configuration;
    }

    protected void configure() {
        bind(CartMid.class);
        bind(JDPayMid.class);
        bind(LevelFactory.class).asEagerSingleton();
        bind(NewScheduler.class);
    }
}
