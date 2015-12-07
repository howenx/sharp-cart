package service;

import domain.CouponVo;

/**
 * 服务
 * Created by howen on 15/12/3.
 */
public interface Service {

    Boolean handOutCoupon(CouponVo couponVo) throws Exception;
}
