package xyz.wagyourtail.launcher.minecraft.auth.xbox;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.Logger;
import xyz.wagyourtail.launcher.minecraft.auth.AbstractStep;

import java.awt.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;

public class Step3XBLToken extends AbstractStep<Step2MSAToken.MSAToken, Step3XBLToken.XBLToken> {
    public static final String XBL_URL = "https://user.auth.xboxlive.com/user/authenticate";

    public Step3XBLToken(Launcher launcher, AbstractStep<?, Step2MSAToken.MSAToken> prevStep) {
        super(launcher, prevStep);
    }

    @Override
    public XBLToken applyStep(Step2MSAToken.MSAToken prev_result, Logger logger) throws IOException {
        logger.info("Authenticating with Xbox Live...");
        HttpURLConnection conn = (HttpURLConnection) new URL(XBL_URL).openConnection();
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
        properties.addProperty("AuthMethod", "RPS");
        properties.addProperty("SiteName", "user.auth.xboxlive.com");
        properties.addProperty("RpsTicket", "d=" + prev_result.getResult().access_token());
        obj.add("properties", properties);
        obj.addProperty("RelyingParty", "http://auth.xboxlive.com");
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
            return new XBLToken(
                this,
                expireTime,
                resp.get("Token").getAsString(),
                resp.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString(),
                prev_result
            );
        }
    }

    @Override
    public XBLToken fromJson(JsonObject json) throws MalformedURLException {
        Step2MSAToken.MSAToken token = prevStep.fromJson(json.getAsJsonObject("prev"));
        return new XBLToken(
            this,
            json.get("expireTimeMs").getAsLong(),
            json.get("token").getAsString(),
            json.get("userHash").getAsString(),
            token
        );
    }

    @Override
    public XBLToken refresh(XBLToken result, Logger logger) throws IOException {
        if (result.expireTimeMs > System.currentTimeMillis()) {
            return result;
        }
        return super.refresh(result, logger);
    }

    public record XBLToken(Step3XBLToken step, long expireTimeMs, String token, String userHash, Step2MSAToken.MSAToken prev) implements AbstractStep.StepResult<XBLToken, Step2MSAToken.MSAToken> {
        @Override
        public AbstractStep<Step2MSAToken.MSAToken, StepResult<XBLToken, Step2MSAToken.MSAToken>> getStep() {
            return (AbstractStep) step;
        }

        @Override
        public Step2MSAToken.MSAToken getPrevResult() {
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
