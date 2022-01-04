package xyz.wagyourtail.launcher.auth.xbox;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.notlog4j.Logger;
import xyz.wagyourtail.launcher.auth.AbstractStep;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;

public class Step4XSTSToken extends AbstractStep<Step3XBLToken.XBLToken, Step4XSTSToken.XSTSToken> {
    public static final String XSTS_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";

    public Step4XSTSToken(LauncherBase launcher, AbstractStep<?, Step3XBLToken.XBLToken> prevStep) {
        super(launcher, prevStep);
    }

    @Override
    public XSTSToken applyStep(Step3XBLToken.XBLToken prev_result, Logger logger) throws IOException {
        logger.info("Requesting XSTS token...");
        HttpURLConnection conn = (HttpURLConnection) new URL(XSTS_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "WagYourLauncher/1.0");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        JsonObject obj = new JsonObject();
        JsonObject properties = new JsonObject();
        properties.addProperty("SandboxId", "RETAIL");
        JsonArray userTokens = new JsonArray();
        userTokens.add(new JsonPrimitive(prev_result.getResult().token()));
        properties.add("UserTokens", userTokens);
        obj.add("properties", properties);
        obj.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        obj.addProperty("TokenType", "JWT");
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
            long expireTime = Instant.parse(resp.get("NotAfter").getAsString()).toEpochMilli();
            return new XSTSToken(
                this,
                expireTime,
                resp.get("Token").getAsString(),
                resp.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString(),
                prev_result
            );
        }
    }

    @Override
    public XSTSToken fromJson(JsonObject json) throws MalformedURLException {
        Step3XBLToken.XBLToken prev = prevStep.fromJson(json.getAsJsonObject("prev"));
        return new XSTSToken(
            this,
            json.get("expireTimeMs").getAsLong(),
            json.get("token").getAsString(),
            json.get("userHash").getAsString(),
            prev
        );
    }

    @Override
    public XSTSToken refresh(XSTSToken result, Logger logger) throws IOException {
        if (result.expireTimeMs > System.currentTimeMillis()) {
            return result;
        }
        return super.refresh(result, logger);
    }

    public record XSTSToken(Step4XSTSToken step, long expireTimeMs, String token, String userHash, Step3XBLToken.XBLToken prev) implements AbstractStep.StepResult<XSTSToken, Step3XBLToken.XBLToken> {

        @Override
        public AbstractStep<Step3XBLToken.XBLToken, StepResult<XSTSToken, Step3XBLToken.XBLToken>> getStep() {
            return (AbstractStep) step;
        }

        @Override
        public Step3XBLToken.XBLToken getPrevResult() {
            return prev;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("expireTimeMs", expireTimeMs);
            json.addProperty("token", token);
            json.addProperty("userHash", userHash);
            json.add("prev", prev.toJson());
            return json;
        }

    }
}
