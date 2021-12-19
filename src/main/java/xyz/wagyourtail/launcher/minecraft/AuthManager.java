package xyz.wagyourtail.launcher.minecraft;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.gui.LauncherGui;
import xyz.wagyourtail.launcher.minecraft.auth.BaseAuthProvider;
import xyz.wagyourtail.launcher.minecraft.auth.MSAAuthProvider;
import xyz.wagyourtail.launcher.minecraft.auth.common.GetProfile;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AuthManager {
    public final Map<String, BaseAuthProvider> authProviders;

    protected final Launcher launcher;
    private KeyStore keyStore;
    protected final Path registeredUsersPath;
    protected final Path keyStorePath;
    protected final Map<String, UUID> registeredUsers;
    private char[] keyStorePass;

    private GetProfile.MCProfile lastUsedCache;

    public AuthManager(Launcher launcher) {
        Map<String, UUID> registeredUsers1 = new HashMap<>();
        this.launcher = launcher;

        this.registeredUsersPath = launcher.minecraftPath.resolve("registered_users.json");
        this.keyStorePath = launcher.minecraftPath.resolve("launcher.jks");

        if (Files.exists(registeredUsersPath)) {
            try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(registeredUsersPath))) {
                registeredUsers1 = new Gson().fromJson(reader, new TypeToken<Map<String, UUID>>() {}.getType());
            } catch (IOException ignored) {}
        }
        registeredUsers = registeredUsers1;
        authProviders = Arrays.stream(new BaseAuthProvider[] {new MSAAuthProvider(launcher)}).collect(Collectors.toMap(BaseAuthProvider::getProviderName, Function.identity()));
    }

    public boolean isRegistered(String username) {
        return registeredUsers.containsKey(username);
    }

    public Map<String, UUID> getRegisteredUsers() {
        return Map.copyOf(registeredUsers);
    }

    public UUID getUUID(String username) {
        return registeredUsers.get(username);
    }

    public String getToken(String username) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, InterruptedException, UnrecoverableEntryException, InvalidKeySpecException {
        return getProfile(username).prev().access_token();
    }

    public String getUserType(String username) throws UnrecoverableEntryException, CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
        return getProfile(username).getPrevResult().user_type();
    }

    private KeyStore getKeyStore() throws InterruptedException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        if (keyStore == null) {
            if (!Files.exists(keyStorePath)) {
                keyStorePass = launcher.promptKeystorePasswordAndWait(true);
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, "".toCharArray());
                Path tmp = keyStorePath.getParent().resolve(keyStorePath.getFileName() + ".tmp");
                keyStore.store(Files.newOutputStream(tmp), keyStorePass);
                Files.move(tmp, keyStorePath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                for (int i = 0; true; i++) {
                    try {
                        keyStorePass = launcher.promptKeystorePasswordAndWait(false);
                        keyStore = KeyStore.getInstance(keyStorePath.toFile(), keyStorePass);
                        break;
                    } catch (IOException e) {
                        keyStorePass = null;
                        if (i != 2) {
                            launcher.getLogger().onInfo("Keystore password incorrect! Please try again.");
                        } else {
                            launcher.getLogger().onInfo("Keystore password incorrect! throwing exception.");
                            throw new IOException("Keystore corrupted or password incorrect!", e);
                        }
                    }
                }
            }
        }
        return keyStore;
    }

    private void setKey(String key, String val) throws InvalidKeySpecException, NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, InterruptedException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBE");
        SecretKey sk = keyFactory.generateSecret(new PBEKeySpec(val.toCharArray()));
        KeyStore ks = getKeyStore();
        KeyStore.PasswordProtection prot = new KeyStore.PasswordProtection(keyStorePass);
        ks.setEntry(key, new KeyStore.SecretKeyEntry(sk), prot);
        Path tmp = keyStorePath.getParent().resolve(keyStorePath.getFileName() + ".tmp");
        ks.store(Files.newOutputStream(tmp, StandardOpenOption.CREATE, StandardOpenOption.WRITE), keyStorePass);
        Files.move(tmp, keyStorePath, StandardCopyOption.REPLACE_EXISTING);
    }

    private String getKey(String key) throws UnrecoverableEntryException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, InterruptedException, InvalidKeySpecException {
        KeyStore ks = getKeyStore();
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBE");
        KeyStore.PasswordProtection prot = new KeyStore.PasswordProtection(keyStorePass);
        KeyStore.SecretKeyEntry ske = (KeyStore.SecretKeyEntry) ks.getEntry(key, prot);
        PBEKeySpec spec = (PBEKeySpec) keyFactory.getKeySpec(ske.getSecretKey(), PBEKeySpec.class);
        char[] val = spec.getPassword();
        return new String(val);
    }

    protected GetProfile.MCProfile getProfile(String username) throws IOException, InterruptedException, UnrecoverableEntryException, CertificateException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException {
        if (lastUsedCache != null && lastUsedCache.name().equals(username)) {
            return lastUsedCache;
        }
        if (registeredUsers.containsKey(username)) {
            try {
                JsonObject json = JsonParser.parseString(getKey(username)).getAsJsonObject();
                for (BaseAuthProvider provider : authProviders.values()) {
                    GetProfile.MCProfile profile = launcher instanceof LauncherGui ? provider.resolveProfileGui(json) : provider.resolveProfile(json);
                    if (profile != null) {
                        lastUsedCache = profile;
                        return profile;
                    }
                }
                registeredUsers.remove(username);
                saveRegisteredUsers();
            } catch (NullPointerException e) {
                registeredUsers.remove(username);
                saveRegisteredUsers();
                e.printStackTrace();
            }
        }
        return null;
    }

    public GetProfile.MCProfile setProfile(GetProfile.MCProfile userProfile) throws IOException, InterruptedException, CertificateException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException {
        setKey(userProfile.name(), userProfile.toJson().toString());
        registeredUsers.put(userProfile.name(), userProfile.id());
        saveRegisteredUsers();
        return userProfile;
    }

    public void saveRegisteredUsers() throws IOException {
        Path tmp = registeredUsersPath.getParent().resolve(registeredUsersPath.getFileName() + ".tmp");
        Files.write(tmp, new Gson().toJson(registeredUsers).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        Files.move(tmp, registeredUsersPath, StandardCopyOption.REPLACE_EXISTING);
    }
}
