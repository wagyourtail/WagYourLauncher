package xyz.wagyourtail.launcher.auth;

import com.google.gson.JsonObject;
import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.auth.common.GetProfile;
import xyz.wagyourtail.launcher.auth.xbox.*;
import xyz.wagyourtail.launcher.gui.screen.login.AddAccountScreen;
import xyz.wagyourtail.notlog4j.Logger;
import xyz.wagyourtail.launcher.auth.common.MCToken;

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

    private final LauncherBase launcher;

    public MSAAuthProvider(LauncherBase launcher) {
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
    public GetProfile.MCProfile withLogger(AddAccountScreen screen) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
        Logger logger = screen.getLogger();

        screen.setProgress(0);
        logger.info("Logging in with Microsoft");
        Step1MSACode.MSACode code = step1.applyStep(null, logger);

        screen.setProgress(100/6);
        logger.info("Got Code");
        Step2MSAToken.MSAToken token = step2.applyStep(code, logger);

        screen.setProgress(200/6);
        logger.info("Got Token, expires: " + Instant.ofEpochMilli(token.expireTimeMs()));
        Step3XBLToken.XBLToken xbl = step3.applyStep(token, logger);

        screen.setProgress(300/6);
        logger.info("Got XBL Token, expires: " + Instant.ofEpochMilli(xbl.expireTimeMs()));
        Step4XSTSToken.XSTSToken xsts = step4.applyStep(xbl, logger);

        screen.setProgress(400/6);
        logger.info("Got XSTS Token, expires: " + Instant.ofEpochMilli(xsts.expireTimeMs()));
        MCToken mc = step5.applyStep(xsts, logger);

        screen.setProgress(500/6);
        logger.info("Got MCToken, expires: " + Instant.ofEpochMilli(mc.expireTime()));
        GetProfile.MCProfile profile = this.profile.applyStep(mc, logger);

        screen.setProgress(600/6);
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
