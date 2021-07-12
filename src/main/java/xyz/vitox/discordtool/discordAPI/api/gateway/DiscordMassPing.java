package xyz.vitox.discordtool.discordAPI.api.gateway;

import com.google.gson.*;
import javafx.application.Platform;
import javafx.scene.control.Button;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;
import xyz.vitox.discordtool.discordAPI.tokenManager.Token;
import xyz.vitox.discordtool.tab.serverSpamComponents.WriteMessageChannel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class DiscordMassPing {

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

    public DiscordMassPing(String channelID, String channelMessage, boolean tts, boolean typing, int delay, File attachment, Button massPingBtn, AtomicBoolean allowedToClickMasspingBtn) {
        this.channelID = channelID;
        this.channelMessage = channelMessage;
        this.tts = tts;
        this.typing = typing;
        this.delay = delay;
        this.attachment = attachment;
        this.massPingBtn = massPingBtn;
        this.allowedToClickMasspingBtn = allowedToClickMasspingBtn;
    }

    private Thread waitThread;
    public Thread startGateway() {
        Thread gatewayThread = new Thread(() -> {
            String guildID = multiRequests.getGuildIDByChannelID(channelID);
            Token token = multiRequests.checkIfTokenIsInGuild(guildID);
            System.out.println(token.getToken());
            handleMessages(token.getToken(), channelID, guildID);
        });

        waitThread = waitForFetch();
        waitThread.start();

        Platform.runLater(() -> {
            allowedToClickMasspingBtn.set(false);
            massPingBtn.setText("Fetching...");
            massPingBtn.setStyle("-fx-background-color: -fx-card-color;");
        });

        return gatewayThread;
    }

    public void handleMessages(String token, String channelID, String guildID) {
        DiscordGateway discordGateway = new DiscordGateway();
        WebsocketClientEndpoint clientEndpoint = discordGateway.initGateway(token);

        clientEndpoint.addMessageHandler(message -> {
            Gson gson = new Gson();
            JsonElement element = gson.fromJson(message, JsonElement.class);
            JsonObject jsonObj = element.getAsJsonObject();
            JsonElement getEventName = jsonObj.get("t");
            JsonElement getCompleteResponse = jsonObj.get("d");
            System.out.println(message);
            if (!(getEventName instanceof JsonNull)) {
                switch (getEventName.getAsString()) {

                    case "READY":
                        clientEndpoint.sendMessage("{\"op\":14,\"d\":{\"guild_id\":\"" + guildID + "\",\"channels\":{\"" + channelID + "\":[[0, 99], [100, 199], [200, 299]]}}}");
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
                            clientEndpoint.userSession.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }

            }

        });
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
