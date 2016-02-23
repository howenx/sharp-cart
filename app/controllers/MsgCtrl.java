package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.Message;
import domain.MsgRec;
import filters.UserAuth;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import service.CartService;
import service.MsgService;
import service.SkuService;

import javax.inject.Inject;

import java.util.List;
import java.util.Optional;

import static play.libs.Json.newObject;

/**
 * 消息
 * Created by sibyl.sun on 16/2/22.
 */
public class MsgCtrl extends Controller{

    private SkuService skuService;

    private CartService cartService;

    private MsgService msgService;

    @Inject
    public MsgCtrl(SkuService skuService, CartService cartService,MsgService msgService){
        this.cartService = cartService;
        this.skuService = skuService;
        this.msgService=msgService;
    }

    /***
     * 指定用户发送消息
     * @param userId
     * @param msgType
     * @param title
     * @param msgContent
     * @param msgImg
     * @param msgUrl
     * @return
     */
    public MsgRec addMsgRec(Long userId,Integer msgType,String title,String msgContent,String msgImg,String msgUrl){
        MsgRec msgRec=new MsgRec();
        msgRec.setUserId(userId);
        msgRec.setMsgType(msgType);
        msgRec.setMsgTitle(title);
        msgRec.setMsgContent(msgContent);
        msgRec.setMsgImg(msgImg);
        msgRec.setMsgUrl(msgUrl);
        if(msgService.insertMsgRec(msgRec)){
            //成功  //TODO 提醒
            return msgRec;
        }
        return null;
    }

    @Security.Authenticated(UserAuth.class)
    public Result getAllMsgs(){
        ObjectNode result = newObject();
        Long userId = (Long) ctx().args.get("userId");
        MsgRec msgRec=new MsgRec();
        msgRec.setUserId(userId);
        msgRec.setStatus(0); //未删除的

        try{
            Optional<List<MsgRec>> msgRecList= Optional.ofNullable(msgService.getMsgRecBy(msgRec));
            if(msgRecList.isPresent()&&msgRecList.get().size()>0){
                result.putPOJO("msgList",msgRecList);
            }

            return ok(result);
        }catch (Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            Logger.error("server exception:",ex);
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
    }

    /**
     * 删除消息
     * @param id
     * @return
     */
    public Result delMsg(Long id){
        ObjectNode result = newObject();
        Long userId = (Long) ctx().args.get("userId");
        try {
            if (msgService.delMsgRec(id)) {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(result);
            }
        }catch(Exception ex) {
            Logger.error("server exception:" + ex.getMessage());
            Logger.error("server exception:", ex);
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
            return ok(result);
        }
        result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SERVER_EXCEPTION.getIndex()), Message.ErrorCode.SERVER_EXCEPTION.getIndex())));
        return ok(result);
    }


}
