package xyz.vitox.discordtool.tab;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.tab.optionComponents.*;
import xyz.vitox.discordtool.tab.serverSpamComponents.*;

public class Options extends StackPane {

    public Options() {
        this.getChildren().add(contentArea());
    }

    public HBox contentArea() {
        HBox content = new HBox();

        GridPane elementGrid = elementGrid(leftElements(), rightElements());

        content.setSpacing(100);
        content.getChildren().add(elementGrid);
        return content;
    }

    private GridPane elementGrid(VBox leftElements, VBox rightElements) {
        GridPane elementGrid = new GridPane();
        elementGrid.setHgap(150);

        elementGrid.add(leftElements, 0, 1);
        elementGrid.add(rightElements, 1, 1);

        return elementGrid;
    }

    private VBox leftElements() {
        VBox leftElements = new VBox();
        leftElements.setSpacing(20);
        leftElements.setPrefWidth(750);

        LoadProfilePictures loadProfilePictures = new LoadProfilePictures();
        DefaultTokens defaultTokens = new DefaultTokens();
        UseVerifiedTokens verifiedTokens = new UseVerifiedTokens();
        ProxyOptions proxyOptions = new ProxyOptions();

        leftElements.getChildren().addAll(verifiedTokens.contentArea(), loadProfilePictures.contentArea(), defaultTokens.contentArea(), proxyOptions.contentArea());
        return leftElements;
    }

    private VBox rightElements() {
        VBox rightElements = new VBox();
        rightElements.setSpacing(20);
        rightElements.setPrefWidth(750);

//        rightElements.getChildren().addAll(friendRequest.contentArea(), writeMessageUser.contentArea());
        return rightElements;
    }

}
