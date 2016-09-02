package util;

/**
 * 并发监听器
 * Created by hao on 16/5/9.
 */

import akka.actor.ActorRef;
import domain.VersionVo;
import play.Logger;
import play.libs.Json;
import redis.clients.jedis.JedisPubSub;

public class ConcurrentRedisListener extends JedisPubSub {

    private ActorRef webRunActor;

    public ConcurrentRedisListener(ActorRef webRunActor) {
        this.webRunActor = webRunActor;
    }


    @Override
    public void onMessage(String channel, String message) {
        try {
            if (message!=null){
                webRunActor.tell(Long.valueOf(message), ActorRef.noSender());
            }
            if (message.equalsIgnoreCase("quit")) {
                this.unsubscribe(channel);
            }

        } catch (Exception e) {
            Logger.error(e.toString());
            this.unsubscribe(channel);
        }
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        try {
            if (message!=null){
                webRunActor.tell(Long.valueOf(message), ActorRef.noSender());
            }
            if (message.equalsIgnoreCase("quit")) {
                this.punsubscribe(pattern);
            }
        } catch (Exception e) {
            Logger.error(e.toString());
            this.punsubscribe(pattern);
        }
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        System.out.println("  <<< 正在订阅(onSubscribe)< Channel:" + channel + " >subscribedChannels:" + subscribedChannels);
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        System.out.println("  <<< 正在取消订阅(onUnsubscribe)< Channel:" + channel + " >subscribedChannels:" + subscribedChannels);
    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        System.out.println("  <<< 正在P取消订阅(onPUnsubscribe)< Channel:" + pattern + " >subscribedChannels:" + subscribedChannels);
    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        System.out.println("  <<< 正在P订阅(onPSubscribe)< Channel:" + pattern + " >subscribedChannels:" + subscribedChannels);
    }
}