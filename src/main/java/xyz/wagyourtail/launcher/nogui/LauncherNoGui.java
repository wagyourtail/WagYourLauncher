package xyz.wagyourtail.launcher.nogui;

import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class LauncherNoGui extends Launcher {

    public LauncherNoGui(Path minecraftPath) throws IOException {
        super(minecraftPath);
    }

    @Override
    public void launch(Profile profile, String username) throws IOException {
        profile.launch(this, username, System.out, System.err);
    }

    public void listProfiles() {
        System.out.println("Profiles:");
        int[] tableSizes = new int[3];
        for (Map.Entry<String, Profile> entry : profiles.getAllProfiles().entrySet()) {
            tableSizes[0] = Math.max(tableSizes[0], entry.getKey().length());
            tableSizes[1] = Math.max(tableSizes[1], entry.getValue().name().length());
            tableSizes[2] = Math.max(tableSizes[2], entry.getValue().lastVersionId().length());
        }
        System.out.println("\t" + rPadTo("id", tableSizes[0]) + "\t" + rPadTo("name", tableSizes[1]) + "\t" + rPadTo("(version)", tableSizes[2]));
        System.out.println(dashes(tableSizes[0] + tableSizes[1] + tableSizes[2] + 12));
        for (Map.Entry<String, Profile> prof : profiles.getAllProfiles().entrySet()) {
            System.out.println("\t" + rPadTo(prof.getKey(), tableSizes[0]) + "\t" + rPadTo(prof.getValue().name(), tableSizes[1]) + "\t" + rPadTo(prof.getValue().lastVersionId(), tableSizes[2]));
        }
    }

    public String dashes(int len) {
        String s = "";
        for (int i = 0; i < len; i++) s += "-";
        return s;
    }

    public String rPadTo(String s, int len) {
        while (s.length() < len) s += " ";
        return s;
    }



}
