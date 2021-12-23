package xyz.wagyourtail.launcher.minecraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;
import xyz.wagyourtail.launcher.minecraft.version.Version;
import xyz.wagyourtail.util.OSUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class AssetsManager {
    public static final String ASSET_BASE_URL = "https://resources.download.minecraft.net/";

    public final Launcher launcher;
    protected final Path dir;

    public AssetsManager(Launcher launcher) {
        this.launcher = launcher;
        this.dir = launcher.minecraftPath.resolve("assets").toAbsolutePath();
    }

    public Path resolveAssets(Profile profile, Version.AssetIndex assetIndex) throws IOException {
        Path index = dir.resolve("indexes").resolve(assetIndex.id() + ".json");
        if (Files.exists(index)) {
            if (Files.size(index) != assetIndex.size() || !LibraryManager.shaMatch(index, assetIndex.sha1())) {
                launcher.getLogger().warn("Assets index \"" + assetIndex.id() + "\" doesn't match!");
                Files.delete(index);
            }
        }

        if (!Files.exists(index)) {
            // 3 tries
            for (int i = 0; i < 3; i++) {
                try {
                    launcher.getLogger().trace("Downloading assets index \"" + assetIndex.id() + "\"...");
                    Files.createDirectories(index.getParent());
                    Path tmp = index.getParent().resolve(index.getFileName() + ".tmp");
                    try (InputStream stream = assetIndex.url().openStream()) {
                        Files.write(tmp, stream.readAllBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    }
                    if (LibraryManager.shaMatch(tmp, assetIndex.sha1())) {
                        Files.move(tmp, index, StandardCopyOption.REPLACE_EXISTING);
                        break;
                    } else {
                        throw new IOException("SHA1 doesn't match!");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!Files.exists(index)) {
            throw new IOException("Failed to download assets index \"" + assetIndex.id() + "\"!");
        }

        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(index))) {
            resolveAssets(profile, JsonParser.parseReader(reader).getAsJsonObject());
        }

        return dir;
    }

    public void resolveAssets(Profile profile, JsonObject indexJson) throws IOException {
        boolean copyToResources = indexJson.has("map_to_resources") && indexJson.get("map_to_resources").getAsBoolean();
        for (String key : indexJson.keySet()) {
            Path keyDir = dir.resolve(key);
            if (indexJson.get(key).isJsonObject()) {
                for (Map.Entry<String, JsonElement> asset : indexJson.getAsJsonObject(key).entrySet()) {
                    String hash = asset.getValue().getAsJsonObject().get("hash").getAsString();
                    Path assetPath = keyDir.resolve(hash.substring(0, 2)).resolve(hash);
                    if (Files.exists(assetPath)) {
                        boolean size = Files.size(assetPath) == asset.getValue().getAsJsonObject().get("size").getAsLong();
                        boolean sha = LibraryManager.shaMatch(assetPath, hash);
                        if (!size || !sha) {
                            launcher.getLogger().warn("Asset \"" + key + "\" \"" + asset.getKey() + "\" doesn't match! size: " + size + ", sha: " + sha);
                            Files.delete(assetPath);
                        }
                    }

                    if (!Files.exists(assetPath)) {
                        for (int i = 0; i < 3; i++) {
                            try {
                                launcher.getLogger().trace(
                                    "Downloading asset \"" + key + ":" + asset.getKey() + "\"...");
                                Path tmp = assetPath.getParent().resolve(assetPath.getFileName() + ".tmp");
                                Files.createDirectories(tmp.getParent());
                                try (
                                    InputStream stream = new URL(
                                        ASSET_BASE_URL + hash.substring(0, 2) + "/" + hash).openStream()
                                ) {
                                    Files.write(
                                        tmp,
                                        stream.readAllBytes(),
                                        StandardOpenOption.CREATE,
                                        StandardOpenOption.WRITE
                                    );
                                }
                                if (LibraryManager.shaMatch(tmp, hash)) {
                                    Files.move(tmp, assetPath, StandardCopyOption.REPLACE_EXISTING);
                                    break;
                                } else {
                                    throw new IOException("SHA1 doesn't match!");
                                }
                            } catch (IOException e) {
                                launcher.getLogger().warn(
                                    "Failed to download asset \"" + key + ": " + asset.getKey() + "\"!" + " (" +
                                        (i + 1) + "/3)");
                                e.printStackTrace();
                            }
                        }
                    }

                    if (!Files.exists(assetPath)) {
                        throw new IOException("Failed to download asset \"" + key + ": " + asset.getKey() + "\"!");
                    }

                    if (copyToResources) {
                        Path resourcePath = profile.gameDir().resolve("resources").resolve(asset.getKey());
                        Files.createDirectories(resourcePath.getParent());
                        if (OSUtils.getOSId().equals("windows")) {
                            //TODO: windows doesn't support symlinks??? without admin anyway...
                            Files.copy(assetPath, resourcePath, StandardCopyOption.REPLACE_EXISTING);
                        } else if (!Files.exists(resourcePath)) {
                            Files.createLink(resourcePath, assetPath);
                        }
                    }
                }
            }
        }
    }

    public Path resolveLogging(Version.Logging.Client.File file) throws IOException {
        Path path = dir.resolve("log_configs").resolve(file.id());
        if (Files.exists(path)) {
            if (Files.size(path) != file.size() || !LibraryManager.shaMatch(path, file.sha1())) {
                launcher.getLogger().warn("Logging config \"" + file.id() + "\" doesn't match!");
                Files.delete(path);
            } else {
                return path;
            }
        }

        if (!Files.exists(path)) {
            for (int i = 0; i < 3; i++) {
                try {
                    launcher.getLogger().trace("Downloading logging config \"" + file.id() + "\"...");
                    Path tmp = path.getParent().resolve(path.getFileName() + ".tmp");
                    Files.createDirectories(tmp.getParent());
                    try (InputStream stream = file.url().openStream()) {
                        Files.write(tmp, stream.readAllBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    }
                    if (LibraryManager.shaMatch(tmp, file.sha1())) {
                        Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        throw new IOException("SHA1 doesn't match!");
                    }
                } catch (IOException e) {
                    launcher.getLogger().warn("Failed to download logging config \"" + file.id() + "\"!" + " (" + (i + 1) + "/3)");
                    e.printStackTrace();
                }
            }
        }

        if (!Files.exists(path)) {
            throw new IOException("Failed to download logging config \"" + file.id() + "\"!");
        }

        return path;
    }

}
