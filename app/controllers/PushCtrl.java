package controllers;


import akka.actor.ActorRef;
import cn.jpush.api.JPushClient;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.SMS;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.IosAlert;
import cn.jpush.api.push.model.notification.Notification;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * 推送消息
 * Created by sibyl.sun on 16/2/19.
 */
public class PushCtrl extends Controller {

//    //appKey:注册应用的应用Key
//    private static final String appKey = "a81748f2ead4ab0faef89329";
//    // masterSecret：注册应用的主密码,即API 主密码
//    private static final String masterSecret = "1bd35ab27b1530d417afb1b9";
//
//    private static JPushClient jpushClient=new JPushClient(masterSecret, appKey) ;

    private ActorRef pushActor;

    @Inject
    public PushCtrl(@Named("pushActor") ActorRef pushActor){
        this.pushActor=pushActor;

    }

    /***
     * 发送推送数据
     * @param payload PushPayload推送数据
     * @return
     */
    public  void sendPush(PushPayload payload){
//        try {
//
//            PushResult result = jpushClient.sendPush(payload);
//            Logger.info("Got result - " + result);
//            return result;
//
//        } catch (APIConnectionException e) {
//            Logger.error("Connection error. Should retry later. ", e);
//
//        } catch (APIRequestException e) {
//            Logger.error("Error response from JPush server. Should review and fix it. ", e);
//            Logger.info("HTTP Status: " + e.getStatus());
//            Logger.info("Error Code: " + e.getErrorCode());
//            Logger.info("Error Message: " + e.getErrorMessage());
//            Logger.info("Msg ID: " + e.getMsgId());
//        }
//        return null;
        pushActor.tell(payload,null);
    }

    public Result testPush(){
        send_push_all("test only content");
        send_push_android_all("test title content url","hmm title",getPushExtras("http://172.28.3.78:9001/comm/detail/888301/111324",""),60);
        return ok("success");
    }

    /***
     * 推送的额外字段
     * @param url
     * @param targetType
     * @return
     */
    public Map<String, String> getPushExtras(String url,String targetType){
        Map<String, String> extras=new HashMap<String, String>();
        extras.put("url", url);
        extras.put("targetType", targetType);
        return extras;
    }

    /***
     * 向所有人发送推送
     * @param alert
     */
    public void send_push_all(String alert){
        sendPush(PushPayload.alertAll(alert));

    }

    /***
     * android 所有人 发送消息
     * @param alter  推送内容(必填)
     * @param title  推送标题
     * @param extras 额外字段  url和targetType
     * @param timeToLive 离线消息保留时长(秒) ,走默认填 -1
     *                   推送当前用户不在线时，为该用户保留多长时间的离线消息，以便其上线时再次推送。默认 86400 （1 天），最长 10 天。设置为 0 表示不保留离线消息，只有推送当前在线的用户可以收到。
     */
    public void send_push_android_all(String alter,String title,Map<String, String> extras,long timeToLive){
        PushPayload pushPayload=PushPayload.newBuilder()
                .setPlatform(Platform.android())
                .setAudience(Audience.all())
                .setNotification(Notification.android(alter, title, extras)).build();
        if(timeToLive>=0){
            pushPayload.resetOptionsTimeToLive(timeToLive);
        }

        sendPush(pushPayload);
    }
    /***
     * ios  所有人     发送消息
     * @param alter  推送内容(必填)
     * @param extras 额外字段
     * @param timeToLive 离线消息保留时长(秒) ,走默认填 -1
     *                   推送当前用户不在线时，为该用户保留多长时间的离线消息，以便其上线时再次推送。默认 86400 （1 天），最长 10 天。设置为 0 表示不保留离线消息，只有推送当前在线的用户可以收到。
     */
    public void send_push_ios_all(String alter,Map<String, String> extras,long timeToLive){
        PushPayload pushPayload=PushPayload.newBuilder()
                .setPlatform(Platform.ios())
                .setAudience(Audience.all())
                .setNotification(Notification.ios(alter,extras)).build();
        if(timeToLive>=0){
            pushPayload.resetOptionsTimeToLive(timeToLive);
        }
        sendPush(pushPayload);
    }


    /***
     * android tag 发送消息
     * @param alter  推送内容(必填)
     * @param title  推送标题
     * @param extras 额外字段
     */
    public void send_push_android_tag(String alter,String title,Map<String, String> extras,String...tagValue){
        PushPayload pushPayload=PushPayload.newBuilder()
                .setPlatform(Platform.android())
                .setAudience(Audience.tag(tagValue))
                .setNotification(Notification.android(alter, title, extras)).build();
        sendPush(pushPayload);
    }

    /***
     * ios  tag      发送消息
     * @param alter  推送内容(必填)
     * @param extras 额外字段
     */
    public void send_push_ios_tag(String alter,Map<String, String> extras,String...tagValue){
        PushPayload pushPayload=PushPayload.newBuilder()
                .setPlatform(Platform.ios())
                .setAudience(Audience.tag(tagValue))
                .setNotification(Notification.ios(alter,extras)).build();
        sendPush(pushPayload);
    }

    /***
     * android alias 发送消息
     * @param alter  推送内容(必填)
     * @param title  推送标题
     * @param extras 额外字段
     */
    public void send_push_android_alias(String alter,String title,Map<String, String> extras,String...alias){
        PushPayload pushPayload=PushPayload.newBuilder()
                .setPlatform(Platform.android())
                .setAudience(Audience.alias(alias))
                .setNotification(Notification.android(alter, title, extras)).build();
        sendPush(pushPayload);
    }

    /***
     * ios  alias    发送消息
     * @param alter  推送内容(必填)
     * @param extras 额外字段
     */
    public void send_push_ios_alias(String alter,Map<String, String> extras,String...alias){
        PushPayload pushPayload=PushPayload.newBuilder()
                .setPlatform(Platform.ios())
                .setAudience(Audience.alias(alias))
                .setNotification(Notification.ios(alter,extras)).build();
        sendPush(pushPayload);
    }

    /**
     * android registration
     * @param alter
     * @param title
     * @param extras
     * @param registrationId
     */
    public void send_push_android_registration_id(String alter,String title,Map<String, String> extras,String...registrationId){
        PushPayload pushPayload=PushPayload.newBuilder()
                .setPlatform(Platform.android())
                .setAudience(Audience.registrationId(registrationId))
                .setNotification(Notification.android(alter,title,extras)).build();

    }
    /**
     * ios registration
     * @param alter
     * @param extras
     * @param registrationId
     */
    public  void send_push_ios_registration_id(String alter,Map<String, String> extras,String...registrationId){
        PushPayload pushPayload=PushPayload.newBuilder()
                .setPlatform(Platform.ios())
                .setAudience(Audience.registrationId(registrationId))
                .setNotification(Notification.ios(alter,extras)).build();

    }
//
//
//
//
//
//    /***
//     * 所有平台 指定别名alias 的人发送推送
//     * @param alter  内容
//     * @param alias  用别名来标识一个用户
//     * @return
//     */
//    public  PushPayload buildPushObject_all_alias_alert(String alter,String... alias) {
//        return PushPayload.newBuilder()
//                .setPlatform(Platform.all())
//                .setAudience(Audience.alias(alias))
//                .setNotification(Notification.alert(alter))
//                .build();
//    }

    /***
     *             PushCtrl.sendPush(PushCtrl.buildPushObject_all_all_alert("buildPushObject_all_all_alert"));
     PushCtrl.sendPush(PushCtrl.buildPushObject_all_alias_alert("buildPushObject_all_alias_alert","song"));
     Map<String, String> extras=new HashMap<String, String>();
     extras.put("newsid", "321");
     extras.put("url", "http://172.28.3.78:9001/comm/detail/888301/111324");
     extras.put("targetType", "B");
     PushCtrl.sendPush(PushCtrl.buildPushObject_android_tag_alertWithTitle("buildPushObject_android_tag_alertWithTitle","title",extras,"song"));
     */
//    public static void testSendIosAlert() {
//        JPushClient jpushClient = new JPushClient(masterSecret, appKey);
//
//        IosAlert alert = IosAlert.newBuilder()
//                .setTitleAndBody("test alert", "test ios alert json")
//                .setActionLocKey("PLAY")
//                .build();
//        try {
//            PushResult result = jpushClient.sendIosNotificationWithAlias(alert, new HashMap<String, String>(), "alias1");
//            Logger.info("Got result - " + result);
//        } catch (APIConnectionException e) {
//            Logger.error("Connection error. Should retry later. ", e);
//        } catch (APIRequestException e) {
//            Logger.error("Error response from JPush server. Should review and fix it. ", e);
//            Logger.info("HTTP Status: " + e.getStatus());
//            Logger.info("Error Code: " + e.getErrorCode());
//            Logger.info("Error Message: " + e.getErrorMessage());
//        }
//    }
//
//    public static void testSendWithSMS() {
//        JPushClient jpushClient = new JPushClient(masterSecret, appKey);
//        try {
//            SMS sms = SMS.content("Test SMS", 10);
//            PushResult result = jpushClient.sendAndroidMessageWithAlias("Test SMS", "test sms", sms, "alias1");
//            Logger.info("Got result - " + result);
//        } catch (APIConnectionException e) {
//            Logger.error("Connection error. Should retry later. ", e);
//        } catch (APIRequestException e) {
//            Logger.error("Error response from JPush server. Should review and fix it. ", e);
//            Logger.info("HTTP Status: " + e.getStatus());
//            Logger.info("Error Code: " + e.getErrorCode());
//            Logger.info("Error Message: " + e.getErrorMessage());
//        }
//    }




}
