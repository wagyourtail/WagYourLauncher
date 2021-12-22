package xyz.wagyourtail.launcher.minecraft.auth;


import com.google.gson.JsonObject;
import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.Logger;
import xyz.wagyourtail.launcher.minecraft.auth.common.GetProfile;
import xyz.wagyourtail.launcher.minecraft.auth.common.MCToken;
import xyz.wagyourtail.launcher.minecraft.auth.yggdrasil.Step1Login;

import javax.swing.*;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class YggdrasilAuthProvider implements BaseAuthProvider {
    private final Launcher launcher;
    private final Step1Login login;
    private final GetProfile profile;

    public YggdrasilAuthProvider(Launcher launcher) {
        this.launcher = launcher;
        this.login = new Step1Login(launcher);
        this.profile = new GetProfile(launcher, login);
    }
    @Override
    public String getProviderName() {
        return "Mojang";
    }

    @Override
    public String getProviderKey() {
        return "yggdrasil";
    }

    @Override
    public GetProfile.MCProfile withLogger(Logger logger, JProgressBar progress) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
        if (progress != null) progress.setValue(0);
        MCToken token = login.applyStep(null, logger);
        if (progress != null) progress.setValue(50);
        GetProfile.MCProfile profile = this.profile.applyStep(token, logger);
        if (progress != null) progress.setValue(100);
        logger.info("Got Profile: {\"name\": \"" + profile.name() + "\", \"uuid\": \"" + profile.id() + "\"}");
        launcher.auth.setProfile(profile);
        return profile;
    }

    @Override
    public GetProfile.MCProfile resolveProfile(Logger logger, JsonObject json, boolean offline) throws IOException {
        GetProfile.MCProfile prof = profile.fromJson(json);
        // not a yggdrasil profile
        if (prof == null) {
            return null;
        }

        if (offline) {
            return prof;
        }

        // expired
        if(System.currentTimeMillis() > prof.prev().expireTime()) {
            logger.info("MCProfile expired, refreshing");
            return profile.refresh(prof, launcher.getLogger()).getResult();
        }

        // valid
        return prof;
    }
}
