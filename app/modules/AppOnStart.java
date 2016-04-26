package modules;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import play.Logger;
import play.api.libs.Codecs;

import static util.SysParCom.SHOPPING_URL;

/**
 * 应用启动时调用
 * Created by howen on 16/3/21.
 */
public class AppOnStart {
    public AppOnStart() {
        try {
            Request request = new Request.Builder().url(SHOPPING_URL + "/client/schedule/" + Codecs.md5("hmm-100901".getBytes())).build();
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
