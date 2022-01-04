package xyz.wagyourtail.launcher.auth;


import com.google.gson.JsonObject;
import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.auth.common.GetProfile;
import xyz.wagyourtail.launcher.auth.yggdrasil.Step1Login;
import xyz.wagyourtail.launcher.gui.screen.login.AddAccountScreen;
import xyz.wagyourtail.notlog4j.Logger;
import xyz.wagyourtail.launcher.auth.common.MCToken;

import javax.swing.*;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.Semaphore;

public class YggdrasilAuthProvider implements BaseAuthProvider {
    private final LauncherBase launcher;
    private final Step1Login login;
    private final GetProfile profile;

    public YggdrasilAuthProvider(LauncherBase launcher) {
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
    public GetProfile.MCProfile withLogger(AddAccountScreen screen) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
        Logger logger = screen.getLogger();
        screen.setProgress(0);
        Semaphore semaphore = new Semaphore(0);
        Step1Login.UsernamePassword[] up = new Step1Login.UsernamePassword[1];
        screen.getUsernamePassword().then((u, p) -> {
            if (u != null && p != null) {
                up[0] = new Step1Login.UsernamePassword(u, p);
            }
            semaphore.release();
        });
        semaphore.acquire();
        MCToken token = login.applyStep(up[0], logger);
        screen.setProgress(50);
        GetProfile.MCProfile profile = this.profile.applyStep(token, logger);
        screen.setProgress(100);
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
