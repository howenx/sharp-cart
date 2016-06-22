package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import play.Logger;
import play.libs.Json;
import redis.clients.jedis.Jedis;
import util.RedisPool;

import javax.inject.Inject;

import static util.SysParCom.REDIS_CHANNEL;

/**
 * 用于produce消息到阿里云mns的Actor
 * Created by howen on 15/12/24.
 */
public class MnsActor extends AbstractActor {

    public MnsActor() {
        receive(ReceiveBuilder.match(Object.class, event -> {
            try (Jedis jedis = RedisPool.createPool().getResource()){
                if (event instanceof ILoggingEvent) {
                    ((ILoggingEvent) event).getMDCPropertyMap().put("projectId", "style-shopping");
                    jedis.publish(REDIS_CHANNEL, Json.mapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false).valueToTree(event).toString());
                }
            }
        }).matchAny(s -> {
            Logger.error("MnsActor received messages not matched: {}", s.toString());
            unhandled(s);
        }).build());
    }
}
