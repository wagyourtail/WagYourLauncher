package xyz.wagyourtail.launcher.gui.screen.login;

import xyz.wagyourtail.launcher.auth.common.GetProfile;
import xyz.wagyourtail.launcher.gui.screen.Screen;
import xyz.wagyourtail.notlog4j.Logger;

public interface AddAccountScreen extends Screen {
    Logger getLogger();

    void setProgress(int progress);

    /**
     * @return a newly opened window
     */
    UsernamePasswordScreen getUsernamePassword();

    /**
     * @param provider string name of the provider
     */
    default void runLogin(String provider) {
        Logger logger = getLogger();

        try {
            GetProfile.MCProfile profile = getLauncher().auth.authProviders.get(provider).withLogger(this);
        } catch (Throwable t) {
            error("failed to login", t);
        }
    }
}
