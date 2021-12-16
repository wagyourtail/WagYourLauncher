package xyz.wagyourtail.launcher.minecraft.profile;

import xyz.wagyourtail.launcher.main.Launcher;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
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

    public Process launch(Launcher launcher, PrintStream out, PrintStream err) throws IOException {
        Version resolvedVersion = Version.resolve(launcher, lastVersionId);
        if (resolvedVersion == null) {
            err.println("Could not resolve version " + lastVersionId);
            return null;
        }
        out.println("Launching " + resolvedVersion.id());
        //TODO:
        return null;
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
