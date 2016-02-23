package modules;

import com.google.inject.AbstractModule;
import middle.CartMid;
import play.Configuration;
import play.Environment;
import play.Logger;

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
        Logger.error(configuration.getString("id.server.url"));
//        bind(LevelFactory.class).asEagerSingleton();
        bind(CartMid.class).asEagerSingleton();
    }
}
