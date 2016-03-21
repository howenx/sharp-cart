package filters;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import domain.Message;
import play.Application;
import play.Logger;
import play.api.libs.Codecs;
import play.http.HttpErrorHandler;
import play.libs.Json;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.ok;

/**
 * 错误请求处理
 * Created by howen on 15/10/23.
 */
public class ErrorHandler implements HttpErrorHandler {
    public CompletionStage<Result> onClientError(RequestHeader request, int statusCode, String message) {
        ObjectNode result = Json.newObject();

        if (statusCode == play.mvc.Http.Status.NOT_FOUND) {
            Logger.error("请求未找到: " + request.host() + request.uri() + " " + request.remoteAddress() + " " + request.getHeader("User-Agent"));
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE_REQUEST_HANDLER_NOT_FOUND.getIndex()), Message.ErrorCode.FAILURE_REQUEST_HANDLER_NOT_FOUND.getIndex())));

        } else if (statusCode == play.mvc.Http.Status.BAD_REQUEST) {
            Logger.error("有错误请求: " + request.host() + request.uri() + " " + request.remoteAddress() + " " + request.getHeader("User-Agent"));
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE_BAD_REQUEST.getIndex()), Message.ErrorCode.FAILURE_BAD_REQUEST.getIndex())));

        }
        return CompletableFuture.completedFuture(ok(result));
    }

    public CompletionStage<Result> onServerError(RequestHeader request, Throwable exception) {
        Logger.error("请求出错: " + request.host() + request.uri() + " " + request.remoteAddress() + " " + request.getHeader("User-Agent"));
        ObjectNode result = Json.newObject();
        exception.printStackTrace();
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.FAILURE_REQUEST_ERROR.getIndex()), Message.ErrorCode.FAILURE_REQUEST_ERROR.getIndex())));
        return CompletableFuture.completedFuture(ok(result));

    }
}
