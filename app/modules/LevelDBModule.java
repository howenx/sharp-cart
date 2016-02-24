package modules;

import com.google.inject.AbstractModule;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import middle.CartMid;
import middle.JDPayMid;
import play.Configuration;
import play.Environment;
import play.Logger;
import play.api.libs.Codecs;

/**
 * 启动leveldb
 * Created by howen on 16/2/19.
 */
public class LevelDBModule extends AbstractModule {

    private final Environment environment;
    private final Configuration configuration;

    public LevelDBModule(
            Environment environment,
            Configuration configuration) {
        this.environment = environment;
        this.configuration = configuration;
    }

    protected void configure() {

//        //作为系统启动后的第一次请求调用
//        try {
//            Request request = new Request.Builder().url(configuration.getString("shopping.server.url")+"/client/schedule/"+ Codecs.md5("hmm-100901".getBytes())).build();
//            OkHttpClient client = new OkHttpClient();
//            Response response = client.newCall(request).execute();
//
//            if (response.isSuccessful()) {
//                String responseUrl = response.body().string();
//                Logger.error("启动调用:\n"+responseUrl);
//            }
//        } catch (Exception e) {
//            Logger.error(e.getMessage());
//        }

        bind(CartMid.class).asEagerSingleton();
        bind(JDPayMid.class).asEagerSingleton();
    }
}
