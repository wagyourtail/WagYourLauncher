package xyz.wagyourtail.launcher;

import xyz.wagyourtail.launcher.minecraft.AssetsManager;
import xyz.wagyourtail.launcher.minecraft.AuthManager;
import xyz.wagyourtail.launcher.minecraft.LibraryManager;
import xyz.wagyourtail.launcher.minecraft.ProfileManager;
import xyz.wagyourtail.launcher.minecraft.userProfile.Profile;
import xyz.wagyourtail.launcher.nogui.ConsoleLogListener;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public abstract class Launcher {
    protected final LogListener launcherLogs = new ConsoleLogListener();

    public final Path minecraftPath;
    public final ProfileManager profiles;
    public final AuthManager auth;
    public final LibraryManager libs;
    public final AssetsManager assets;
    public final Set<String> features = new HashSet<>();

    protected Launcher(Path minecraftPath) throws IOException {
        this.minecraftPath = minecraftPath;
        profiles = new ProfileManager(this);
        profiles.populate();
        auth = new AuthManager(this);
        libs = new LibraryManager(this);
        assets = new AssetsManager(this);
    }

    public LogListener getLogger() {
        return launcherLogs;
    }
    public abstract LogListener getProfileLogger(Profile userProfile);

    public abstract void launch(Profile userProfile, String username) throws Exception;

    public Path getJavaDir(int version) throws IOException {
        //TODO: default java dirs
        throw new IOException("failed to find java dir");
    }

    public String getName() {
        return "WagYourLauncher";
    }

    public String getVersion() {
        return "1.0.0";
    }

    abstract public char[] promptKeystorePasswordAndWait(boolean isNew) throws InterruptedException;
}
