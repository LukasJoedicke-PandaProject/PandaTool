package xyz.vitox.discordtool.tab.voiceSpamComponents;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import xyz.vitox.discordtool.Main;
import xyz.vitox.discordtool.controller.DashboardController;
import xyz.vitox.discordtool.discordAPI.api.DiscordAPI;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.AudioConnection;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.DiscordVoiceGateway;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.DiscordVoiceUDPGateway;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.sendSystem.DefaultSendSystem;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.sendSystem.SendHandler;
import xyz.vitox.discordtool.discordAPI.tokenManager.TokenManager;
import xyz.vitox.discordtool.tab.voiceSpamComponents.musicManager.TokenAudioPlayer;
import xyz.vitox.discordtool.util.FXUtil;

import java.io.File;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class VoiceChannel {

    private DiscordMultiRequests discordMultiRequests = new DiscordMultiRequests();
    private Thread songThread;
    private String guildID;
    public static HBox enableFullVoiceConnectionBox;

    public void createVoiceChannels(ArrayList<HashMap<String, String>> rawVoiceChannels, String guildID) {
        this.guildID = guildID;
        Platform.runLater(() -> {
            DashboardController.getVoiceSpam().getChildren().remove(0);
            DashboardController.getVoiceSpam().getChildren().addAll(unloadComponent(), content(rawVoiceChannels, guildID));
        });
    }

    public GridPane content(ArrayList<HashMap<String, String>> rawVoiceChannels, String guildID) {
        VBox leftElements = new VBox();

        VoiceSpamSettings voiceSpamSettings = new VoiceSpamSettings();
        VoiceSpamInfo voiceSpamInfo = new VoiceSpamInfo();
        Separator voiceChannelSeperator = new Separator();
        voiceChannelSeperator.setHalignment(HPos.CENTER);

        leftElements.getChildren().addAll(
                voiceSpamInfo.contentArea(),
                voiceSpamSettings.contentArea(),
                voiceChannelSeperator,
                channelElements(rawVoiceChannels, guildID));
        leftElements.setPrefWidth(1200);
        leftElements.setSpacing(40);

        VBox rightElements = new VBox();
        rightElements.setPrefWidth(1500);
        rightElements.getChildren().addAll(trackOptions(), enableFullVoiceConnectionBox());

        GridPane elementGrid = new GridPane();
        elementGrid.setHgap(50);
        elementGrid.add(leftElements, 0, 1);
        elementGrid.add(rightElements, 1, 1);

        return elementGrid;
    }

    public HBox enableFullVoiceConnectionBox() {
        HBox elements = new HBox();
        elements.setStyle("-fx-background-color: rgba(31,38,50,0.84);");
        Label test = new Label("Enable the full voice connection option\nif you want to play music to Voice-Channels.");
        elements.setTranslateY(-15);
        elements.setPrefHeight(1000);
        elements.setPrefWidth(1000);
        elements.setAlignment(Pos.CENTER);
        elements.getChildren().add(test);
        enableFullVoiceConnectionBox = elements;
        return elements;
    }

    private ScrollPane channelElements(ArrayList<HashMap<String, String>> rawVoiceChannels, String guildID) {
        ScrollPane scrollPane = new ScrollPane();

        VBox elements = new VBox();

        for (HashMap<String, String> channelIDandName : rawVoiceChannels) {
            for (Map.Entry<String, String> channelInfos : channelIDandName.entrySet()) {

                String channelID = channelInfos.getKey();
                String channelName = channelInfos.getValue();
                HBox channel = channel(channelName, channelID, guildID);

                elements.getChildren().add(channel);
            }
        }

        elements.setSpacing(20);

        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(elements);
        return scrollPane;
    }

    private VBox trackOptions() {
        VBox trackOptions = new VBox();

        HBox loadBox = new HBox();

        loadBox.setSpacing(10);
        loadBox.setAlignment(Pos.CENTER);

        FlowPane mediaBox = new FlowPane();
        mediaBox.setHgap(20);
        mediaBox.setVgap(20);
        mediaBox.setTranslateY(30);

        TextField mediaInput = mediaInputField();
        Button mediaLoadBtn = loadMediaButton(mediaInput, mediaBox);
        Button searchInSystemButton = searchInSystemButton(mediaInput);

        loadBox.getChildren().addAll(mediaInput, searchInSystemButton, mediaLoadBtn);

        mediaInput.setPrefWidth(500);
        trackOptions.setTranslateY(20);
        trackOptions.getChildren().addAll(loadBox, mediaBox);
        return trackOptions;
    }


    private TextField mediaInputField() {
        TextField mediaInputText = new TextField();
        mediaInputText.setPromptText("Local mp3 files, YouTube Links, SoundCloud Links");
        return mediaInputText;
    }

    private Button searchInSystemButton(TextField mediaInput) {
        Button searchInSystemButton = new Button("X");

        searchInSystemButton.setOnAction(event -> {
            File mp3File = FXUtil.newFileChooser().showOpenDialog(Main.getInstance().getStage());

            if (mp3File != null) {
                mediaInput.setText(mp3File.getAbsolutePath());
            }
        });

        return searchInSystemButton;
    }

    private Button loadMediaButton(TextField mediaInput, FlowPane mediaBox) {
        Button mediaLoadButton = new Button("Load Track");
        AtomicBoolean isClickable = new AtomicBoolean(false);

        mediaLoadButton.setOnAction(event -> {
            if (isClickable.get()) {
                File file = new File(mediaInput.getText());

                if ((file.exists() && file.getName().endsWith(".mp3")) || mediaInput.getText().contains("youtube.com/watch") || mediaInput.getText().contains("https://soundcloud.com/")) {
                    VBox mediaElement = mediaElement(mediaInput);
                    mediaBox.getChildren().add(mediaElement);
                }
            }
        });

        mediaInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaInput.getText().contains("youtube.com/watch") || mediaInput.getText().contains("https://soundcloud.com/") || mediaInput.getText().length() > 5) {
                isClickable.set(true);
                mediaLoadButton.setStyle("-fx-background-color: -fx-positive;");
            } else {
                isClickable.set(false);
                mediaLoadButton.setStyle("-fx-background-color: -fx-card-color;");
            }
        });


        return mediaLoadButton;
    }

    public static boolean canClickPlayButton = true;
    private VBox mediaElement(TextField mediaInput) {
        VBox mediaElement = new VBox();

        VBox elements = new VBox();
        elements.setAlignment(Pos.CENTER);
        elements.setSpacing(10);

        String urlToPlay = mediaInput.getText();
        Label trackName = trackLabel(mediaInput);
        Button playButton = new Button("Play");
        playButton.setStyle("-fx-background-color: -fx-positive;");

        playButton.setOnAction(event -> {
            if (canClickJoinButton) {
                if (AudioConnection.voiceConnections > 0) {
                    playButton.setText("Starting...");
                    playButton.setStyle("-fx-background-color: -fx-card-color;");
                    discordMultiRequests.playMusic(urlToPlay, guildID, playButton).start();
                }
            }
        });

        elements.getChildren().addAll(trackName, playButton);

        mediaElement.setMinWidth(150);
        mediaElement.setSpacing(10);
        mediaElement.setPadding(new Insets(10, 10, 10, 10));
        mediaElement.setStyle("-fx-background-color: #202834");
        mediaElement.getChildren().addAll(elements);
        return mediaElement;
    }

    private Label trackLabel(TextField mediaInput) {
        Label trackLabel = new Label();
        if (mediaInput.getText().contains("youtube.com")) {
            trackLabel.setText("YouTube Track");
        } else if (mediaInput.getText().contains("https://soundcloud.com/")) {
            trackLabel.setText("Soundcloud Track");
        } else {
            File f = new File(mediaInput.getText());
            if (f.exists()) {
                trackLabel.setText(f.getName());
            } else {
                trackLabel.setStyle("-fx-text-fill: #b71515;");
                trackLabel.setText("Couldnt find file.");
            }
        }

        return trackLabel;
    }

    private HBox channel(String name, String channelID, String guildID) {
        HBox channel = new HBox();

        Label channelName = new Label(name);
        channel.setAlignment(Pos.CENTER);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        channel.getChildren().addAll(soundImage(), channelName, spacer, joinButton(channelID, guildID), leaveButton());

        channel.setSpacing(10);
        channel.setPadding(new Insets(10, 10, 10, 10));
        channel.setStyle("-fx-background-color: #202834");
        return channel;
    }

    private SVGPath soundImage() {
        SVGPath soundImage = new SVGPath();
        soundImage.setFill(Color.WHITE);
        soundImage.setContent("M11.383 3.07904C11.009 2.92504 10.579 3.01004 10.293 3.29604L6 8.00204H3C2.45 8.00204 2 8.45304 2 9.00204V15.002C2 15.552 2.45 16.002 3 16.002H6L10.293 20.71C10.579 20.996 11.009 21.082 11.383 20.927C11.757 20.772 12 20.407 12 20.002V4.00204C12 3.59904 11.757 3.23204 11.383 3.07904ZM14 5.00195V7.00195C16.757 7.00195 19 9.24595 19 12.002C19 14.759 16.757 17.002 14 17.002V19.002C17.86 19.002 21 15.863 21 12.002C21 8.14295 17.86 5.00195 14 5.00195ZM14 9.00195C15.654 9.00195 17 10.349 17 12.002C17 13.657 15.654 15.002 14 15.002V13.002C14.551 13.002 15 12.553 15 12.002C15 11.451 14.551 11.002 14 11.002V9.00195Z");
        return soundImage;
    }

    public static boolean canClickJoinButton = true;
    private Button joinButton(String channelID, String guildID) {
        Button joinBtn = new Button("Join");

        joinBtn.setOnAction(event -> {
            try {
                if (canClickJoinButton) {
                    canClickJoinButton = false;
                    clearPreviousStuff();
                    VoiceSpamInfo.botsConnected.setText("Bots connected: " + AudioConnection.voiceConnections + "/" + TokenManager.verifiedTokens.size());
                    discordMultiRequests.joinVoiceChannel(guildID, channelID, joinBtn).start();
                    joinBtn.setText("Joining...");
                    joinBtn.setStyle("-fx-background-color: -fx-card-color;");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        joinBtn.setStyle("-fx-background-color: -fx-positive;");
        return joinBtn;
    }

    private Button leaveButton() {
        Button leaveButton = new Button("Leave");
        leaveButton.setOnAction(event -> {
            clearPreviousStuff();
            VoiceSpamInfo.botsConnected.setText("Bots connected: " + AudioConnection.voiceConnections + "/" + TokenManager.verifiedTokens.size());
            DiscordVoiceGateway.closeGateways(0);
            DiscordVoiceUDPGateway.closeGateways(0);
        });

        leaveButton.setStyle("-fx-background-color: -fx-negative;");
        return leaveButton;
    }

    public HBox unloadComponent() {
        HBox content = new HBox();

        content.setTranslateY(-28);
        content.setSpacing(10);
        content.setAlignment(Pos.TOP_CENTER);
        content.getChildren().addAll(unloadButton());
        return content;
    }

    private Button unloadButton() {
        Button unloadBtn = new Button("Remove this Server");

        unloadBtn.setOnAction(event -> DashboardController.getVoiceSpam().restoreView());

        return unloadBtn;
    }

    public void clearPreviousStuff() {
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
