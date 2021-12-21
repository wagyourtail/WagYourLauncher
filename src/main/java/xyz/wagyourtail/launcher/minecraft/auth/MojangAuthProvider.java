package xyz.wagyourtail.launcher.minecraft.auth;

import com.google.gson.JsonObject;
import xyz.wagyourtail.launcher.Logger;
import xyz.wagyourtail.launcher.gui.windows.login.GuiLogin;
import xyz.wagyourtail.launcher.minecraft.auth.common.GetProfile;

import javax.swing.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

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
    public GetProfile.MCProfile withLogger(Logger logger, JProgressBar progress) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
        return null;
    }

    @Override
    public GetProfile.MCProfile resolveProfile(Logger logger, JsonObject json, boolean offline) {
        return null;
    }

    @Override
    public GetProfile.MCProfile resolveProfileGui(Logger logger, JsonObject json, boolean offline) throws MalformedURLException {
        return null;
    }

}
