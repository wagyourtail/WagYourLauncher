package xyz.wagyourtail.launcher.minecraft.auth.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.Logger;
import xyz.wagyourtail.launcher.minecraft.auth.AbstractStep;
import xyz.wagyourtail.launcher.minecraft.auth.xbox.Step5MCToken;
import xyz.wagyourtail.launcher.minecraft.auth.yggdrasil.Step1Login;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

@SuppressWarnings("rawtypes")
public class GetProfile extends AbstractStep<MCToken, GetProfile.MCProfile> {
    public static final String MINECRAFT_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    public GetProfile(Launcher launcher, AbstractStep prevStep) {
        super(launcher, prevStep);
    }

    @Override
    public MCProfile applyStep(MCToken prev_result, Logger logger) throws IOException {
        JsonObject json = getProfile(prev_result, logger);
        return getProfile(json, prev_result);
    }

    public JsonObject getProfile(MCToken prev_result, Logger logger) throws IOException {
        logger.info("Getting profile...");
        HttpURLConnection conn = (HttpURLConnection) new URL(MINECRAFT_PROFILE_URL).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", prev_result.token_type() + " " + prev_result.access_token());
        conn.setRequestProperty("User-Agent", "WagYourLauncher/1.0");
        conn.setRequestProperty("Accept", "application/json");
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
            if (resp.has("error")) {
                throw new IOException("Error: No valid minecraft profile found");
            }
            return resp;
        }
    }

    public MCProfile getProfile(JsonObject json, MCToken prev_result) throws MalformedURLException {
        return new MCProfile(
            this,
            UUID.fromString(json.get("id").getAsString().replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
            )),
            json.get("name").getAsString(),
            new URL(json.get("skins").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString()),
            prev_result
        );
    }

    @Override
    public MCProfile fromJson(JsonObject json) throws MalformedURLException {
        if (((AbstractStep)prevStep) instanceof Step5MCToken && json.getAsJsonObject("prev").get("user_type").getAsString().equals("msa")) {
            MCToken prev_result = prevStep.fromJson(json.getAsJsonObject("prev"));
            return new MCProfile(
                this,
                UUID.fromString(json.get("id").getAsString()),
                json.get("name").getAsString(),
                new URL(json.get("skin_url").getAsString()),
                prev_result
            );
        }

        if (((AbstractStep)prevStep) instanceof Step1Login && json.getAsJsonObject("prev").get("user_type").getAsString().equals("yggdrasil")) {
            MCToken prev_result = prevStep.fromJson(json.getAsJsonObject("prev"));
            return new MCProfile(
                this,
                UUID.fromString(json.get("id").getAsString()),
                json.get("name").getAsString(),
                new URL(json.get("skin_url").getAsString()),
                prev_result
            );
        }

        return null;
    }


    public record MCProfile(GetProfile step, UUID id, String name, URL skin_url, MCToken prev) implements StepResult<MCProfile, MCToken> {

        @Override
        public AbstractStep<MCToken, StepResult<MCProfile, MCToken>> getStep() {
            return (AbstractStep) step;
        }

        @Override
        public MCToken getPrevResult() {
            return prev;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("id", id.toString());
            json.addProperty("name", name);
            json.addProperty("skin_url", skin_url.toString());
            json.add("prev", prev.toJson());
            return json;
        }

    }
}
