package xyz.vitox.discordtool.tab.tokenSettingsComponents;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;

public class TokenCleaner {

    private DiscordMultiRequests multiRequests = new DiscordMultiRequests();

    public VBox contentArea() {
        VBox content = new VBox();

        CheckBox removeGuilds = removeGuilds();
        HBox buttons = buttons(removeGuilds);

        content.getChildren().addAll(headingLabel(), removeGuilds, buttons);

        content.setSpacing(10);
        content.setPadding(new Insets(10, 10, 10, 10));
        content.setStyle("-fx-background-color: -fx-card-color");
        return content;
    }

    private Label headingLabel() {
        Label heading = new Label("Token Cleaner: ");

        return heading;
    }

    private CheckBox removeGuilds() {
        CheckBox removeGuilds = new CheckBox("Leave all Guilds");

        return removeGuilds;
    }

    private HBox buttons(CheckBox removeGuilds) {
        HBox buttons = new HBox();

        Button startBtn = startButton(removeGuilds);
        buttons.getChildren().addAll(startBtn);
        return buttons;
    }

    private Button startButton(CheckBox removeGuilds) {
        Button startBtn = new Button("Start");

        startBtn.setStyle("-fx-background-color: -fx-positive;");

        startBtn.setOnAction(event -> {
            if (removeGuilds.isSelected()) {
                multiRequests.leaveAllGuilds().start();
            }
        });

        return startBtn;
    }


}
