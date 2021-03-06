package xyz.wagyourtail.launcher.auth.xbox;

import com.google.gson.JsonObject;
import org.graalvm.collections.Pair;
import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.auth.AbstractStep;
import xyz.wagyourtail.launcher.auth.MSAAuthProvider;
import xyz.wagyourtail.notlog4j.Logger;

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
    public static final String REDIRECT_URI = "http://localhost:";
    public static final String AUTH_URL = "https://login.live.com/oauth20_authorize.srf";

    public Step1MSACode(LauncherBase launcher) {
        super(launcher, null);
    }

    @Override
    public MSACode applyStep(StepResult<?, ?> prev_result, Logger logger) {
        logger.info("Waiting for login...");
        Pair<CompletableFuture<ServerSocket>, CompletableFuture<String>> code = startServer();
        logger.info("If the following URL doesn't open, please copy and paste it into your browser.");
        ServerSocket socket = code.t.join();
        String authURL = getAuthURL(socket);
        logger.info(authURL);
        tryWebBrowserOpen(authURL);
        timeoutServer(socket, code.u);
        try {
            String code_ = code.u.join();
            if (code_ == null) {
                throw new RuntimeException("Failed to get MSA Code");
            }
            return new MSACode(this, code_, socket.getLocalPort());
        } catch (Exception e) {
            logger.error("Login timed out.");
            throw new RuntimeException("Failed to get MSA Code. Login timed out.");
        }
    }


    @Override
    public MSACode refresh(MSACode result, Logger logger) {
        return this.applyStep(result.getPrevResult(), logger);
    }

    @Override
    public MSACode fromJson(JsonObject json) {
        return new MSACode(this, json.get("code").getAsString(), json.get("port").getAsInt());
    }

    public Pair<CompletableFuture<ServerSocket>, CompletableFuture<String>> startServer() {
        CompletableFuture<ServerSocket> server = CompletableFuture.supplyAsync(() -> {
                try {
                    ServerSocket serverSocket = new ServerSocket(0);
                    return serverSocket;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            });
        return new Pair<>(server,
            server.thenApplyAsync(serverSocket -> {
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
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            })
        );
    }

    public void tryWebBrowserOpen(String authURL) {
        // try web browser open
        try {
            Desktop.getDesktop().browse(URI.create(authURL));
        } catch (UnsupportedOperationException | IOException e) {
            try {
                Runtime.getRuntime().exec("xdg-open " + authURL);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public void timeoutServer(ServerSocket socket, CompletableFuture<String> code) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(60000);
                socket.close();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            code.cancel(true);
        });
    }

    protected String getAuthURL(ServerSocket socket) {
        return AUTH_URL +
            "?client_id=" + MSAAuthProvider.CLIENT_ID +
            "&scope=" + MSAAuthProvider.SCOPES.replace(" ", "%20") +
            "&response_type=code" +
            "&prompt=select_account" +
            "&redirect_uri=" + REDIRECT_URI + socket.getLocalPort();
    }

    public record MSACode(Step1MSACode step, String code, int port) implements StepResult<MSACode, StepResult<?, ?>> {

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
            json.addProperty("port", port);
            return json;
        }

    }

    public record Pair<T, U>(T t, U u) {}
}
