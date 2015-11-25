package mapper;

import domain.Address;

/**
 * Id库
 * Created by howen on 15/11/25.
 */
public interface IdMapper {
    Address getAddress(Address address) throws Exception;
}
