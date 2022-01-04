package xyz.wagyourtail.launcher.nogui;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.notlog4j.Logger;
import xyz.wagyourtail.launcher.auth.common.GetProfile;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;
import xyz.wagyourtail.notlog4j.ConsoleLogger;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class LauncherNoGui extends LauncherBase {

    public LauncherNoGui(Path minecraftPath) throws IOException {
        super(minecraftPath);
    }

    public static void launchProfile(LauncherBase launcher, String name, String username) throws Exception {
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

    @Override
    public void error(String message) {
        getLogger().error(message);
    }

    @Override
    public void error(String message, Throwable t) {
        getLogger().error(message);
        t.printStackTrace();
    }

    @Override
    protected void init() throws IOException {

    }

    @Override
    public void refreshLaunchedProfiles() {

    }

    @Override
    public char[] promptKeystorePasswordAndWait(boolean isNew) {
        Console console = System.console();
        if (console == null) {
            launcherLogs.warn("No console available, using insecure scanner... (run with java, not javaw)");
        } else {
            return console.readPassword("Enter keystore password: ");
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter keystore password: ");
        return scanner.nextLine().toCharArray();
    }

    public void listProfiles() {
        launcherLogs.info("Profiles:");
        int[] tableSizes = new int[3];
        for (Map.Entry<String, Profile> entry : profiles.getAllProfiles().entrySet()) {
            tableSizes[0] = Math.max(tableSizes[0], entry.getKey().length());
            tableSizes[1] = Math.max(tableSizes[1], entry.getValue().name().length());
            tableSizes[2] = Math.max(tableSizes[2], entry.getValue().lastVersionId().length());
        }
        launcherLogs.info("\t" + rPadTo("id", tableSizes[0]) + "\t" + rPadTo("name", tableSizes[1]) + "\t" + rPadTo("(version)", tableSizes[2]));
        launcherLogs.info(dashes(tableSizes[0] + tableSizes[1] + tableSizes[2] + 12));
        for (Map.Entry<String, Profile> prof : profiles.getAllProfiles().entrySet()) {
            launcherLogs.info("\t" + rPadTo(prof.getKey(), tableSizes[0]) + "\t" + rPadTo(prof.getValue().name(), tableSizes[1]) + "\t" + rPadTo(prof.getValue().lastVersionId(), tableSizes[2]));
        }
    }

    public void listUsers() {
        launcherLogs.info("Users:");
        int[] tableSizes = new int[2];
        for (Map.Entry<String, UUID> stringUUIDEntry : auth.getRegisteredUsers().entrySet()) {
            tableSizes[0] = Math.max(tableSizes[0], stringUUIDEntry.getKey().length());
            tableSizes[1] = Math.max(tableSizes[1], stringUUIDEntry.getValue().toString().length());
        }
        launcherLogs.info("\t" + rPadTo("username", tableSizes[0]) + "\t" + rPadTo("uuid", tableSizes[1]));
        launcherLogs.info(dashes(tableSizes[0] + tableSizes[1] + 12));
        for (Map.Entry<String, UUID> stringUUIDEntry : auth.getRegisteredUsers().entrySet()) {
            launcherLogs.info("\t" + rPadTo(stringUUIDEntry.getKey(), tableSizes[0]) + "\t" + rPadTo(stringUUIDEntry.getValue().toString(), tableSizes[1]));
        }
    }

    public void run() throws InterruptedException {
        CommandManager profiles = new CommandManager(this)
            .registerCommand("list", "", "Lists all profiles", (args) -> {
                listProfiles();
                return true;
            });
        CommandManager users = new CommandManager(this)
            .registerCommand("list", "", "Lists all users", (args) -> {
                listUsers();
                return true;
            })
            .registerCommand("listauth", "", "List available auth methods", (args) -> {
                launcherLogs.info("Available auth methods:");
                for (String s : auth.authProviders.keySet()) {
                    launcherLogs.info("\t" + s);
                }
                return true;
            })
            .registerCommand("adduser", "<provider>", "Adds a user", (args) -> {
                if (args.length != 2) {
                    launcherLogs.warn("Usage: adduser <provider>");
                    return true;
                }
                String provider = args[1];
                if (!auth.authProviders.containsKey(provider)) {
                    launcherLogs.error("Unknown auth provider: " + provider);
                    return true;
                }
                try {
                    GetProfile.MCProfile profile = auth.authProviders.get(provider).withLogger(launcherLogs, null);
                    if (profile != null) {
                        launcherLogs.info("Successfully added user " + profile.name() + " (" + profile.id() + ")");
                    }
                } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException | InvalidKeySpecException | InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            });
        CommandManager base = new CommandManager(this)
            .registerCommand("profiles", "", "open profile manager", s -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    synchronized (System.err) {
                        try {
                            System.err.wait(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.print("Profiles $ ");
                    String line = scanner.nextLine();
                    if (!profiles.run(line)) break;
                }
                return true;
            })
            .registerCommand("users", "", "open user manager", s -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    synchronized (System.err) {
                        try {
                            System.err.wait(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.print("Users $ ");
                    String line = scanner.nextLine();
                    if (!users.run(line)) break;
                }
                return true;
            })
            .registerCommand("launch", "<profile> <username>", "Launches a profile", s -> {
                if (s.length < 3) {
                    System.err.println("Not enough arguments");
                    return true;
                }
                try {
                    launchProfile(this, s[1], s[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                    return true;
                }
                return false;
            });

        Scanner scanner = new Scanner(System.in);
        while (true) {
            synchronized (System.err) {
                System.err.wait(200);
            }
            System.out.print("$ ");
            String line = scanner.nextLine();
            if (!base.run(line)) break;
        }
    }


}
