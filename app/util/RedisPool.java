package util;

import com.typesafe.config.ConfigFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


/**
 * redis实例化
 * Created by howen on 16/4/28.
 */
public class RedisPool {

    private static final String REDIS_URL = ConfigFactory.defaultApplication().getString("redis.host");
    private static final Integer REDIS_PORT = ConfigFactory.defaultApplication().getInt("redis.port");
    private static final String REDIS_PASSWORD = ConfigFactory.defaultApplication().getString("redis.password");

    public static Jedis create() {

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(200);
        config.setMaxTotal(300);
        config.setTestOnBorrow(false);
        config.setTestOnReturn(false);
        JedisPool pool = new JedisPool(config, REDIS_URL, REDIS_PORT, 3000, REDIS_PASSWORD);
        return pool.getResource();
    }
}
