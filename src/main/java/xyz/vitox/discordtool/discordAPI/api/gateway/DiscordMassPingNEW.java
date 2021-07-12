package xyz.vitox.discordtool.discordAPI.api.gateway;

import com.google.gson.*;
import javafx.application.Platform;
import javafx.scene.control.Button;
import okhttp3.*;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;
import xyz.vitox.discordtool.discordAPI.api.gateway.captchaCrack.CaptchaCrack;
import xyz.vitox.discordtool.discordAPI.tokenManager.Token;
import xyz.vitox.discordtool.discordAPI.tokenManager.TokenManager;
import xyz.vitox.discordtool.tab.serverSpamComponents.WriteMessageChannel;
import xyz.vitox.discordtool.tab.verifierComponents.ServerCaptchaVerifiy;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DiscordMassPingNEW extends WebSocketListener {

    public static ArrayList<WebSocket> openUserGateways = new ArrayList<>();
    public static int gatewayConnections = 0;

    private WebSocket webSocket;

    public static Thread spamThread;
    public static ArrayList<String> guildMembers = new ArrayList<>();
    private DiscordMultiRequests multiRequests = new DiscordMultiRequests();

    public File attachment;
    public String channelID;
    public String channelMessage;
    public boolean tts;
    public boolean typing;
    public AtomicBoolean allowedToClickMasspingBtn;
    public int delay;
    public Button massPingBtn;
    private String guildID;
    private Token token;

    public DiscordMassPingNEW(String channelID, String channelMessage, boolean tts, boolean typing, int delay, File attachment, Button massPingBtn, AtomicBoolean allowedToClickMasspingBtn) {
        this.channelID = channelID;
        this.channelMessage = channelMessage;
        this.tts = tts;
        this.typing = typing;
        this.delay = delay;
        this.attachment = attachment;
        this.massPingBtn = massPingBtn;
        this.allowedToClickMasspingBtn = allowedToClickMasspingBtn;

        this.guildID = multiRequests.getGuildIDByChannelID(channelID);
        this.token = multiRequests.checkIfTokenIsInGuild(guildID);
    }

    private Thread waitThread;
    public WebSocket newGateay() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("wss://gateway.discord.gg/?v=6&encoding=json").build();
        webSocket = client.newWebSocket(request, this);
        openUserGateways.add(webSocket);
        waitThread = waitForFetch();
        waitThread.start();

        Platform.runLater(() -> {
            allowedToClickMasspingBtn.set(false);
            massPingBtn.setText("Fetching...");
            massPingBtn.setStyle("-fx-background-color: -fx-card-color;");
        });
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
        JsonElement getCompleteResponse = jsonObj.get("d");
        if (!(getEventName instanceof JsonNull)) {
            switch (getEventName.getAsString()) {
                case "READY":
                    webSocket.send("{\"op\":14,\"d\":{\"guild_id\":\"" + this.guildID + "\",\"channels\":{\"" + channelID + "\":[[0, 99], [100, 199], [200, 299]]}}}");
                    break;
                case "GUILD_MEMBER_LIST_UPDATE":
                    for (int i = 0; i < getCompleteResponse.getAsJsonObject().get("ops").getAsJsonArray().size(); i++) {
                        if (getCompleteResponse.getAsJsonObject().get("ops").getAsJsonArray().get(i).getAsJsonObject().get("op").getAsString().equals("SYNC")) {
                            JsonArray members = getCompleteResponse.getAsJsonObject().get("ops").getAsJsonArray().get(i).getAsJsonObject().get("items").getAsJsonArray();
                            for (int memberCount = 0; memberCount < members.size(); memberCount++) {
                                if (members.get(memberCount).getAsJsonObject().get("member") != null) {
                                    guildMembers.add("<@" + members.get(memberCount).getAsJsonObject().get("member").getAsJsonObject().get("user").getAsJsonObject().get("id").getAsString() + ">");
                                }
                            }
                        }
                    }

                    waitThread.interrupt();
                    spamThread = initSpam();
                    spamThread.start();
                    try {
                        webSocket.close(1000, "Automatically closed.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

            }

        }
    }

    public Thread waitForFetch() {
        return new Thread(() -> {
            try {
                Thread.sleep(6600);
                Platform.runLater(() -> {
                    allowedToClickMasspingBtn.set(true);
                    WriteMessageChannel.isMassPingPressed = false;
                    massPingBtn.setText("Mass Ping (Failed)");
                    massPingBtn.setStyle("-fx-background-color: -fx-positive;");
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
    }

    public Thread initSpam() {

        Platform.runLater(() -> {
            allowedToClickMasspingBtn.set(true);
            massPingBtn.setText("Stop Massping");
            massPingBtn.setStyle("-fx-background-color: -fx-negative;");
        });

        return new Thread(() -> {
            DiscordMultiRequests discordMultiRequests = new DiscordMultiRequests();
            int chunk_size = 1;

            if (guildMembers.size() > 80) {
                chunk_size = 2;
            }
            if (guildMembers.size() > 160) {
                chunk_size = 3;
            }
            if (guildMembers.size() >= 240) {
                chunk_size = 4;
            }

            System.out.println(guildMembers.size());

            int chunk = guildMembers.size() / chunk_size; // chunk size to divide

            for (int i = 0; i < guildMembers.size(); i += chunk) {
                String allMentionableMembers = Arrays.toString(Arrays.copyOfRange(guildMembers.toArray(), i, Math.min(guildMembers.size(), i + chunk)));
                allMentionableMembers = allMentionableMembers.replaceAll("\\[", "");
                allMentionableMembers = allMentionableMembers.replaceAll("]", "");
                allMentionableMembers = allMentionableMembers.replaceAll(",", "");
                allMentionableMembers = allMentionableMembers.replaceAll("\\s", "");

                Thread massPingThread = discordMultiRequests.writeMessageNoThread(channelMessage + allMentionableMembers, channelID, tts, typing, attachment, delay);
                massPingThread.start();
                DiscordMultiRequests.massPingThread.add(massPingThread);
            }
        });

    }
}
