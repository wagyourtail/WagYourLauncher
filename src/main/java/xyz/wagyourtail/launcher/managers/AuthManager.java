package xyz.wagyourtail.launcher.managers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.auth.BaseAuthProvider;
import xyz.wagyourtail.launcher.auth.MSAAuthProvider;
import xyz.wagyourtail.launcher.auth.YggdrasilAuthProvider;
import xyz.wagyourtail.launcher.auth.common.GetProfile;
import xyz.wagyourtail.notlog4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class AuthManager {
    public final Map<String, BaseAuthProvider> authProviders;

    protected final LauncherBase launcher;
    private KeyStore keyStore;
    protected final Path registeredUsersPath;
    protected final Path keyStorePath;
    protected final Map<String, UUID> registeredUsers;
    private char[] keyStorePass;

    private GetProfile.MCProfile lastUsedCache;

    private String selectedProfile;

    public AuthManager(LauncherBase launcher) {
        Map<String, UUID> registeredUsers1 = new HashMap<>();
        this.launcher = launcher;

        // set files
        this.registeredUsersPath = launcher.minecraftPath.resolve("registered_users.json");
        this.keyStorePath = launcher.minecraftPath.resolve("launcher.jks");

        // register providers
        BaseAuthProvider[] providers = new BaseAuthProvider[] {
            new MSAAuthProvider(launcher),
            new YggdrasilAuthProvider(launcher)
        };
        authProviders = new LinkedHashMap<>();
        for (BaseAuthProvider provider : providers) {
            authProviders.put(provider.getProviderName(), provider);
        }

        // load registered users
        if (Files.exists(registeredUsersPath)) {
            try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(registeredUsersPath))) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                registeredUsers1 = new Gson().fromJson(json.get("registered"), new TypeToken<Map<String, UUID>>() {}.getType());
                if (json.has("selected")) {
                    selectedProfile = json.get("selected").getAsString();
                }
            } catch (Throwable ignored) {
                registeredUsers1 = new HashMap<>();
            }
        }
        if (registeredUsers1 == null) {
            registeredUsers1 = new HashMap<>();
        }
        registeredUsers = registeredUsers1;

        // select a default if none
        if (selectedProfile == null) {
            selectedProfile = registeredUsers.keySet().stream().findFirst().orElse(null);
        }
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

    public String getToken(Logger logger, String username) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, InterruptedException, UnrecoverableEntryException, InvalidKeySpecException {
        return getProfile(logger, username, false).prev().access_token();
    }

    public String getUserType(Logger logger, String username, boolean offline) throws UnrecoverableEntryException, CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
        return getProfile(logger, username, offline).getPrevResult().user_type();
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
                            launcher.getLogger().info("Keystore password incorrect! Please try again.");
                        } else {
                            launcher.getLogger().info("Keystore password incorrect! throwing exception.");
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
        ks.store(Files.newOutputStream(tmp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), keyStorePass);
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

    public GetProfile.MCProfile getProfile(Logger logger, String username, boolean offline) throws IOException, InterruptedException, UnrecoverableEntryException, CertificateException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException {
        if (lastUsedCache != null && lastUsedCache.name().equals(username)) {
            return lastUsedCache;
        }
        if (registeredUsers.containsKey(username)) {
            try {
                JsonObject json = JsonParser.parseString(getKey(username)).getAsJsonObject();
                for (BaseAuthProvider provider : authProviders.values()) {
                    GetProfile.MCProfile profile = provider.resolveProfile(logger, json, offline);
                    if (profile != null && !offline) {
                        lastUsedCache = profile;
                    }
                    return profile;
                }
                registeredUsers.remove(username);
                saveRegisteredUsers();
            } catch (NullPointerException e) {
                registeredUsers.remove(username);
                saveRegisteredUsers();
                throw new IOException("Failed to parse json for " + username, e);
            }
        }
        return null;
    }

    public void setSelectedProfile(GetProfile.MCProfile profile) {
        selectedProfile = profile.name();
    }

    public String getSelectedProfile() {
        return selectedProfile;
    }

    public GetProfile.MCProfile setProfile(GetProfile.MCProfile userProfile) throws IOException, InterruptedException, CertificateException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException {
        setKey(userProfile.name(), userProfile.toJson().toString());
        registeredUsers.put(userProfile.name(), userProfile.id());
        saveRegisteredUsers();
        return userProfile;
    }

    public void saveRegisteredUsers() throws IOException {
        launcher.refreshProfiles();
        Path tmp = registeredUsersPath.getParent().resolve(registeredUsersPath.getFileName() + ".tmp");
        JsonObject json = new JsonObject();
        if (selectedProfile != null)
            json.addProperty("selected", selectedProfile);
        json.add("registered", new Gson().toJsonTree(registeredUsers));
        Files.writeString(tmp, json.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.move(tmp, registeredUsersPath, StandardCopyOption.REPLACE_EXISTING);
    }
}
