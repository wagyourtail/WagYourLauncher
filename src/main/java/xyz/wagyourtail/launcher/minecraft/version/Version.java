package xyz.wagyourtail.launcher.minecraft.version;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.minecraft.LibraryManager;
import xyz.wagyourtail.launcher.minecraft.data.VersionManifest;
import xyz.wagyourtail.launcher.minecraft.userProfile.Profile;
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
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public record Version(
        String id,
        String type,
        Version inheritsFrom,
        Path jar,
        long time,
        long releaseTime,
        int minimumLauncherVersion,
        String mainClass,
        AssetIndex assetIndex,
        String assets,
        Map<String, Download> downloads,
        JavaVersion javaVersion,
        Arguments arguments,
        Library[] libraries,
        Logging logging,
        int complianceLevel
    ) {

    public String getMainClass() throws IOException {
        if (mainClass == null) {
            if (inheritsFrom != null) {
                return inheritsFrom.getMainClass();
            }
            throw new IOException("No main class found");
        }
        return mainClass;
    }

    public int getJavaMajorVersion() {
        if (javaVersion == null) {
            if (inheritsFrom != null) {
                return inheritsFrom.getJavaMajorVersion();
            } else {
                return 8;
            }
        }
        return javaVersion.majorVersion();
    }

    public String[] getJavaArgs(Launcher launcher, Profile userProfile, Path nativePath, String javaArgs, String classPath) throws IOException {
        String natives = nativePath.toString();
        List<String> args = new ArrayList<>();
        if (javaArgs != null) {
            args.addAll(Arrays.asList(javaArgs.split(" ")));
        } else {
            args.addAll(
                Arrays.asList(
                    "-Xmx4G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M"
                        .split(" ")
                )
            );
        }
        if (arguments != null && arguments.jvm() != null) {
            for (Version.Arguments.Argument arg : arguments().jvm()) {
                if (arg.rules().length == 0) {
                    args.addAll(List.of(arg.values()));
                } else {
                    if (Arrays.stream(arg.rules()).allMatch(rule -> rule.testRules(launcher))) {
                        args.addAll(List.of(arg.values()));
                    }
                }
            }
        } else if (inheritsFrom != null) {
            args.addAll(Arrays.asList(inheritsFrom.getJavaArgs(launcher, userProfile, nativePath, null, classPath)));
        } else {
            // default args
            Arguments.Argument[] arguments = new Arguments.Argument[]{
                new Arguments.Argument(
                    new Rule[] {
                        new Rule("allow", new Os("osx", null, null), null)
                    },
                    new String[] {
                        "-XstartOnFirstThread"
                    }
                ),
                new Arguments.Argument(
                    new Rule[] {
                        new Rule("allow", new Os("windows", null, null), null)
                    },
                    new String[] {
                        "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump"
                    }
                ),
                new Arguments.Argument(
                    new Rule[] {
                        new Rule("allow", new Os("windows", "^10\\.", null), null)
                    },
                    new String[] {
                        "-Dos.name=Windows 10",
                        "-Dos.version=10.0"
                    }
                ),
                new Arguments.Argument(
                    new Rule[] {
                        new Rule("allow", new Os(null, null, "x86"), null)
                    },
                    new String[] {
                        "-Xss1M"
                    }
                ),
                new Arguments.Argument(
                    new Rule[0],
                    new String[] {
                        "-Djava.library.path=${natives_directory}"
                    }
                ),
                new Arguments.Argument(
                    new Rule[0],
                    new String[] {
                        "-Dminecraft.launcher.brand=${launcher_name}",
                    }
                ),
                new Arguments.Argument(
                    new Rule[0],
                    new String[] {
                        "-Dminecraft.launcher.version=${launcher_version}",
                    }
                ),
                new Arguments.Argument(
                    new Rule[0],
                    new String[] {
                        "-cp"
                    }
                ),
                new Arguments.Argument(
                    new Rule[0],
                    new String[] {
                        "${classpath}"
                    }
                )
            };
            for (Version.Arguments.Argument arg : arguments) {
                if (arg.rules().length == 0) {
                    args.addAll(List.of(arg.values()));
                } else {
                    if (Arrays.stream(arg.rules()).allMatch(rule -> rule.testRules(launcher))) {
                        args.addAll(List.of(arg.values()));
                    }
                }
            }
        }
        return args.stream().map(e -> e
            .replace("${natives_directory}", natives)
            .replace("${classpath}", classPath)
            .replace("${launcher_name}", launcher.getName())
            .replace("${launcher_version}", launcher.getVersion())
        ).toArray(String[]::new);
    }

    public List<Path> getLibraries(Launcher launcher, Profile userProfile) throws IOException {
        List<Path> libs = new ArrayList<>();
        if (inheritsFrom != null) {
            libs.addAll(inheritsFrom.getLibraries(launcher, userProfile));
        }
        if (libraries != null) {
            libs.addAll(launcher.libs.resolveAll(userProfile, libraries()).stream().map(Path::toAbsolutePath).toList());
        }
        return libs;
    }

    public String getClassPath(Launcher launcher, Profile userProfile) throws IOException {
        List<String> classPath = new ArrayList<>(getLibraries(launcher, userProfile).stream().map(Path::toString).toList());

        // Add the version's jar
        classPath.add(resolveClientJar(launcher).toAbsolutePath().toString());
        return String.join(":", classPath);
    }

    public Version.Download getClientDownload() throws IOException {
        if (downloads != null) {
            Version.Download download = downloads.get("client");
            if (download != null) {
                return download;
            }
        }
        if (inheritsFrom != null) {
            return inheritsFrom.getClientDownload();
        }
        throw new IOException("No client download found");
    }

    public Path jarPath() {
        if (jar != null) {
            return jar;
        }
        if (inheritsFrom != null) {
            return inheritsFrom.jarPath();
        }
        throw new IllegalStateException("No jar path found");
    }

    public Path resolveClientJar(Launcher launcher) throws IOException {
        Version.Download client = getClientDownload();

        Path clientJar = jarPath();

        if (Files.exists(clientJar)) {
            if (Files.size(clientJar) != client.size() || !LibraryManager.shaMatch(clientJar, client.sha1())) {
                System.out.println("Client jar is outdated, downloading new one");
                Files.delete(clientJar);
            }
        }

        if (!Files.exists(clientJar)) {
            for (int i = 0; i < 3; i++) {
                try {
                    System.out.println("Downloading client jar");
                    Files.createDirectories(clientJar.getParent());
                    Path tmp = clientJar.getParent().resolve(clientJar.getFileName().toString() + ".tmp");
                    try (InputStream stream = client.url().openStream()) {
                        Files.write(tmp, stream.readAllBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    }
                    if (LibraryManager.shaMatch(tmp, client.sha1())) {
                        Files.move(tmp, clientJar, StandardCopyOption.REPLACE_EXISTING);
                        break;
                    } else {
                        throw new IOException("SHA1 mismatch");
                    }
                } catch (IOException e) {
                    System.out.println("Failed to download client jar, retrying " + i + " of 3");
                    e.printStackTrace();
                }
            }
        }

        if (!Files.exists(clientJar)) {
            throw new IOException("Failed to download client jar");
        }

        return clientJar;
    }

    public AssetIndex getAssets() throws IOException {
        if (assetIndex != null) {
            return assetIndex;
        }
        if (inheritsFrom != null) {
            return inheritsFrom.getAssets();
        }
        throw new IOException("No assets found");
    }

    public String[] getGameArgs(Launcher launcher, String username, Path gameDir) throws IOException {
        if (arguments == null || arguments.game == null) {
            if (inheritsFrom != null) {
                return inheritsFrom.getGameArgs(launcher, username, gameDir);
            } else {
                throw new IOException("No game arguments found");
            }
        }
        List<String> args = new ArrayList<>();
        for (Version.Arguments.Argument arg : arguments.game) {
            if (arg.rules().length == 0) {
                args.addAll(List.of(arg.values()));
            } else {
                if (Arrays.stream(arg.rules()).allMatch(rule -> rule.testRules(launcher))) {
                    args.addAll(List.of(arg.values()));
                }
            }
        }
        AssetIndex assetIndex = getAssets();
        Path assets = launcher.assets.resolveAssets(assetIndex);
        return args.stream().map(e -> {
                try {
                    return e
                        .replace("${auth_player_name}", username)
                        .replace("${version_name}", id)
                        .replace("${game_directory}", gameDir.toAbsolutePath().toString())
                        .replace("${assets_root}", assets.toString())
                        .replace("${assets_index_name}", assetIndex.id)//assetIndex.id)
                        .replace("${auth_uuid}", launcher.auth.getUUID(username).toString().replace("-", ""))
                        .replace("${auth_xuid}", "")
                        .replace("${auth_access_token}", launcher.auth.getToken(username))
                        .replace("${clientid}", launcher.getName())
                        .replace("${user_type}", launcher.auth.getUserType(username))
                        .replace("${version_type}", type);
                } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException | InterruptedException | UnrecoverableEntryException | InvalidKeySpecException ex) {
                    throw new RuntimeException(ex);
                }
            }
        ).toArray(String[]::new);
    }

    public String[] getLogging(Launcher launcher) throws IOException {
        if (logging == null || logging.client == null) {
            if (inheritsFrom != null) {
                return inheritsFrom.getLogging(launcher);
            } else {
                return new String[0];
            }
        }

        Path loggingFile = launcher.assets.resolveLogging(logging.client.file);

        // Download logging file
        return new String[] { logging.client.argument.replace("${path}", loggingFile.toAbsolutePath().toString()) };
    }

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
            version = Version.parse(launcher, isr);
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
                version = Version.parse(launcher, isr);
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

    public static Version parse(Launcher launcher, InputStreamReader isr) throws IOException {
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
        Version inheritsFrom = get(json, "inheritsFrom").map(JsonElement::getAsString).map(e -> {
                try {
                    return Version.resolve(launcher, e);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }).orElse(null);
        return new Version(
                get(json, "id").map(JsonElement::getAsString).orElseThrow(() -> new IOException("Missing id")),
                get(json, "type").map(JsonElement::getAsString).orElse(null),
                inheritsFrom,
                get(json, "jar").map(JsonElement::getAsString).map(e -> launcher.minecraftPath.resolve("versions").resolve(json.get("id").getAsString()).resolve(e + ".json")).orElseGet(() -> {
                    if (inheritsFrom != null) {
                        return null;
                    }
                    return launcher.minecraftPath.resolve("versions").resolve(json.get("id").getAsString()).resolve(json.get("id").getAsString() + ".jar");
                }),
                get(json, "time").map(JsonElement::getAsString).map(Instant::parse).map(Instant::getEpochSecond).orElse(0L),
                get(json, "releaseTime").map(JsonElement::getAsString).map(Instant::parse).map(Instant::getEpochSecond).orElse(0L),
                get(json, "minimumLauncherVersion").map(JsonElement::getAsInt).orElse(0),
                get(json, "mainClass").map(JsonElement::getAsString).orElse(null),
                get(json, "assetIndex").map(JsonElement::getAsJsonObject).map(AssetIndex::parse).orElse(null),
                get(json, "assets").map(JsonElement::getAsString).orElse(null),
                get(json, "downloads").map(JsonElement::getAsJsonObject).map(Download::parseAll).orElse(null),
                get(json, "javaVersion").map(JsonElement::getAsJsonObject).map(JavaVersion::parse).orElse(null),
                get(json, "arguments").map(JsonElement::getAsJsonObject).map(Arguments::parse).orElse(arguments),
                get(json, "libraries").map(JsonElement::getAsJsonArray).map(Library::parse).orElse(new Library[0]),
                get(json, "logging").map(JsonElement::getAsJsonObject).map(Logging::parse).orElse(null),
                get(json, "complianceLevel").map(JsonElement::getAsInt).orElse(0)
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
