package filters;

import play.Application;
import play.GlobalSettings;
import play.Logger;

/**
 *
 * Created by howen on 15/10/23.
 */
public class Global extends GlobalSettings {

    public void onStart(Application app) {
        Logger.error("Application has started");
    }

    public void onStop(Application app) {
        Logger.error("Application shutdown...");
    }
}
