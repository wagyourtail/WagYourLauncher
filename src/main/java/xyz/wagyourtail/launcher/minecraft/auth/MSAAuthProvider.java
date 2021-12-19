package xyz.wagyourtail.launcher.minecraft.auth;

import com.google.gson.JsonObject;
import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.minecraft.auth.common.GetProfile;
import xyz.wagyourtail.launcher.minecraft.auth.common.MCToken;
import xyz.wagyourtail.launcher.minecraft.auth.xbox.*;
import xyz.wagyourtail.launcher.nogui.LauncherNoGui;

import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
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
    public GetProfile.MCProfile displayLoginTerminal() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
        launcher.getLogger().onInfo("Logging in with Microsoft");
        Step1MSACode.MSACode code = step1.applyStep(null);
        launcher.getLogger().onInfo("Got Code");
        Step2MSAToken.MSAToken token = step2.applyStep(code);
        launcher.getLogger().onInfo("Got Token, expires: " + Instant.ofEpochMilli(token.expireTimeMs()));
        Step3XBLToken.XBLToken xbl = step3.applyStep(token);
        launcher.getLogger().onInfo("Got XBL Token, expires: " + Instant.ofEpochMilli(xbl.expireTimeMs()));
        Step4XSTSToken.XSTSToken xsts = step4.applyStep(xbl);
        launcher.getLogger().onInfo("Got XSTS Token, expires: " + Instant.ofEpochMilli(xsts.expireTimeMs()));
        MCToken mc = step5.applyStep(xsts);
        launcher.getLogger().onInfo("Got MCToken, expires: " + Instant.ofEpochMilli(mc.expireTime()));
        GetProfile.MCProfile profile = this.profile.applyStep(mc);
        launcher.getLogger().onInfo("Got Profile: {\"name\": \"" + profile.name() + "\", \"uuid\": \"" + profile.id() + "\"}");
        launcher.auth.setProfile(profile);
        return profile;
    }

    @Override
    public GetProfile.MCProfile displayLoginGui() {
        return null;
    }

    @Override
    public GetProfile.MCProfile resolveProfile(JsonObject json) throws IOException {
        GetProfile.MCProfile prof = profile.fromJson(json).getResult();
        // not a msa profile
        if (prof == null) {
            return null;
        }

        // expired
        if(System.currentTimeMillis() > prof.prev().expireTime()) {
            launcher.getLogger().onInfo("Profile expired, refreshing");
            return profile.refresh(prof).getResult();
        }

        // valid
        return prof;
    }

    @Override
    public GetProfile.MCProfile resolveProfileGui(JsonObject json) {
        return null;
    }

    public static void main(String[] args) throws IOException, UnrecoverableEntryException, CertificateException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
        MSAAuthProvider msa = new MSAAuthProvider(new LauncherNoGui(Path.of("./run/")));
        msa.displayLoginTerminal();
    }
}
