package xyz.wagyourtail.launcher.minecraft.auth;

import com.google.gson.JsonObject;
import xyz.wagyourtail.launcher.minecraft.auth.common.GetProfile;

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

    GetProfile.MCProfile displayLoginTerminal() throws IOException, UnrecoverableEntryException, CertificateException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException;
    GetProfile.MCProfile displayLoginGui();

    GetProfile.MCProfile resolveProfile(JsonObject json) throws IOException;

    GetProfile.MCProfile resolveProfileGui(JsonObject json) throws MalformedURLException;
}
