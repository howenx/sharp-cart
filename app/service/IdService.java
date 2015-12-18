package service;

import domain.Address;
import domain.IdPlus;

/**
 * Id service
 * Created by howen on 15/11/25.
 */
public interface IdService {

    Address getAddress(Address address) throws Exception;

    IdPlus getIdPlus(IdPlus idPlus) throws Exception;

    Boolean insertIdPlus(IdPlus idPlus) throws Exception;

    Boolean updateIdPlus(IdPlus idPlus) throws Exception;
}
