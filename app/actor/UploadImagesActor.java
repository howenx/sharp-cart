package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.okhttp.*;
import domain.Refund;
import domain.Remark;
import play.Logger;
import play.libs.Json;
import service.CartService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 上传
 * Created by howen on 16/1/19.
 */
@SuppressWarnings("unchecked")
public class UploadImagesActor extends AbstractActor {

    private final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/jpeg");

    @Inject
    public UploadImagesActor(CartService cartService) {

        receive(ReceiveBuilder.match(HashMap.class, s -> {


            List<byte[]> files = (List<byte[]>) s.get("files");

            List<String> imgs = new ArrayList<>();

            for (byte[] bytes : files) {
                RequestBody requestBody = new MultipartBuilder()
                        .type(MultipartBuilder.FORM)
                        .addFormDataPart("params", "minify")
                        .addFormDataPart("photo", "1.jpg", RequestBody.create(MEDIA_TYPE_PNG, bytes))
                        .build();

                Request request = new Request.Builder()
                        .url(s.get("url") + "/upload")
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    JsonNode json = Json.parse(new String(response.body().bytes(), UTF_8));
                    Logger.error("上传返回:\n" + json.toString());
                    imgs.add(json.get("oss_url").asText());
                }
            }
            if (!imgs.isEmpty()) {
                if (s.containsKey("refundId")) {
                    Refund refund = new Refund();
                    refund.setId((Long) s.get("refundId"));
                    refund.setRefundImg(Json.stringify(Json.toJson(imgs)));
                    cartService.updateRefund(refund);
                } else if (s.containsKey("remarkId")) {
                    Remark remark = new Remark();
                    remark.setId((Long) s.get("remarkId"));
                    remark.setPicture(Json.stringify(Json.toJson(imgs)));
                    cartService.updateRemark(remark);
                }
            } else {
                Logger.error("上传图片到style-imgprocess失败..");
            }
        }).matchAny(s -> Logger.error("UploadImagesActor received messages not matched: {}", s.toString())).build());
    }
}
