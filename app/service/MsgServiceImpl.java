package service;

import domain.Msg;
import domain.MsgRec;
import mapper.MsgMapper;

import javax.inject.Inject;
import java.util.List;

/**
 * 消息
 * Created by sibyl.sun on 16/2/22.
 */
public class MsgServiceImpl implements MsgService{
    @Inject
    private MsgMapper msgMapper;

    @Override
    public Boolean insertMsg(Msg msg) {
        return msgMapper.insertMsg(msg)>0;
    }

    @Override
    public List<Msg> getMsgBy(Msg msg) {
        return msgMapper.getMsgBy(msg);
    }

    @Override
    public Boolean insertMsgRec(MsgRec msgRec) {
        return msgMapper.insertMsgRec(msgRec)>0;
    }

    @Override
    public List<MsgRec> getMsgRecBy(MsgRec msgRec) {
        return msgMapper.getMsgRecBy(msgRec);
    }

    @Override
    public Boolean updateMsgRec(MsgRec msgRec) {
        return msgMapper.updateMsgRec(msgRec)>0;
    }

    @Override
    public Boolean delMsgRec(Long id) {
        return null;
    }
}
