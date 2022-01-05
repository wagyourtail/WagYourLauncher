package xyz.wagyourtail.launcher.gui.screen;

import xyz.wagyourtail.launcher.gui.screen.login.AddAccountScreen;
import xyz.wagyourtail.launcher.gui.screen.profile.ProfileCreateScreen;
import xyz.wagyourtail.launcher.gui.screen.profile.ProfileScreen;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public interface MainScreen extends Screen {
     ProfileScreen getProfileScreen(Profile profile);

    ProfileCreateScreen openAddProfile();

    AddAccountScreen openAddAccount();

    default void launchProfile(Profile profile, boolean offline) {
        getProfileScreen(profile).launch(offline);
    }

    default void selectAccount(String account) throws UnrecoverableEntryException, CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
        getLauncher().auth.setSelectedProfile(getLauncher().auth.getProfile(getLauncher().getLogger(), account, true));
    }

    @Override
    default MainScreen getMainWindow() {
        return this;
    }
}
