package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.xerces.impl.dv.util.Base64;
import play.Logger;
import play.api.cache.Cache;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import net.spy.memcached.MemcachedClient;

import javax.crypto.Cipher;
import java.io.IOException;

public class Application extends Controller {


    public Result index() throws Exception {
        return ok(views.html.index.render(Encryption.getInstance().getPublicKey(), Encryption.getInstance().getPrivateKey(), "123123", Encryption.handleEncrypt(Encryption.getInstance().getPrivateKey(), "123123"), Encryption.handleDecrypt(Encryption.getInstance().getPublicKey(),Encryption.handleEncrypt(Encryption.getInstance().getPrivateKey(), "123123"))));
    }

    public Result getKey() {

        return ok(Encryption.getInstance().getPublicKey());
    }

    public Result getToken() {
        try {
            MemcachedClient cache_client = new MemcachedClient();
            cache_client.set("123123",0,Encryption.handleEncrypt(Encryption.getInstance().getPrivateKey(), "123123"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
