package xyz.wagyourtail.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JavaUtils {
    private static final Set<JavaVersion> versions = new HashSet<>();

    public static Set<JavaVersion> getVersions() {
        return Set.copyOf(versions);
    }

    public static void refreshVersions() throws IOException {
        versions.clear();
        switch (OSUtils.getOSId()) {
            case "linux" -> refreshVersionsNix();
            case "windows" -> refreshVersionsWin();
            case "osx" -> refreshVersionsMac();
        }
    }

    public static void refreshVersionsNix() throws IOException {
        try {
            versions.add(JavaVersion.fromVersionString(runJavaVersion(Path.of("java"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkJvmPathForJavas(Path.of("/usr/lib64/jvm"));
        checkJvmPathForJavas(Path.of("/usr/lib/jvm"));
    }

    private static void checkJvmPathForJavas(Path jvm) throws IOException {
        if (Files.exists(jvm)) {
            for (Path p : Files.list(jvm).toArray(Path[]::new)) {
                if (Files.isDirectory(p)) {
                    if (Files.exists(p.resolve("bin/java"))) {
                        Path jPath = p.resolve("bin/java");
                        versions.add(JavaVersion.fromVersionString(runJavaVersion(jPath)));
                    }
                }
            }
        }
    }

    public static String runJavaVersion(Path java) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(java.toString(), "-XshowSettings:properties", "-version");
        pb.redirectErrorStream(true);
        Process p = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            StringBuilder str = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                str.append(line).append("\n");
            }
            return str.toString();
        }
    }

    public static void refreshVersionsWin() {
        throw new AssertionError("Not implemented");
    }

    public static void refreshVersionsMac() {
        throw new AssertionError("Not implemented");
    }


    public static void main(String[] args) throws IOException {
        JavaUtils.refreshVersions();
        System.out.println(String.join("\n", JavaUtils.getVersions().stream().map(Object::toString).toArray(String[]::new)));
    }

    public record JavaVersion(String vendor, String arch, String version, Path path) {
        public static JavaVersion fromVersionString(String version) {
            Map<String, String> map = propertiesBuilder(version);
            return new JavaVersion(map.get("java.vendor"), map.get("sun.arch.data.model"), map.get("java.version").replace("_", "+"), Path.of(map.get("java.home")).resolve("bin/java"));
        }
    }

    private static Map<String, String> propertiesBuilder(String version) {
        String[] split = version.split("\n");
        Map<String, String> map = new HashMap<>();
        String lastProp = "";
        for (String s : split) {
            if (s.matches("\\s+.+")) {
                if (s.contains("=")) {
                    String[] split1 = s.split("=");
                    map.put(split1[0].trim(), split1[1].trim());
                    lastProp = split1[0].trim();
                } else {
                    map.put(lastProp, map.get(lastProp) + " " + s.trim());
                }
            }
        }
        return map;
    }
}
