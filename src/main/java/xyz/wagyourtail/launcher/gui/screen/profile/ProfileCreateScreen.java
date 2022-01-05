package xyz.wagyourtail.launcher.gui.screen.profile;

import xyz.wagyourtail.launcher.gui.screen.Screen;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;
import xyz.wagyourtail.launcher.versions.BaseVersionProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public interface ProfileCreateScreen extends Screen {

    String getName();

    BaseVersionProvider.BaseVersionData getSelectedVersion();

    default Profile createProfile() throws IOException {
        BaseVersionProvider.BaseVersionData version = getSelectedVersion();

        if (version == null) {
            throw new IOException("No version selected");
        }

        String lastVersionId = version.provide();

        String id = UUID.randomUUID().toString();
        String name = getName();

        Path parentDir = getLauncher().minecraftPath.resolve("profiles");
        Path gameDir = parentDir.resolve(name.isEmpty() ? "_0" : name);

        int i = 1;
        while (Files.exists(gameDir)) {
            gameDir = gameDir.getParent().resolve(name + "_" + i++);
        }

        Profile profile = new Profile(
            id,
            name,
            gameDir,
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            "Furnace",
            null,
            null,
            lastVersionId,
            Profile.Type.CUSTOM
        );

        getLauncher().profiles.addProfile(id, profile);

        getLauncher().profiles.write();

        getLauncher().refreshProfiles();

        return profile;
    }
}
