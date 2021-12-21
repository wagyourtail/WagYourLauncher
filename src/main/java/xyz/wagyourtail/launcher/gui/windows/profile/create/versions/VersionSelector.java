package xyz.wagyourtail.launcher.gui.windows.profile.create.versions;

import xyz.wagyourtail.launcher.gui.windows.profile.create.GuiNewProfile;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public abstract class VersionSelector extends JPanel {
    protected GuiNewProfile parent;
    protected boolean initialized = false;

    public VersionSelector(GuiNewProfile parent) {
        this.parent = parent;
    }

    public abstract void init();

    public abstract void create();

    public void createVanillaProfile(String lastVersionId) {
        String id = UUID.randomUUID().toString();
        String name = parent.getProfileName();

        Path gameDir = parent.launcher.minecraftPath.resolve("profiles").resolve(name);

        int i = 0;
        while (Files.exists(gameDir)) {
            gameDir = gameDir.getParent().resolve(name + "_" + i);
        }

        parent.launcher.profiles.addProfile(id, new Profile(
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
        ));
        try {
            parent.launcher.profiles.write();
            parent.launcher.mainWindow.populateProfiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isInitialized() {
        return initialized;
    }
}
