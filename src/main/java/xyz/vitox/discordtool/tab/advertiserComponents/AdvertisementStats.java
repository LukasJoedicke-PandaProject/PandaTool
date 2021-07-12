package xyz.vitox.discordtool.tab.advertiserComponents;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;

public class AdvertisementStats {

    private DiscordMultiRequests multiRequests = new DiscordMultiRequests();

    public VBox contentArea() {
        VBox content = new VBox();

        content.getChildren().addAll(elements());

        content.setPadding(new Insets(10, 10, 10, 10));
        content.setStyle("-fx-background-color: -fx-card-color");
        return content;
    }

    public VBox elements() {
        VBox elements = new VBox();

        Label guildsScrapedLabel = new Label("Unique guilds: 0");
        guildsScrapedLabel.setTranslateY(3);

        Label usersScrapedLabel = new Label("Unique users: 0");

        elements.setSpacing(10);
        elements.getChildren().addAll(guildsScrapedLabel, usersScrapedLabel);
        return elements;
    }

}
