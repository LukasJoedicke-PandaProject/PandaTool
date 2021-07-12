package xyz.vitox.discordtool.tab.serverSpamComponents;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.Main;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;
import xyz.vitox.discordtool.discordAPI.api.gateway.DiscordMassPing;
import xyz.vitox.discordtool.util.FXUtil;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class WriteMessageUser {

    public static boolean isSpamPressed, isMassPingPressed;
    private Thread writeMessageThread, massPingThread;
    private File attachmentFile;
    private boolean isSpamming = false;
    private DiscordMultiRequests multiRequests = new DiscordMultiRequests();

    public VBox contentArea() {
        VBox content = new VBox();

        DelaySlider delaySlider = new DelaySlider();
        VBox sliderElements = delaySlider.delaySlider("Delay", "ms", 500, 0);

        Label headingLabel = headingLabel();
        TextField channelID = channelID();
        TextArea channelMessage = channelMessage();
        HBox buttons = buttons(channelID, channelMessage, delaySlider, content);

        content.setSpacing(10);
        content.setPadding(new Insets(10, 10, 10, 10));
        content.setStyle("-fx-background-color: -fx-card-color");
        content.getChildren().addAll(headingLabel, channelID, channelMessage, sliderElements, buttons);
        return content;
    }

    public Label headingLabel() {
        Label headingLabel = new Label("Write message to User: (not recommended)");

        return headingLabel;
    }

    public TextField channelID() {
        TextField channelIDText = new TextField();

        channelIDText.setPromptText("User ID");

        return channelIDText;
    }

    public TextArea channelMessage() {
        TextArea channelMessage = new TextArea();
        channelMessage.setPromptText("Enter message here! Options: %random%");
        FXUtil.addTextLimiter(channelMessage, 2000);

        return channelMessage;
    }

    public HBox buttons(TextField channelID, TextArea channelMessage, DelaySlider delaySlider, VBox content) {
        HBox buttonBox = new HBox();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        CheckBox enableTTS = enableTextToSpeech();
        CheckBox enableTyping = enableTyping();
        Button attachmentButton = attachmentButton(buttonBox, content);
        Button writeButton = writeButton(channelID, channelMessage, enableTTS, enableTyping, delaySlider);

        buttonBox.setSpacing(10);
        buttonBox.getChildren().addAll(writeButton, spacer, enableTTS, enableTyping, attachmentButton);
        return buttonBox;
    }

    public Button writeButton(TextField channelID, TextArea channelMessage, CheckBox tts, CheckBox typing, DelaySlider delaySlider) {
        Button writeButton = new Button("Spam!");
        writeButton.setMinWidth(125);
        AtomicBoolean allowedToClick = new AtomicBoolean();
        AtomicBoolean allowedToClick2 = new AtomicBoolean();

        writeButton.setOnAction(event -> {
            if (allowedToClick.get() && allowedToClick2.get()) {

                if (isSpamPressed) {
                    writeButton.setText("Spam");
                    writeButton.setStyle("-fx-background-color: -fx-positive;");
                    writeMessageThread.stop();
                } else {
                    writeButton.setText("Stop Spam");
                    writeButton.setStyle("-fx-background-color: -fx-negative;");
                    writeMessageThread = multiRequests.writeUser(channelMessage.getText(), channelID.getText(), tts.isSelected(), typing.isSelected(), attachmentFile, delaySlider.getSliderValue());
                    writeMessageThread.start();
                }

                isSpamPressed = !isSpamPressed;
            }
        });

        channelMessage.textProperty().addListener((observable, oldValue, newValue) -> {
            allowedToClick2.set(channelMessage.getText().length() > 0);
            writeButton.setStyle(allowedToClick.get() && allowedToClick2.get() ? "-fx-background-color: -fx-positive;" : "-fx-background-color: -fx-component-color;");
        });

        channelID.textProperty().addListener((observable, oldValue, newValue) -> {
            allowedToClick.set(channelID.getText().length() > 8);
            writeButton.setStyle(allowedToClick.get() && allowedToClick2.get() ? "-fx-background-color: -fx-positive;" : "-fx-background-color: -fx-component-color;");
        });

        return writeButton;
    }

    public Button attachmentButton(HBox buttons, VBox content) {
        Button attachmentButton = new Button("Attachment");
        attachmentButton.setStyle("-fx-background-color: -fx-positive;");

        attachmentButton.setOnAction(event -> {
            attachmentFile = FXUtil.newFileChooser().showOpenDialog(Main.getInstance().getStage());

            if (attachmentFile != null) {
                HBox attachmentBox = new HBox();
                Label attachmentLabel = new Label("Attachment: ");
                Label attachmentContent = new Label();
                Button attachmentRemoveBtn = new Button("Remove");
                Region attachmentSpacer = new Region();
                HBox.setHgrow(attachmentSpacer, Priority.ALWAYS);

                content.getChildren().remove(buttons);
                content.getChildren().addAll(attachmentBox, buttons);
                attachmentRemoveBtn.setStyle("-fx-background-color: -fx-negative;");
                attachmentBox.setStyle("-fx-background-color: #202834;");
                attachmentBox.setPadding(new Insets(5, 10, 5, 10));
                attachmentLabel.setPadding(new Insets(4, 0, 0, 0));
                attachmentContent.setPadding(new Insets(4, 0, 0, 0));
                attachmentContent.setText(attachmentFile.getPath());
                attachmentBox.getChildren().addAll(attachmentLabel, attachmentContent, attachmentSpacer, attachmentRemoveBtn);

                attachmentRemoveBtn.setOnAction(event1 -> {
                    attachmentFile = null;
                    content.getChildren().remove(attachmentBox);
                });
            }
        });

        return attachmentButton;
    }

    public CheckBox enableTextToSpeech() {
        CheckBox enableTextToSpeech = new CheckBox("TTS");

        enableTextToSpeech.setTranslateY(4);

        return enableTextToSpeech;
    }

    public CheckBox enableTyping() {
        CheckBox enableTyping = new CheckBox("Typing");

        enableTyping.setTranslateY(4);

        return enableTyping;
    }

}
