package xyz.wagyourtail.launcher.minecraft.auth.xbox;

import com.google.gson.JsonObject;
import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.Logger;
import xyz.wagyourtail.launcher.gui.component.logging.LoggingTextArea;
import xyz.wagyourtail.launcher.gui.windows.login.GuiLogin;
import xyz.wagyourtail.launcher.minecraft.auth.AbstractStep;
import xyz.wagyourtail.launcher.minecraft.auth.MSAAuthProvider;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Step1MSACode extends AbstractStep<AbstractStep.StepResult<?, ?>, Step1MSACode.MSACode> {
    public static final int REDIRECT_PORT = 8086;
    public static final String REDIRECT_URI = "http://localhost:" + REDIRECT_PORT;
    public static final String AUTH_URL = "https://login.live.com/oauth20_authorize.srf";

    public Step1MSACode(Launcher launcher) {
        super(launcher, null);
    }

    @Override
    public MSACode applyStep(StepResult<?, ?> prev_result, Logger logger) {
        logger.info("Waiting for login...");
        CompletableFuture<String> code = startServer();
        logger.info("If the following URL doesn't open, please copy and paste it into your browser.");
        if (logger instanceof LoggingTextArea) {
            logger.info("<a href=\"" + getAuthURL() + "\">" + getAuthURL() + "</a>");
        } else {
            logger.info(getAuthURL());
        }
        tryWebBrowserOpen();
        timeoutServer(code);
        return new MSACode(this, code.join());
    }


    @Override
    public MSACode refresh(MSACode result, Logger logger) {
        return this.applyStep(result.getPrevResult(), logger);
    }

    @Override
    public MSACode fromJson(JsonObject json) {
        return new MSACode(this, json.get("code").getAsString());
    }

    public CompletableFuture<String> startServer() {
        return CompletableFuture.supplyAsync(() -> {
            // open server socket
            try (ServerSocket serverSocket = new ServerSocket(REDIRECT_PORT)) {
                try (Socket socket = serverSocket.accept()) {
                    Scanner scanner = new Scanner(socket.getInputStream(), StandardCharsets.UTF_8);
                    String GET = scanner.nextLine();
                    String response = "HTTP/1.1 200 OK\r\nConnection: Close\r\n\r\n";
                    response += "You have been logged in! You can close this window.";
                    socket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
                    Matcher m = Pattern.compile("code=([^&\\s]+)").matcher(GET);
                    if (m.find()) {
                        return m.group(1);
                    }
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public void tryWebBrowserOpen() {
        // try web browser open
        try {
            Desktop.getDesktop().browse(URI.create(getAuthURL()));
        } catch (UnsupportedOperationException | IOException e) {
            try {
                Runtime.getRuntime().exec("xdg-open " + getAuthURL());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public void timeoutServer(CompletableFuture<String> code) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            code.cancel(true);
        });
    }

    protected String getAuthURL() {
        return AUTH_URL +
            "?client_id=" + MSAAuthProvider.CLIENT_ID +
            "&scope=" + MSAAuthProvider.SCOPES.replace(" ", "%20") +
            "&response_type=code" +
            "&prompt=select_account" +
            "&redirect_uri=" + REDIRECT_URI;
    }

    public record MSACode(Step1MSACode step, String code) implements StepResult<MSACode, StepResult<?, ?>> {

        @Override
        public AbstractStep<StepResult<?, ?>, StepResult<MSACode, StepResult<?, ?>>> getStep() {
            return (AbstractStep) step;
        }

        @Override
        public StepResult<?, ?> getPrevResult() {
            return null;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("code", code);
            return json;
        }

    }
}
