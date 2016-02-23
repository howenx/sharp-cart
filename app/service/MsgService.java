package service;

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

}
