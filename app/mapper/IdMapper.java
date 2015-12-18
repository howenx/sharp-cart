package mapper;

import domain.Address;
import domain.IdPlus;

/**
 * Id库
 * Created by howen on 15/11/25.
 */
public interface IdMapper {
    Address getAddress(Address address) throws Exception;

    IdPlus getIdPlus(IdPlus idPlus) throws Exception;

    int insertIdPlus(IdPlus idPlus) throws Exception;

    int updateIdPlus(IdPlus idPlus) throws Exception;
}
