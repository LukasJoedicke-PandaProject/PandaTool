package xyz.vitox.discordtool.tab;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.tab.advertiserComponents.AdvertisementOverview;
import xyz.vitox.discordtool.tab.advertiserComponents.AdvertisementStats;

public class Advertiser extends StackPane {

    public Advertiser() {
        this.getChildren().add(loadAccountsFirst());
    }

    public HBox contentArea() {
        HBox content = new HBox();

        GridPane elementGrid = elementGrid(leftElements(), rightElements());

        content.setSpacing(100);
        content.getChildren().add(elementGrid);
        return content;
    }

    public HBox loadAccountsFirst() {
        HBox elements = new HBox();
        elements.setStyle("-fx-background-color: rgba(31,38,50,0.84);");
        Label test = new Label("Please load some tokens first.");
        elements.setAlignment(Pos.CENTER);
        elements.getChildren().add(test);
        return elements;
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

        AdvertisementOverview advertisementOverview = new AdvertisementOverview();
        AdvertisementStats advertisementStats = new AdvertisementStats();

        leftElements.getChildren().addAll(advertisementOverview.contentArea(), advertisementStats.contentArea());
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
