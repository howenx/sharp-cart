package service;

import domain.CouponVo;

import javax.inject.Inject;

/**
 * 服务实现
 * Created by howen on 15/12/3.
 */
public class ServiceImpl implements Service{

    @Inject
    CartService cartService;

    @Override
    public Boolean handOutCoupon(CouponVo couponVo) throws Exception{
        return cartService.insertCoupon(couponVo);
    }
}
