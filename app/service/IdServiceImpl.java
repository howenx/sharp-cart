package service;

import domain.Address;
import domain.ID;
import domain.IdPlus;
import domain.IdThree;
import mapper.IdMapper;

import javax.inject.Inject;

/**
 * impl
 * Created by howen on 15/11/25.
 */
public class IdServiceImpl implements IdService{

    @Inject
    private IdMapper idMapper;

    @Override
    public Address getAddress(Address address) throws Exception{
        return idMapper.getAddress(address);
    }

    @Override
    public IdPlus getIdPlus(IdPlus idPlus) throws Exception {
        return idMapper.getIdPlus(idPlus);
    }

    @Override
    public Boolean insertIdPlus(IdPlus idPlus) throws Exception {
        return idMapper.insertIdPlus(idPlus)>=0;
    }

    @Override
    public Boolean updateIdPlus(IdPlus idPlus) throws Exception {
        return idMapper.updateIdPlus((idPlus))>=0;
    }

    @Override
    public ID getID(Long userId) throws Exception {
        return idMapper.getID(userId);
    }

    @Override
    public IdThree getIdThree(Long userId) throws Exception {
        return idMapper.getIdThree(userId);
    }
}
