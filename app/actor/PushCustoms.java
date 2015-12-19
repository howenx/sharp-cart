package actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.Logger;
import play.Play;
import play.libs.Akka;
import play.libs.ws.WS;
import scala.concurrent.duration.Duration;
import util.Crypto;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by handy on 15/12/19.
 * kakao china
 */
@SuppressWarnings("unchecked")
public class PushCustoms extends UntypedActor{

    private static String[] customs = {"guangzhou","ningbo","hangzhou","gzjichang","gzluogang","shanghai","zhengzhou"};
    /**
     * params 里面应该包含如下的参数:
     * 订单流水号: out_trade_no
     * 子单流水号: sub_out_trade_no
     * 子单编号: sub_order_no
     * 订单所在保税区: custom      customs 里面的一个数值
     *
     *
     * 还有需要的参数,应该是固定的,从config里面获得
     * 电商企业海关编号: cbe_code  3302461805
     * 电商企业海关名称: cbe_name 全球购
     * 电商企业国检编号: cbe_code_insp 3302461805
     * 电商平台海关编号: ecp_code 3302461805
     * 电商平台海关名称: ecp_name 全球购
     * 电商平台国检编号: ecp_code_insp 3302461805
     * @param message m
     * @throws Exception
     */
    @Override
    public void onReceive(Object message) throws Exception {
        final Map<String, String> params = (Map<String, String>) message;
        String push_url = Play.application().configuration().getString("jd_push_url");

        //TODO 是否检查参数

        if(ArrayUtils.contains(customs,params.get("custom"))) {
            params.put("customer_no",Play.application().configuration().getString("jd_seller"));
            //params.put("KEY",Play.application().configuration().getString("jd_secret"));
            DateTimeFormatter f = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss");
            String req_date = f.print(new DateTime());
            params.put("request_datetime",req_date);
            params.put("sign_type","MD5");
            params.put("cbe_code",Play.application().configuration().getString("jd_cbe_code"));
            params.put("cbe_name",Play.application().configuration().getString("jd_cbe_name"));
            params.put("cbe_code_insp",Play.application().configuration().getString("jd_cbe_code_insp"));
            params.put("ecp_code",Play.application().configuration().getString("jd_ecp_code"));
            params.put("ecp_name",Play.application().configuration().getString("jd_ecp_name"));
            params.put("ecp_code_insp",Play.application().configuration().getString("jd_ecp_code_insp"));
            params.put("sign_data", Crypto.create_sign(params,Play.application().configuration().getString("jd_secret")));

            StringBuilder sb = new StringBuilder();
            params.forEach((k,v)->{
                if(sb.length()> 0) {
                    sb.append("&");
                }
                sb.append(String.format("%s=%s",k,v));
            });

            WS.url(push_url).setContentType("application/x-www-form-urlencoded").post(sb.toString()).map(wsResponse -> {
                Logger.debug("" + wsResponse.getStatus() + " : " + wsResponse.asJson());
                JsonNode ret = wsResponse.asJson();

                //是否发送成功
                if(ret.get("is_success").textValue().equalsIgnoreCase("Y")) {
                    String out_trade_no = ret.get("out_trade_no").textValue();
                    String sub_out_trade_no = ret.get("sub_out_trade_no").textValue();
                    String sub_order_no = ret.get("sub_order_no").textValue();
                    //TODO 需要更新数据库表的报关状态


                    //每次不用初始化的话,不用杀死该actor
                    //getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());

                    Map<String,String> send = new HashMap<>();
                    send.put("out_trade_no",out_trade_no);
                    send.put("sub_out_trade_no", sub_out_trade_no);
                    send.put("sub_order_no", sub_order_no);

                    //30 分钟之后发起查询状态actor
                    ActorRef ar = Akka.system().actorOf(Props.create(CustomStatus.class));
                    Akka.system().scheduler().scheduleOnce(Duration.create(30, TimeUnit.MINUTES), ar, send, Akka.system().dispatcher(), ActorRef.noSender());
                }
                else {
                    Logger.info("post customer error ->" + ret.get("response_code").textValue());
                    //每次不用初始化的话,不用杀死该actor
                    //getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
                }
                return null;
            });

        }
        else {
            //end actor
        }


    }

    public void preStart() {
        Logger.debug("push customs actor start");
    }

    public void postStop() {
        Logger.debug("push customs actor stop");

    }
}
