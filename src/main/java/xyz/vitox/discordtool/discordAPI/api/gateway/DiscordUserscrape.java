package xyz.vitox.discordtool.discordAPI.api.gateway;

import com.google.gson.*;
import javafx.application.Platform;
import javafx.scene.control.Button;
import xyz.vitox.discordtool.discordAPI.api.DiscordAPI;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;
import xyz.vitox.discordtool.discordAPI.tokenManager.Token;
import xyz.vitox.discordtool.tab.serverSpamComponents.WriteMessageChannel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class DiscordUserscrape {

    public static ArrayList<JsonObject> guildChannelJsonObjects = new ArrayList<>();
    private final DiscordAPI discordAPI = new DiscordAPI();
    public static ArrayList<String> guildMembers = new ArrayList<>();
    public Token scrapeToken;

    public DiscordUserscrape(Token scrapeToken) {
        this.scrapeToken = scrapeToken;
    }

    public void collectServerIDs() throws IOException {
        String guildArray = discordAPI.getAllGuilds(scrapeToken);

        System.out.println("GUILD ARRAY: " + guildArray);

        JsonElement guildElement = new Gson().fromJson(guildArray, JsonElement.class);
        JsonArray guilds = guildElement.getAsJsonArray();

        guildLoop:
        for (int guildIndex = 0; guildIndex < guilds.size(); guildIndex++) {
            JsonObject guildObject = guilds.get(guildIndex).getAsJsonObject();

            String guildID = guildObject.get("id").getAsString();
            long guildPermissions = guildObject.get("permissions").getAsLong();

            String guildChannels = discordAPI.getGuildChannels(scrapeToken, guildID);

            JsonElement channelElement = new Gson().fromJson(guildChannels, JsonElement.class);
            JsonArray channelJsonArray = channelElement.getAsJsonArray();

            for (int channelIndex = 0; channelIndex < channelJsonArray.size(); channelIndex++) {

                JsonObject channelObject = channelJsonArray.get(channelIndex).getAsJsonObject();

                int channelType = channelObject.get("type").getAsInt();

                //TextChannel is 0
                if (channelType == 0) {

                    String channelName = channelObject.get("name").toString();
                    String channelTopic = channelObject.get("topic").toString();
                    JsonArray permissionOverwritesArray = channelObject.get("permission_overwrites").getAsJsonArray();

                    if (permissionOverwritesArray.size() == 0) {
                        if (channelName.contains("general")) {
                            System.out.println("Found writable channel: " + channelName);
                            guildChannelJsonObjects.add(channelObject);
                            continue guildLoop;
                        }

                        if (channelTopic.contains("general")) {
                            System.out.println("Found writable channel: " + channelName);
                            guildChannelJsonObjects.add(channelObject);
                            continue guildLoop;
                        }

                        System.out.println("Found writable channel: " + channelName);
                        guildChannelJsonObjects.add(channelObject);
                        continue guildLoop;
                    }

                    for (int permissionOverwriteIndex = 0; permissionOverwriteIndex < permissionOverwritesArray.size(); permissionOverwriteIndex++) {
                        JsonObject permissionObject = permissionOverwritesArray.get(permissionOverwriteIndex).getAsJsonObject();

                        String permissionID = permissionObject.get("id").getAsString();

                        //@everyone Role
                        if (permissionID.equals(guildID)) {
                            int viewChannelPermission = 0x0000000400;

                            boolean generalAllowedToSeeChannels = ((guildPermissions & viewChannelPermission) == viewChannelPermission);

                            long allowPermissions = permissionObject.get("allow").getAsLong();
                            long denyPermissions = permissionObject.get("deny").getAsLong();

                            if ((allowPermissions & viewChannelPermission) == viewChannelPermission) {

                                if (channelName.contains("general")) {
                                    System.out.println("Found writable channel: " + channelName);
                                    guildChannelJsonObjects.add(channelObject);
                                    continue guildLoop;
                                }

                                if (channelTopic.contains("general")) {
                                    System.out.println("Found writable channel: " + channelName);
                                    guildChannelJsonObjects.add(channelObject);
                                    continue guildLoop;
                                }

                                System.out.println("Found writable channel: " + channelName);
                                guildChannelJsonObjects.add(channelObject);
                                continue guildLoop;
                            } else if ((denyPermissions & viewChannelPermission) == viewChannelPermission) {
                                System.out.println("You CANNOT see: " + channelName);
                            } else if (allowPermissions == 0 && generalAllowedToSeeChannels) {

                                if (channelName.contains("general")) {
                                    System.out.println("Found writable channel: " + channelName);
                                    guildChannelJsonObjects.add(channelObject);
                                    continue guildLoop;
                                }

                                if (channelTopic.contains("general")) {
                                    System.out.println("Found writable channel: " + channelName);
                                    guildChannelJsonObjects.add(channelObject);
                                    continue guildLoop;
                                }

                                System.out.println("Found writable channel: " + channelName);
                                guildChannelJsonObjects.add(channelObject);
                                continue guildLoop;
                            } else {
                                System.out.println("You cannot see: " + channelName);
                            }

                        }
                    }

                }

            }
        }

        System.out.println("Found " +  guildChannelJsonObjects.size() + " viewable channels in " + guilds.size() + " guilds.");
        startGateway();
    }

    public void startGateway() {
        DiscordGateway discordGateway = new DiscordGateway();
        WebsocketClientEndpoint clientEndpoint = discordGateway.initGateway(scrapeToken.getToken());

        clientEndpoint.addMessageHandler(message -> {
            Gson gson = new Gson();
            JsonElement element = gson.fromJson(message, JsonElement.class);
            JsonObject jsonObj = element.getAsJsonObject();
            JsonElement getEventName = jsonObj.get("t");
            JsonElement getCompleteResponse = jsonObj.get("d");
            if (!(getEventName instanceof JsonNull)) {
                switch (getEventName.getAsString()) {

                    case "READY":
                        for (JsonObject channelObject : guildChannelJsonObjects) {
                            String guildID = channelObject.get("guild_id").getAsString();
                            String channelID = channelObject.get("id").getAsString();
                            System.out.println(channelObject.get("name").getAsString());

                            clientEndpoint.sendMessage("{\"op\":14,\"d\":{\"guild_id\":\"" + guildID + "\",\"channels\":{\"" + channelID + "\":[[0, 99], [100, 199], [200, 299]]}}}");
                            clientEndpoint.sendMessage("{\"op\":14,\"d\":{\"guild_id\":\"" + guildID + "\",\"channels\":{\"" + channelID + "\":[[300, 399], [400, 499], [500, 599]]}}}");
                            clientEndpoint.sendMessage("{\"op\":14,\"d\":{\"guild_id\":\"" + guildID + "\",\"channels\":{\"" + channelID + "\":[[600, 699], [700, 799], [800, 899]]}}}");
                        }
                        break;
                    case "GUILD_MEMBER_LIST_UPDATE":
                        System.out.println(message);
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
                        System.out.println("Members collected: " + guildMembers.size());
                }

            }

        });
    }

}
