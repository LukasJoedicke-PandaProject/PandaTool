package xyz.vitox.discordtool.tab.voiceSpamComponents;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import xyz.vitox.discordtool.discordAPI.api.DiscordAPI;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;
import xyz.vitox.discordtool.discordAPI.tokenManager.Token;
import xyz.vitox.discordtool.tab.VoiceSpam;

import java.util.ArrayList;
import java.util.HashMap;

public class VoiceParser {

    private DiscordMultiRequests discordMultiRequests = new DiscordMultiRequests();
    private DiscordAPI discordAPI = new DiscordAPI();

    public void collectVoiceChannels(String guildID, Button startButton, Label errorLabel) {

        ArrayList<HashMap<String, String>> voiceChannelList = new ArrayList<>();

        try {
            Token token = discordMultiRequests.checkIfTokenIsInGuild(guildID);
            String discordChannelsJSON = discordAPI.getGuildChannels(token, guildID);

            System.out.println(discordChannelsJSON);

            JsonElement element = new Gson().fromJson(discordChannelsJSON, JsonElement.class);

            for (JsonElement voiceChannel: element.getAsJsonArray()) {
                if (voiceChannel.getAsJsonObject().get("type").getAsInt() == 2) {
                    HashMap<String, String> idAndName = new HashMap<>();
                    String voiceChannelID = voiceChannel.getAsJsonObject().get("id").getAsString();
                    String voiceChannelName = voiceChannel.getAsJsonObject().get("name").getAsString();
                    System.out.println(voiceChannelName);
                    idAndName.put(voiceChannelID, voiceChannelName);
                    voiceChannelList.add(idAndName);
                }
            }

            createVoiceChannelUI(voiceChannelList, guildID);
            VoiceSpam.allowedToClick = true;
        } catch (Exception e) {
            VoiceSpam.allowedToClick = true;
            Platform.runLater(() -> {
                startButton.setText("Start");
                errorLabel.setText("Error while fetching Voice Channels.\nMake sure you entered the correct Guild ID and your tokens \n" +
                        "are in the guild.\n\nError Message:\n\n" + e.getMessage());
            });
            e.printStackTrace();
        }
    }

    public void createVoiceChannelUI(ArrayList<HashMap<String, String>> voiceChannels, String guildID) {
        VoiceChannel voiceChannel = new VoiceChannel();
        voiceChannel.createVoiceChannels(voiceChannels, guildID);
    }

}
