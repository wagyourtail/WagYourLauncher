package xyz.wagyourtail.launcher.minecraft.auth;

import com.google.gson.JsonObject;
import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.Logger;
import xyz.wagyourtail.launcher.minecraft.auth.common.GetProfile;
import xyz.wagyourtail.launcher.minecraft.auth.common.MCToken;
import xyz.wagyourtail.launcher.minecraft.auth.xbox.*;

import javax.swing.*;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;

public class MSAAuthProvider implements BaseAuthProvider {
    // Steps
    private final Step1MSACode step1;
    private final Step2MSAToken step2;
    private final Step3XBLToken step3;
    private final Step4XSTSToken step4;
    private final Step5MCToken step5;
    private final GetProfile profile;

    public static final String CLIENT_ID = "91a46970-06ab-45f5-9691-cb9f2a490475";
    public static final String SCOPES = "XboxLive.signin XboxLive.offline_access";

    private final Launcher launcher;

    public MSAAuthProvider(Launcher launcher) {
        this.launcher = launcher;
        this.step1 = new Step1MSACode(launcher);
        this.step2 = new Step2MSAToken(launcher, step1);
        this.step3 = new Step3XBLToken(launcher, step2);
        this.step4 = new Step4XSTSToken(launcher, step3);
        this.step5 = new Step5MCToken(launcher, step4);
        this.profile = new GetProfile(launcher, step5);
    }

    @Override
    public String getProviderName() {
        return "Microsoft";
    }

    @Override
    public String getProviderKey() {
        return "msa";
    }

    @Override
    public GetProfile.MCProfile withLogger(Logger logger, JProgressBar progress) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
        if (progress != null) progress.setValue(0);
        logger.info("Logging in with Microsoft");
        Step1MSACode.MSACode code = step1.applyStep(null, logger);

        if (progress != null) progress.setValue(100/6);
        logger.info("Got Code");
        Step2MSAToken.MSAToken token = step2.applyStep(code, logger);

        if (progress != null) progress.setValue(200/6);
        logger.info("Got Token, expires: " + Instant.ofEpochMilli(token.expireTimeMs()));
        Step3XBLToken.XBLToken xbl = step3.applyStep(token, logger);

        if (progress != null) progress.setValue(300/6);
        logger.info("Got XBL Token, expires: " + Instant.ofEpochMilli(xbl.expireTimeMs()));
        Step4XSTSToken.XSTSToken xsts = step4.applyStep(xbl, logger);

        if (progress != null) progress.setValue(400/6);
        logger.info("Got XSTS Token, expires: " + Instant.ofEpochMilli(xsts.expireTimeMs()));
        MCToken mc = step5.applyStep(xsts, logger);

        if (progress != null) progress.setValue(500/6);
        logger.info("Got MCToken, expires: " + Instant.ofEpochMilli(mc.expireTime()));
        GetProfile.MCProfile profile = this.profile.applyStep(mc, logger);

        if (progress != null) progress.setValue(600/6);
        logger.info("Got Profile: {\"name\": \"" + profile.name() + "\", \"uuid\": \"" + profile.id() + "\"}");
        launcher.auth.setProfile(profile);
        return profile;
    }

    @Override
    public GetProfile.MCProfile resolveProfile(Logger logger, JsonObject json, boolean offline) throws IOException {
        GetProfile.MCProfile prof = profile.fromJson(json);
        // not a msa profile
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
