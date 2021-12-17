package xyz.wagyourtail.launcher.gui;

import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.LogListener;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;

import java.io.IOException;
import java.nio.file.Path;

public class LauncherGui extends Launcher {
    public LauncherGui(Path minecraftPath) throws IOException {
        super(minecraftPath);
        init();
    }

    @Override
    public LogListener getLogger(Profile profile) {
        return null;
    }

    @Override
    public void launch(Profile profile, String username) throws IOException {

    }

    protected void init() {

    }

}
