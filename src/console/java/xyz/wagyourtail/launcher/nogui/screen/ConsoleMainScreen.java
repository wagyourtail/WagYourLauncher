package xyz.wagyourtail.launcher.nogui.screen;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.gui.screen.MainScreen;
import xyz.wagyourtail.launcher.gui.screen.profile.ProfileScreen;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;
import xyz.wagyourtail.launcher.nogui.CommandManager;
import xyz.wagyourtail.launcher.nogui.screen.profile.ConsoleProfileScreen;

import java.util.Map;

public class ConsoleMainScreen extends ConsoleScreen implements MainScreen {
    private final CommandManager commandManager = new CommandManager();

    protected ConsoleMainScreen(LauncherBase launcher, MainScreen mainScreen) throws InterruptedException {
        super(launcher, mainScreen, "");
        commandManager.registerCommand("listprofiles", "", "list available profiles", this::listProfiles);
        commandManager.registerCommand("selectprofile", "<id>", "select a profile", this::selectProfile);
        init();
    }

    private boolean listProfiles(String[] args) {
        getLauncher().getLogger().info("Profiles:");
        int[] tableSizes = new int[3];
        for (Map.Entry<String, Profile> entry : getLauncher().profiles.getAllProfiles().entrySet()) {
            tableSizes[0] = Math.max(tableSizes[0], entry.getKey().length());
            tableSizes[1] = Math.max(tableSizes[1], entry.getValue().name().length());
            tableSizes[2] = Math.max(tableSizes[2], entry.getValue().lastVersionId().length());
        }
        getLauncher().getLogger().info("\t" + rPadTo("id", tableSizes[0]) + "\t" + rPadTo("name", tableSizes[1]) + "\t" + rPadTo("(version)", tableSizes[2]));
        getLauncher().getLogger().info(dashes(tableSizes[0] + tableSizes[1] + tableSizes[2] + 12));
        for (Map.Entry<String, Profile> prof : getLauncher().profiles.getAllProfiles().entrySet()) {
            getLauncher().getLogger().info("\t" + rPadTo(prof.getKey(), tableSizes[0]) + "\t" + rPadTo(prof.getValue().name(), tableSizes[1]) + "\t" + rPadTo(prof.getValue().lastVersionId(), tableSizes[2]));
        }
        return true;
    }

    private boolean selectProfile(String[] args) {
        if (args.length != 1) {
            getLauncher().getLogger().error("Usage: selectprofile <id>");
            return false;
        }
        if (!getLauncher().profiles.getAllProfiles().containsKey(args[0])) {
            getLauncher().getLogger().error("Profile " + args[0] + " does not exist");
            return false;
        }
        openProfile(getLauncher().profiles.getAllProfiles().get(args[0]));
        return true;
    }

    @Override
    public ProfileScreen openProfile(Profile profile) {
        return new ConsoleProfileScreen(getLauncher(), getMainWindow(), profile);
    }

    @Override
    public void openGlobalSettings() {

    }

    @Override
    public void openAddProfile() {

    }

    @Override
    public void openAddAccount() {

    }

}
