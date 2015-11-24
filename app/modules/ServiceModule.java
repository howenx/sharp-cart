package modules;

import com.google.inject.AbstractModule;
import service.CartService;
import service.CartServiceImpl;

/**
 * 用于注入service
 * Created by howen on 15/11/22.
 */
public class ServiceModule extends AbstractModule{

    @Override
    protected void configure() {
        binder().bind(CartService.class).to(CartServiceImpl.class);
    }
}
