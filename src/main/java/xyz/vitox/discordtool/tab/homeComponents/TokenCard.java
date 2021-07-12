package xyz.vitox.discordtool.tab.homeComponents;

import javafx.beans.binding.Bindings;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import xyz.vitox.discordtool.Main;
import xyz.vitox.discordtool.discordAPI.tokenManager.Token;
import xyz.vitox.discordtool.discordAPI.tokenManager.TokenManager;
import xyz.vitox.discordtool.tab.homeComponents.selenium.ChromeUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public class TokenCard {

    public ScrollPane discordUserPane() {
        FlowPane elements = new FlowPane();

        for (Token tokens : TokenManager.tokenList) {
            addUserCardElement(tokens, elements);
        }

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPannable(true);
        elements.prefWidthProperty().bind(Bindings.add(-5, scrollPane.widthProperty()));
        elements.prefHeightProperty().bind(Bindings.add(-5, scrollPane.heightProperty()));
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(elements);

        return scrollPane;
    }

    public void addUserCardElement(Token token, FlowPane elements) {

        VBox texts = new VBox();
        Label discordUsername = new Label(token.getName());
        texts.setAlignment(Pos.CENTER);
        texts.getChildren().addAll(discordUsername);

        discordUsername.setPadding(new Insets(0, 0, 0, 10));
        HBox hbox = new HBox();
        hbox.setStyle("-fx-background-color: -fx-card-color;");
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(0, 0, 0, 10));

        Circle circle = null;
        String avatarPath;

        if (Main.getSettings().readSettingBoolean("loadProfilePictures")) {
            if (token.getAvatar().equals("null")) {
                circle = defaultImage(25);
            } else {
                avatarPath = "https://cdn.discordapp.com/avatars/" + token.getId() + "/" + token.getAvatar() + ".jpg?size=64";
                circle = userImage(avatarPath, 25);
            }
        } else {
            circle = defaultImage(25);
        }

        hbox.getChildren().addAll(circle, texts);

        hbox.setMinWidth(200);
        hbox.setMinHeight(75);
        elements.setVgap(10);
        elements.setHgap(10);
        elements.setStyle("-fx-background-color: -fx-bg-color;");
        elements.getChildren().addAll(hbox);

        MenuItem getInformations = new MenuItem("Get Information");
        getInformations.setOnAction(e -> {
            UserInformation userInformation = new UserInformation();
            userInformation.showUserinformation(token);
        });

        MenuItem loginToAccount = new MenuItem("Login to Account");
        loginToAccount.setOnAction(e -> {
            ChromeUtil chromeUtil = new ChromeUtil();
            chromeUtil.checkIfChromeDriverExists(token);
        });

        ContextMenu menu = new ContextMenu(getInformations, loginToAccount);
        hbox.setOnContextMenuRequested(e -> {
            menu.show(hbox.getScene().getWindow(), e.getScreenX(), e.getScreenY());
        });
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

    public Circle userImage(String imgUrl, int radius) {
        Circle circle = null;
        try {
            URL url = new URL(imgUrl);
            URLConnection openConnection = url.openConnection();
            boolean check = true;

            openConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            openConnection.connect();
            if (openConnection.getContentLength() > 8000000) {
                System.out.println(" file size is too big.");
                check = false;
            }
            if (check) {
                BufferedImage img = null;
                InputStream in = new BufferedInputStream(openConnection.getInputStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[2048];
                int n;
                while (-1 != (n = in.read(buf))) {
                    out.write(buf, 0, n);
                }
                out.close();
                in.close();
                byte[] response = out.toByteArray();
                img = ImageIO.read(new ByteArrayInputStream(response));
                Image image = SwingFXUtils.toFXImage(img, null);
                circle = new Circle(radius);
                circle.setStroke(Color.SEAGREEN);
                circle.setFill(new ImagePattern(image));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return circle;
    }

}
