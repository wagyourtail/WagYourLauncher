package xyz.wagyourtail.launcher.minecraft.profile;

import com.google.gson.JsonObject;
import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.Logger;
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
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

public record Profile(
        String key,
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
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        if (gameDir != null) json.addProperty("gameDir", gameDir.toAbsolutePath().toString());
        json.addProperty("created", Instant.ofEpochMilli(created).toString());
        json.addProperty("lastUsed", Instant.ofEpochMilli(lastUsed).toString());
        if (icon != null) json.addProperty("icon", icon);
        if (javaArgs != null) json.addProperty("javaArgs", javaArgs);
        if (javaDir != null) json.addProperty("javaDir", javaDir.toAbsolutePath().toString());
        json.addProperty("lastVersionId", lastVersionId);
        json.addProperty("type", type.id);
        return json;
    }

    @Override
    public String toString() {
        if (name == null || name.equals("")) return key;
        return name;
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

    public void pipeOutput(Launcher launcher, Process p, Logger logger) {
        AtomicBoolean end = new AtomicBoolean(false);
        Thread t = new Thread(() -> {
            try {
                p.getErrorStream().transferTo(new OutputStream() {
                    String line = "";
                    @Override
                    public void write(int b) {
                        if (b == '\n') {
                            logger.fatal(line);
                            line = "";
                        } else {
                            line += (char) b;
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();
        t = new Thread(() -> {
            XMLStreamReader reader = null;
            try {
                reader = XMLInputFactory.newFactory().createXMLStreamReader(new SequenceInputStream(
                    new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root xmlns:log4j=\"https://apache.org/\">".getBytes(StandardCharsets.UTF_8)),
                    p.getInputStream()
                ));
                Logger.LogLevel level = Logger.LogLevel.INFO;
                String time = "";
                String thread = "";
                while (!end.get()) {
                    if (reader.hasNext()) {
                        reader.next();
                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                            if (reader.getLocalName().equals("Event")) {
                                level = Logger.LogLevel.valueOf(reader.getAttributeValue(null, "level"));
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
        p.onExit().thenAccept(pr -> {
            end.set(true);
            if (logger != null) {
                if (pr.exitValue() == 0) {
                    logger.info("Minecraft exited successfully (exit code " + pr.exitValue() + ")");
                } else {
                    logger.error("Minecraft exited with error (exit code " + pr.exitValue() + ")");
                }
            }
        });
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
