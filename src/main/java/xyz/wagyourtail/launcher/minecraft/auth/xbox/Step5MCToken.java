package xyz.wagyourtail.launcher.minecraft.auth.xbox;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.Logger;
import xyz.wagyourtail.launcher.minecraft.auth.AbstractStep;
import xyz.wagyourtail.launcher.minecraft.auth.common.MCToken;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Step5MCToken extends AbstractStep<Step4XSTSToken.XSTSToken, MCToken> {
    public static final String MINECRAFT_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";

    public Step5MCToken(Launcher launcher, AbstractStep<?, Step4XSTSToken.XSTSToken> prevStep) {
        super(launcher, prevStep);
    }

    @Override
    public MCToken applyStep(Step4XSTSToken.XSTSToken prev_result, Logger logger) throws IOException {
        logger.info("Authenticating with Minecraft Services...");
        HttpURLConnection conn = (HttpURLConnection) new URL(MINECRAFT_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "WagYourLauncher/1.0");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        JsonObject obj = new JsonObject();
        obj.addProperty("identityToken", "XBL3.0 x=" + prev_result.getResult().userHash() + ";" + prev_result.getResult().token());
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
                resp.get("access_token").getAsString(),
                resp.get("token_type").getAsString(),
                // expire 5 seconds early to account for time differences
                System.currentTimeMillis() + resp.get("expires_in").getAsLong()  - 5000L,
                "msa",
                prev_result
            );
        }
    }

    @Override
    public MCToken refresh(MCToken result, Logger logger) throws IOException {
        if (result.expireTime() > System.currentTimeMillis()) {
            return result;
        }
        return super.refresh(result, logger);
    }

    @Override
    public MCToken fromJson(JsonObject json) throws MalformedURLException {
        Step4XSTSToken.XSTSToken prev = prevStep.fromJson(json.getAsJsonObject("prev"));
        return new MCToken(
            this,
            json.get("access_token").getAsString(),
            json.get("token_type").getAsString(),
            json.get("expireTime").getAsLong(),
            "msa",
            prev
        );
    }

}
