package xyz.wagyourtail.launcher.nogui.screen.login;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.gui.screen.MainScreen;
import xyz.wagyourtail.launcher.gui.screen.login.UsernamePasswordScreen;
import xyz.wagyourtail.launcher.nogui.screen.ConsoleScreen;

import java.io.Console;
import java.util.Scanner;
import java.util.function.BiConsumer;

public class ConsoleUsernamePasswordScreen extends ConsoleScreen implements UsernamePasswordScreen {
    private BiConsumer<String, char[]> r;
    private String username;
    private char[] password;

    public ConsoleUsernamePasswordScreen(LauncherBase launcher, MainScreen mainScreen) throws InterruptedException {
        super(launcher, mainScreen, "Username/Password");
        init();
    }

    @Override
    public void init() {
        Console console = System.console();
        if (console == null) {
            getLauncher().getLogger().warn("No console object available, using \"insecure\" scanner... (run with java, not javaw)");

            Scanner scanner = new Scanner(System.in);
            System.out.println("Username: ");
            username = scanner.nextLine();
            System.out.print("Password: ");
            scanner.nextLine().toCharArray();
        } else {
            username = console.readLine("Username: ");
            password = console.readPassword("Password: ");
        }
        synchronized (this) {
            if (r != null) {
                r.accept(username, password);
            }
        }
    }

    @Override
    public synchronized void then(BiConsumer<String, char[]> r) {
        this.r = r;
        if (username != null && password != null) {
            r.accept(username, password);
        }
    }

}
