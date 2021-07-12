package xyz.vitox.discordtool.tab.serverSpamComponents;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.discordAPI.api.DiscordAPI;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;
import xyz.vitox.discordtool.discordAPI.tokenManager.TokenManager;
import xyz.vitox.discordtool.tab.verifierComponents.ServerCaptchaVerifiy;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class JoinLeave {

    public static boolean isJoinButtonPressed, isLeaveButtonPressed;
    private Thread joinServerThread, leaveServerThread;
    private DiscordMultiRequests multiRequests = new DiscordMultiRequests();
    private DiscordAPI discordAPI = new DiscordAPI();
    public static Label joinedCountLabel;

    public VBox contentArea() {
        VBox content = new VBox();

        content.getChildren().addAll(joinServerElements(), leaveServerElements());

        content.setSpacing(10);
        content.setPadding(new Insets(10, 10, 10, 10));
        content.setStyle("-fx-background-color: -fx-card-color");
        return content;
    }

    private VBox joinServerElements() {
        HBox joinItems = new HBox();
        VBox joinServerElements = new VBox();

        DelaySlider delaySlider = new DelaySlider();
        VBox sliderElements = delaySlider.delaySlider("Join Delay", "ms", 500, 0);

        joinedCountLabel = new Label("Joined: 0/0");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label joinLabel = new Label("Join Guild:    ");
        TextField joinGuildTextfield = joinGuildTextfield();
        Button joinGuildButton = joinGuildButton(joinGuildTextfield, delaySlider);

        joinItems.setSpacing(10);
        joinItems.getChildren().addAll(joinLabel, joinGuildTextfield, joinGuildButton, spacer, joinedCountLabel);

        joinServerElements.getChildren().addAll(joinItems, sliderElements);
        return joinServerElements;
    }

    private TextField joinGuildTextfield() {
        TextField joinGuildTextfield = new TextField();
        joinGuildTextfield.setPromptText("Invite code");

        return joinGuildTextfield;
    }

    private Button joinGuildButton(TextField inviteCode, DelaySlider delaySlider) {
        Button joinGuildButton = new Button("Join");

        AtomicBoolean allowedToPress = new AtomicBoolean(false);

        joinGuildButton.setOnAction(event -> {
            if (allowedToPress.get()) {
                if (isJoinButtonPressed) {
                    joinGuildButton.setText("Join");
                    joinGuildButton.setStyle("-fx-background-color: -fx-positive;");
                    joinServerThread.stop();
                } else {
                    joinGuildButton.setText("Stop");
                    joinGuildButton.setStyle("-fx-background-color: -fx-negative;");

                    String invite;
                    if (inviteCode.getText().contains("/")) {
                        String[] splittedCode = inviteCode.getText().split("/");
                        String code = splittedCode[splittedCode.length - 1];
                        invite = code;
                    } else {
                        invite = inviteCode.getText();
                    }

                    joinServerThread = multiRequests.joinDiscordServer(invite, delaySlider.getSliderValue(), joinGuildButton);
                    joinServerThread.start();

                    if (ServerCaptchaVerifiy.startButtonPressed) {
                        new Thread(() -> {
                            try {
                                ServerCaptchaVerifiy.failedTokensLastServer.clear();
                                String guildInformation = discordAPI.getGuildInformationFromInvite(TokenManager.tokensToUse().get(0), invite);
                                JsonElement element = new Gson().fromJson(guildInformation, JsonElement.class);
                                JsonObject jsonObj = element.getAsJsonObject();

                                Platform.runLater(() -> ServerCaptchaVerifiy.lastServerJoinedName.setText("Last guild joined: " + jsonObj.get("guild").getAsJsonObject().get("name").getAsString()));
                                ServerCaptchaVerifiy.lastServerJoinedInvite = jsonObj.get("code").getAsString();
                                ServerCaptchaVerifiy.lastServerJoinedID = jsonObj.get("guild").getAsJsonObject().get("id").getAsString();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                }

                isJoinButtonPressed = !isJoinButtonPressed;
            }
        });

        inviteCode.textProperty().addListener((observable, oldValue, newValue) -> {
            allowedToPress.set(inviteCode.getText().length() > 2);
            joinGuildButton.setStyle(allowedToPress.get() ? "-fx-background-color: -fx-positive;" : "-fx-background-color: -fx-component-color;");
        });

        return joinGuildButton;
    }

    private VBox leaveServerElements() {
        HBox leaveItems = new HBox();
        VBox leaveServerElements = new VBox();

        DelaySlider delaySlider = new DelaySlider();
        VBox sliderElements = delaySlider.delaySlider("Leave Delay", "ms", 500, 0);

        Label leaveLabel = new Label("Leave Guild: ");
        TextField leaveGuildTextfield = leaveGuildTextfield();
        Button leaveGuildButton = leaveGuildButton(leaveGuildTextfield, delaySlider);

        leaveItems.setSpacing(10);
        leaveItems.getChildren().addAll(leaveLabel, leaveGuildTextfield, leaveGuildButton);

        leaveServerElements.getChildren().addAll(leaveItems, sliderElements);
        return leaveServerElements;
    }

    private TextField leaveGuildTextfield() {
        TextField leaveGuildTextfield = new TextField();
        leaveGuildTextfield.setPromptText("Server ID");

        return leaveGuildTextfield;
    }

    private Button leaveGuildButton(TextField serverID, DelaySlider delaySlider) {
        Button leaveGuildButton = new Button("Leave");

        AtomicBoolean allowedToPress = new AtomicBoolean(false);

        leaveGuildButton.setOnAction(event -> {
            if (allowedToPress.get()) {
                if (isLeaveButtonPressed) {
                    leaveGuildButton.setText("Leave");
                    leaveGuildButton.setStyle("-fx-background-color: -fx-positive;");
                    leaveServerThread.stop();
                } else {
                    leaveGuildButton.setText("Stop");
                    leaveGuildButton.setStyle("-fx-background-color: -fx-negative;");

                    ServerCaptchaVerifiy.verifiedTokensOnGuild = 0;
                    ServerCaptchaVerifiy.successfullyVerifiedBotsOnGuild.setText("Successfully verified bots on this guild: 0");
                    leaveServerThread = multiRequests.leaveDiscordServer(serverID.getText(), delaySlider.getSliderValue(), leaveGuildButton);
                    leaveServerThread.start();
                }

                isLeaveButtonPressed = !isLeaveButtonPressed;
            }
        });

        serverID.textProperty().addListener((observable, oldValue, newValue) -> {
            allowedToPress.set(serverID.getText().length() > 4);
            leaveGuildButton.setStyle(allowedToPress.get() ? "-fx-background-color: -fx-positive;" : "-fx-background-color: -fx-component-color;");
        });

        return leaveGuildButton;
    }

}
