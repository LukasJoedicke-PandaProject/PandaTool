package xyz.vitox.discordtool.discordAPI.api.gateway;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import xyz.vitox.discordtool.discordAPI.api.DiscordAPI;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;
import xyz.vitox.discordtool.discordAPI.tokenManager.Token;
import xyz.vitox.discordtool.tab.ServerRecon;
import xyz.vitox.discordtool.tab.serverRecon.PingableRole;
import xyz.vitox.discordtool.tab.serverRecon.ServerEmoji;

import java.io.IOException;
import java.util.HashMap;

public class DiscordServerParser {

    public static DiscordServerParser instance = new DiscordServerParser();
    private DiscordAPI discordAPI = new DiscordAPI();
    private DiscordMultiRequests multiRequests = new DiscordMultiRequests();

    public void startGuildParser(String[] tokenArray, String guildID) {
        Thread checkThread = new Thread(() -> {
            ServerRecon.pingableRolesList.clear();
            ServerRecon.emojiList.clear();
            String response = null;
            try {
                Token token = multiRequests.checkIfTokenIsInGuild(guildID);
                response = discordAPI.getGuildInformation(token, guildID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            getRequiredInformations(response, guildID);
        });
        checkThread.setDaemon(true);
        checkThread.start();
    }

    public void getRequiredInformations(String response, String guildID) {
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(response, JsonElement.class);
        JsonObject jsonObj = element.getAsJsonObject();

        System.out.println(jsonObj);

        try {
            String serverName = jsonObj.get("name").getAsString();
            String serverRegion = jsonObj.get("region").getAsString().toUpperCase();
            String verificationLevel = translateVerificationLevel(jsonObj.get("verification_level").getAsString());
            JsonArray roles = jsonObj.get("roles").getAsJsonArray();
            JsonArray emojis = jsonObj.get("emojis").getAsJsonArray();
            HashMap<String, String> mentionableRoles = new HashMap<>();
            HashMap<String, String> serverEmojis = new HashMap<>();

            setPingableRoles(roles, mentionableRoles);
            setServerEmojis(emojis, serverEmojis);

            mentionableRoles.forEach((name, id) -> ServerRecon.pingableRolesList.add(new PingableRole(name, id)));
            serverEmojis.forEach((name, id) -> ServerRecon.emojiList.add(new ServerEmoji(name, id)));

            Platform.runLater(() -> {
                ServerRecon.staticServerName.setText("Server Name: " + serverName);
                ServerRecon.staticServerRegion.setText("Server Region: " + serverRegion);
                ServerRecon.staticServerVerificationLevel.setText("Verification Level: " + verificationLevel);

            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                ServerRecon.staticServerName.setText("Server Name: " + "Error: Couldn't find Server. Wrong ID?");
                ServerRecon.staticServerRegion.setText("Server Region: " + "Error: Couldn't find Server. Wrong ID?");
                ServerRecon.staticServerVerificationLevel.setText("Verification Level: " + "Error: Couldn't find Server. Wrong ID?");
            });
        }
    }

    public void setPingableRoles(JsonArray roles, HashMap<String, String> roleAndId) {
        for (int i = 0; i < roles.size(); i++) {
            String name = roles.get(i).getAsJsonObject().get("name").getAsString();
            String id = roles.get(i).getAsJsonObject().get("id").getAsString();
            boolean mentionable = roles.get(i).getAsJsonObject().get("mentionable").getAsBoolean();
            if (mentionable) {
                roleAndId.put(name, id);
            }
        }
    }

    public void setServerEmojis(JsonArray emojis, HashMap<String, String> nameAndID) {
        for (int i = 0; i < emojis.size(); i++) {
            String name = emojis.get(i).getAsJsonObject().get("name").getAsString();
            String id = emojis.get(i).getAsJsonObject().get("id").getAsString();
            boolean available = emojis.get(i).getAsJsonObject().get("available").getAsBoolean();
            if (available) {
                nameAndID.put(name, id);
            }
        }
    }

    public String translateVerificationLevel(String verificationLevel) {
        String translatedVerification = null;
        switch (verificationLevel) {
            case "0":
                translatedVerification = "No verification";
                break;
            case "1":
                translatedVerification = "Low";
                break;
            case "2":
                translatedVerification = "Medium";
                break;
            case "3":
                translatedVerification = "High";
                break;
            case "4":
                translatedVerification = "Highest";
                break;
        }
        return translatedVerification;
    }

}