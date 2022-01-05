package xyz.wagyourtail.launcher.minecraft.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VersionManifest {

    private static Map<String, Version> versions = null;
    private static String latestRelease = null;
    private static String latestSnapshot = null;


    public static URL getManifestURL() {
        try {
            return new URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Optional<JsonElement> get(JsonObject json, String key) {
        return Optional.ofNullable(json.get(key));
    }

    public static void refresh() throws IOException {
        String data = new String(getManifestURL().openStream().readAllBytes(), StandardCharsets.UTF_8);
        versions = new LinkedHashMap<>();

        JsonObject json = JsonParser.parseString(data).getAsJsonObject();

        JsonObject latest = json.getAsJsonObject("latest");
        latestRelease = latest.get("release").getAsString();
        latestSnapshot = latest.get("snapshot").getAsString();

        JsonArray versions = json.getAsJsonArray("versions");
        for (JsonElement entry : versions) {
            JsonObject version = entry.getAsJsonObject();
            VersionManifest.versions.put(
                version.get("id").getAsString(),
                new Version(
                    version.get("id").getAsString(),
                    Type.byId(version.get("type").getAsString()),
                    new URL(get(version, "url").map(JsonElement::getAsString).orElse("")),
                    get(version, "time").map(e -> Instant.parse(e.getAsString()).toEpochMilli()).orElse(0L),
                    get(version, "releaseTime").map(e -> Instant.parse(e.getAsString()).toEpochMilli()).orElse(0L),
                    get(version, "sha1").map(JsonElement::getAsString).orElse(""),
                    get(version, "complianceLevel").map(JsonElement::getAsInt).orElse(0)
                )
            );
        }
    }

    public static Version getVersion(String byID) throws IOException {
        if (versions == null) {
                refresh();
        }
        return versions.get(byID);
    }

    public static Version getLatestRelease() throws IOException {
        if (versions == null || latestRelease == null) {
            refresh();
        }
        return versions.get(latestRelease);
    }

    public static Version getLatestSnapshot() throws IOException {
        if (versions == null || latestSnapshot == null) {
            refresh();
        }
        return versions.get(latestSnapshot);
    }

    public static Map<String, Version> getAllVersions() throws IOException {
        if (versions == null) {
            refresh();
        }
        return new LinkedHashMap<>(versions);
    }

    public enum Type {
        RELEASE("release"),
        SNAPSHOT("snapshot"),
        OLD_BETA("old_beta"),
        OLD_ALPHA("old_alpha"),
        EXPERIMENTAL("experimental");

        public final String id;

        Type(String id) {
           this.id = id;
        }

        public String getId() {
            return id;
        }

        private static final Map<String, Type> byId = Arrays.stream(Type.values()).collect(Collectors.toMap(Type::getId, Function.identity()));

        public static Type byId(String id) {
            return byId.get(id);
        }
    }

    public record Version(String id, Type type, URL url, long time, long releaseTime, String sha1, int complianceLevel) {}


    // TODO: hard-code missing versions (like experiments)
}
