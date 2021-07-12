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

import java.util.concurrent.atomic.AtomicBoolean;

public class FriendRequest {

    public static boolean isSendButtonPressed, isRemoveButtonPressed, isLoopButtonPressed;
    private Thread sendRequestThread, removeRequestThread, loopRequestThread;
    private DiscordMultiRequests multiRequests = new DiscordMultiRequests();

    private Button sendBtn, loopBtn, removeBtn;

    public VBox contentArea() {
        VBox content = new VBox();

        DelaySlider delaySlider = new DelaySlider();
        VBox sliderElements = delaySlider.delaySlider("Loop Delay", "ms", 500 , 0);

        TextField userID = userIDField();

        content.getChildren().addAll(headingLabel(), userID, sliderElements, buttons(userID, delaySlider));

        content.setSpacing(10);
        content.setPadding(new Insets(10, 10, 10, 10));
        content.setStyle("-fx-background-color: -fx-card-color");
        return content;
    }

    private Label headingLabel() {
        Label heading = new Label("Send friend requests to user");

        return heading;
    }

    private TextField userIDField() {
        TextField userID = new TextField();
        userID.setPromptText("User ID");

        return userID;
    }

    private HBox buttons(TextField userID, DelaySlider delaySlider) {
        HBox buttons = new HBox();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        sendBtn = sendButton(userID, delaySlider);
        removeBtn = removeButton(userID, delaySlider);
        loopBtn = loopButton(userID, delaySlider);

        buttons.setSpacing(10);
        buttons.getChildren().addAll(sendBtn, spacer, loopBtn, removeBtn);
        return buttons;
    }

    private Button sendButton(TextField userID, DelaySlider delaySlider) {
        Button sendBtn = new Button("Send");

        AtomicBoolean allowedToClick = new AtomicBoolean(false);

        sendBtn.setOnAction(event -> {
            if (allowedToClick.get()) {
                if (isSendButtonPressed) {
                    sendBtn.setText("Send");
                    sendBtn.setStyle("-fx-background-color: -fx-positive;");
                    sendRequestThread.stop();
                } else {
                    sendBtn.setText("Stop");
                    sendBtn.setStyle("-fx-background-color: -fx-negative;");
                    sendRequestThread = multiRequests.sendFriendRequest(userID.getText(), delaySlider.getSliderValue(), sendBtn);
                    sendRequestThread.start();
                }
                isSendButtonPressed = !isSendButtonPressed;
            }

        });

        userID.textProperty().addListener((observable, oldValue, newValue) -> {
            allowedToClick.set(userID.getText().length() > 5);
            sendBtn.setStyle(allowedToClick.get() ? "-fx-background-color: -fx-positive;" : "-fx-background-color: -fx-component-color;");
        });


        return sendBtn;
    }

    private Button removeButton(TextField userID, DelaySlider delaySlider) {
        Button removeBtn = new Button("Remove");

        removeBtn.setMinWidth(150);

        AtomicBoolean allowedToClick = new AtomicBoolean(false);

        removeBtn.setOnAction(event -> {

            if (allowedToClick.get()) {
                if (isRemoveButtonPressed) {
                    removeBtn.setText("Remove");
                    removeRequestThread.stop();
                } else {
                    removeBtn.setText("Stop");
                    removeRequestThread = multiRequests.removeFriendRequest(userID.getText(), delaySlider.getSliderValue(), removeBtn);
                    removeRequestThread.start();
                }
                isRemoveButtonPressed = !isRemoveButtonPressed;
            }
        });

        userID.textProperty().addListener((observable, oldValue, newValue) -> {
            allowedToClick.set(userID.getText().length() > 5);
            removeBtn.setStyle(allowedToClick.get() ? "-fx-background-color: -fx-negative;" : "-fx-background-color: -fx-component-color;");
        });

        return removeBtn;
    }

    private Button loopButton(TextField userID, DelaySlider delaySlider) {
        Button loopBtn = new Button("Loop");

        AtomicBoolean allowedToClick = new AtomicBoolean(false);

        loopBtn.setOnAction(event -> {

            if (allowedToClick.get()) {
                if (isLoopButtonPressed) {
                    loopBtn.setText("Loop");
                    loopBtn.setStyle("-fx-background-color: -fx-positive;");
                    loopRequestThread.stop();
                } else {
                    loopBtn.setText("Stop");
                    loopBtn.setStyle("-fx-background-color: -fx-negative;");
                    loopRequestThread = multiRequests.friendRequestLoop(userID.getText(), delaySlider.getSliderValue());
                    loopRequestThread.start();
                }
                isLoopButtonPressed = !isLoopButtonPressed;
            }
        });

        userID.textProperty().addListener((observable, oldValue, newValue) -> {
            allowedToClick.set(userID.getText().length() > 5);
            loopBtn.setStyle(allowedToClick.get() ? "-fx-background-color: -fx-positive;" : "-fx-background-color: -fx-component-color;");
        });
        return loopBtn;
    }


}
