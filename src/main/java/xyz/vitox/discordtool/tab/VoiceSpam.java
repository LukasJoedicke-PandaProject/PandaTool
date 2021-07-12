package xyz.vitox.discordtool.tab;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.discordAPI.api.DiscordAPI;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.AudioConnection;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.sendSystem.DefaultSendSystem;
import xyz.vitox.discordtool.tab.voiceSpamComponents.VoiceParser;

import java.net.DatagramSocket;

public class VoiceSpam extends StackPane {

    public static boolean allowedToClick = true;

    public VoiceSpam() {
        this.getChildren().addAll(contentArea(), loadAccountsFirst());
    }

    public VBox contentArea() {
        VBox content = new VBox();
        HBox initElements = new HBox();

        TextField guildIDInputField = guildIDInput();
        Label errorLabel = errorLabel();
        Button startButton = startButton(guildIDInputField, errorLabel);

        initElements.setAlignment(Pos.CENTER);
        initElements.setSpacing(10);
        initElements.getChildren().addAll(guildIDInputField, startButton);

        content.setAlignment(Pos.CENTER);
        content.setSpacing(10);
        content.getChildren().addAll(infoLabel(), initElements, errorLabel);
        return content;
    }

    public HBox loadAccountsFirst() {
        HBox elements = new HBox();
        elements.setStyle("-fx-background-color: rgba(31,38,50,0.84);");
        Label test = new Label("Please load some tokens first.");
        elements.setAlignment(Pos.CENTER);
        elements.getChildren().add(test);
        return elements;
    }

    private Label infoLabel() {
        Label infoLabel = new Label("Enter Guild ID:");

        return infoLabel;
    }

    private Label errorLabel() {
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #b71515;");

        return errorLabel;
    }

    public TextField guildIDInput() {
        TextField guildIDInputField = new TextField();
        guildIDInputField.setPrefWidth(250);

        return guildIDInputField;
    }

    private Button startButton(TextField guildID, Label errorLabel) {
        Button startBtn = new Button("Start");

        startBtn.setOnAction(event -> {
            if (allowedToClick) {
                allowedToClick = false;
                startBtn.setText("Loading...");
                startBtn.setStyle("-fx-background-color: -fx-component-color");

                VoiceParser voiceParser = new VoiceParser();
                new Thread(() -> voiceParser.collectVoiceChannels(guildID.getText(), startBtn, errorLabel)).start();
            }
        });

        guildID.textProperty().addListener((observable, oldValue, newValue) -> {
            startBtn.setStyle(guildID.getText().length() > 5 ? "-fx-background-color: -fx-positive;" : "-fx-background-color: -fx-card-color;");
        });

        return startBtn;
    }

    public void restoreView() {
        getChildren().removeAll(this.getChildren());
        getChildren().add(contentArea());

        AudioConnection.voiceConnections = 0;
        DiscordAPI.sendHandlers.clear();
        DiscordAPI.audioPlayers.clear();

        if (DefaultSendSystem.sendingSockets.size() > 0) {
            for (DatagramSocket socket : DefaultSendSystem.sendingSockets) {
                socket.close();
            }
            DefaultSendSystem.sendingSockets.clear();
        }

        if (DefaultSendSystem.sendingThreads.size() > 0) {
            for (Thread thread : DefaultSendSystem.sendingThreads) {
                thread.interrupt();
            }
            DefaultSendSystem.sendingThreads.clear();
        }
    }

}
