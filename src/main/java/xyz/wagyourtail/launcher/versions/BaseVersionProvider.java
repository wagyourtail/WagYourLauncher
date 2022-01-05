package xyz.wagyourtail.launcher.versions;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public interface BaseVersionProvider<T extends BaseVersionProvider.BaseVersionData> {

    boolean hasIcons();

    String[] getTableHeaders();

    List<T> getVersions() throws IOException;

    T getLatestStable() throws IOException;

    T getLatestSnapshot() throws IOException;

    void refreshVersions() throws IOException;

    String[] versionFilters();

    interface BaseVersionData {

        URL getIconUrl();

        String[] getTableParts();

        boolean hasSubProviders();

        String[] filterMatches();

        /**
         * @return sub version info
         */
        BaseVersionProvider<?> getSubProvider();

        /**
         * downloads the version and provides the version id
         * @return the version id
         */
        String provide();

    }
}
