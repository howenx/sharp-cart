package service;

import domain.Address;

/**
 * Id service
 * Created by howen on 15/11/25.
 */
public interface IdService {

    Address getAddress(Address address) throws Exception;
}
