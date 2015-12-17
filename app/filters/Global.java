package filters;

import domain.SysParameter;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import service.SkuService;

import javax.inject.Inject;

/**
 *
 * Created by howen on 15/10/23.
 */
public class Global extends GlobalSettings {



    //行邮税收税标准
    public static String POSTAL_STANDARD = "";

    public static String POSTAL_LIMIT = "";

    public static String FREE_SHIP = "";

    public void onStart(Application app) {

        Logger.error("Application has started");
    }

    public void onStop(Application app) {
        Logger.error("Application shutdown...");
    }
}
