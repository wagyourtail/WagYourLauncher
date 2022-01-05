package xyz.wagyourtail.launcher.nogui.screen.profile;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.gui.screen.MainScreen;
import xyz.wagyourtail.launcher.gui.screen.profile.ProfileCreateScreen;
import xyz.wagyourtail.launcher.nogui.screen.ConsoleScreen;
import xyz.wagyourtail.launcher.versions.BaseVersionProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

public class ConsoleProfileCreateScreen extends ConsoleScreen implements ProfileCreateScreen {
    private String name;
    private BaseVersionProvider.BaseVersionData version;

    public ConsoleProfileCreateScreen(LauncherBase launcher, MainScreen mainScreen) throws InterruptedException {
        super(launcher, mainScreen, "Create profile");
        commandManager.registerCommand("setname", "<name>", "Sets the name of the profile", this::setName);
        commandManager.registerCommand("listProviders", "",  "Lists the available version providers", this::listProviders);
        commandManager.registerCommand("listVersions", "<provider> [version] <filter>", "Lists the available versions for the given provider", this::listVersions);
        commandManager.registerCommand("selectVersion", "<provider> <version> [<subversion>]", "Selects the given version", this::selectVersion);
        commandManager.registerCommand("createProfile", "", "Creates the profile", (Function<String[], Boolean>) this::createProfile);
        init();
    }

    public boolean setName(String[] args) {
        name = String.join(" ", args);
        return true;
    }

    public boolean listProviders(String[] args) {
        BaseVersionProvider<?>[] providers = getLauncher().profiles.versionProviders;
        getLauncher().getLogger().info("Available version providers:");
        for (BaseVersionProvider<?> provider : providers) {
            getLauncher().getLogger().info("  " + provider.getName() + " (filters: " + Arrays.toString(provider.versionFilters()) + ")");
        }
        return true;
    }

    public boolean listVersions(String[] args) {
        if (args.length < 2) {
            getLauncher().getLogger().info("Usage: listVersions <provider> <filter>");
            return true;
        }
        BaseVersionProvider<?> provider = Arrays.stream(getLauncher().profiles.versionProviders).filter(e -> e.getName().toLowerCase(Locale.ROOT).equals(args[0].toLowerCase(Locale.ROOT))).findFirst().orElse(null);
        if (provider == null) {
            getLauncher().getLogger().error("Provider not found");
            return true;
        }
        String filter = args[1];
        if (args.length > 2) {
            provider = provider.byId(args[1]).getSubProvider();
            filter = args[2];
        }
        getLauncher().getLogger().info("Available versions for provider " + provider.getName() + ":");
        try {
            String[] headers = provider.getTableHeaders();
            String finalFilter = filter;
            String[][] data = provider.getVersions().stream().filter(e -> Arrays.stream(e.filterMatches()).anyMatch(finalFilter::equalsIgnoreCase)).map(BaseVersionProvider.BaseVersionData::getTableParts).toArray(String[][]::new);
            tabulate(headers, data, getLauncher().getLogger()::info);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean selectVersion(String[] args) {
        if (args.length < 2) {
            getLauncher().getLogger().info("Usage: selectVersion <provider> <version> [<subversion>]");
            return true;
        }
        BaseVersionProvider<?> provider = Arrays.stream(getLauncher().profiles.versionProviders).filter(e -> e.getName().toLowerCase(Locale.ROOT).equals(args[0].toLowerCase(Locale.ROOT))).findFirst().orElse(null);
        if (provider == null) {
            getLauncher().getLogger().error("Provider not found");
            return true;
        }
        BaseVersionProvider.BaseVersionData version = provider.byId(args[1]);
        if (version == null) {
            getLauncher().getLogger().error("Version not found");
            return true;
        }
        if (args.length > 2) {
            BaseVersionProvider.BaseVersionData subversion = version.getSubProvider().byId(args[2]);
            if (subversion == null) {
                getLauncher().getLogger().error("Subversion not found");
                return true;
            }
            version = subversion;
        }
        this.version = version;
        return true;
    }

    public boolean createProfile(String[] args) {
        try {
            createProfile();
        } catch (IOException e) {
            getLauncher().error("Failed to create profile", e);
        }
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BaseVersionProvider.BaseVersionData getSelectedVersion() {
        return version;
    }

}
