package mapper;

import domain.Feedback;
import domain.Msg;
import domain.MsgRec;

import java.util.List;

/**
 * Created by sibyl.sun on 16/2/22.
 */
public interface MsgMapper {
    Integer insertMsg(Msg msg);
    List<Msg> getMsgBy(Msg msg);
    Integer insertMsgRec(MsgRec msgRec);
    List<MsgRec> getMsgRecBy(MsgRec msgRec);
    Integer updateMsgRec(MsgRec msgRec);
    Integer delMsgRec(Long id);

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
    Integer updateReadStatus(MsgRec msgRec);

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
    Integer cleanMsg();
    /**
     * 定期清理已经删除的消息
     * @return
     */
    Integer cleanMsgRec();

    /**
     * 清除某人指定类型消息
     * @param msgRec
     * @return
     */
    Integer cleanMsgRecBy(MsgRec msgRec);
    /**
     * 意见反馈
     * @param feedback
     * @return
     */
    Integer insertFeedBack(Feedback feedback);
}
