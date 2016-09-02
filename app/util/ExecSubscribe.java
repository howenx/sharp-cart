package util;

import akka.actor.ActorRef;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import util.ConcurrentRedisListener;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 订阅
 * Created by howen on 16/6/22.
 */
@Singleton
public class ExecSubscribe {

    @Inject
    @Named("shoppingRunActor")
    private ActorRef webRunActor;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ExecSubscribe() {
        executor.submit(() -> {
            try {
                Jedis jedis = RedisPool.createPool().getResource();
                JedisPubSub listener = new ConcurrentRedisListener(webRunActor);
                jedis.psubscribe(listener, "style-shopping-version");
                return listener.isSubscribed();
            } catch (Exception ignore) {
                return false;
            }
        });
    }
}
