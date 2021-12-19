package xyz.wagyourtail.launcher.minecraft;

import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.minecraft.userProfile.Profile;
import xyz.wagyourtail.launcher.minecraft.version.Version;
import xyz.wagyourtail.util.OSUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LibraryManager {
    public final Launcher launcher;
    public final Path globalLibPath;

    public LibraryManager(Launcher launcher) {
        this.launcher = launcher;
        this.globalLibPath = launcher.minecraftPath.resolve("libraries").toAbsolutePath();
    }

    public List<Path> resolveAll(Profile userProfile, Version.Library[] librarys) throws IOException {
        List<Path> paths = new ArrayList<>();
        for (Version.Library library : librarys) {
            paths.addAll(resolve(userProfile, library));
        }
        return paths;
    }

    public List<Path> resolve(Profile userProfile, Version.Library library) throws IOException {
        if (!Arrays.stream(library.rules()).allMatch(rule -> rule.testRules(launcher))) {
            return List.of();
        }
        List<Path> paths = new ArrayList<>();
        if (library.downloads() != null) {
            if (library.downloads().artifact() != null) {
                paths.add(resolveArtifact(userProfile, library.downloads().artifact()));
            }
            if (library.natives() != null) {
                Version.Artifact artifact = library.downloads().classifier().get(library.natives().get(OSUtils.getOSId()));
                Path natives = resolveArtifact(userProfile, artifact);
                paths.add(natives);
                if (library.extract() != null) {
                    doExtract(userProfile, natives, library.extract());
                }
            }
        } else {
            paths.add(resolveMaven(userProfile, library));
        }
        return paths;
    }

    public Path resolveArtifact(Profile userProfile, Version.Artifact artifact) throws IOException {
        Path global = globalLibPath.resolve(artifact.path());
        Path local = userProfile.gameDir().resolve("libraries").resolve(artifact.path());
        // already exists and matches
        if (Files.exists(local)) {
            if (Files.size(local) == artifact.size() && shaMatch(local, artifact.sha1())) {
                return local;
            } else {
                Files.delete(local);
                System.out.println("Deleted local library " + local.getFileName() + " because it didn't match the sha1 or file size");
            }
        }
        if (Files.exists(global)) {
            // if the file exists in global but doesn't match the sha store it local to the userProfile
            if (Files.size(global) != artifact.size() || !shaMatch(global, artifact.sha1())) {
                global = local;
                System.out.println("Storing library " + global.getFileName() + " local to userProfile as the global didn't match the sha1 or file size");
            }
        }

        if (!Files.exists(global)) {
            // three tries to download
            for (int i = 0; i < 3; i++) {
                try {
                    System.out.println("Downloading library " + global.getFileName());
                    Files.createDirectories(global.getParent());
                    Path tmp = global.getParent().resolve(global.getFileName() + ".tmp");
                    try (InputStream stream = artifact.url().openStream()) {
                        Files.write(tmp, stream.readAllBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    }
                    if (shaMatch(tmp, artifact.sha1())) {
                        Files.move(tmp, global, StandardCopyOption.REPLACE_EXISTING);
                        break;
                    } else {
                        throw new IOException("SHA1 didn't match");
                    }
                } catch (IOException e) {
                    System.err.println("Failed to download library " + global.getFileName() + " (try " + (i + 1) + ")");
                    e.printStackTrace();
                }
            }
            if (!Files.exists(global)) {
                throw new IOException("Failed to download library " + global.getFileName());
            }
        }

        if (!global.equals(local)) {
            Files.createDirectories(local.getParent());
            if (!Files.exists(local)) {
                if (OSUtils.getOSId().equals("windows")) {
                    //TODO: windows doesn't support symlinks??? without admin anyway...
                    Files.copy(global, local, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.createLink(local, global);
                }
            }
        }
        return local;
    }

    //TODO: maybe these should be local only? and always try to redownload?
    public Path resolveMaven(Profile userProfile, Version.Library library) throws IOException {
        String[] maven = library.name().split(":");
        if (maven.length < 3 || maven.length > 4) {
            throw new IOException("Invalid maven library " + library.name());
        }
        String group = maven[0];
        String artifact = maven[1];
        String version = maven[2];
        String classifier = null;
        String ext = "jar";
        if (maven.length == 4) {
            classifier = maven[3];
            if (classifier.contains("@")) {
                String[] parts = classifier.split("@");
                classifier = parts[0];
                ext = parts[1];
            }
        } else if (version.contains("@")) {
            String[] parts = version.split("@");
            version = parts[0];
            ext = parts[1];
        }
        Path pth = Path.of(group.replace('.', '/'), artifact, version);
        pth = classifier == null ? pth.resolve(artifact + "-" + version + "." + ext) : pth.resolve(artifact + "-" + version + "-" + classifier + "." + ext);
        Path global = globalLibPath.resolve(pth);
        Path local = userProfile.gameDir().resolve("libraries").resolve(pth);

        // already exists
        if (Files.exists(local)) {
            System.out.println("Local library for " + library.name() + " already exists, skipping");
            return local;
        }

        if (!Files.exists(global)) {
            // download
            System.out.println("Downloading library " + library.name());
            Files.createDirectories(global.getParent());
            Path tmp = global.getParent().resolve(global.getFileName() + ".tmp");
            String mavenUrl = library.url().toString();
            if (!mavenUrl.endsWith("/")) {
                mavenUrl += "/";
            }
            try (InputStream stream = new URL(mavenUrl + pth.toString()).openStream()) {
                Files.write(tmp, stream.readAllBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            }
            Files.move(tmp, global, StandardCopyOption.REPLACE_EXISTING);
        }

        Files.createDirectories(local.getParent());
        if (!Files.exists(local)) {
            if (OSUtils.getOSId().equals("windows")) {
                //TODO: windows doesn't support symlinks??? without admin anyway...
                Files.copy(global, local, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.createLink(local, global);
            }
        }
        return local;
    }

    public void doExtract(Profile userProfile, Path path, Version.Extract extract) throws IOException {
        try (FileSystem fs = FileSystems.newFileSystem(path, Map.of())) {
            for (Path pth : fs.getRootDirectories()) {
                Files.walkFileTree(pth, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path relPath = pth.relativize(file);
                        if (Arrays.stream(extract.exclude()).anyMatch(relPath.toString()::startsWith)) {
                            return FileVisitResult.CONTINUE;
                        }
                        Path target = userProfile.nativePath(launcher).resolve(relPath.toString());
                        Files.createDirectories(target.getParent());
                        Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }
    }

    public static boolean shaMatch(Path path, String sha1) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] fileSha = md.digest(Files.readAllBytes(path));
            String fSha = "";
            for (byte b : fileSha) {
                fSha += String.format("%02x", b);
            }
            return fSha.equals(sha1);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }

    }

}
