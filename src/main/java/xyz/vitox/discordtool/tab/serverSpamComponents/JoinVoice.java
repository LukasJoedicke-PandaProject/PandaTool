package xyz.vitox.discordtool.tab.serverSpamComponents;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;
import xyz.vitox.discordtool.discordAPI.api.gateway.DiscordGateway;

import java.util.concurrent.atomic.AtomicBoolean;


public class JoinVoice {

    private Thread voiceChannelThread;
    private DiscordMultiRequests multiRequests = new DiscordMultiRequests();

    public VBox contentArea() {
        VBox content = new VBox();

        DelaySlider delaySlider = new DelaySlider();
        VBox sliderElements = delaySlider.delaySlider("Join Delay", "ms", 500, 0);

        TextField serverIDField = serverIDField();
        TextField voiceIDField = voiceIDField();

        content.getChildren().addAll(headingLabel(), serverIDField, voiceIDField, sliderElements, buttons(voiceIDField, serverIDField, delaySlider));

        content.setSpacing(10);
        content.setPadding(new Insets(10, 10, 10, 10));
        content.setStyle("-fx-background-color: rgba(31,38,50,0.55)");
        return content;
    }

    private Label headingLabel() {
        Label heading = new Label("Join voice Channel");

        return heading;
    }

    private TextField serverIDField() {
        TextField serverIDField = new TextField();

        serverIDField.setPromptText("Server ID");

        return serverIDField;
    }

    private TextField voiceIDField() {
        TextField voiceIDField = new TextField();

        voiceIDField.setPromptText("Voice Channel ID");

        return voiceIDField;
    }

    private HBox buttons(TextField voiceIDField, TextField serverIDField, DelaySlider delaySlider) {
        HBox buttons = new HBox();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttons.setSpacing(10);
        buttons.getChildren().addAll(joinButton(voiceIDField, serverIDField, delaySlider), spacer, leaveButton(voiceIDField, serverIDField, delaySlider));

        return buttons;
    }

    private Button joinButton(TextField voiceIDField, TextField serverIDField, DelaySlider delaySlider) {
        Button joinBtn = new Button("Join");

        AtomicBoolean allowedToClick1 = new AtomicBoolean(false);
        AtomicBoolean allowedToClick2 = new AtomicBoolean(false);

        joinBtn.setOnAction(event -> {
            if (allowedToClick1.get() && allowedToClick2.get()) {
//                multiRequests.joinVoiceChannel(serverIDField.getText(), voiceIDField.getText()).start();
//                voiceChannelThread = multiRequests.joinVoiceChannel(serverIDField.getText(), voiceIDField.getText(), delaySlider.getSliderValue());
//                voiceChannelThread.start();
            }
        });

        voiceIDField.textProperty().addListener((observable, oldValue, newValue) -> {
            allowedToClick1.set(voiceIDField.getText().length() >= 5);
            joinBtn.setStyle(allowedToClick1.get() && allowedToClick2.get() ? "-fx-background-color: -fx-positive;" : "-fx-background-color: -fx-card-color;");
        });

        serverIDField.textProperty().addListener((observable, oldValue, newValue) -> {
            allowedToClick2.set(serverIDField.getText().length() > 0);
            joinBtn.setStyle(allowedToClick1.get() && allowedToClick2.get() ? "-fx-background-color: -fx-positive;" : "-fx-background-color: -fx-card-color;");
        });


        return joinBtn;
    }

    private Button leaveButton(TextField voiceIDField, TextField serverIDField, DelaySlider delaySlider) {
        Button leaveBtn = new Button("Leave");

        leaveBtn.setMinWidth(150);

        AtomicBoolean allowedToClick1 = new AtomicBoolean(false);
        AtomicBoolean allowedToClick2 = new AtomicBoolean(false);

        leaveBtn.setOnAction(event -> {
            if (allowedToClick1.get() && allowedToClick2.get()) {
                DiscordGateway.closeGateways(delaySlider.getSliderValue());
            }
        });

        voiceIDField.textProperty().addListener((observable, oldValue, newValue) -> {
            allowedToClick1.set(voiceIDField.getText().length() >= 5);
            leaveBtn.setStyle(allowedToClick1.get() && allowedToClick2.get() ? "-fx-background-color: -fx-negative;" : "-fx-background-color: -fx-card-color;");
        });

        serverIDField.textProperty().addListener((observable, oldValue, newValue) -> {
            allowedToClick2.set(serverIDField.getText().length() > 0);
            leaveBtn.setStyle(allowedToClick1.get() && allowedToClick2.get() ? "-fx-background-color: -fx-negative;" : "-fx-background-color: -fx-card-color;");
        });


        return leaveBtn;
    }

}
