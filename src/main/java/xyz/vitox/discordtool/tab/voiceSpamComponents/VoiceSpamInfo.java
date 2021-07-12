package xyz.vitox.discordtool.tab.voiceSpamComponents;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.discordAPI.tokenManager.TokenManager;

public class VoiceSpamInfo {

    public static Label currentlyPlaying, botsConnected;

    public VBox contentArea() {
        VBox content = new VBox();

        currentlyPlaying = new Label("Currently Playing: ");
        botsConnected = new Label("Bots connected: 0/" + TokenManager.verifiedTokens.size());

        content.setSpacing(20);
        content.setTranslateY(20);
        content.setPadding(new Insets(10, 10, 10, 10));
        content.setStyle("-fx-background-color: #202834");
        content.getChildren().addAll(botsConnected, currentlyPlaying);
        return content;
    }

}
