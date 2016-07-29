package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import cn.jpush.api.JPushClient;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.PushPayload;
import com.typesafe.config.ConfigFactory;
import play.Logger;
import util.SysParCom;

import javax.inject.Inject;

/**
 * 推送消息
 * Created by sibyl.sun on 16/2/22.
 */
public class PushActor extends AbstractActor {

//    //appKey:注册应用的应用Key
//    private static final String appKey = "a81748f2ead4ab0faef89329";
//    // masterSecret：注册应用的主密码,即API 主密码
//    private static final String masterSecret = "1bd35ab27b1530d417afb1b9";

    private static JPushClient jpushClient=new JPushClient(ConfigFactory.defaultApplication().getString("push.app.key"), ConfigFactory.defaultApplication().getString("push.master.secret")) ;

    @Inject
    public PushActor(){
        receive(ReceiveBuilder.match(PushPayload.class,pushPayload -> {
            try {


                PushResult result = jpushClient.sendPush(pushPayload);
                Logger.info("Got result - " + result);

            } catch (APIConnectionException e) {
                Logger.error("Connection error. Should retry later. ", e);

            } catch (APIRequestException e) {
                Logger.error("Error response from JPush server. Should review and fix it. ", e);
                Logger.info("HTTP Status: " + e.getStatus());
                Logger.info("Error Code: " + e.getErrorCode());
                Logger.info("Error Message: " + e.getErrorMessage());
                Logger.info("Msg ID: " + e.getMsgId());
            }

        }).matchAny(s -> Logger.error("PushActor received messages not matched: {}", s.toString())).build());
    }

}
