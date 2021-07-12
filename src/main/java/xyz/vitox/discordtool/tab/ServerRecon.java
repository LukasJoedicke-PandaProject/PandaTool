package xyz.vitox.discordtool.tab;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import xyz.vitox.discordtool.discordAPI.TokenLoader;
import xyz.vitox.discordtool.discordAPI.api.gateway.DiscordServerParser;
import xyz.vitox.discordtool.tab.serverRecon.PingableRole;
import xyz.vitox.discordtool.tab.serverRecon.ServerEmoji;
import xyz.vitox.discordtool.tab.serverSpamComponents.ReactEmoji;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Random;

public class ServerRecon extends StackPane {

    public ServerRecon() {
        this.getChildren().addAll(reconLayout(), loadAccountsFirst());
    }

    public static ServerRecon instance = new ServerRecon();

    public static Label staticServerName, staticServerRegion, staticServerVerificationLevel;
    public static Circle staticServerIcon;

    public HBox loadAccountsFirst() {

        HBox elements = new HBox();
        elements.setStyle("-fx-background-color: rgba(31,38,50,0.84);");
        Label test = new Label("Please load some tokens first.");
        elements.setAlignment(Pos.CENTER);
        elements.getChildren().add(test);
        return elements;

    }

    public HBox reconLayout() {
        HBox layout = new HBox();

        VBox elements = new VBox();

        layout.setSpacing(10);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setStyle("-fx-background-color: rgba(31,38,50,0.36)");
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(elements);
        Separator separator = new Separator();
        elements.getChildren().addAll(inputElements(), separator, serverInformations(), addResultElements());
        elements.setSpacing(50);

        return layout;
    }

    public HBox inputElements() {
        HBox elements = new HBox();

        TextField serverIDInput = new TextField();
        serverIDInput.setPromptText("Server ID");
        serverIDInput.setFocusTraversable(false);
        Button startBtn = new Button("Start");

        serverIDInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (serverIDInput.getText().length() > 0) {
                startBtn.setStyle("-fx-background-color: -fx-positive;");
            } else {
                startBtn.setStyle("-fx-background-color: -fx-card-color;");
            }
        });

        startBtn.setOnAction(event -> DiscordServerParser.instance.startGuildParser(TokenLoader.discordTokens.toArray(new String[0]), serverIDInput.getText()));

        serverIDInput.setPrefWidth(1100);
        startBtn.setPrefWidth(150);

        elements.setSpacing(10);
        elements.setAlignment(Pos.CENTER);
        elements.getChildren().addAll(serverIDInput, startBtn);
        return elements;
    }

    public HBox serverInformations() {
        HBox elements = new HBox();

        Circle serverIcon = defaultImage(35);

        VBox infoElements = new VBox();
        Label serverName = new Label("Server Name:");
        Label serverRegion = new Label("Server Region:");
        Label verificationLevel = new Label("Verification Level:");

        staticServerName = serverName;
        staticServerRegion = serverRegion;
        staticServerVerificationLevel = verificationLevel;
        staticServerIcon = serverIcon;

        elements.setPadding(new Insets(10, 10, 10, 10));
        elements.setStyle("-fx-background-color: #202834");
        infoElements.getChildren().addAll(serverName, serverRegion, verificationLevel);
        infoElements.setSpacing(10);

        elements.setSpacing(15);
        elements.getChildren().addAll(serverIcon, infoElements);

        return elements;
    }

    public HBox addResultElements() {
        HBox elements = new HBox();
        VBox elementsLeft = new VBox();
        elementsLeft.setSpacing(40);
        elementsLeft.setPrefWidth(650);
        elementsLeft.getChildren().addAll(pingableRoles());

        VBox elementsRight = new VBox();
        elementsRight.setSpacing(40);
        elementsRight.setPrefWidth(650);
        elementsRight.getChildren().addAll(serverEmojis());

        GridPane gridPane = new GridPane();
        gridPane.setHgap(50);
        gridPane.add(elementsLeft, 0, 1);
        gridPane.add(elementsRight, 1, 1);

        elements.setSpacing(100);
        elements.getChildren().addAll(gridPane);
        return elements;
    }

    public static ObservableList<PingableRole> pingableRolesList = FXCollections.observableArrayList();

    public VBox pingableRoles() {
        VBox elements = new VBox();
        Label nameLabel = new Label("Pingable Roles");
        nameLabel.setFont(new Font(13));
        nameLabel.setMaxWidth(1500);
        nameLabel.setAlignment(Pos.CENTER);
        TableView<PingableRole> pingableRoleTable = new TableView<>();
        roleContextmenu(pingableRoleTable);
        pingableRoleTable.setStyle("-fx-background-color: #202834");
        TableColumn<PingableRole, String> roleName = new TableColumn<>("Name");
        TableColumn<PingableRole, String> roleID = new TableColumn<>("ID");

        roleName.setPrefWidth(300);
        roleID.setPrefWidth(300);

        roleName.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        roleID.setCellValueFactory(new PropertyValueFactory<>("roleID"));

        pingableRoleTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pingableRoleTable.setItems(pingableRolesList);
        pingableRoleTable.setPlaceholder(new Label(""));
        pingableRoleTable.getColumns().addAll(roleName, roleID);
        elements.setSpacing(10);
        elements.getChildren().addAll(nameLabel, pingableRoleTable);

        return elements;
    }

    public static ObservableList<ServerEmoji> emojiList = FXCollections.observableArrayList();

    public VBox serverEmojis() {
        VBox elements = new VBox();
        Label nameLabel = new Label("Server Emojis");
        nameLabel.setFont(new Font(13));
        nameLabel.setMaxWidth(1500);
        nameLabel.setAlignment(Pos.CENTER);
        TableView<ServerEmoji> emojiTable = new TableView<>();
        emojiContextMenu(emojiTable);
        emojiTable.setStyle("-fx-background-color: #202834");
        TableColumn<ServerEmoji, String> roleName = new TableColumn<>("Name");
        TableColumn<ServerEmoji, String> roleID = new TableColumn<>("ID");

        roleName.setPrefWidth(300);
        roleID.setPrefWidth(300);

        roleName.setCellValueFactory(new PropertyValueFactory<>("emojiName"));
        roleID.setCellValueFactory(new PropertyValueFactory<>("emojiID"));

        emojiTable.setItems(emojiList);
        emojiTable.setPlaceholder(new Label(""));
        emojiTable.getColumns().addAll(roleName, roleID);
        elements.setSpacing(10);
        elements.getChildren().addAll(nameLabel, emojiTable);

        return elements;
    }
    private void emojiContextMenu(TableView<ServerEmoji> table) {
        ContextMenu menu = new ContextMenu();
        MenuItem copyIDItem = new MenuItem("Copy ID");
        MenuItem copyIntoreaction = new MenuItem("Copy Name + ID into 'React with emoji' ");

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        copyIDItem.setOnAction(event -> {
            ServerEmoji serverEmoji = table.getSelectionModel().getSelectedItem();
            StringSelection stringSelection = new StringSelection(serverEmoji.emojiID);
            clipboard.setContents(stringSelection, null);
        });

        copyIntoreaction.setOnAction(event -> {
            ServerEmoji serverEmoji = table.getSelectionModel().getSelectedItem();
            ReactEmoji.reactionEmoji.setText(serverEmoji.emojiName);
            ReactEmoji.emojiID.setText(serverEmoji.emojiID);
        });

        menu.getItems().addAll(copyIntoreaction, copyIDItem);
        table.setContextMenu(menu);
    }

    private void roleContextmenu(TableView<PingableRole> table) {
        ContextMenu menu = new ContextMenu();
        MenuItem copyIDItem = new MenuItem("Copy Mentionable ID(s)");
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        copyIDItem.setOnAction(event -> {
            ObservableList<PingableRole> selectedItems = table.getSelectionModel().getSelectedItems();

            StringBuilder allIdsAsString = new StringBuilder();

            for (PingableRole roleIds : selectedItems) {
                allIdsAsString.append("<@&" + roleIds.roleID + ">");
            }

            StringSelection stringSelection = new StringSelection(allIdsAsString.toString());
            clipboard.setContents(stringSelection, null);
        });

        menu.getItems().add(copyIDItem);
        table.setContextMenu(menu);
    }

    public Circle defaultImage(int radius) {
        String[] defaultAvatars = {"xyz/vitox/discordtool/data/defaultGreen.png", "xyz/vitox/discordtool/data/defaultGray.png", "xyz/vitox/discordtool/data/defaultYellow.png", "xyz/vitox/discordtool/data/defaultBlue.png", "xyz/vitox/discordtool/data/defaultRed.png"};
        Circle circle = new Circle(radius);
        int idx = new Random().nextInt(defaultAvatars.length);
        Image image = new Image(defaultAvatars[idx]);
        circle.setStroke(Color.SEAGREEN);
        circle.setFill(new ImagePattern(image));
        return circle;
    }

}
