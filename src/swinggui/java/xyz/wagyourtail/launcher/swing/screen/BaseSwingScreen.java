package xyz.wagyourtail.launcher.swing.screen;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.gui.screen.MainScreen;
import xyz.wagyourtail.launcher.gui.screen.Screen;

import javax.swing.*;

public abstract class BaseSwingScreen extends JFrame implements Screen {
    protected final LauncherBase launcher;
    protected final MainScreen parent;

    protected BaseSwingScreen(LauncherBase launcher, MainScreen mainWindow) {
        this.launcher = launcher;
        this.parent = mainWindow;
    }


    @Override
    public LauncherBase getLauncher() {
        return launcher;
    }

    @Override
    public void error(String error) {
        getLauncher().error(error);
    }

    @Override
    public void error(String error, Throwable e) {
        getLauncher().error(error, e);
    }

    @Override
    public void close() {
        dispose();
    }

    @Override
    public MainScreen getMainWindow() {
        return parent;
    }

}
