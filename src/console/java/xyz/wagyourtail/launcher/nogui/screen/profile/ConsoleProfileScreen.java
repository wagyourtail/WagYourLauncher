package xyz.wagyourtail.launcher.nogui.screen.profile;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.gui.screen.MainScreen;
import xyz.wagyourtail.launcher.gui.screen.profile.ProfileScreen;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;
import xyz.wagyourtail.launcher.nogui.screen.ConsoleScreen;
import xyz.wagyourtail.notlog4j.Logger;

import java.util.function.Function;

public class ConsoleProfileScreen extends ConsoleScreen implements ProfileScreen {
    private Profile profile;

    public ConsoleProfileScreen(LauncherBase launcher, MainScreen mainScreen, Profile profile) {
        super(launcher, mainScreen, "Profile: " + profile.key());
        this.profile = profile;
        commandManager.registerCommand("launch", "", "Launches the profile", (Function<String[], Boolean>) this::launch);
        commandManager.registerCommand("launchoffline", "", "Launches the profile in offline mode", (Function<String[], Boolean>) this::launch);
    }

    private boolean launch(String[] args) {
        launch(false);
        return true;
    }

    private boolean launchOffline(String[] args) {
        launch(true);
        return true;
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
        ProfileScreen.super.editProfile(newProfile);
        this.profile = newProfile;
    }

}
