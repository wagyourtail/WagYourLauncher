package xyz.wagyourtail.launcher.minecraft.profile;

import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.LogListener;
import xyz.wagyourtail.launcher.minecraft.version.Version;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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

    public void launch(Launcher launcher, String username) throws Exception {
        Version resolvedVersion = Version.resolve(launcher, lastVersionId);
        LogListener logger = launcher.getLogger(this);
        if (resolvedVersion == null) {
            logger.onError("Could not resolve version " + lastVersionId);
            logger.close();
            return;
        }
        logger.onInfo("Launching " + resolvedVersion.id() + "\n");
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
        args.addAll(Arrays.asList(resolvedVersion.getJavaArgs(launcher, this, nativePath(launcher), javaArgs, resolvedVersion.getClassPath(launcher, this))));
        args.addAll(Arrays.asList(resolvedVersion.getLogging(launcher)));
        //TODO: add logging for -Dlog4j.configurationFile fix
        args.add(resolvedVersion.getMainClass());
        args.addAll(Arrays.asList(resolvedVersion.getGameArgs(launcher, username, gameDir)));

        System.out.println("Launching with args: " + String.join(" ", args).replaceAll("--accessToken [^ ]+", "--accessToken ***"));

        ProcessBuilder pb = new ProcessBuilder(args.toArray(String[]::new));
        pb.directory(gameDir.toFile());
        Process p = pb.start();
//        new Thread(() -> {
//            try {
//                p.getInputStream().transferTo(System.out);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
        new Thread(() -> {
            try {
                p.getErrorStream().transferTo(new OutputStream() {
                    String line = "";
                    @Override
                    public void write(int b) throws IOException {
                        if (b == '\n') {
                            logger.onError(line);
                            line = "";
                        } else {
                            line += (char) b;
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        pipeOutput(launcher, p, logger);
    }

    public Path nativePath(Launcher launcher) {
        return gameDir.resolve("natives").toAbsolutePath();
    }

    public String getJavaDir(Launcher launcher) throws IOException {
        Version resolvedVersion = Version.resolve(launcher, lastVersionId);
        if (javaDir == null) {
            if (resolvedVersion == null) {
                return launcher.getJavaDir(8).toAbsolutePath().toString();
            }
            return launcher.getJavaDir(resolvedVersion.getJavaMajorVersion()).toAbsolutePath().toString();
        } else {
            return javaDir.toAbsolutePath().toString();
        }
    }



    public void pipeOutput(Launcher launcher, Process p, LogListener logger) {
        Thread t = new Thread(() -> {
            XMLStreamReader reader = null;
            AtomicBoolean end = new AtomicBoolean(false);
            try {
                reader = XMLInputFactory.newFactory().createXMLStreamReader(new SequenceInputStream(
                    new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root xmlns:log4j=\"https://apache.org/\">".getBytes(StandardCharsets.UTF_8)),
                    p.getInputStream()
                ));
                p.onExit().thenAccept(pr -> {
                    end.set(true);
                    if (logger != null) {
                        if (pr.exitValue() == 0) {
                            logger.onInfo("Minecraft exited successfully (exit code " + pr.exitValue() + ")");
                        } else {
                            logger.onError("Minecraft exited with error (exit code " + pr.exitValue() + ")");
                        }
                    }
                });
                LogListener.LogLevel level = LogListener.LogLevel.INFO;
                String time = "";
                String thread = "";
                while (!end.get()) {
                    if (reader.hasNext()) {
                        reader.next();
                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                            if (reader.getLocalName().equals("Event")) {
                                level = LogListener.LogLevel.valueOf(reader.getAttributeValue(null, "level"));
                                time = Instant.ofEpochMilli(Long.parseLong(reader.getAttributeValue(null, "timestamp"))).toString();
                                thread = reader.getAttributeValue(null, "thread");
                            } else if (reader.getLocalName().equals("Message") || reader.getLocalName().equals("Throwable")) {
                                logger.log(level, String.format("[%s] [%s/%s] %s", time, thread, level, reader.getElementText()));
                            }
                            continue;
                        } else if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                            continue;
                        } else if (reader.getEventType() == XMLStreamConstants.CHARACTERS) {
                            reader.getProperty("");
                        }
                    }
                    Thread.yield();
                }
            } catch (XMLStreamException ignored) {
            } finally {
                try {
                    if (logger != null) {
                        logger.close();
                    }
                } catch (Exception ignored) {
                } finally {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (XMLStreamException ignored) {}
                }
            }
        });
        t.start();
    }

    public void declareNamespace(XMLStreamReader reader, String prefix, String uri) {

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
