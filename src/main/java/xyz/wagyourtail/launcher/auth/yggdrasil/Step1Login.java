package xyz.wagyourtail.launcher.auth.yggdrasil;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.notlog4j.Logger;
import xyz.wagyourtail.launcher.auth.AbstractStep;
import xyz.wagyourtail.launcher.auth.common.MCToken;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Step1Login extends AbstractStep<Step1Login.UsernamePassword, MCToken> {
    private static final String URL_AUTH = "https://authserver.mojang.com/authenticate";
    private static final String URL_REFRESH = "https://authserver.mojang.com/refresh";
    private static final String URL_VALIDATE = "https://authserver.mojang.com/validate";


    public Step1Login(LauncherBase launcher) {
        super(launcher, null);
    }

    @Override
    public MCToken applyStep(UsernamePassword up, Logger logger) throws IOException {
        logger.info("Got username/password, sending to server...");
        if (up == null) {
            throw new IOException("No username/password provided");
        }
        return usernamePasswordToToken(up);
    }

    @Override
    public MCToken refresh(MCToken result, Logger logger) throws IOException {
        if (isStillValid(result)) {
            return result;
        }
        HttpURLConnection conn = (HttpURLConnection) new URL(URL_REFRESH).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "WagYourLauncher/1.0");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        JsonObject obj = new JsonObject();
        obj.addProperty("accessToken", result.access_token());
        try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            out.writeBytes(obj.toString());
        }
        if (conn.getResponseCode() != 200) {
            throw new IOException("Failed to refresh token");
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            JsonObject resp = JsonParser.parseString(response.toString()).getAsJsonObject();
            return new MCToken(
                this,
                resp.get("accessToken").getAsString(),
                "Bearer",
                System.currentTimeMillis() + 60000L,
                "yggdrasil",
                null
            );
        }
    }

    public boolean isStillValid(MCToken token) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(URL_VALIDATE).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "WagYourLauncher/1.0");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        JsonObject obj = new JsonObject();
        obj.addProperty("accessToken", token.access_token());
        try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            out.writeBytes(obj.toString());
        }
        return conn.getResponseCode() == 204;
    }

    private MCToken usernamePasswordToToken(UsernamePassword up) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(URL_AUTH).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "WagYourLauncher/1.0");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        JsonObject obj = new JsonObject();
        JsonObject agent = new JsonObject();
        agent.addProperty("name", "Minecraft");
        agent.addProperty("version", 1);
        obj.add("agent", agent);
        obj.addProperty("username", up.username);
        obj.addProperty("password", new String(up.password));
        try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            out.writeBytes(obj.toString());
        }
        if (conn.getResponseCode() != 200) {
            throw new IOException("HTTP " + conn.getResponseCode() + ": " + conn.getResponseMessage());
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            JsonObject resp = JsonParser.parseString(response.toString()).getAsJsonObject();
            return new MCToken(
                this,
                resp.get("accessToken").getAsString(),
                "Bearer",
                // expire in a min because not available in response
                System.currentTimeMillis() + 60000L,
                "yggdrasil",
                null
            );
        }
    }

    @Override
    public MCToken fromJson(JsonObject json) throws MalformedURLException {
        return new MCToken(
            this,
            json.get("access_token").getAsString(),
            json.get("token_type").getAsString(),
            json.get("expireTime").getAsLong(),
            "msa",
            null
        );
    }

    public record UsernamePassword(String username, char[] password) implements StepResult<UsernamePassword, StepResult<?, ?>> {

        @Override
        public AbstractStep<StepResult<?, ?>, StepResult<UsernamePassword, StepResult<?, ?>>> getStep() {
            return null;
        }

        @Override
        public StepResult<?, ?> getPrevResult() {
            return null;
        }

        @Override
        public JsonObject toJson() {
            return null;
        }

    }

}
