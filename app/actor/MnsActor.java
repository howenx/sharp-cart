package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.client.CloudTopic;
import com.aliyun.mns.common.ClientException;
import com.aliyun.mns.common.ServiceException;
import com.aliyun.mns.model.*;
import play.Logger;
import play.libs.Json;
import util.MnsInit;

import javax.inject.Inject;

/**
 * 用于produce消息到阿里云mns的Actor
 * Created by howen on 15/12/24.
 */
public class MnsActor extends AbstractActor {

    @Inject
    public MnsActor() {
        receive(ReceiveBuilder.match(Object.class, event -> {

            if (event instanceof ILoggingEvent) {
                ((ILoggingEvent) event).getMDCPropertyMap().put("projectId", "style-shopping");
                System.out.println("日志Json格式---->" + Json.toJson(event).toString());
                createQueue();
//
//                try {
//
//                    CloudTopic topic = MnsInit.mnsClient.getTopicRef("kakao");// replace with your queue name
//
//                    System.out.println("topic url: " + topic.getTopicURL());
//
//                    System.out.println(topic.getTopicURL());
//                    TopicMessage message = new Base64TopicMessage();
//                    message.setMessageBody(Json.toJson(event).toString()); // use your own message body here
//                    TopicMessage putMsg = topic.publishMessage(message);
//                    System.out.println("返回消息ID: " + putMsg.getMessageId());
//
//                } catch (ClientException ce) {
//                    System.err.println("请求连接异常," + ce.getMessage());
//                    ce.printStackTrace();
//                } catch (ServiceException se) {
//                    if (se.getErrorCode().equals("TopicNotExist") || se.getErrorCode().equals("InvalidRequestURL")) {
//                        createQueue();
//                        System.err.println("主题不存在");
//                    } else if (se.getErrorCode().equals("TimeExpired")) {
//                        System.err.println("The request is time expired. Please check your local machine timeclock");
//                    }
//                    System.err.println("尼玛的错误--->"+se.getErrorCode());
//                    se.printStackTrace();
//                } catch (Exception e) {
//                    System.err.println("Unknown exception happened!");
//                    e.printStackTrace();
//                }
            }
        }).matchAny(s -> {
            Logger.error("MnsActor received messages not matched: {}", s.toString());
            unhandled(s);
        }).build());
    }

    //Create Queue
    private void createQueue() {
        try {
//            QueueMeta qMeta = new QueueMeta();
//            qMeta.setQueueName("cloud-queue-demo");
//            qMeta.setPollingWaitSeconds(30);//use long polling when queue is empty.
//            qMeta.setLoggingEnabled(true);

            TopicMeta meta1 = new TopicMeta();
            meta1.setTopicName("style-shopping");
            CloudTopic cQueue = MnsInit.mnsClient.createTopic(meta1);
            System.out.println("创建的topic的请求地址: " + cQueue.getTopicURL());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("create topic error, " + e.getMessage());
        }
    }
}
