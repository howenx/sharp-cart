package filters;

import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.Message;
import play.GlobalSettings;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.mvc.Http;

import static play.mvc.Results.notFound;

/**
 *
 * Created by howen on 15/10/23.
 */
public class Global extends GlobalSettings {

    public F.Promise<play.mvc.Result> onHandlerNotFound(Http.RequestHeader request) {
        Logger.error("请求未找到: "+request.host()+request.uri()+" "+request.remoteAddress()+" "+request.getHeader("User-Agent"));
        ObjectNode result = Json.newObject();
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE_REQUEST_HANDLER_NOT_FOUND.getIndex()), Message.ErrorCode.FAILURE_REQUEST_HANDLER_NOT_FOUND.getIndex())));
        return F.Promise.<play.mvc.Result>pure(notFound(result));
    }
    public F.Promise<play.mvc.Result> onError(Http.RequestHeader request, Throwable t) {
        Logger.error("请求出错: "+request.host()+request.uri()+" "+request.remoteAddress()+" "+request.getHeader("User-Agent"));
        ObjectNode result = Json.newObject();
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE_REQUEST_ERROR.getIndex()), Message.ErrorCode.FAILURE_REQUEST_ERROR.getIndex())));
        return F.Promise.<play.mvc.Result>pure(notFound(result));
    }

    public F.Promise<play.mvc.Result> onBadRequest(Http.RequestHeader request, String error) {
        Logger.error("有错误请求: "+request.host()+request.uri()+" "+request.remoteAddress()+" "+request.getHeader("User-Agent"));
        ObjectNode result = Json.newObject();
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE_BAD_REQUEST.getIndex()), Message.ErrorCode.FAILURE_BAD_REQUEST.getIndex())));
        return F.Promise.<play.mvc.Result>pure(notFound(result));
    }
}
