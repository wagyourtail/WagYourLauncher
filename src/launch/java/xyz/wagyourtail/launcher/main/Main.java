package xyz.wagyourtail.launcher.main;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.nogui.LauncherNoGui;
import xyz.wagyourtail.launcher.swing.SwingWindowManager;

import java.nio.file.Path;

public class Main {
    public static final ArgHandler argHandler = new ArgHandler(
        // Arg, desc, length, aliases
        new ArgHandler.Arg("--help", "Prints the help message", 1, "-h"),
        new ArgHandler.Arg("--launch", "Directly launch the specified userProfile", 2, "-lp"),
        new ArgHandler.Arg("--username", "The username to use when logging in", 2, "-u"),
        new ArgHandler.Arg("--nogui", "Disables the GUI", 1, "-n"),
        new ArgHandler.Arg("--path", "the path of the .minecraft folder, defaults to ./", 2, "-p")
    );

    public static void main(String[] args) throws Exception {
        main(argHandler.parseStringArgs(args));
    }

    public static void main(ArgHandler.ParsedArgs args) throws Exception {
        if (args.has("--help")) {
            argHandler.printHelp();
            return;
        }
        Path path = args.get("--path").map(e -> Path.of(e[1])).orElse(Path.of("./"));
        //TODO fix:, probably another source set for a launch args version...
//        if (args.has("--launch")) {
//            String profileName = args.get("--launch").get()[1];
//            String username = args.get("--username").map(e -> e[1]).orElse(null);
//            LauncherBase launcher;
//            if (args.has("--nogui")) {
//                launcher = new LauncherNoGui(path);
//            } else {
//                launcher = new SwingWindowManager(path);
//            }
//            if (username != null) {
//                launcher.auth.setSelectedProfile(launcher.auth.getProfile(launcher.getLogger(), username, false));
//            }
//            launcher.mainWindow.launchProfile(launcher.profiles.getProfileById(profileName).orElseThrow(), false);
//            return;
//        }
        if (args.has("--nogui")) {
            new LauncherNoGui(path).init();
        } else {
            new SwingWindowManager(path).init();
        }
    }

}
