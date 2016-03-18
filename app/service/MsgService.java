package service;

import domain.Feedback;
import domain.Msg;
import domain.MsgRec;

import java.util.List;

/**
 * 消息
 * Created by sibyl.sun on 16/2/22.
 */
public interface MsgService {
    Boolean insertMsg(Msg msg);
    List<Msg> getMsgBy(Msg msg);
    Boolean insertMsgRec(MsgRec msgRec);
    List<MsgRec> getMsgRecBy(MsgRec msgRec);
    Boolean updateMsgRec(MsgRec msgRec);
    Boolean delMsgRec(Long id);
    /**
     * 根据消息类型获取未读消息条数
     * @param msgRec
     * @return
     */
    int getNotReadMsgNum(MsgRec msgRec);

    /**
     * 根据消息类型更新阅读状态
     * @param msgRec
     * @return
     */
    Boolean updateReadStatus(MsgRec msgRec);

    /***
     * 获取未接收的未过期的系统消息
     * @param userId
     * @return
     */
    List<Msg> getNotRecMsg(Long userId);

    /**
     * 定期清理过期的系统消息
     * @return
     */
    Boolean cleanMsg();
    /**
     * 定期清理已经删除的消息
     * @return
     */
    Boolean cleanMsgRec();
    Boolean cleanMsgRecBy(MsgRec msgRec);

    /**
     * 意见反馈
     * @param feedback
     * @return
     */
    Boolean insertFeedBack(Feedback feedback);

}
