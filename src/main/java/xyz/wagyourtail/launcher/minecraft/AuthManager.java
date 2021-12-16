package xyz.wagyourtail.launcher.minecraft;

import xyz.wagyourtail.launcher.Launcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthManager {
    protected final Launcher launcher;
    protected final Map<String, UUID> registeredUsers = new HashMap<>();

    public AuthManager(Launcher launcher) {
        this.launcher = launcher;
        registeredUsers.put("wagyourtail", UUID.randomUUID());
    }

    public boolean isRegistered(String username) {
        return registeredUsers.containsKey(username);
    }

    public UUID getUUID(String username) {
        return registeredUsers.get(username);
    }

    public String getToken(String username) {
        return registeredUsers.get(username).toString();
    }

    public String getUserType(String username) {
        return "msa";
    }

}
