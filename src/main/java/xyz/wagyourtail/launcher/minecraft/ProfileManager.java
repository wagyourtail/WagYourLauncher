package xyz.wagyourtail.launcher.minecraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.wagyourtail.launcher.main.Launcher;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;

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
                JsonObject profile = entry.getValue().getAsJsonObject();
                Profile p = new Profile(
                    get(profile, "name").map(JsonElement::getAsString).orElse(null),
                    get(profile, "gameDir").map(e -> launcher.minecraftPath.resolve(e.getAsString())).orElse(launcher.minecraftPath),
                    get(profile, "created").map(JsonElement::getAsString).map(Instant::parse).map(Instant::getEpochSecond).orElse(0L),
                    get(profile, "lastUsed").map(JsonElement::getAsString).map(Instant::parse).map(Instant::getEpochSecond).orElse(0L),
                    get(profile, "icon").map(JsonElement::getAsString).orElse(null),
                    get(profile, "javaArgs").map(JsonElement::getAsString).orElse(null),
                    get(profile, "javaDir").map(e -> launcher.minecraftPath.resolve(e.getAsString())).orElse(null),
                    get(profile, "lastVersionId").map(JsonElement::getAsString).orElse(null),
                    get(profile, "type").map(e -> Profile.Type.byId(e.getAsString())).orElse(Profile.Type.CUSTOM)
                );
                profilesById.put(entry.getKey(), p);
                profilesByName.computeIfAbsent(profile.get("name").getAsString(), k -> new ArrayList<>()).add(new ProfileWithID(entry.getKey(), p));
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

    public void addProfile(String id, Profile profile) {
        profilesById.put(id, profile);
        profilesByName.computeIfAbsent(profile.name(), k -> new ArrayList<>()).add(new ProfileWithID(id, profile));
    }

    public void write() throws IOException {
        Path profilePath = launcher.minecraftPath.resolve("launcher_profiles.json");
        JsonObject json = new JsonObject();
        JsonObject profiles = new JsonObject();
        json.add("profiles", profiles);
        for (Map.Entry<String, Profile> ep : profilesById.entrySet()) {
            Profile p = ep.getValue();
            JsonObject profile = new JsonObject();
            if (p.name() != null) profile.addProperty("name", p.name());
            if (p.gameDir() != null) profile.addProperty("gameDir", p.gameDir().toAbsolutePath().toString());
            if (p.created() != 0L) profile.addProperty("created", Instant.ofEpochSecond(p.created()).toString());
            if (p.lastUsed() != 0L) profile.addProperty("lastUsed", Instant.ofEpochSecond(p.lastUsed()).toString());
            if (p.icon() != null) profile.addProperty("icon", p.icon());
            if (p.javaArgs() != null) profile.addProperty("javaArgs", p.javaArgs());
            if (p.javaDir() != null) profile.addProperty("javaDir", p.javaDir().toAbsolutePath().toString());
            if (p.lastVersionId() != null) profile.addProperty("lastVersionId", p.lastVersionId());
            profile.addProperty("type", p.type().id);
            profiles.add(ep.getKey(), profile);
        }
        Files.writeString(profilePath, json.toString(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
    }

    public record ProfileWithID(String id, Profile profile) {}
}
