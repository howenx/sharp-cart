package modules;

import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import play.Configuration;
import play.api.Environment;

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
//        Logger.error("id.server.url");
        File leveldbDir = new File(configuration.getString("leveldb.persistence.local.dir"));

        Iq80DBFactory factory = new Iq80DBFactory();
        leveldb = factory.open(leveldbDir, new Options().createIfMissing(true).compressionType(CompressionType.NONE));
    }

    public DB getLeveldb(){
//        WriteBatch batch2 =leveldb.createWriteBatch();
//
//        try {
//            batch2.put("90001".getBytes(),"你妈带来的".getBytes(Charset.forName("utf-8")));
//            batch2.put("30001".getBytes(),"奋斗到底".getBytes(Charset.forName("utf-8")));
//            leveldb.write(batch2, new WriteOptions().sync(Play.application().configuration().getBoolean("akka.persistence.journal.leveldb.fsync")).snapshot(false));
//        } finally {
//            try {
//                batch2.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        return leveldb;
    }
}