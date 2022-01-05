package xyz.wagyourtail.launcher.gui.screen;

import java.util.function.Consumer;

public interface KeystorePasswordScreen extends Screen {
    void then(Consumer<char[]> r);
}
