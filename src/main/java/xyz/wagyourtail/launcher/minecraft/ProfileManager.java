package xyz.wagyourtail.launcher.minecraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.minecraft.userProfile.Profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.*;

public class ProfileManager {
    protected final Launcher launcher;
    protected final Map<String, Profile> profilesById = new HashMap<>();

    protected final Map<String, List<ProfileWithID>> profilesByName = new HashMap<>();

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
                profilesByName.computeIfAbsent(userProfile.get("name").getAsString(), k -> new ArrayList<>()).add(new ProfileWithID(entry.getKey(), p));
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

    public List<ProfileWithID> getProfileByName(String name) {
        return List.copyOf(profilesByName.get(name));
    }

    public void addProfile(String id, Profile userProfile) {
        profilesById.put(id, userProfile);
        profilesByName.computeIfAbsent(userProfile.name(), k -> new ArrayList<>()).add(new ProfileWithID(id, userProfile));
    }

    public void write() throws IOException {
        Path profilePath = launcher.minecraftPath.resolve("launcher_profiles.json");
        JsonObject json = new JsonObject();
        JsonObject profiles = new JsonObject();
        json.add("profiles", profiles);
        for (Map.Entry<String, Profile> ep : profilesById.entrySet()) {
            Profile p = ep.getValue();
            JsonObject userProfile = new JsonObject();
            if (p.name() != null) userProfile.addProperty("name", p.name());
            if (p.gameDir() != null) userProfile.addProperty("gameDir", p.gameDir().toAbsolutePath().toString());
            if (p.created() != 0L) userProfile.addProperty("created", Instant.ofEpochSecond(p.created()).toString());
            if (p.lastUsed() != 0L) userProfile.addProperty("lastUsed", Instant.ofEpochSecond(p.lastUsed()).toString());
            if (p.icon() != null) userProfile.addProperty("icon", p.icon());
            if (p.javaArgs() != null) userProfile.addProperty("javaArgs", p.javaArgs());
            if (p.javaDir() != null) userProfile.addProperty("javaDir", p.javaDir().toAbsolutePath().toString());
            if (p.lastVersionId() != null) userProfile.addProperty("lastVersionId", p.lastVersionId());
            userProfile.addProperty("type", p.type().id);
            profiles.add(ep.getKey(), userProfile);
        }
        Files.writeString(profilePath, json.toString(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
    }

    public record ProfileWithID(String id, Profile userProfile) {}
}
