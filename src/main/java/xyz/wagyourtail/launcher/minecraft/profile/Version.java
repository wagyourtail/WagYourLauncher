package xyz.wagyourtail.launcher.minecraft.profile;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.minecraft.data.VersionManifest;
import xyz.wagyourtail.util.OSUtils;
import xyz.wagyourtail.util.SemVerUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public record Version(
        Arguments arguments,
        AssetIndex assetIndex,
        String assets,
        int complianceLevel,
        Map<String, Download> downloads,
        String id,
        JavaVersion javaVersion,
        Library[] libraries,
        Logging logging,
        String mainClass,
        int minimumLauncherVersion,
        long releaseTime,
        long time,
        String type
    ) {

    private static final Map<String, Version> resolveCache = new HashMap<>();



    public static Version resolve(Launcher launcher, String versionId) throws IOException {
        if (versionId.equals("latest-snapshot")) {
            return resolve(launcher, VersionManifest.getLatestSnapshot().id());
        } else if (versionId.equals("latest-release")) {
            return resolve(launcher, VersionManifest.getLatestRelease().id());
        }
        if (resolveCache.containsKey(versionId)) {
            return resolveCache.get(versionId);
        }
        Path versionPath = launcher.minecraftPath.resolve("versions").resolve(versionId);
        if (!Files.exists(versionPath)) {
            Files.createDirectories(versionPath);
        }

        // check if version exists, if not download it
        Path versionJson = versionPath.resolve(versionId + ".json");
        if (!Files.exists(versionJson)) {
            VersionManifest.Version manifest = VersionManifest.getVersion(versionId);
            if (manifest == null) {
                throw new IOException("Version not found");
            } else {
                // write the version.json
                try (InputStream is = manifest.url().openStream()) {
                    try (OutputStream os = Files.newOutputStream(versionJson, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                        is.transferTo(os);
                    }
                }
            }
        }

        Version version;
        // parse the version.json
        try (InputStreamReader isr = new InputStreamReader(Files.newInputStream(versionJson))) {
            version = Version.parse(isr);
        }

        // check if outdated
        VersionManifest.Version manifest = VersionManifest.getVersion(versionId);
        if (manifest != null && manifest.time() > version.time()) {
            // download the new version
            try (InputStream is = manifest.url().openStream()) {
                try (OutputStream os = Files.newOutputStream(versionJson, StandardOpenOption.WRITE)) {
                    is.transferTo(os);
                }
            }
            // parse the new version.json
            try (InputStreamReader isr = new InputStreamReader(Files.newInputStream(versionJson))) {
                version = Version.parse(isr);
            } catch (RuntimeException e) {
                throw new IOException("Failed to parse version.json", e.getCause());
            }
        }
        resolveCache.put(versionId, version);
        return version;
    }

    private static Optional<JsonElement> get(JsonObject json, String key) {
        return Optional.ofNullable(json.get(key));
    }

    public static Version parse(InputStreamReader isr) {
        JsonObject json = JsonParser.parseReader(isr).getAsJsonObject();
        Arguments arguments = null;
        if (json.has("minecraftArguments")) {
            arguments = new Arguments(
                Arrays.stream(json.get("minecraftArguments").getAsString().split(" "))
                    .map(e -> new Arguments.Argument(new Rule[0], new String[] {e}))
                    .toArray(Arguments.Argument[]::new),
                null
            );
        }
        return new Version(
                get(json, "arguments").map(JsonElement::getAsJsonObject).map(Arguments::parse).orElse(arguments),
                get(json, "assetIndex").map(JsonElement::getAsJsonObject).map(AssetIndex::parse).orElse(null),
                get(json, "assets").map(JsonElement::getAsString).orElse(null),
                get(json, "complianceLevel").map(JsonElement::getAsInt).orElse(0),
                get(json, "downloads").map(JsonElement::getAsJsonObject).map(Download::parseAll).orElse(null),
                get(json, "id").map(JsonElement::getAsString).orElse(null),
                get(json, "javaVersion").map(JsonElement::getAsJsonObject).map(JavaVersion::parse).orElse(null),
                get(json, "libraries").map(JsonElement::getAsJsonArray).map(Library::parse).orElse(null),
                get(json, "logging").map(JsonElement::getAsJsonObject).map(Logging::parse).orElse(null),
                get(json, "mainClass").map(JsonElement::getAsString).orElse(null),
                get(json, "minimumLauncherVersion").map(JsonElement::getAsInt).orElse(0),
                get(json, "releaseTime").map(JsonElement::getAsString).map(Instant::parse).map(Instant::getEpochSecond).orElse(0L),
                get(json, "time").map(JsonElement::getAsString).map(Instant::parse).map(Instant::getEpochSecond).orElse(0L),
                get(json, "type").map(JsonElement::getAsString).orElse(null)
        );
    }

    public record Rule(String action, Os os, Map<String, Boolean> features) {

        public static Rule[] parse(JsonArray json) {
            Rule[] rules = new Rule[json.size()];
            for (int i = 0; i < json.size(); i++) {
                rules[i] = Rule.parse(json.get(i).getAsJsonObject());
            }
            return rules;
        }

        public static Rule parse(JsonObject json) {
            return new Rule(
                get(json, "action").map(JsonElement::getAsString).orElse(null),
                get(json, "os").map(JsonElement::getAsJsonObject).map(Os::parse).orElse(null),
                get(json, "features").map(JsonElement::getAsJsonObject).map(e -> e.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e2 -> e2.getValue().getAsBoolean()))).orElse(null)
            );
        }

        public boolean testRules(Launcher launcher) {
            if (os != null && !os.test(launcher)) return !action.equals("allow");
            if (features != null) {
                for (Map.Entry<String, Boolean> entry : features.entrySet()) {
                    if (launcher.features.contains(entry.getKey()) != entry.getValue()) return !action.equals("allow");
                }
            }
            return action.equals("allow");
        }
    }

    public record Os(String name, String version, String arch) {

        public static Os parse(JsonObject json) {
            return new Os(
                    get(json, "name").map(JsonElement::getAsString).orElse(null),
                    get(json, "version").map(JsonElement::getAsString).orElse(null),
                    get(json, "arch").map(JsonElement::getAsString).orElse(null)
            );
        }

        public boolean test(Launcher launcher) {
            if (name != null && !name.equals(OSUtils.getOSId())) return false;
            if (version != null && !SemVerUtils.matches(OSUtils.getOsVersion(), version)) return false;
            return arch == null || arch.equals(OSUtils.getOsArch());
        }
    }

    public record Arguments(Argument[] game, Argument[] jvm) {

        public static Arguments parse(JsonObject json) {
            return new Arguments(
                    get(json, "game").map(JsonElement::getAsJsonArray).map(Argument::parse).orElse(null),
                    get(json, "jvm").map(JsonElement::getAsJsonArray).map(Argument::parse).orElse(null)
            );
        }

        public record Argument(Rule[] rules, String[] values) {

            public static Argument[] parse(JsonArray json) {
                Argument[] args = new Argument[json.size()];
                for (int i = 0; i < args.length; i++) {
                    args[i] = Argument.parse(json.get(i));
                }
                return args;
            }

            public static Argument parse(JsonElement el) {
                if (el.isJsonObject()) {
                    JsonObject json = el.getAsJsonObject();
                    JsonArray jsonValues = get(json, "values").map(JsonElement::getAsJsonArray).orElseGet(JsonArray::new);
                    String[] value = new String[jsonValues.size()];
                    for (int i = 0; i < value.length; i++) {
                        value[i] = jsonValues.get(i).getAsString();
                    }
                    return new Argument(
                            get(json, "rule").map(JsonElement::getAsJsonArray).map(Rule::parse).orElse(new Rule[0]),
                            value
                    );
                } else {
                    return new Argument(
                            new Rule[0],
                            new String[] { el.getAsString() }
                    );
                }
            }

        }
    }

    public record AssetIndex(String id, String sha1, URL url, long size, long totalSize) {

        public static AssetIndex parse(JsonObject json) {
            return new AssetIndex(
                    get(json, "id").map(JsonElement::getAsString).orElse(null),
                    get(json, "sha1").map(JsonElement::getAsString).orElse(null),
                    get(json, "url").map(JsonElement::getAsString).map(e -> {
                        try {
                            return new URL(e);
                        } catch (MalformedURLException e1) {
                            throw new RuntimeException(e1);
                        }
                    }).orElse(null),
                    get(json, "size").map(JsonElement::getAsLong).orElse(0L),
                    get(json, "totalSize").map(JsonElement::getAsLong).orElse(0L)
            );
        }
    }

    public record Download(String sha1, URL url, long size) {

        public static Map<String, Download> parseAll(JsonObject json) {
            Map<String, Download> downloads = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                downloads.put(entry.getKey(), Download.parse(entry.getValue().getAsJsonObject()));
            }
            return downloads;
        }

        public static Download parse(JsonObject json) {
            return new Download(
                    get(json, "sha1").map(JsonElement::getAsString).orElse(null),
                    get(json, "url").map(JsonElement::getAsString).map(e -> {
                        try {
                            return new URL(e);
                        } catch (MalformedURLException e1) {
                            throw new RuntimeException(e1);
                        }
                    }).orElse(null),
                    get(json, "size").map(JsonElement::getAsLong).orElse(0L)
            );
        }
    }

    public record JavaVersion(String component, int majorVersion) {

        public static JavaVersion parse(JsonObject json) {
            return new JavaVersion(
                    get(json, "component").map(JsonElement::getAsString).orElse(null),
                    get(json, "majorVersion").map(JsonElement::getAsInt).orElse(8)
            );
        }
    }

    public record Library(Downloads downloads, String name, URL url, Map<String, String> natives, Extract extract, Rule[] rules) {

        public static Library[] parse(JsonArray json) {
            Library[] libs = new Library[json.size()];
            for (int i = 0; i < libs.length; i++) {
                libs[i] = Library.parse(json.get(i).getAsJsonObject());
            }
            return libs;
        }

        public static Library parse(JsonObject json) {
            return new Library(
                    get(json, "downloads").map(JsonElement::getAsJsonObject).map(Downloads::parse).orElse(null),
                    get(json, "name").map(JsonElement::getAsString).orElse(null),
                    get(json, "url").map(JsonElement::getAsString).map(e -> {
                        try {
                            return new URL(e);
                        } catch (MalformedURLException e1) {
                            throw new RuntimeException(e1);
                        }
                    }).orElse(null),
                    get(json, "natives").map(JsonElement::getAsJsonObject).map(e -> e.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e1 -> e1.getValue().getAsString()))).orElse(null),
                    get(json, "extract").map(JsonElement::getAsJsonObject).map(Extract::parse).orElse(null),
                    get(json, "rules").map(JsonElement::getAsJsonArray).map(Rule::parse).orElse(new Rule[0])
            );
        }
    }

    public record Downloads(Artifact artifact, Map<String, Artifact> classifier) {

        public static Downloads parse(JsonObject json) {
            return new Downloads(
                    get(json, "artifact").map(JsonElement::getAsJsonObject).map(Artifact::parse).orElse(null),
                    get(json, "classifiers").map(JsonElement::getAsJsonObject).map(e -> e.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e1 -> Artifact.parse(e1.getValue().getAsJsonObject())))).orElse(null)
            );
        }
    }

    public record Artifact(Path path, String sha1, long size, URL url) {

        public static Artifact parse(JsonObject json) {
            return new Artifact(
                get(json, "path").map(JsonElement::getAsString).map(Path::of).orElse(null),
                get(json, "sha1").map(JsonElement::getAsString).orElse(null),
                get(json, "size").map(JsonElement::getAsLong).orElse(0L),
                get(json, "url").map(JsonElement::getAsString).map(e -> {
                    try {
                        return new URL(e);
                    } catch (MalformedURLException e1) {
                        throw new RuntimeException(e1);
                    }
                }).orElse(null)
            );
        }
    }

    public record Extract(String[] exclude) {

        public static Extract parse(JsonObject json) {
            JsonArray jsonExclude = get(json, "exclude").map(JsonElement::getAsJsonArray).orElseGet(JsonArray::new);
            String[] exclude = new String[jsonExclude.size()];
            for (int i = 0; i < jsonExclude.size(); i++) {
                exclude[i] = jsonExclude.get(i).getAsString();
            }
            return new Extract(exclude);
        }
    }

    public record Logging(Client client) {

        public static Logging parse(JsonObject json) {
            return new Logging(
                    get(json, "client").map(JsonElement::getAsJsonObject).map(Client::parse).orElse(null)
            );
        }

        public record Client(String argument, File file, String type) {

            public static Client parse(JsonObject json) {
                return new Client(
                        get(json, "argument").map(JsonElement::getAsString).orElse(null),
                        get(json, "file").map(JsonElement::getAsJsonObject).map(File::parse).orElse(null),
                        get(json, "type").map(JsonElement::getAsString).orElse(null)
                );
            }

            public record File(String id, String sha1, URL url, long size) {

                public static File parse(JsonObject json) {
                    return new File(
                            get(json, "id").map(JsonElement::getAsString).orElse(null),
                            get(json, "sha1").map(JsonElement::getAsString).orElse(null),
                            get(json, "url").map(JsonElement::getAsString).map(e -> {
                                try {
                                    return new URL(e);
                                } catch (MalformedURLException e1) {
                                    throw new RuntimeException(e1);
                                }
                            }).orElse(null),
                            get(json, "size").map(JsonElement::getAsLong).orElse(0L)
                    );
                }

            }
        }
    }
}
