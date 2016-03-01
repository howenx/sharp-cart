package modules;

import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import domain.Persist;
import org.iq80.leveldb.*;
import org.iq80.leveldb.impl.Iq80DBFactory;
import play.Configuration;
import play.Logger;
import play.api.Environment;
import util.SerializerJava;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static util.SerializerJava.deserializeAndCast;

/**
 * 工厂类
 * Created by howen on 16/2/19.
 */
@SuppressWarnings("unchecked")
@Singleton
public class LevelFactory {

    public Map<Object, Persist> map;//用于存储所有的persist对象

    public Map<Object, Cancellable> delMap;//用于存储所有的调用删除schedule Actor的Cancellable

    private DB db ;

    @Inject
    private ActorSystem system;

    @Inject
    public LevelFactory(
            Environment environment,
            Configuration configuration) throws IOException {
        map = new HashMap<>();
        delMap = new HashMap<>();
        File lock = new File(configuration.getString("leveldb.local.dir")+"/LOCK");
        if (lock.exists()) {
            boolean delete = lock.delete();
            Logger.info("启动删除Lock-->"+delete);
        }
        db  = new Iq80DBFactory().open(new File(configuration.getString("leveldb.local.dir")), new Options().createIfMissing(true));
    }

    //leveldb存操作
    public void put(Object key, Object value) throws Exception {
        WriteBatch batch = db.createWriteBatch();
        try {
            batch.put(SerializerJava.serializeObject(key), SerializerJava.serializeObject(value));
            db.write(batch);
        } finally {
            batch.close();
        }
    }

    //leveldb读操作
    public <T> T get(Object key) throws Exception {
        ReadOptions ro = new ReadOptions();
        ro.snapshot(db.getSnapshot());
        byte[] getval = db.get(SerializerJava.serializeObject(key),ro);
        ro.snapshot().close();
        if (getval != null) return (T) deserializeAndCast(getval);
        else return null;
    }

    //leveldb删除操作
    public void delete(Object key) throws Exception {
        WriteBatch batch = db.createWriteBatch();
        try {
            batch.delete(SerializerJava.serializeObject(key));
            db.write(batch);
        } finally {
            batch.close();
        }
    }

    //leveldb遍历操作
    public List<Persist> iterator() throws Exception {

        ReadOptions ro = new ReadOptions().snapshot(db.getSnapshot());
        DBIterator iterator = db.iterator(ro);
        List<Persist> persists = new ArrayList<>();
        try {
            for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                persists.add(deserializeAndCast(iterator.peekNext().getValue()));
            }
        } finally {
            ro.snapshot().close();
            iterator.close();
        }
        return persists;
    }
}