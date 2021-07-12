package xyz.vitox.discordtool.tab;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.tab.serverSpamComponents.*;

public class ServerSpam extends StackPane {

    public ServerSpam() {
        this.getChildren().addAll(contentArea(), loadAccountsFirst());
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
        leftElements.setSpacing(40);
        leftElements.setPrefWidth(750);

        JoinLeave joinLeave = new JoinLeave();
        WriteMessageChannel writeMessageChannel = new WriteMessageChannel();
        ReactEmoji reactEmoji = new ReactEmoji();

        leftElements.getChildren().addAll(joinLeave.contentArea(), writeMessageChannel.contentArea(), reactEmoji.contentArea());
        return leftElements;
    }

    private VBox rightElements() {
        VBox rightElements = new VBox();
        rightElements.setSpacing(40);
        rightElements.setPrefWidth(750);

        FriendRequest friendRequest = new FriendRequest();
        WriteMessageUser writeMessageUser = new WriteMessageUser();
//        JoinVoice joinVoice = new JoinVoice();

        rightElements.getChildren().addAll(friendRequest.contentArea(), writeMessageUser.contentArea());
        return rightElements;
    }

}
