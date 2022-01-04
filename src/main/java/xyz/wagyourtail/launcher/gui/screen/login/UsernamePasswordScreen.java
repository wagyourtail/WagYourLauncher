package xyz.wagyourtail.launcher.gui.screen.login;

import xyz.wagyourtail.launcher.gui.screen.Screen;

import java.util.function.BiConsumer;

public interface UsernamePasswordScreen extends Screen {

    void then(BiConsumer<String, char[]> r);

}
