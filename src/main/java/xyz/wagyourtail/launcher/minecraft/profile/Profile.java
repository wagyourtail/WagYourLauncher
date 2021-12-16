package xyz.wagyourtail.launcher.minecraft.profile;

import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.minecraft.LibraryManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record Profile(
        String name,
        Path gameDir,
        long created,
        long lastUsed,
        String icon,
        String javaArgs,
        Path javaDir,
        String lastVersionId,
        Type type
    ) {

    public Process launch(Launcher launcher, String username, PrintStream out, PrintStream err) throws IOException {
        Version resolvedVersion = Version.resolve(launcher, lastVersionId);
        if (resolvedVersion == null) {
            err.println("Could not resolve version " + lastVersionId);
            return null;
        }
        out.println("Launching " + resolvedVersion.id());
        /*
         /usr/lib/jvm/java-17-graalvm/bin/java
                -Xss1M
                -Djava.library.path=/home/william/.minecraft/bin/4b44-41ce-652b-fad2
                -Dminecraft.launcher.brand=minecraft-launcher
                -Dminecraft.launcher.version=2.2.8473
                -cp /home/william/.minecraft/libraries/com/mojang/blocklist/1.0.6/blocklist-1.0.6.jar:/home/william/.minecraft/libraries/com/mojang/patchy/2.1.6/patchy-2.1.6.jar:/home/william/.minecraft/libraries/com/github/oshi/oshi-core/5.8.2/oshi-core-5.8.2.jar:/home/william/.minecraft/libraries/net/java/dev/jna/jna/5.9.0/jna-5.9.0.jar:/home/william/.minecraft/libraries/net/java/dev/jna/jna-platform/5.9.0/jna-platform-5.9.0.jar:/home/william/.minecraft/libraries/org/slf4j/slf4j-api/1.8.0-beta4/slf4j-api-1.8.0-beta4.jar:/home/william/.minecraft/libraries/org/apache/logging/log4j/log4j-slf4j18-impl/2.14.1/log4j-slf4j18-impl-2.14.1.jar:/home/william/.minecraft/libraries/com/ibm/icu/icu4j/69.1/icu4j-69.1.jar:/home/william/.minecraft/libraries/com/mojang/javabridge/1.2.24/javabridge-1.2.24.jar:/home/william/.minecraft/libraries/net/sf/jopt-simple/jopt-simple/5.0.4/jopt-simple-5.0.4.jar:/home/william/.minecraft/libraries/io/netty/netty-all/4.1.68.Final/netty-all-4.1.68.Final.jar:/home/william/.minecraft/libraries/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:/home/william/.minecraft/libraries/com/google/guava/guava/31.0.1-jre/guava-31.0.1-jre.jar:/home/william/.minecraft/libraries/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar:/home/william/.minecraft/libraries/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar:/home/william/.minecraft/libraries/commons-codec/commons-codec/1.15/commons-codec-1.15.jar:/home/william/.minecraft/libraries/com/mojang/brigadier/1.0.18/brigadier-1.0.18.jar:/home/william/.minecraft/libraries/com/mojang/datafixerupper/4.0.26/datafixerupper-4.0.26.jar:/home/william/.minecraft/libraries/com/google/code/gson/gson/2.8.8/gson-2.8.8.jar:/home/william/.minecraft/libraries/com/mojang/authlib/3.2.38/authlib-3.2.38.jar:/home/william/.minecraft/libraries/org/apache/commons/commons-compress/1.21/commons-compress-1.21.jar:/home/william/.minecraft/libraries/org/apache/httpcomponents/httpclient/4.5.13/httpclient-4.5.13.jar:/home/william/.minecraft/libraries/commons-logging/commons-logging/1.2/commons-logging-1.2.jar:/home/william/.minecraft/libraries/org/apache/httpcomponents/httpcore/4.4.14/httpcore-4.4.14.jar:/home/william/.minecraft/libraries/it/unimi/dsi/fastutil/8.5.6/fastutil-8.5.6.jar:/home/william/.minecraft/libraries/org/apache/logging/log4j/log4j-api/2.14.1/log4j-api-2.14.1.jar:/home/william/.minecraft/libraries/org/apache/logging/log4j/log4j-core/2.14.1/log4j-core-2.14.1.jar:/home/william/.minecraft/libraries/org/lwjgl/lwjgl/3.2.2/lwjgl-3.2.2.jar:/home/william/.minecraft/libraries/org/lwjgl/lwjgl-jemalloc/3.2.2/lwjgl-jemalloc-3.2.2.jar:/home/william/.minecraft/libraries/org/lwjgl/lwjgl-openal/3.2.2/lwjgl-openal-3.2.2.jar:/home/william/.minecraft/libraries/org/lwjgl/lwjgl-opengl/3.2.2/lwjgl-opengl-3.2.2.jar:/home/william/.minecraft/libraries/org/lwjgl/lwjgl-glfw/3.2.2/lwjgl-glfw-3.2.2.jar:/home/william/.minecraft/libraries/org/lwjgl/lwjgl-stb/3.2.2/lwjgl-stb-3.2.2.jar:/home/william/.minecraft/libraries/org/lwjgl/lwjgl-tinyfd/3.2.2/lwjgl-tinyfd-3.2.2.jar:/home/william/.minecraft/libraries/com/mojang/text2speech/1.11.3/text2speech-1.11.3.jar:/home/william/.minecraft/versions/1.18.1/1.18.1.jar
                -Xmx4G
                -XX:+UnlockExperimentalVMOptions
                -XX:+UseG1GC
                -XX:G1NewSizePercent=20
                -XX:G1ReservePercent=20
                -XX:MaxGCPauseMillis=50
                -XX:G1HeapRegionSize=32M
                -Dlog4j.configurationFile=/home/william/.minecraft/assets/log_configs/client-1.12.xml
            net.minecraft.client.main.Main
                --username wagyourtail
                --version 1.18.1
                --gameDir /home/william/.minecraft/profiles/test
                --assetsDir /home/william/.minecraft/assets
                --assetIndex 1.18
                --uuid ***
                --accessToken **
                --clientId ***
                --xuid ***
                --userType msa
                --versionType release
         */

        List<String> args = new ArrayList<>();
        args.add(getJavaDir(launcher));
        args.addAll(Arrays.asList(getJavaArgs(launcher)));
        //TODO: add logging for -Dlog4j.configurationFile fix
        args.add(resolvedVersion.mainClass());
        args.addAll(Arrays.asList(getGameArgs(launcher, username)));

        System.out.println("Launching with args: " + String.join(" ", args));

        ProcessBuilder pb = new ProcessBuilder(args.toArray(String[]::new));
        pb.directory(gameDir.toFile());
        Process p = pb.start();
        Thread t = new Thread(() -> {
            try {
                p.getErrorStream().transferTo(err);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(err != System.err);
        t.start();
        t = new Thread(() -> {
            try {
                p.getInputStream().transferTo(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(out != System.out);
        t.start();
        return p;
    }

    public String[] getJavaArgs(Launcher launcher) throws IOException {
        Version resolvedVersion = Version.resolve(launcher, lastVersionId);
        String cp = getClassPath(launcher);
        String natives = nativePath(launcher).toString();
        if (resolvedVersion == null) {
            return new String[] {};
        }
        List<String> args = new ArrayList<>();
        if (javaArgs != null) {
            args.addAll(Arrays.asList(javaArgs.split(" ")));
        }
        for (Version.Arguments.Argument arg : resolvedVersion.arguments().jvm()) {
            if (arg.rules().length == 0) {
                args.addAll(List.of(arg.values()));
            } else {
                if (Arrays.stream(arg.rules()).allMatch(rule -> rule.testRules(launcher))) {
                    args.addAll(List.of(arg.values()));
                }
            }
        }
        return args.stream().map(e -> e
                .replace("${natives_directory}", natives)
                .replace("${classpath}", cp)
                .replace("${launcher_name}", launcher.getName())
                .replace("${launcher_version}", launcher.getVersion())
            ).toArray(String[]::new);
    }

    public Path nativePath(Launcher launcher) throws IOException {
        return gameDir.resolve("natives").toAbsolutePath();
    }

    public String[] getGameArgs(Launcher launcher, String username) throws IOException {
        Version resolvedVersion = Version.resolve(launcher, lastVersionId);
        if (resolvedVersion == null) {
            throw new IOException("Could not resolve game args for version " + lastVersionId);
        }
        List<String> args = new ArrayList<>();
        for (Version.Arguments.Argument arg : resolvedVersion.arguments().game()) {
            if (arg.rules().length == 0) {
                args.addAll(List.of(arg.values()));
            } else {
                if (Arrays.stream(arg.rules()).allMatch(rule -> rule.testRules(launcher))) {
                    args.addAll(List.of(arg.values()));
                }
            }
        }
        String assets = launcher.assets.resolveAssets(resolvedVersion.assetIndex()).toString();
        return args.stream().map(e -> e
                .replace("${auth_player_name}", username)
                .replace("${version_name}", resolvedVersion.id())
                .replace("${game_directory}", gameDir.toAbsolutePath().toString())
                .replace("${assets_root}", assets)
                .replace("${assets_index_name}", resolvedVersion.assets())
                .replace("${auth_uuid}", launcher.auth.getUUID(username).toString())
                .replace("${auth_access_token}", launcher.auth.getToken(username))
                .replace("${clientid}", launcher.getName())
                .replace("${user_type}", launcher.auth.getUserType(username))
                .replace("${version_type}", resolvedVersion.type())
            ).toArray(String[]::new);
    }

    public String getJavaDir(Launcher launcher) throws IOException {
        Version resolvedVersion = Version.resolve(launcher, lastVersionId);
        if (javaDir == null) {
            if (resolvedVersion == null) {
                return launcher.getJavaDir(8).toAbsolutePath().toString();
            }
            return launcher.getJavaDir(resolvedVersion.javaVersion().majorVersion()).toAbsolutePath().toString();
        } else {
            return javaDir.toAbsolutePath().toString();
        }
    }

    public String getClassPath(Launcher launcher) throws IOException {
        Version resolvedVersion = Version.resolve(launcher, lastVersionId);
        if (resolvedVersion == null) {
            throw new IOException("Could not resolve class path for version " + lastVersionId);
        } else {
            List<String> classPath = new ArrayList<>(launcher.libs.resolveAll(this, resolvedVersion.libraries()).stream().map(Path::toAbsolutePath).map(Path::toString).toList());
            // Add the version's jar
            classPath.add(resolveClientJar(launcher).toAbsolutePath().toString());
            return String.join(":", classPath);
        }
    }

    public Path resolveClientJar(Launcher launcher) throws IOException {
        Version resolvedVersion = Version.resolve(launcher, lastVersionId);
        if (resolvedVersion == null) {
            throw new IOException("Could not resolve client jar for version " + lastVersionId);
        } else {
            Version.Download client = resolvedVersion.downloads().get("client");
            if (client == null) {
                throw new IOException("Could not resolve client jar for version " + lastVersionId);
            }

            Path clientJar = launcher.minecraftPath.resolve("versions").resolve(lastVersionId).resolve(lastVersionId + ".jar");

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
    }

    public enum Type {
        CUSTOM("custom"),
        LATEST_SNAPSHOT("latest-snapshot"),
        LATEST_RELEASE("latest-release");

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
}
