package mapper;

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
  //  Integer
}
