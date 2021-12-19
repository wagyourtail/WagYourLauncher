package xyz.wagyourtail.launcher.gui;

import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.LogListener;
import xyz.wagyourtail.launcher.minecraft.userProfile.Profile;

import java.io.IOException;
import java.nio.file.Path;

public class LauncherGui extends Launcher {
    public LauncherGui(Path minecraftPath) throws IOException {
        super(minecraftPath);
        init();
    }

    @Override
    public LogListener getProfileLogger(Profile userProfile) {
        return null;
    }

    @Override
    public void launch(Profile userProfile, String username) throws IOException {

    }

    @Override
    public char[] promptKeystorePasswordAndWait(boolean isNew) throws InterruptedException {
        return null;
    }

    protected void init() {

    }

}
