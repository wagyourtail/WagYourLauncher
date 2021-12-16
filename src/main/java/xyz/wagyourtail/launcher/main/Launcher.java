package xyz.wagyourtail.launcher.main;

import xyz.wagyourtail.launcher.minecraft.ProfileManager;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;

import java.io.IOException;
import java.nio.file.Path;

public abstract class Launcher {
    public final Path minecraftPath;
    public final ProfileManager profiles = new ProfileManager(this);

    protected Launcher(Path minecraftPath) throws IOException {
        this.minecraftPath = minecraftPath;
        profiles.populate();
    }

    public abstract void launch(Profile profile) throws IOException;
}
