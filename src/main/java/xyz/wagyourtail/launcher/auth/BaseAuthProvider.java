package xyz.wagyourtail.launcher.auth;

import com.google.gson.JsonObject;
import xyz.wagyourtail.launcher.auth.common.GetProfile;
import xyz.wagyourtail.launcher.gui.screen.login.AddAccountScreen;
import xyz.wagyourtail.notlog4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public interface BaseAuthProvider {

    String getProviderName();
    String getProviderKey();

    GetProfile.MCProfile withLogger(AddAccountScreen screen) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException;

    GetProfile.MCProfile resolveProfile(Logger logger, JsonObject json, boolean offline) throws IOException;
}
