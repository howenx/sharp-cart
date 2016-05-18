package mapper;

import domain.Address;
import domain.ID;
import domain.IdPlus;
import domain.IdThree;

/**
 * Id库
 * Created by howen on 15/11/25.
 */
public interface IdMapper {
    Address getAddress(Address address) throws Exception;

    IdPlus getIdPlus(IdPlus idPlus) throws Exception;

    int insertIdPlus(IdPlus idPlus) throws Exception;

    int updateIdPlus(IdPlus idPlus) throws Exception;

    ID getID(Long userId) throws Exception;

    //第三方登录数据
    IdThree getIdThree(Long userId) throws Exception;
}
