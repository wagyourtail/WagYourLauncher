package xyz.wagyourtail.launcher;

import xyz.wagyourtail.launcher.minecraft.AssetsManager;
import xyz.wagyourtail.launcher.minecraft.AuthManager;
import xyz.wagyourtail.launcher.minecraft.LibraryManager;
import xyz.wagyourtail.launcher.minecraft.ProfileManager;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public abstract class Launcher {
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

    public abstract LogListener getLogger(Profile profile);

    public abstract void launch(Profile profile, String username) throws Exception;

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
}
