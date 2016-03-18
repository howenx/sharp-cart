package service;

import domain.Feedback;
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
        return msgMapper.delMsgRec(id)>0;
    }

    @Override
    public int getNotReadMsgNum(MsgRec msgRec) {
        return msgMapper.getNotReadMsgNum(msgRec);
    }

    @Override
    public Boolean updateReadStatus(MsgRec msgRec) {
        return msgMapper.updateReadStatus(msgRec)>0;
    }

    @Override
    public List<Msg> getNotRecMsg(Long userId) {
        return msgMapper.getNotRecMsg(userId);
    }

    @Override
    public Boolean cleanMsg() {
        return msgMapper.cleanMsg()>0;
    }

    @Override
    public Boolean cleanMsgRec() {
        return msgMapper.cleanMsgRec()>0;
    }

    @Override
    public Boolean cleanMsgRecBy(MsgRec msgRec) {
        return msgMapper.cleanMsgRecBy(msgRec)>0;
    }

    @Override
    public Boolean insertFeedBack(Feedback feedback) {
        return msgMapper.insertFeedBack(feedback)>0;
    }
}
