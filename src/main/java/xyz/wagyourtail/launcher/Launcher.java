package xyz.wagyourtail.launcher;

import xyz.wagyourtail.launcher.minecraft.AssetsManager;
import xyz.wagyourtail.launcher.minecraft.AuthManager;
import xyz.wagyourtail.launcher.minecraft.LibraryManager;
import xyz.wagyourtail.launcher.minecraft.ProfileManager;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;
import xyz.wagyourtail.launcher.nogui.ConsoleLogger;
import xyz.wagyourtail.util.JavaUtils;
import xyz.wagyourtail.util.SemVerUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public abstract class Launcher {
    protected final Logger launcherLogs = new ConsoleLogger();

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

    public Logger getLogger() {
        return launcherLogs;
    }
    public abstract Logger getProfileLogger(Profile userProfile);

    public abstract void launch(Profile profile, String username, boolean offline) throws Exception;

    public Path getJavaDir(int version) throws IOException {
        Set<JavaUtils.JavaVersion> versions = JavaUtils.getVersions();
        if (versions.isEmpty()) {
            JavaUtils.refreshVersions();
            versions = JavaUtils.getVersions();
        }
        if (versions.isEmpty()) {
            throw new IOException("No Java version found");
        }
        String verMatchString = "^" + version + ".";
        if (version < 10) {
            verMatchString = "~1." + version + ".";
        }

        for (JavaUtils.JavaVersion v : versions) {
            if (SemVerUtils.matches(v.version(), verMatchString)) {
                return v.path();
            }
        }
        throw new IOException("No Java version found matching \"^" + version + ".\"");
    }

    public String getName() {
        return "WagYourLauncher";
    }

    public String getVersion() {
        return "1.0.0";
    }

    abstract public char[] promptKeystorePasswordAndWait(boolean isNew) throws InterruptedException;
}
