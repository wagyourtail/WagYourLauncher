package xyz.wagyourtail.launcher.versions;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public interface BaseVersionProvider<T extends BaseVersionProvider.BaseVersionData> {

    String getName();

    boolean hasIcons();

    String[] getTableHeaders();

    List<T> getVersions() throws IOException;

    T getLatestStable() throws IOException;

    T getLatestSnapshot() throws IOException;

    void refreshVersions() throws IOException;

    BaseVersionData byId(String id);

    String[] versionFilters();

    interface BaseVersionData {

        URL getIconUrl();

        String getId();

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
