package controllers;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import common.MsgTypeEnum;
import domain.*;
import filters.UserAuth;
import modules.SysParCom;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import service.CartService;
import service.MsgService;
import service.SkuService;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static play.libs.Json.newObject;

/**
 * 消息
 * Created by sibyl.sun on 16/2/22.
 */
public class MsgCtrl extends Controller {

    private SkuService skuService;

    private CartService cartService;

    private MsgService msgService;

    private ActorRef schedulerCleanMsgActor;

    @Inject
    public MsgCtrl(SkuService skuService, CartService cartService, MsgService msgService, @Named("schedulerCleanMsgActor") ActorRef schedulerCleanMsgActor) {
        this.cartService = cartService;
        this.skuService = skuService;
        this.msgService = msgService;
        this.schedulerCleanMsgActor = schedulerCleanMsgActor;
    }

    //@Security.Authenticated(UserAuth.class)
    public Result testMsg() {
        //Long userId = (Long) ctx().args.get("userId");
        addMsgRec(1000230L, MsgTypeEnum.Discount, "title", "content", "item/photo/20160407/2df4b58a804d4c799df91a9e1bb8949a1460017299392.jpg", "/comm/detail/item/888276/111318", "D");
//        addSysMsg(MsgTypeEnum.Logistics, "titlesys111111", "Logistics contentsys", "item/photo/20160407/2df4b58a804d4c799df91a9e1bb8949a1460017299392.jpg", "/comm/detail/item/888276/111318", "D", new Timestamp(System.currentTimeMillis() + 24 * 60 * 60 * 1000));
        addMsgRec(1000230L, MsgTypeEnum.Coupon, "title", "Coupon content", "item/photo/20160407/2df4b58a804d4c799df91a9e1bb8949a1460017299392.jpg", "/comm/detail/item/888276/111318", "D");
        addMsgRec(1000230L, MsgTypeEnum.Goods, "Goods title", "Goods content", "item/photo/20160407/2df4b58a804d4c799df91a9e1bb8949a1460017299392.jpg", "/comm/detail/item/888276/111318", "D");
        addMsgRec(1000230L, MsgTypeEnum.Logistics, "Logistics title", "Logistics content", "item/photo/20160407/2df4b58a804d4c799df91a9e1bb8949a1460017299392.jpg", "/comm/detail/item/888276/111318", "D");
        addMsgRec(1000230L, MsgTypeEnum.System, "System title", "System Coupon content", "item/photo/20160407/2df4b58a804d4c799df91a9e1bb8949a1460017299392.jpg", "/comm/detail/item/888276/111318", "D");

        addMsgRec(1000132L, MsgTypeEnum.Discount, "title", "content", "item/photo/20160407/2df4b58a804d4c799df91a9e1bb8949a1460017299392.jpg", "/comm/detail/item/888276/111318", "D");
//        addSysMsg(MsgTypeEnum.Logistics, "titlesys111111", "Logistics contentsys", "item/photo/20160407/2df4b58a804d4c799df91a9e1bb8949a1460017299392.jpg", "/comm/detail/item/888276/111318", "D", new Timestamp(System.currentTimeMillis() + 24 * 60 * 60 * 1000));
        addMsgRec(1000132L, MsgTypeEnum.Coupon, "title", "Coupon content", "item/photo/20160407/2df4b58a804d4c799df91a9e1bb8949a1460017299392.jpg", "/comm/detail/item/888276/111318", "D");
        addMsgRec(1000132L, MsgTypeEnum.Goods, "Goods title", "Goods content", "item/photo/20160407/2df4b58a804d4c799df91a9e1bb8949a1460017299392.jpg", "/comm/detail/item/888276/111318", "D");
        addMsgRec(1000132L, MsgTypeEnum.Logistics, "Logistics title", "Logistics content", "item/photo/20160407/2df4b58a804d4c799df91a9e1bb8949a1460017299392.jpg", "/comm/detail/item/888276/111318", "D");
        addMsgRec(1000132L, MsgTypeEnum.System, "System title", "System Coupon content", "item/photo/20160407/2df4b58a804d4c799df91a9e1bb8949a1460017299392.jpg", "/comm/detail/item/888276/111318", "D");

        // checkRecSysMsgOnline(1000073L);
        //cleanMsgAtFixedTime();
        ObjectNode result = newObject();

        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
        return ok(result);
    }

    /**
     * 添加给全体发送的系统消息
     *
     * @param msgTypeEnum 消息类型
     * @param title       标题
     * @param msgContent  内容
     * @param msgImg      图片地址
     * @param msgUrl      url
     * @param targetType  T:主题，D:详细页面，P:拼购商品页，A:活动页面，U:一个促销活动的链接
     * @param endAt       失效时间
     * @return
     */
    public Msg addSysMsg(MsgTypeEnum msgTypeEnum, String title, String msgContent, String msgImg, String msgUrl, String targetType, Timestamp endAt) {
        Msg msg = new Msg();
        msg.setMsgType(msgTypeEnum.getMsgType());
        msg.setMsgTitle(title);
        msg.setMsgContent(msgContent);
        msg.setMsgImg(msgImg);
        msg.setMsgUrl(msgUrl);
        msg.setTargetType(targetType);
        msg.setEndAt(endAt);
        if (msgService.insertMsg(msg)) {
            return msg;
        }
        return null;
    }

    /***
     * 接收未接收的系统消息
     */
    public void checkNotRecSysMsg(Long userId) {
        Optional<List<Msg>> msgList = Optional.ofNullable(msgService.getNotRecMsg(userId));
        if (msgList.isPresent() && msgList.get().size() > 0) {
            msgList.get().forEach(msg -> {
                addSysMsgRec(userId, msg);
            });
        }
    }

    /***
     * 定期清理消息  TODO 调用
     */
    public void cleanMsgAtFixedTime() {
        //顺序不可换
        msgService.cleanMsg();  //定期清理过期的系统消息
        msgService.cleanMsgRec();//定期清理已经删除的消息
    }

    /***
     * 指定用户发送消息
     *
     * @param userId      用户ID
     * @param msgTypeEnum 消息类型
     * @param title       标题
     * @param msgContent  内容
     * @param msgImg      图片地址
     * @param msgUrl      url
     * @return
     */
    public MsgRec addMsgRec(Long userId, MsgTypeEnum msgTypeEnum, String title, String msgContent, String msgImg, String msgUrl, String targetType) {
        return createMsgRec(userId, msgTypeEnum.getMsgType(), title, msgContent, msgImg, msgUrl, targetType, new Timestamp(System.currentTimeMillis()), 1, 0L);
    }

    /***
     * 指定用户发送系统消息
     *
     * @param userId
     * @param msg
     * @return
     */
    private MsgRec addSysMsgRec(Long userId, Msg msg) {
        return createMsgRec(userId, msg.getMsgType(), msg.getMsgTitle(), msg.getMsgContent(), msg.getMsgImg(), msg.getMsgUrl(), msg.getTargetType(),
                msg.getCreateAt(), 2, msg.getMsgId());
    }

    /**
     * 创建接受的消息
     *
     * @param userId
     * @param msgType
     * @param title
     * @param msgContent
     * @param msgImg
     * @param msgUrl
     * @param targetType
     * @param createAt
     * @param msgRecType
     * @param msgId
     * @return
     */
    private MsgRec createMsgRec(Long userId, String msgType, String title, String msgContent, String msgImg, String msgUrl, String targetType, Timestamp createAt, Integer msgRecType, Long msgId) {
        MsgRec msgRec = new MsgRec();
        msgRec.setUserId(userId);
        msgRec.setMsgType(msgType);
        msgRec.setMsgTitle(title);
        msgRec.setMsgContent(msgContent);
        msgRec.setMsgImg(msgImg);
        msgRec.setMsgUrl(msgUrl);
        msgRec.setReadStatus(1);
        msgRec.setDelStatus(1);
        msgRec.setCreateAt(createAt);
        msgRec.setTargetType(targetType);
        msgRec.setMsgRecType(msgRecType);
        msgRec.setMsgId(msgId);
        if (msgService.insertMsgRec(msgRec)) {
            //成功  //TODO 提醒
            return msgRec;
        }
        return null;
    }

    /**
     * 获取所有的消息类型
     *
     * @return
     */
    @Security.Authenticated(UserAuth.class)
    public Result getAllMsgType() {
        ObjectNode result = newObject();
        Long userId = (Long) ctx().args.get("userId");
        checkNotRecSysMsg(userId);
       // Map<String, Integer> msgTypeMap = new HashMap<String, Integer>();
        List<MsgTypeDTO> msgTypeDTOList=new ArrayList<>();
        try {

            for (MsgTypeEnum msgTypeEnum : MsgTypeEnum.values()) {
                MsgRec msgRec = new MsgRec();
                msgRec.setUserId(userId);
                msgRec.setMsgType(msgTypeEnum.getMsgType());
                msgRec.setDelStatus(1);//未删除的
                Optional<List<MsgRec>> msgRecList = Optional.ofNullable(msgService.getMsgRecBy(msgRec)); //该类别下有消息
                if (msgRecList.isPresent() && msgRecList.get().size() > 0) {
                    msgRec.setReadStatus(1);
                    MsgTypeDTO msgTypeDTO=new MsgTypeDTO();
                    msgTypeDTO.setMsgType(msgTypeEnum.getMsgType());
                    msgTypeDTO.setNum(msgService.getNotReadMsgNum(msgRec));
                    msgTypeDTO.setContent(msgRecList.get().get(0).getMsgContent());
                    msgTypeDTO.setCreateAt(msgRecList.get().get(0).getCreateAt());
                    msgTypeDTOList.add(msgTypeDTO);
                  //  msgTypeMap.put(msgTypeEnum.getMsgType(), msgService.getNotReadMsgNum(msgRec)); //未读条数
                }
            }
            result.putPOJO("msgTypeDTOList", Json.toJson(msgTypeDTOList));
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            return ok(result);
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            Logger.error("server exception:", ex);
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    @Security.Authenticated(UserAuth.class)
    public Result getAllMsgs(String msgType) {
        ObjectNode result = newObject();
        Long userId = (Long) ctx().args.get("userId");
        MsgRec msgRec = new MsgRec();
        msgRec.setUserId(userId);
        msgRec.setMsgType(msgType);
        msgRec.setDelStatus(1);//未删除的

        final boolean[] isHaveNotRead = {false};
        try {
            Optional<List<MsgRec>> msgRecList = Optional.ofNullable(msgService.getMsgRecBy(msgRec));
            List<MsgRec> msgList = new ArrayList<MsgRec>();
            if (msgRecList.isPresent() && msgRecList.get().size() > 0) {
                msgList = msgRecList.get().stream().map(m -> {
                    if (m.getMsgImg().contains("url")) {
                        JsonNode jsonNode = Json.parse(m.getMsgImg());
                        if (jsonNode.has("url")) {
                            m.setMsgImg(SysParCom.IMAGE_URL + jsonNode.get("url").asText());
                        }
                    } else
                        m.setMsgImg(SysParCom.IMAGE_URL + m.getMsgImg());

                    if (m.getTargetType().equals("V")) m.setMsgUrl(SysParCom.PROMOTION_URL + m.getMsgUrl());
                    else m.setMsgUrl(SysParCom.DEPLOY_URL + m.getMsgUrl());

                    if (m.getReadStatus() == 1) {
                        isHaveNotRead[0] = true;
                    }
                    return m;

                }).collect(Collectors.toList());
            }
            if (isHaveNotRead[0]) {
                msgRec.setReadStatus(2); //已读
                msgService.updateReadStatus(msgRec);
            }
            result.putPOJO("msgList", Json.toJson(msgList));
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            return ok(result);
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            Logger.error("server exception:", ex);
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    /**
     * 删除消息
     *
     * @param id
     * @return
     */
    @Security.Authenticated(UserAuth.class)
    public Result delMsg(Long id) {
        ObjectNode result = newObject();
        Long userId = (Long) ctx().args.get("userId");
        try {
            if (msgService.delMsgRec(id)) {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(result);
            }
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            Logger.error("server exception:", ex);
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
        return ok(result);
    }

    /***
     * 按照消息类别清空消息
     *
     * @param msgType
     * @return
     */
    @Security.Authenticated(UserAuth.class)
    public Result cleanMsg(String msgType) {
        ObjectNode result = newObject();
        Long userId = (Long) ctx().args.get("userId");
        MsgRec msgRec = new MsgRec();
        msgRec.setUserId(userId);
        if (!"all".equalsIgnoreCase(msgType)) {//按照消息类别清空消息
            msgRec.setMsgType(msgType);
        }

        try {
            if (msgService.cleanMsgRecBy(msgRec)) {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(result);
            }
        } catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            Logger.error("server exception:", ex);
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
        return ok(result);
    }

    /**
     * 意见反馈
     *
     * @return
     */
    @Security.Authenticated(UserAuth.class)
    public Result feedback() {
        JsonNode json = request().body().asJson();
        String content = "";
        if (json.has("content")) {
            content = json.findValue("content").asText().trim();
        }
        ObjectNode result = newObject();
        Long userId = (Long) ctx().args.get("userId");
        try {
            if (content.length() > 0 && content.length() < 140) {
                Feedback feedback = new Feedback();
                feedback.setUserId(userId);
                feedback.setContent(content);
                if (msgService.insertFeedBack(feedback)) {
                    result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                    return ok(result);
                }
            }

        } catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            Logger.error("server exception:", ex);
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
        return ok(result);
    }


}
