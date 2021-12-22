package xyz.wagyourtail.launcher.minecraft.auth;

import com.google.gson.JsonObject;
import xyz.wagyourtail.launcher.Logger;
import xyz.wagyourtail.launcher.minecraft.auth.common.GetProfile;

import javax.swing.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public interface BaseAuthProvider {

    String getProviderName();
    String getProviderKey();

    GetProfile.MCProfile withLogger(Logger logger, JProgressBar progressBar) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException;

    GetProfile.MCProfile resolveProfile(Logger logger, JsonObject json, boolean offline) throws IOException;
}
