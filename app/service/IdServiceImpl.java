package service;

import domain.Address;
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
}
