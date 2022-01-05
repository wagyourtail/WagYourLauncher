package xyz.wagyourtail.launcher.gui.screen.profile;

import xyz.wagyourtail.launcher.gui.screen.Screen;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;
import xyz.wagyourtail.notlog4j.Logger;

import java.io.IOException;
import java.util.Arrays;

public interface ProfileScreen extends Screen {
    Logger getLogger();

    Profile getProfile();

    default void editProfile(Profile newProfile) {
        getLauncher().profiles.modifyProfile(getProfile(), newProfile);
    }

    default void launch(boolean offline) {
        if (getLauncher().profiles.getRunningProfiles().contains(getProfile())) {
            error("Profile is already running!");
            return;
        }
        String username = getLauncher().auth.getSelectedProfile();
        if (username == null) {
            error("No username selected!");
            return;
        }
        if (!getLauncher().auth.isRegistered(username)) {
            error("User " + username + " is not logged in!");
            return;
        }
        getLogger().clear();
        try {
            getLauncher().profiles.launch(getProfile(), getLogger(), username, offline);
        } catch (Exception e) {
            getLogger().fatal("Error launching: " + e.getMessage());
            Arrays.stream(e.getStackTrace()).forEach(s -> getLogger().error(s.toString()));
        }
    }

    default void kill(boolean force) {
        if (!getLauncher().profiles.getRunningProfiles().contains(getProfile())) {
            error("Profile is not running!");
        }
        if (force) {
            getLauncher().profiles.forceKillRunning(getProfile());
        } else {
            getLauncher().profiles.killRunning(getProfile());
        }
    }
}
