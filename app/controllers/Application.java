package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import net.spy.memcached.MemcachedClient;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

public class Application extends Controller {

    @Inject
    private MemcachedClient cache;

    public Result index() throws Exception {
        return ok(views.html.index.render(Encryption.getInstance().getPublicKey(), Encryption.getInstance().getPrivateKey(), "123123", Encryption.handleEncrypt(Encryption.getInstance().getPrivateKey(), "123123"), Encryption.handleDecrypt(Encryption.getInstance().getPublicKey(),Encryption.handleEncrypt(Encryption.getInstance().getPrivateKey(), "123123"))));
    }

    public Result getKey() {

        return ok(Encryption.getInstance().getPublicKey());
    }

    public Result getToken() {

        return ok(Encryption.handleEncrypt(Encryption.getInstance().getPrivateKey(), "123123"));
    }

    public Result cart() {
        JsonNode json = request().body().asJson();
        //{client_token:"",client_id:""}

        Logger.error("客户端Token: "+json.findValue("client_token").asText());
        Logger.error("客户端ID: "+json.findValue("client_id").asText());

        return ok(Json.toJson(Encryption.handleEncrypt(Encryption.getInstance().getPrivateKey(), json.findValue("client_id").asText()).equals(json.findValue("client_token").asText())));
    }



}
