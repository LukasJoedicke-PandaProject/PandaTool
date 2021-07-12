package xyz.vitox.discordtool.tab.serverSpamComponents;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;
import xyz.vitox.discordtool.util.emoji.EmojiSearchListController;

import java.net.URLEncoder;

public class ReactEmoji {

    private final boolean[] allowedToClick = new boolean[3];
    public static boolean isReactPressed, isRemovePressed;
    private Thread reactThread, removeThread;
    private Button reactMessageButton;
    private Button removeReactionButton;
    public static TextField reactionEmoji, emojiID;
    private TextField channelID;
    private TextField messageID;
    public static Label reactionCount;
    private DiscordMultiRequests multiRequests = new DiscordMultiRequests();

    public VBox contentArea() {
        VBox content = new VBox();

        HBox headerContent = new HBox();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        reactionCount = new Label("Reacted: 0/0");

        channelID = channelID();
        messageID = messageID();

        DelaySlider delaySlider = new DelaySlider();
        VBox sliderElements = delaySlider.delaySlider("React Delay", "ms", 500, 0);

        headerContent.getChildren().addAll(headingLabel(), spacer, reactionCount);
        content.getChildren().addAll(headerContent, channelID, messageID, reactElements(), sliderElements, buttons(delaySlider));

        content.setSpacing(10);
        content.setPadding(new Insets(10, 10, 10, 10));
        content.setStyle("-fx-background-color: -fx-card-color");
        return content;
    }

    private Label headingLabel() {
        Label headingLabel = new Label("React with Emoji:");

        return headingLabel;
    }

    private TextField channelID() {
        TextField channelID = new TextField();
        channelID.setPromptText("Channel ID");

        channelID.textProperty().addListener((observable, oldValue, newValue) -> {
            allowedToClick[0] = channelID.getText().length() >= 5;
            reactMessageButton.setStyle(allowedToClick[0] && allowedToClick[1] && allowedToClick[2] ? "-fx-background-color: -fx-positive;" : "-fx-background-color: -fx-component-color;");
            removeReactionButton.setStyle(allowedToClick[0] && allowedToClick[1] && allowedToClick[2] ? "-fx-background-color: -fx-negative;" : "-fx-background-color: -fx-component-color;");
        });

        return channelID;
    }

    private TextField messageID() {
        TextField messageID = new TextField();
        messageID.setPromptText("Message ID");

        messageID.textProperty().addListener((observable, oldValue, newValue) -> {
            allowedToClick[1] = messageID.getText().length() >= 5;
            reactMessageButton.setStyle(allowedToClick[0] && allowedToClick[1] && allowedToClick[2] ? "-fx-background-color: -fx-positive;" : "-fx-background-color: -fx-component-color;");
            removeReactionButton.setStyle(allowedToClick[0] && allowedToClick[1] && allowedToClick[2] ? "-fx-background-color: -fx-negative;" : "-fx-background-color: -fx-component-color;");
        });

        return messageID;
    }

    private HBox reactElements() {
        HBox reactElements = new HBox();

        reactionEmoji = reactionEmoji();
        this.emojiID = emojiID();
        Button selectEmojiButton = selectEmojiButton();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        reactElements.setSpacing(5);
        reactElements.getChildren().addAll(reactionEmoji, selectEmojiButton, spacer, this.emojiID);

        return reactElements;
    }

    private TextField reactionEmoji() {
        TextField reactionEmoji = new TextField();
        reactionEmoji.setPromptText("Emoji");

        reactionEmoji.setAlignment(Pos.CENTER);
        reactionEmoji.setMinWidth(250);

        reactionEmoji.textProperty().addListener((observable, oldValue, newValue) -> {
            allowedToClick[2] = reactionEmoji.getText().length() > 0;
            reactMessageButton.setStyle(allowedToClick[0] && allowedToClick[1] && allowedToClick[2] ? "-fx-background-color: -fx-positive;" : "-fx-background-color: -fx-component-color;");
            removeReactionButton.setStyle(allowedToClick[0] && allowedToClick[1] && allowedToClick[2] ? "-fx-background-color: -fx-negative;" : "-fx-background-color: -fx-component-color;");

        });

        return reactionEmoji;
    }

    private TextField emojiID() {
        TextField emojiID = new TextField();
        emojiID.setPromptText("Emoji ID (for custom emotes)");

        emojiID.setAlignment(Pos.CENTER);
        emojiID.setMinWidth(250);

        emojiID.textProperty().addListener((observable, oldValue, newValue) -> {
            allowedToClick[2] = emojiID.getText().length() > 0;
            reactMessageButton.setStyle(allowedToClick[0] && allowedToClick[1] && allowedToClick[2] ? "-fx-background-color: -fx-positive;" : "-fx-background-color: -fx-component-color;");
            removeReactionButton.setStyle(allowedToClick[0] && allowedToClick[1] && allowedToClick[2] ? "-fx-background-color: -fx-negative;" : "-fx-background-color: -fx-component-color;");
        });

        return emojiID;
    }

    private Button selectEmojiButton() {
        Button emojiButton = new Button();

        emojiButton.setAlignment(Pos.CENTER);
        emojiButton.setTranslateX(-20);
        emojiButton.setStyle("-fx-background-color: #2e394e");

        ImageView emojiButtonImage = new ImageView("xyz/vitox/discordtool/data/emoji_images/twemoji/1f643.png");
        emojiButtonImage.setFitWidth(15);
        emojiButtonImage.setFitHeight(15);
        emojiButton.setGraphic(emojiButtonImage);

        emojiButton.setOnAction(event -> {
            EmojiSearchListController emojiSearchListController = new EmojiSearchListController();
            emojiSearchListController.createEmojiSelectWindow();
        });

        return emojiButton;
    }

    private HBox buttons(DelaySlider delaySlider) {
        HBox buttons = new HBox();

        this.reactMessageButton = reactMessageButton(delaySlider);
        this.removeReactionButton = removeReactionButton(delaySlider);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttons.getChildren().addAll(this.reactMessageButton, spacer, this.removeReactionButton);
        return buttons;
    }

    private Button reactMessageButton(DelaySlider delaySlider) {
        Button reactButton = new Button("React");

        reactButton.setOnAction(event -> {
            if (allowedToClick[0] && allowedToClick[1] && allowedToClick[2]) {
                try {

                    if (isReactPressed) {
                        reactButton.setText("React");
                        reactButton.setStyle("-fx-background-color: -fx-positive;");
                        reactThread.stop();
                    } else {
                        reactButton.setText("Stop");
                        reactButton.setStyle("-fx-background-color: -fx-negative;");

                        String urlEncodedEmoji;
                        if (emojiID.getText().isEmpty()) {
                            urlEncodedEmoji = URLEncoder.encode(reactionEmoji.getText(), "UTF-8");
                        } else {
                            urlEncodedEmoji = URLEncoder.encode(reactionEmoji.getText() + ":" + emojiID.getText(), "UTF-8");
                        }

                        reactThread = multiRequests.reactToMessage(channelID.getText(), messageID.getText(), urlEncodedEmoji, delaySlider.getSliderValue(), reactButton);
                        reactThread.start();
                    }

                    isReactPressed = !isReactPressed;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return reactButton;
    }

    private Button removeReactionButton(DelaySlider delaySlider) {
        Button removeReactionBtn = new Button("Remove Reaction");

        removeReactionBtn.setOnAction(event -> {
            if (allowedToClick[0] && allowedToClick[1] && allowedToClick[2]) {
                try {
                    String urlEncodedEmoji;
                    if (emojiID.getText().isEmpty()) {
                        urlEncodedEmoji = URLEncoder.encode(reactionEmoji.getText(), "UTF-8");
                    } else {
                        urlEncodedEmoji = URLEncoder.encode(reactionEmoji.getText() + ":" + emojiID.getText(), "UTF-8");
                    }

                    multiRequests.removeReaction(channelID.getText(), messageID.getText(), urlEncodedEmoji, delaySlider.getSliderValue()).start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return removeReactionBtn;
    }

}
