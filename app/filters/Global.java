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
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.lang.reflect.Method;

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
        return F.Promise.pure(notFound(result));
    }

    public F.Promise<play.mvc.Result> onError(Http.RequestHeader request, Throwable t) {
        Logger.error("请求出错: " + request.host() + request.uri() + " " + request.remoteAddress() + " " + request.getHeader("User-Agent"));
        ObjectNode result = Json.newObject();
        t.printStackTrace();
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE_REQUEST_ERROR.getIndex()), Message.ErrorCode.FAILURE_REQUEST_ERROR.getIndex())));
        return F.Promise.pure(notFound(result));
    }

    public F.Promise<play.mvc.Result> onBadRequest(Http.RequestHeader request, String error) {
        Logger.error("有错误请求: " + request.host() + request.uri() + " " + request.remoteAddress() + " " + request.getHeader("User-Agent"));
        ObjectNode result = Json.newObject();
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE_BAD_REQUEST.getIndex()), Message.ErrorCode.FAILURE_BAD_REQUEST.getIndex())));
        return F.Promise.pure(notFound(result));
    }

    public Action onRequest(Http.Request request, Method actionMethod) {
        if (request.getHeader("User-Agent").contains("Alibaba.Security")){
            ObjectNode result = Json.newObject();
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE_BAD_REQUEST.getIndex()), Message.ErrorCode.FAILURE_BAD_REQUEST.getIndex())));
            return new Action.Simple() {
                public F.Promise<Result> call(Http.Context ctx) throws Throwable {
                    return F.Promise.pure(notFound(result));
                }
            };
        }else return super.onRequest(request,actionMethod);
    }
}
