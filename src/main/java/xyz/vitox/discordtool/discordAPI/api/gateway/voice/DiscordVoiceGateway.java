package xyz.vitox.discordtool.discordAPI.api.gateway.voice;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import okhttp3.*;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.sendSystem.SendHandler;
import xyz.vitox.discordtool.discordAPI.tokenManager.TokenManager;
import xyz.vitox.discordtool.tab.voiceSpamComponents.VoiceSpamInfo;
import xyz.vitox.discordtool.tab.voiceSpamComponents.VoiceSpamSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class DiscordVoiceGateway extends WebSocketListener {

    public static ArrayList<WebSocket> openGateways = new ArrayList<>();

    private String token;
    private String serverID;
    private String channelID;
    private String tokenID;

    private String voiceSessionID;
    private String voiceEndpoint;
    private String voiceToken;
    private WebSocket webSocket;

    private boolean receivedVoiceServerUpdate = false, receivedVoiceStateUpdate = false;

    public DiscordVoiceGateway(String serverID, String channelID) {
        this.serverID = serverID;
        this.channelID = channelID;
    }

    public DiscordVoiceGateway(String token, String tokenID, String serverID, String channelID) {
        this.token = token;
        this.tokenID = tokenID;
        this.serverID = serverID;
        this.channelID = channelID;
    }

    public WebSocket newGateay() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("wss://gateway.discord.gg/?v=6&encoding=json").build();
        webSocket = client.newWebSocket(request, this);
        openGateways.add(webSocket);
        client.dispatcher().executorService().shutdown();
        return webSocket;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        webSocket.send("{\n" +
                "  \"op\": 2,\n" +
                "  \"d\": {\n" +
                "    \"token\": \"" + token + "\",\n" +
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
                    requestVoiceServer(webSocket, serverID, channelID);
                    break;

                case "VOICE_SERVER_UPDATE":
                    if (VoiceSpamSettings.advancedConnection) {
                        receivedVoiceServerUpdate = true;
                        voiceEndpoint = jsonObj.get("d").getAsJsonObject().get("endpoint").getAsString();
                        voiceToken = jsonObj.get("d").getAsJsonObject().get("token").getAsString();
                        if (receivedVoiceServerUpdate && receivedVoiceStateUpdate) {
                            establishConnection(webSocket, tokenID, voiceSessionID, voiceEndpoint, voiceToken);
                        }
                    }
                    break;

                case "VOICE_STATE_UPDATE":
                    if (VoiceSpamSettings.advancedConnection) {
                        String userID = jsonObj.get("d").getAsJsonObject().get("user_id").getAsString();
                        if (userID.equals(tokenID)) {
                            receivedVoiceStateUpdate = true;
                        }
                        voiceSessionID = jsonObj.get("d").getAsJsonObject().get("session_id").getAsString();
                        if (receivedVoiceServerUpdate && receivedVoiceStateUpdate) {
                            establishConnection(webSocket, tokenID, voiceSessionID, voiceEndpoint, voiceToken);
                        }
                    }
                    break;
            }

        }
    }

    private void establishConnection(WebSocket webSocket, String tokenID, String sessionID, String voiceEndpoint, String voiceToken) {
        DiscordVoiceUDPGateway discordVoiceUDPGateway = new DiscordVoiceUDPGateway(serverID, tokenID, sessionID, voiceEndpoint, voiceToken, channelID);
        discordVoiceUDPGateway.run();
    }

    public void requestVoiceServer(WebSocket webSocket, String serverID, String channelID) {
        if (AudioConnection.voiceConnections < TokenManager.tokensToUse().size() && !VoiceSpamSettings.advancedConnection) {
            AudioConnection.voiceConnections++;
            Platform.runLater(() -> VoiceSpamInfo.botsConnected.setText("Bots connected: " + AudioConnection.voiceConnections + "/" + TokenManager.tokensToUse().size()));
        }
        webSocket.send("{\n" +
                "  \"op\": 4,\n" +
                "  \"d\": {\n" +
                "    \"guild_id\": \"" + serverID + "\",\n" +
                "    \"channel_id\": \"" + channelID + "\",\n" +
                "    \"self_mute\": false,\n" +
                "    \"self_deaf\": false\n" +
                "  }\n" +
                "}");
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
        if (openGateways.size() > 0) {

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(5, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, queue, handler);

            openGateways.forEach(session -> {
                executor.execute(() -> {
                    session.close(1000, "Closed by User.");
                });
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            openGateways.clear();
        }
    }
}
