package filters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.Message;
import net.spy.memcached.MemcachedClient;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import javax.inject.Inject;
import java.util.Optional;

/**
 * 用户校验
 * Created by howen on 15/11/25.
 */
public class UserAuth extends Security.Authenticator {

    @Inject
    private MemcachedClient cache;

    @Override
    public String getUsername(Http.Context ctx) {
        Optional<String> requestHeader = Optional.ofNullable(ctx.request().getHeader("id-token"));
        Optional<String> flashHeader = Optional.ofNullable(ctx.flash().get("id-token"));
        if (requestHeader.isPresent() || flashHeader.isPresent()) {
            String header = requestHeader.isPresent()?requestHeader.get():(flashHeader.isPresent()?flashHeader.get():null);
            if (header!=null){
                Optional<Object> token = Optional.ofNullable(cache.get(header));
                if (token.isPresent()) {
                    JsonNode userJson = Json.parse(token.get().toString());
                    Long userId = Long.valueOf(userJson.findValue("id").asText());
                    String  username = userJson.findValue("name").toString();
                    ctx.args.put("userId",userId);
                    ctx.args.put("username",username);
                    ctx.args.put("userPhoto",userJson.findValue("photo").toString().trim());
                    return username;
                }
                else return null;
            }else return null;
        }else return null;
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        ObjectNode result = Json.newObject();
        Logger.error("403 无权登录");
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.BAD_USER_TOKEN.getIndex()), Message.ErrorCode.BAD_USER_TOKEN.getIndex())));
        return ok(result);
    }

}

