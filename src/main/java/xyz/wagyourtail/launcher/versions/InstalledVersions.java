package xyz.wagyourtail.launcher.versions;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.minecraft.version.Version;
import xyz.wagyourtail.util.SemVerUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InstalledVersions implements BaseVersionProvider<InstalledVersions.InstalledVersion> {
    private final LauncherBase launcher;
    private final Path versionsPath;
    private final List<InstalledVersion> versions = new ArrayList<>();

    public InstalledVersions(LauncherBase launcher) {
        this.launcher = launcher;
        versionsPath = launcher.minecraftPath.resolve("versions");
    }

    @Override
    public boolean hasIcons() {
        return false;
    }

    @Override
    public String[] getTableHeaders() {
        return new String[] {"Version", "Released", "Type"};
    }

    @Override
    public List<InstalledVersion> getVersions() {
        return versions;
    }

    @Override
    public InstalledVersion getLatestStable() {
        return versions.stream().filter(e -> e.version.type().equals("release")).min((a, b) -> {
            if (SemVerUtils.isSemVer(a.version.id())) {
                if (SemVerUtils.isSemVer(b.version.id())) {
                    return SemVerUtils.SemVer.create(a.version.id())
                        .compareTo(SemVerUtils.SemVer.create(b.version.id()));
                }
                return -1;
            } else {
                if (SemVerUtils.isSemVer(b.version.id())) {
                    return 1;
                }
                return -1;
            }
        }).orElse(null);
    }

    @Override
    public InstalledVersion getLatestSnapshot() {
        return versions.stream().min((a, b) -> {
            if (SemVerUtils.isSemVer(a.version.id())) {
                if (SemVerUtils.isSemVer(b.version.id())) {
                    return SemVerUtils.SemVer.create(a.version.id())
                        .compareTo(SemVerUtils.SemVer.create(b.version.id()));
                }
                return -1;
            } else {
                if (SemVerUtils.isSemVer(b.version.id())) {
                    return 1;
                }
                return -1;
            }
        }).orElse(null);
    }

    @Override
    public void refreshVersions() throws IOException {
        versions.clear();
        if (!Files.exists(versionsPath)) {
            return;
        }
        for (Path p : Files.list(versionsPath).toList()) {
            try {
                Version version = Version.resolve(launcher, p.getFileName().toString());
                versions.add(new InstalledVersion(version));
            } catch (IOException e) {
                launcher.error("Failed to load version " + p.getFileName().toString(), e);
            }
        }
    }

    @Override
    public String[] versionFilters() {
        return new String[] {
            "Release",
            "Snapshot"
        };
    }

    public record InstalledVersion(Version version) implements BaseVersionData {

        @Override
        public URL getIconUrl() {
            return null;
        }

        @Override
        public String[] getTableParts() {
            return new String[] {
                version.id(),
                Instant.ofEpochMilli(version.releaseTime()).toString().substring(0, 10),
                version.type()
            };
        }

        @Override
        public boolean hasSubProviders() {
            return false;
        }

        @Override
        public String[] filterMatches() {
            String type = version.type();
            type = type.substring(0, 1).toUpperCase(Locale.ROOT) + type.substring(1);
            return new String[] { type };
        }

        @Override
        public BaseVersionProvider<?> getSubProvider() {
            return null;
        }

        @Override
        public String provide() {
            return version.id();
        }

    }
}
