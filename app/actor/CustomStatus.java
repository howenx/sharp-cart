package actor;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.Logger;
import play.Play;
import play.libs.Akka;
import play.libs.ws.WS;
import scala.concurrent.duration.Duration;
import util.Crypto;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by handy on 15/12/19.
 * kakao china
 */
public class CustomStatus extends UntypedActor{

    /**
     *
     * params 里面应该包含如下的参数:
     * 订单流水号: out_trade_no
     * 子单流水号: sub_out_trade_no
     * 子单编号: sub_order_no
     * @param message
     * @throws Exception
     */
    @Override
    public void onReceive(Object message) throws Exception {

        final Map<String, String> params = (Map<String, String>) message;
        String query_url = Play.application().configuration().getString("jd_query_url");

        params.put("customer_no",Play.application().configuration().getString("jd_seller"));
        //params.put("KEY",Play.application().configuration().getString("jd_secret"));
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss");
        String req_date = f.print(new DateTime());
        params.put("request_datetime",req_date);
        params.put("sign_type","MD5");

        params.put("sign_data", Crypto.create_sign(params,Play.application().configuration().getString("jd_secret")));

        StringBuilder sb = new StringBuilder();
        params.forEach((k,v)->{
            if(sb.length()> 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",k,v));
        });

        WS.url(query_url).setContentType("application/x-www-form-urlencoded").post(sb.toString()).map(wsResponse -> {

            Logger.debug("" + wsResponse.getStatus() + " : " + wsResponse.asJson());
            JsonNode ret = wsResponse.asJson();

            if(ret.get("is_success").textValue().equalsIgnoreCase("Y")) {

                String custom_push_status = ret.get("custom_push_status").textValue();
                if(custom_push_status.equalsIgnoreCase("SUCCESS")) {
                    //报关成功
                    //TODO 更新报关状态

                    getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
                }
                else if (custom_push_status.equalsIgnoreCase("SAVE") || custom_push_status.equalsIgnoreCase("SEND")) {

                    // 30 分钟调度一次,直到成功
                    Akka.system().scheduler().scheduleOnce(Duration.create(30, TimeUnit.MINUTES), getSelf(), message, Akka.system().dispatcher(), ActorRef.noSender());
                }
                else {
                    //status is FAIL EXCEPTION
                    getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
                }

            }

            else {
                //杀掉该actor
                getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
            }

            return null;

        });

    }

    public void preStart() {
        Logger.debug("query customs actor start");
    }

    public void postStop() {
        Logger.debug("query customs actor stop");

    }
}
