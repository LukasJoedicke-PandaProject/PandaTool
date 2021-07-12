package xyz.vitox.discordtool.tab.homeComponents;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import xyz.vitox.discordtool.Main;
import xyz.vitox.discordtool.discordAPI.tokenManager.Token;

public class UserInformation {

    public void showUserinformation(Token client) {
        VBox usernameBox = new VBox();
        Label informationUsername = new Label(client.getName() + "#" + client.getDiscriminator());
        Label usernameLabel = new Label("Username".toUpperCase());
        usernameLabel.setStyle("-fx-text-fill: #929292; -fx-font-weight: bold");

        Label informationEmail = new Label(client.getEmail());
        Label emailLabel = new Label("E-Mail".toUpperCase());
        emailLabel.setStyle("-fx-text-fill: #929292; -fx-font-weight: bold");
        emailLabel.setPadding(new Insets(8, 0, 0, 0));

        usernameBox.setSpacing(2);
        usernameBox.setAlignment(Pos.CENTER_LEFT);
        usernameBox.getChildren().addAll(usernameLabel, informationUsername, emailLabel, informationEmail);
        usernameBox.setPadding(new Insets(0, 0, 0, 10));

        VBox buttonBox = new VBox();
        buttonBox.setAlignment(Pos.TOP_RIGHT);
        Button editBtn = new Button("Edit");
        Button loginBtn = new Button("Login");

        editBtn.setStyle("-fx-background-color: #7289da; -fx-text-fill: white;");
        loginBtn.setStyle("-fx-background-color: #7289da; -fx-text-fill: white;");

//        loginBtn.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                LoginUtil.loginToDiscord(client);
//            }
//        });

        editBtn.setMinWidth(50);
        loginBtn.setMinWidth(50);
        buttonBox.setSpacing(10);
        buttonBox.setTranslateX(-8);
        buttonBox.setTranslateY(8);
        buttonBox.getChildren().addAll(editBtn, loginBtn);

        VBox extraInformationBox = new VBox();
        Label inforamtionPhoneNumber = new Label(client.getPhoneNumber());
        Label phoneNumberLabel = new Label("Phonenumber".toUpperCase());
        phoneNumberLabel.setStyle("-fx-text-fill: #929292; -fx-font-weight: bold");

        Label informationVerified = new Label(client.getVerified());
        Label verifiedLabel = new Label("Verified".toUpperCase());
        verifiedLabel.setStyle("-fx-text-fill: #929292; -fx-font-weight: bold");
        verifiedLabel.setPadding(new Insets(8, 0, 0, 0));

        extraInformationBox.setSpacing(2);
        extraInformationBox.setAlignment(Pos.CENTER_LEFT);
        extraInformationBox.getChildren().addAll(phoneNumberLabel, inforamtionPhoneNumber, verifiedLabel, informationVerified);
        extraInformationBox.setPadding(new Insets(0, 0, 0, 10));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox informationBox = new HBox();
        informationBox.setAlignment(Pos.CENTER_LEFT);
        informationBox.setPadding(new Insets(0, 0, 0, 10));

        Circle infoCircle = null;
        TokenCard tokenCard = new TokenCard();
        String infoAvatarPath;
        if (client.getAvatar().equals("null")) {
            infoCircle = tokenCard.defaultImage(40);
        } else {
            infoAvatarPath = "https://cdn.discordapp.com/avatars/" + client.getId() + "/" + client.getAvatar() + ".jpg?size=64";
            infoCircle = tokenCard.userImage(infoAvatarPath, 40);
        }

        informationBox.setSpacing(5);
        informationBox.getChildren().addAll(infoCircle, usernameBox, extraInformationBox, spacer, buttonBox);

        StackPane secondaryLayout = new StackPane();
        Scene informationScene = new Scene(secondaryLayout, 850, 400);
        Stage informationWindow = new Stage();
        informationBox.setMaxWidth(informationScene.getWidth() - 50);
        informationBox.setMaxHeight(120);

        secondaryLayout.getChildren().add(informationBox);
        informationScene.getStylesheets().add("xyz/vitox/discordtool/css/theme/standard/dark.css");
        informationScene.getStylesheets().add("xyz/vitox/discordtool/css/style.css");
        informationScene.getStylesheets().add("xyz/vitox/discordtool/css/theme/standard/main.css");
        informationBox.setStyle("-fx-background-color: -fx-card-color;");
        secondaryLayout.setStyle("-fx-background-color: -fx-bg-color;");
        informationWindow.setTitle("Informations for: " + client.getName() + "#" + client.getDiscriminator());
        informationWindow.setScene(informationScene);
        informationWindow.setX(Main.getInstance().getStage().getX() + 200);
        informationWindow.setY(Main.getInstance().getStage().getY() + 100);

        informationWindow.show();
    }


}
