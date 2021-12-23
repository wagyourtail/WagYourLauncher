package xyz.wagyourtail.launcher.main;

import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.gui.LauncherGui;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;
import xyz.wagyourtail.launcher.nogui.LauncherNoGui;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

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
        if (args.has("--launch")) {
            String profileName = args.get("--launch").get()[1];
            String username = args.get("--username").orElseThrow(() -> new IllegalArgumentException("No username specified"))[1];
            Launcher launcher;
            if (args.has("--nogui")) {
                launcher = new LauncherNoGui(path);
            } else {
                launcher = new LauncherGui(path);
            }
            launchProfile(launcher, profileName, username);
            return;
        }
        if (args.has("--nogui")) {
            new LauncherNoGui(path).run();
        } else {
            new LauncherGui(path);
        }
    }

    public static void launchProfile(Launcher launcher, String name, String username) throws Exception {
        Optional<Profile> byId = launcher.profiles.getProfileById(name);
        if (byId.isEmpty()) {
            List<Profile> byName = launcher.profiles.getProfileByName(name);
            if (byName.isEmpty()) {
                throw new IOException("No userProfile found with the name " + name);
            } else if (byName.size() > 1) {
                launcher.getLogger().info("Multiple profiles found with the name " + name);
                Scanner scanner = new Scanner(System.in);
                System.out.println("Please select a userProfile:");
                for (int i = 0; i < byName.size(); i++) {
                    Profile prof = byName.get(i);
                    launcher.getLogger().info((i + 1) + "\t" + prof.key() + "\t" + prof.name() + "\t(" + prof.lastVersionId() + ")");
                }
                System.out.print("> ");
                int choice = scanner.nextInt();
                if (choice < 1 || choice > byName.size()) {
                    throw new IOException("Invalid choice");
                }
                launcher.launch(byName.get(choice - 1), username, false);
            } else {
                launcher.launch(byName.get(0), username, false);
            }
        } else {
            launcher.launch(byId.get(), username, false);
        }
    }
}
