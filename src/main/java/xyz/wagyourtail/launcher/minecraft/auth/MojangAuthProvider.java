package xyz.wagyourtail.launcher.minecraft.auth;

import com.google.gson.JsonObject;
import xyz.wagyourtail.launcher.minecraft.auth.common.GetProfile;

import java.net.MalformedURLException;

public class MojangAuthProvider implements BaseAuthProvider {
    @Override
    public String getProviderName() {
        return "Mojang";
    }

    @Override
    public String getProviderKey() {
        return "yggdrasil";
    }

    @Override
    public GetProfile.MCProfile displayLoginTerminal() {
        return null;
    }

    @Override
    public GetProfile.MCProfile displayLoginGui() {
        return null;
    }

    @Override
    public GetProfile.MCProfile resolveProfile(JsonObject json) {
        return null;
    }

    @Override
    public GetProfile.MCProfile resolveProfileGui(JsonObject json) throws MalformedURLException {
        return null;
    }

}
