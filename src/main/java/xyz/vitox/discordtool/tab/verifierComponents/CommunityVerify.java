package xyz.vitox.discordtool.tab.verifierComponents;

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
import xyz.vitox.discordtool.discordAPI.tokenManager.TokenManager;
import xyz.vitox.discordtool.util.InfoButton;

import java.util.concurrent.atomic.AtomicBoolean;

public class CommunityVerify {

    private DiscordMultiRequests multiRequests = new DiscordMultiRequests();

    public static Label communityVerifiedCountLabel;

    public VBox contentArea() {
        VBox content = new VBox();

        content.getChildren().addAll(topElements(), elements());

        content.setSpacing(10);
        content.setPadding(new Insets(10, 10, 10, 10));
        content.setStyle("-fx-background-color: -fx-card-color");
        return content;
    }

    public HBox topElements() {
        HBox elements = new HBox();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        InfoButton infoButton = new InfoButton();
        elements.getChildren().addAll(headingLabel(), spacer, infoButton.content("https://www.youtube.com/watch?v=fwH94jO30ic"));
        return elements;
    }

    public HBox elements() {
        HBox elements = new HBox();

        Label inviteLabel = new Label("Invitecode:");
        inviteLabel.setTranslateY(3);
        TextField inviteField = guildInviteField();
        Button startBtn = startButton(inviteField);

        elements.setSpacing(10);
        elements.getChildren().addAll(inviteLabel, inviteField, startBtn, verifiedTokensLabel());
        return elements;
    }

    public Label verifiedTokensLabel() {
        communityVerifiedCountLabel = new Label("Verified (0/" + TokenManager.tokensToUse().size() + ")");
        communityVerifiedCountLabel.setTranslateY(4);
        communityVerifiedCountLabel.setVisible(false);
        return communityVerifiedCountLabel;
    }

    public Label headingLabel() {
        Label headingLabel = new Label("Community-Guild Verifier");
        return headingLabel;
    }

    public TextField guildInviteField() {
        TextField inviteField = new TextField();
        inviteField.setPromptText("Guild Invite");
        return inviteField;
    }

    public Button startButton(TextField inviteField) {
        Button startBtn = new Button("Start");

        AtomicBoolean allowedToClick = new AtomicBoolean(false);

        startBtn.setOnAction(event -> {
            if (allowedToClick.get()) {
                startBtn.setText("Verifying...");
                startBtn.setStyle("-fx-background-color: -fx-component-color");
                communityVerifiedCountLabel.setVisible(true);
                multiRequests.verifyCommunityGuild(inviteField.getText(), startBtn).start();
            }
        });

        inviteField.textProperty().addListener((observable, oldValue, newValue) -> {
            allowedToClick.set(inviteField.getText().length() > 2);
            startBtn.setStyle(allowedToClick.get() ? "-fx-background-color: -fx-positive;" : "-fx-background-color: -fx-component-color;");
        });

        return startBtn;
    }

}
