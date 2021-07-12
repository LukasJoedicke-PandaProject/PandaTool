package xyz.vitox.discordtool.discordAPI.api.gateway;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import okhttp3.*;
import xyz.vitox.discordtool.discordAPI.api.gateway.captchaCrack.CaptchaCrack;
import xyz.vitox.discordtool.discordAPI.tokenManager.Token;
import xyz.vitox.discordtool.discordAPI.tokenManager.TokenManager;
import xyz.vitox.discordtool.tab.verifierComponents.ServerCaptchaVerifiy;

import java.util.ArrayList;
import java.util.concurrent.*;

public class DiscordUserGateway extends WebSocketListener {

    public static ArrayList<WebSocket> openUserGateways = new ArrayList<>();
    public static int gatewayConnections = 0;

    private Token token;
    private WebSocket webSocket;

    public DiscordUserGateway(Token token) {
        this.token = token;
    }

    public WebSocket newGateay() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("wss://gateway.discord.gg/?v=6&encoding=json").build();
        webSocket = client.newWebSocket(request, this);
        openUserGateways.add(webSocket);
        client.dispatcher().executorService().shutdown();
        return webSocket;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        webSocket.send("{\n" +
                "  \"op\": 2,\n" +
                "  \"d\": {\n" +
                "    \"token\": \"" + token.getToken() + "\",\n" +
                "    \"properties\": {\n" +
                "      \"$os\": \"linux\",\n" +
                "      \"$browser\": \"my_library\",\n" +
                "      \"$device\": \"my_library\"\n" +
                "    }\n" +
                "  }\n" +
                "}");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        JsonElement element = new Gson().fromJson(text, JsonElement.class);
        JsonObject jsonObj = element.getAsJsonObject();
        JsonElement getEventName = jsonObj.get("t");
        if (!(getEventName instanceof JsonNull)) {
            switch (getEventName.getAsString()) {
                case "READY":
                    gatewayConnections++;
                    Platform.runLater(() -> ServerCaptchaVerifiy.connectedCount.setText("Bots Connected: " + gatewayConnections + "/" + TokenManager.tokensToUse().size()));
                    break;
                case "MESSAGE_CREATE":
                    if (text.contains("512333785338216465")) {

                        if (text.contains("Attempt Failed, please try again.")) {
                            Platform.runLater(() -> {
                                ServerCaptchaVerifiy.failedCaptchaCount++;
                                ServerCaptchaVerifiy.failedCaptchas.setText("Failed Captchas: " + ServerCaptchaVerifiy.failedCaptchaCount);
                                ServerCaptchaVerifiy.failedTokensLastServer.add(token);
                            });
                            break;
                        }

                        if (text.contains("You have been verified in guild")) {
                            Platform.runLater(() -> {
                                ServerCaptchaVerifiy.solvedCaptchaCount++;
                                ServerCaptchaVerifiy.verifiedTokensOnGuild++;
                                ServerCaptchaVerifiy.solvedCaptchas.setText("Solved Captchas: " + ServerCaptchaVerifiy.solvedCaptchaCount);
                                ServerCaptchaVerifiy.successfullyVerifiedBotsOnGuild.setText("Successfully verified bots on this guild: " + ServerCaptchaVerifiy.verifiedTokensOnGuild);
                            });
                            break;
                        }

                        new Thread(() -> {
                            JsonElement eventContent = jsonObj.get("d");
                            CaptchaCrack captchaCrack = new CaptchaCrack(eventContent, token);
                            captchaCrack.start();
                        }).start();
                    }
                    break;

            }

        }
    }

    public void initHeartbeat(WebSocket clientEndPoint) {
        new Thread(() -> {
            while (true) {
                clientEndPoint.send("{\n" +
                        "    \"op\": 1,\n" +
                        "    \"d\": 251\n" +
                        "}");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    System.out.println("Failed send heartbeat to token. Invalid token?");
                }
            }
        }).start();
    }

    public static void closeGateways(int delay) {
        if (openUserGateways.size() > 0) {

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(5, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, queue, handler);

            openUserGateways.forEach(session -> {
                executor.execute(() -> {
                    session.close(1000, "Closed by User.");
                });
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            openUserGateways.clear();
        }
    }
}
