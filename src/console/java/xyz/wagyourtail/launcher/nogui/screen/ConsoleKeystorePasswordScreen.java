package xyz.wagyourtail.launcher.nogui.screen;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.gui.screen.KeystorePasswordScreen;
import xyz.wagyourtail.launcher.gui.screen.MainScreen;

import java.io.Console;
import java.util.Scanner;
import java.util.function.Consumer;

public class ConsoleKeystorePasswordScreen extends ConsoleScreen implements KeystorePasswordScreen {
    private Consumer<char[]> r;
    private char[] password;

    public ConsoleKeystorePasswordScreen(LauncherBase launcher, MainScreen mainScreen) {
        super(launcher, mainScreen, "Keystore Password");
        init();
    }

    @Override
    public void init() {
        Console console = System.console();
        if (console == null) {
            getLauncher().getLogger().warn("No console object available, using \"insecure\" scanner... (run with java, not javaw)");
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter Keystore Password: ");
            scanner.nextLine().toCharArray();
        } else {
            password = console.readPassword("Enter Keystore Password: ");
        }
        synchronized (this) {
            if (r != null) {
                r.accept(password);
            }
        }
    }

    @Override
    public synchronized void then(Consumer<char[]> r) {
        this.r = r;
        if (password != null) {
            r.accept(password);
        }
    }

}
