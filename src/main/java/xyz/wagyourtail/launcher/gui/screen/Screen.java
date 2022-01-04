package xyz.wagyourtail.launcher.gui.screen;

import xyz.wagyourtail.launcher.LauncherBase;

public interface Screen {
    LauncherBase getLauncher();

    void error(String error);

    void error(String error, Throwable e);

    void close();

    MainScreen getMainWindow();
}
