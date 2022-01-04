package xyz.wagyourtail.launcher.nogui.screen.profile;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.gui.screen.MainScreen;
import xyz.wagyourtail.launcher.gui.screen.profile.ProfileScreen;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;
import xyz.wagyourtail.launcher.nogui.screen.ConsoleScreen;
import xyz.wagyourtail.notlog4j.Logger;

public class ConsoleProfileScreen extends ConsoleScreen implements ProfileScreen {
    private Profile profile;

    public ConsoleProfileScreen(LauncherBase launcher, MainScreen mainScreen, Profile profile) {
        super(launcher, mainScreen, "Profile: " + profile.key());
    }

    @Override
    public Logger getLogger() {
        return getLauncher().getLogger();
    }

    @Override
    public Profile getProfile() {
        return profile;
    }

    @Override
    public void editProfile(Profile newProfile) {
        getLauncher().profiles.modifyProfile(profile, newProfile);
        this.profile = newProfile;
    }

}
