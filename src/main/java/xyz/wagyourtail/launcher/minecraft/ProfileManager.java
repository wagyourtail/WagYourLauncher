package xyz.wagyourtail.launcher.minecraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.*;

public class ProfileManager {
    protected final Launcher launcher;
    protected final Map<String, Profile> profilesById = new HashMap<>();

    protected final Map<String, List<Profile>> profilesByName = new HashMap<>();

    public ProfileManager(Launcher launcher) {
        this.launcher = launcher;
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
                    get(userProfile, "created").map(JsonElement::getAsString).map(Instant::parse).map(Instant::getEpochSecond).orElse(0L),
                    get(userProfile, "lastUsed").map(JsonElement::getAsString).map(Instant::parse).map(Instant::getEpochSecond).orElse(0L),
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
        Files.writeString(tmp, json.toString(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        Files.move(tmp, profilePath, StandardCopyOption.REPLACE_EXISTING);
    }
}
