package xyz.wagyourtail.launcher.nogui.screen;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.gui.screen.MainScreen;
import xyz.wagyourtail.launcher.gui.screen.Screen;
import xyz.wagyourtail.launcher.nogui.CommandManager;

import java.util.Scanner;

public class ConsoleScreen implements Screen {
    private final CommandManager commandManager = new CommandManager();
    private final LauncherBase launcher;
    private final MainScreen mainScreen;
    private final String screenName;

    protected ConsoleScreen(LauncherBase launcher, MainScreen mainScreen, String name) {
        this.launcher = launcher;
        this.mainScreen = mainScreen;
        this.screenName = name;
    }


    @Override
    public LauncherBase getLauncher() {
        return launcher;
    }

    @Override
    public void error(String error) {
        launcher.error(error);
    }

    @Override
    public void error(String error, Throwable e) {
        launcher.error(error, e);
    }

    @Override
    public void close() {

    }

    @Override
    public MainScreen getMainWindow() {
        return mainScreen;
    }


    public void init() throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            synchronized (System.err) {
                System.err.wait(200);
            }
            System.out.print("[" + screenName + "]~$ ");
            String line = scanner.nextLine();
            if (!commandManager.run(line)) break;
        }
    }

    public static String dashes(int len) {
        String s = "";
        for (int i = 0; i < len; i++) s += "-";
        return s;
    }

    public static String rPadTo(String s, int len) {
        while (s.length() < len) s += " ";
        return s;
    }

}
