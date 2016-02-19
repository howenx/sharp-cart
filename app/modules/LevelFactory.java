package modules;

import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import play.Configuration;
import play.Environment;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

/**
 * 工厂类
 * Created by howen on 16/2/19.
 */
@Singleton
public class LevelFactory {


    private DB leveldb;

    @Inject
    public LevelFactory(
            Environment environment,
            Configuration configuration) throws IOException {
        Logger.error("id.server.url");
        File leveldbDir = new File(configuration.getString("akka.persistence.journal.leveldb.dir"));

        Iq80DBFactory factory = new Iq80DBFactory();
        leveldb = factory.open(leveldbDir, new Options().createIfMissing(true).compressionType(CompressionType.NONE));
    }

    public DB getLeveldb(){
        return leveldb;
    }
}
