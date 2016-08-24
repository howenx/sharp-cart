package mapper;

import domain.Address;
import domain.ID;
import domain.IdPlus;
import domain.IdThree;

import java.util.List;

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
    List<IdThree> getIdThree(IdThree idThree) throws Exception;
}
