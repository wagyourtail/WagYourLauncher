package xyz.wagyourtail.launcher.managers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.versions.BaseVersionProvider;
import xyz.wagyourtail.launcher.versions.InstalledVersions;
import xyz.wagyourtail.launcher.versions.VanillaVersions;
import xyz.wagyourtail.notlog4j.Logger;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;
import xyz.wagyourtail.launcher.minecraft.version.Version;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.*;

public class ProfileManager {
    protected final LauncherBase launcher;
    protected final Map<String, Profile> profilesById = new HashMap<>();

    protected final Map<String, List<Profile>> profilesByName = new HashMap<>();

    protected final Map<Profile, Process> runningLock = new HashMap<>();

    public final BaseVersionProvider<?>[] versionProviders;

    public ProfileManager(LauncherBase launcher) {
        this.launcher = launcher;
        versionProviders = new BaseVersionProvider[] {
            new InstalledVersions(launcher),
            new VanillaVersions(launcher)
        };
    }

    protected static Optional<JsonElement> get(JsonObject json, String key) {
        return Optional.ofNullable(json.get(key));
    }

    public void populate() throws IOException {
        Path profilePath = launcher.minecraftPath.resolve("launcher_profiles.json");
        if (Files.exists(profilePath)) {
            for (Map.Entry<String, JsonElement> entry : JsonParser.parseString(Files.readString(profilePath)).getAsJsonObject().getAsJsonObject("profiles").entrySet()) {
                JsonObject userProfile = entry.getValue().getAsJsonObject();
                Profile p = new Profile(
                    entry.getKey(),
                    get(userProfile, "name").map(JsonElement::getAsString).orElse(null),
                    get(userProfile, "gameDir").map(e -> launcher.minecraftPath.resolve(e.getAsString())).orElse(launcher.minecraftPath),
                    get(userProfile, "created").map(JsonElement::getAsString).map(Instant::parse).map(Instant::toEpochMilli).orElse(0L),
                    get(userProfile, "lastUsed").map(JsonElement::getAsString).map(Instant::parse).map(Instant::toEpochMilli).orElse(0L),
                    get(userProfile, "icon").map(JsonElement::getAsString).orElse(null),
                    get(userProfile, "javaArgs").map(JsonElement::getAsString).orElse(null),
                    get(userProfile, "javaDir").map(e -> launcher.minecraftPath.resolve(e.getAsString())).orElse(null),
                    get(userProfile, "lastVersionId").map(JsonElement::getAsString).orElse(null),
                    get(userProfile, "type").map(e -> Profile.Type.byId(e.getAsString())).orElse(Profile.Type.CUSTOM)
                );
                profilesById.put(entry.getKey(), p);
                profilesByName.computeIfAbsent(userProfile.get("name").getAsString(), k -> new ArrayList<>()).add(p);
            }
        } else {
            write();
        }
    }

    public Map<String, Profile> getAllProfiles() {
        return Map.copyOf(profilesById);
    }

    public Optional<Profile> getProfileById(String id) {
        return Optional.ofNullable(profilesById.get(id));
    }

    public List<Profile> getProfileByName(String name) {
        return List.copyOf(profilesByName.get(name));
    }

    public void addProfile(String id, Profile userProfile) {
        profilesById.put(id, userProfile);
        profilesByName.computeIfAbsent(userProfile.name(), k -> new ArrayList<>()).add(userProfile);
    }

    public void write() throws IOException {
        Path profilePath = launcher.minecraftPath.resolve("launcher_profiles.json");
        JsonObject json = new JsonObject();
        JsonObject profiles = new JsonObject();
        json.add("profiles", profiles);
        for (Map.Entry<String, Profile> ep : profilesById.entrySet()) {
            Profile p = ep.getValue();
            profiles.add(ep.getKey(), p.toJson());
        }
        Path tmp = profilePath.getParent().resolve(profilePath.getFileName() + ".tmp");
        Files.writeString(tmp, json.toString(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        Files.move(tmp, profilePath, StandardCopyOption.REPLACE_EXISTING);
    }

    public void modifyProfile(Profile old, Profile newP) {
        profilesById.put(newP.key(), newP);
        profilesByName.get(old.name()).remove(old);
        profilesByName.computeIfAbsent(newP.name(), k -> new ArrayList<>()).add(newP);
    }

    public synchronized void launch(Profile profile, Logger profileLogger, String username, boolean offline) throws IOException {
        if (runningLock.containsKey(profile)) {
            throw new IOException("Profile " + profile.name() + " is already running");
        }
        Version resolved = Version.resolve(launcher, profile.lastVersionId());
        if (resolved == null) {
            profileLogger.error("Could not resolve version " + profile.lastVersionId());
            return;
        }
        profileLogger.info("Launching " + profile.name() + " (version " + resolved.id() + ")");
        Path gameDir = profile.gameDir();
        if (!Files.exists(gameDir)) {
            Files.createDirectories(gameDir);
        }
        List<String> args = new ArrayList<>();
        args.add(profile.getJavaDir(launcher));
        args.addAll(Arrays.asList(resolved.getJavaArgs(launcher, profile, profile.nativePath(launcher), profile.javaArgs(), resolved.getClassPath(launcher, profile))));
        args.addAll(Arrays.asList(resolved.getLogging(launcher)));
        args.add(resolved.getMainClass());
        args.addAll(Arrays.asList(resolved.getGameArgs(launcher, profile, profileLogger, username, profile.gameDir(), offline)));

        profileLogger.info("Launching with args: ");
        boolean prevArgSensitive = false;
        boolean prevArgCP = false;
        for (String arg : args) {
            if (prevArgSensitive) {
                prevArgSensitive = false;
                profileLogger.info("    *****");
                continue;
            }
            if (prevArgCP) {
                prevArgCP = false;
                profileLogger.info("        " + String.join(":\n        ", arg.split(":")));
                continue;
            }
            if (arg.matches("--accessToken")) prevArgSensitive = true;
            if (arg.matches("-cp")) prevArgCP = true;
            profileLogger.info("    " + arg);
        }
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(gameDir.toFile());
        Process p = pb.start();
        runningLock.put(profile, p);
        launcher.refreshProfiles();
        p.onExit().thenRun(() -> {
            runningLock.remove(profile);
            launcher.refreshProfiles();
        });
        profile.pipeOutput(launcher, p, profileLogger);
    }

    public Set<Profile> getRunningProfiles() {
        return runningLock.keySet();
    }

    public void killRunning(Profile profile) {
        Process p = runningLock.get(profile);
        if (p != null) {
            p.destroy();
        }
    }

    public void forceKillRunning(Profile profile) {
        Process p = runningLock.get(profile);
        if (p != null) {
            p.destroyForcibly();
        }
    }
}
