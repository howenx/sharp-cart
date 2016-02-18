package controllers;

import filters.UserAuth;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import service.CartService;
import service.IdService;
import service.PromotionService;
import service.SkuService;

import javax.inject.Inject;

/**
 * 拼购
 * Created by howen on 16/2/17.
 */
public class PinCtrl extends Controller {

    private SkuService skuService;

    private CartService cartService;

    private IdService idService;

    private PromotionService promotionService;

    @Inject
    public PinCtrl(SkuService skuService, CartService cartService, IdService idService, PromotionService promotionService) {
        this.cartService = cartService;
        this.idService = idService;
        this.skuService = skuService;
        this.promotionService = promotionService;

    }

    @Security.Authenticated(UserAuth.class)
    public Result pinActivity() {

        return null;
    }

}
