package xyz.wagyourtail.launcher.versions;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.minecraft.data.VersionManifest;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

public class VanillaVersions implements BaseVersionProvider<VanillaVersions.VanillaVersion> {
    private final LauncherBase launcher;
    private final Map<String, VanillaVersion> versions = new HashMap<>();

    public VanillaVersions(LauncherBase launcher) {
        this.launcher = launcher;
    }

    @Override
    public String getName() {
        return "Vanilla";
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
    public List<VanillaVersion> getVersions() {
        return List.copyOf(versions.values());
    }

    @Override
    public VanillaVersion getLatestStable() throws IOException {
        VersionManifest.Version latest = VersionManifest.getLatestRelease();
        return versions.values().stream().filter(e -> e.version.equals(latest)).findFirst().orElse(null);
    }

    @Override
    public VanillaVersion getLatestSnapshot() throws IOException {
        VersionManifest.Version latest = VersionManifest.getLatestSnapshot();
        return versions.values().stream().filter(e -> e.version.equals(latest)).findFirst().orElse(null);
    }

    @Override
    public void refreshVersions() throws IOException {
        versions.clear();
        VersionManifest.refresh();
        for (VersionManifest.Version version : VersionManifest.getAllVersions().values()) {
            versions.put(version.id(), new VanillaVersion(version));
        }
    }

    @Override
    public BaseVersionData byId(String id) {
        return null;
    }

    @Override
    public String[] versionFilters() {
        return Arrays.stream(VersionManifest.Type.values()).map(e -> e.id).toArray(String[]::new);
    }

    public record VanillaVersion(VersionManifest.Version version) implements BaseVersionData {

        @Override
        public URL getIconUrl() {
            return null;
        }

        @Override
        public String getId() {
            return version.id();
        }

        @Override
        public String[] getTableParts() {
            return new String[] {
                version.id(),
                Instant.ofEpochMilli(version.releaseTime()).toString().substring(0, 10),
                version.type().id
            };
        }

        @Override
        public boolean hasSubProviders() {
            return false;
        }

        @Override
        public String[] filterMatches() {
            return new String[] { version.type().id };
        }

        @Override
        public BaseVersionProvider<?> getSubProvider() {
            return null;
        }

        @Override
        public String provide() {
            // vanilla versions don't have to be downloaded as they are automagically downloaded when launched for the first time
            return version.id();
        }

    }

}
