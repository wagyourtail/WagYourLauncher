package xyz.wagyourtail.launcher.nogui.screen;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.gui.screen.MainScreen;
import xyz.wagyourtail.launcher.gui.screen.login.AddAccountScreen;
import xyz.wagyourtail.launcher.gui.screen.profile.ProfileCreateScreen;
import xyz.wagyourtail.launcher.gui.screen.profile.ProfileScreen;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;
import xyz.wagyourtail.launcher.nogui.screen.login.ConsoleAddAccountScreen;
import xyz.wagyourtail.launcher.nogui.screen.profile.ConsoleProfileCreateScreen;
import xyz.wagyourtail.launcher.nogui.screen.profile.ConsoleProfileScreen;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.function.Function;

public class ConsoleMainScreen extends ConsoleScreen implements MainScreen {

    public ConsoleMainScreen(LauncherBase launcher) {
        super(launcher, null, "");
        commandManager.registerCommand("listprofiles", "", "list available profiles", this::listProfiles);
        commandManager.registerCommand("selectprofile", "<id>", "select a profile", this::selectProfile);
        commandManager.registerCommand("listusers", "", "list available users", this::listUsers);
        commandManager.registerCommand("selectuser", "<name>", "select a user", this::selectAuthUser);
        commandManager.registerCommand("addprofile", "", "add a profile", (Function<String[], Boolean>) this::openAddProfile);
        commandManager.registerCommand("login", "", "login", this::login);
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
            return true;
        }
        if (!getLauncher().profiles.getAllProfiles().containsKey(args[0])) {
            getLauncher().getLogger().error("Profile " + args[0] + " does not exist");
            return true;
        }
        try {
            ((ConsoleProfileScreen) getProfileScreen(getLauncher().profiles.getAllProfiles().get(args[0]))).init();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean listUsers(String[] args) {
        getLauncher().getLogger().info("Users:");
        tabulate(new String[] {"username", "uuid"}, getLauncher().auth.getRegisteredUsers().entrySet().stream().map(e -> new String[] {e.getKey(), e.getValue().toString()}).toArray(String[][]::new), getLauncher().getLogger()::info);
        return true;
    }

    private boolean selectAuthUser(String[] args) {
        if (args.length != 1) {
            getLauncher().getLogger().error("Usage: selectuser <name>");
            return true;
        }
        if (!getLauncher().auth.getRegisteredUsers().containsKey(args[0])) {
            getLauncher().getLogger().error("User " + args[0] + " does not exist");
            return true;
        }
        try {
            getLauncher().auth.setSelectedProfile(getLauncher().auth.getProfile(getLauncher().getLogger(), args[0], true));
        } catch (IOException | KeyStoreException | CertificateException | UnrecoverableEntryException | InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean login(String[] args) {
        openAddAccount();
        return true;
    }

    @Override
    public ProfileScreen getProfileScreen(Profile profile) {
        return new ConsoleProfileScreen(getLauncher(), getMainWindow(), profile);
    }

    private boolean openAddProfile(String[] args) {
        openAddProfile();
        return true;
    }

    @Override
    public ProfileCreateScreen openAddProfile() {
        try {
            return new ConsoleProfileCreateScreen(getLauncher(), getMainWindow());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AddAccountScreen openAddAccount() {
        return new ConsoleAddAccountScreen(getLauncher(), getMainWindow());
    }

}
