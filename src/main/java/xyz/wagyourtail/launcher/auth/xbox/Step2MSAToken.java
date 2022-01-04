package xyz.wagyourtail.launcher.auth.xbox;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.auth.AbstractStep;
import xyz.wagyourtail.launcher.auth.MSAAuthProvider;
import xyz.wagyourtail.notlog4j.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Step2MSAToken extends AbstractStep<Step1MSACode.MSACode, Step2MSAToken.MSAToken> {
    public static final String TOKEN_URL = "https://login.live.com/oauth20_token.srf";

    public Step2MSAToken(LauncherBase launcher, Step1MSACode prevStep) {
        super(launcher, prevStep);
    }

    @Override
    public MSAToken applyStep(Step1MSACode.MSACode prev_result, Logger logger) throws IOException {
        logger.info("Getting MSA Token...");
        return apply(prev_result.getResult().code(), "authorization_code", prev_result);
    }

    @Override
    public MSAToken refresh(MSAToken result, Logger logger) throws IOException {
        if (result.expireTimeMs() > System.currentTimeMillis()) {
            return result;
        }
        return apply(result.getResult().refresh_token, "refresh_token", result.getPrevResult().getResult());
    }

    public MSAToken apply(String code, String type, Step1MSACode.MSACode prev) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(TOKEN_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "WagYourLauncher/1.0");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        StringBuilder sb = new StringBuilder();
        sb.append("client_id=").append(MSAAuthProvider.CLIENT_ID).append("\n");
        sb.append("&scope=").append(MSAAuthProvider.SCOPES).append("\n");
        if (type.equals("refresh_token")) {
            sb.append("&grant_type=").append(type).append("\n");
            sb.append("&refresh_token=").append(code).append("\n");
        } else {
            sb.append("&code=").append(code).append("\n");
            sb.append("&grant_type=").append(type).append("\n");
        }
        sb.append("&redirect_uri=").append(Step1MSACode.REDIRECT_URI).append("\n");
        try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
            out.writeBytes(sb.toString());
        }
        if (connection.getResponseCode() != 200) {
            System.out.println(new String(connection.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));
            throw new IOException("HTTP " + connection.getResponseCode() + ": " + connection.getResponseMessage());
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            // from web json
            JsonObject obj = JsonParser.parseString(response.toString()).getAsJsonObject();
            return new MSAToken(
                this,
                obj.get("user_id").getAsString(),
                // expire 5 seconds early to account for time differences
                System.currentTimeMillis() + (obj.get("expires_in").getAsLong() - 5L) * 1000L,
                obj.get("access_token").getAsString(),
                obj.get("refresh_token").getAsString(),
                prev
            );

        } finally {
            connection.disconnect();
        }
    }

    @Override
    public MSAToken fromJson(JsonObject json) throws MalformedURLException {
        Step1MSACode.MSACode code = prevStep.fromJson(json.getAsJsonObject("prev"));
        return new MSAToken(
            this,
            json.get("user_id").getAsString(),
            json.get("expireTimeMs").getAsLong(),
            json.get("access_token").getAsString(),
            json.get("refresh_token").getAsString(),
            code
        );
    }

    public record MSAToken(Step2MSAToken step, String user_id, long expireTimeMs, String access_token, String refresh_token, Step1MSACode.MSACode prev) implements StepResult<MSAToken, Step1MSACode.MSACode> {

        @Override
        public AbstractStep<Step1MSACode.MSACode, StepResult<MSAToken, Step1MSACode.MSACode>> getStep() {
            return (AbstractStep) step;
        }

        @Override
        public Step1MSACode.MSACode getPrevResult() {
            return prev;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("user_id", user_id);
            json.addProperty("expireTimeMs", expireTimeMs);
            json.addProperty("access_token", access_token);
            json.addProperty("refresh_token", refresh_token);
            json.add("prev", prev.toJson());
            return json;
        }

    }
}
