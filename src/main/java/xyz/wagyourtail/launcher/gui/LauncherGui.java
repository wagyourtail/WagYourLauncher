package xyz.wagyourtail.launcher.gui;

import xyz.wagyourtail.launcher.main.Launcher;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;

import java.io.IOException;
import java.nio.file.Path;

public class LauncherGui extends Launcher {
    public LauncherGui(Path minecraftPath) throws IOException {
        super(minecraftPath);
        init();
    }

    @Override
    public void launch(Profile profile) throws IOException {

    }

    protected void init() {

    }

}
