package filters;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import domain.Message;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.libs.Codecs;
import play.libs.F;
import play.libs.Json;
import play.mvc.Http;

import static play.mvc.Results.notFound;

/**
 * Global
 * Created by howen on 15/10/23.
 */
public class Global extends GlobalSettings {

    public F.Promise<play.mvc.Result> onHandlerNotFound(Http.RequestHeader request) {
        Logger.error("请求未找到: " + request.host() + request.uri() + " " + request.remoteAddress() + " " + request.getHeader("User-Agent"));
        ObjectNode result = Json.newObject();
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE_REQUEST_HANDLER_NOT_FOUND.getIndex()), Message.ErrorCode.FAILURE_REQUEST_HANDLER_NOT_FOUND.getIndex())));
        return F.Promise.<play.mvc.Result>pure(notFound(result));
    }

    public F.Promise<play.mvc.Result> onError(Http.RequestHeader request, Throwable t) {
        Logger.error("请求出错: " + request.host() + request.uri() + " " + request.remoteAddress() + " " + request.getHeader("User-Agent"));
        ObjectNode result = Json.newObject();
        t.printStackTrace();
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE_REQUEST_ERROR.getIndex()), Message.ErrorCode.FAILURE_REQUEST_ERROR.getIndex())));
        return F.Promise.<play.mvc.Result>pure(notFound(result));
    }

    public F.Promise<play.mvc.Result> onBadRequest(Http.RequestHeader request, String error) {
        Logger.error("有错误请求: " + request.host() + request.uri() + " " + request.remoteAddress() + " " + request.getHeader("User-Agent"));
        ObjectNode result = Json.newObject();
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE_BAD_REQUEST.getIndex()), Message.ErrorCode.FAILURE_BAD_REQUEST.getIndex())));
        return F.Promise.<play.mvc.Result>pure(notFound(result));
    }

    public void onStart(Application app) {//作为系统启动后的第一次请求调用
        try {
            Request request = new Request.Builder().url(play.Play.application().configuration().getString("shopping.server.url") + "/client/schedule/" + Codecs.md5("hmm-100901".getBytes())).build();
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String responseUrl = response.body().string();
                Logger.error("启动调用:\n" + responseUrl);
            } else client.newCall(request).execute();
        } catch (Exception ignored) {
        }
    }
}
