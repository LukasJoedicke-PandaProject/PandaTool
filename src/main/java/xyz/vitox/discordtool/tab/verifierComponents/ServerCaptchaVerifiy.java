package xyz.vitox.discordtool.tab.verifierComponents;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import xyz.vitox.discordtool.Main;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;
import xyz.vitox.discordtool.discordAPI.api.gateway.DiscordUserGateway;
import xyz.vitox.discordtool.discordAPI.tokenManager.Token;
import xyz.vitox.discordtool.discordAPI.tokenManager.TokenManager;
import xyz.vitox.discordtool.util.InfoButton;
import xyz.vitox.discordtool.util.SystemUtil;

import java.io.File;
import java.util.ArrayList;

public class ServerCaptchaVerifiy {

    private DiscordMultiRequests multiRequests = new DiscordMultiRequests();

    private Thread captchaConnectionThread;

    private File pandaLibrariesDir = new File(Main.MAIN_FILE_PATH + "/libraries/data");
    public static File captchaDir = new File(Main.MAIN_FILE_PATH + "/captcha");
    public static Label infoLabel, connectedCount, solvedCaptchas, failedCaptchas, lastServerJoinedName, successfullyVerifiedBotsOnGuild;
    public static String lastServerJoinedInvite, lastServerJoinedID;
    public static boolean startButtonPressed;
    public static int solvedCaptchaCount, failedCaptchaCount, verifiedTokensOnGuild;
    public static ArrayList<Token> failedTokensLastServer = new ArrayList<>();

    public VBox contentArea() {
        VBox content = new VBox();

        ProgressBar downloadProgressbar = new ProgressBar();
        downloadProgressbar.setVisible(false);
        content.getChildren().addAll(topElements(), bottomElements(downloadProgressbar), downloadProgressBox(downloadProgressbar));

        removeProgressBox(content);
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
        elements.getChildren().addAll(headingLabel(), spacer, infoButton.content("https://www.youtube.com/watch?v=0wBKk2-Hdrs"));
        return elements;
    }

    public void removeProgressBox(VBox content) {
        if (pandaLibrariesDir.exists()) {
            content.getChildren().remove(2);
        }
    }

    public HBox bottomElements(ProgressBar downloadProgressbar) {
        HBox bottomElements = new HBox();
        if (!pandaLibrariesDir.exists()) {
            VBox vBoxElements = new VBox();

            infoLabel = new Label("To use the Verifier for the \"Server Captcha Bot#3928\" you need to\ndownload additional libraries. (~100mb)\n\n" +
                    "While downloading, don't close Panda and wait for the \"Success\" message.");
            infoLabel.setStyle("-fx-text-fill: #b71515");
            Button downloadButton = downloadBtn(downloadProgressbar);

            vBoxElements.getChildren().addAll(infoLabel, downloadButton);
            vBoxElements.setSpacing(10);
            bottomElements.getChildren().addAll(vBoxElements);
        } else {
            captchaDir.mkdir();

            VBox rightElements = new VBox();
            VBox leftElements = new VBox();

            connectedCount = new Label("Bots Connected: 0/" + TokenManager.tokensToUse().size());
            solvedCaptchas = new Label("Solved Captchas: 0");
            failedCaptchas = new Label("Failed Captchas: 0");

            lastServerJoinedName = new Label("Last guild joined: Unknown");
            successfullyVerifiedBotsOnGuild = new Label("Successfully verified bots on this guild: 0");

            leftElements.getChildren().addAll(startButton(), connectedCount, solvedCaptchas, failedCaptchas);
            leftElements.setSpacing(10);

            rightElements.getChildren().addAll(reconnectButton(), lastServerJoinedName, successfullyVerifiedBotsOnGuild);
            rightElements.setSpacing(10);

            GridPane elements = elementGrid(leftElements, rightElements);
            bottomElements.getChildren().addAll(elements);
        }

        return bottomElements;
    }

    private Button reconnectButton() {
        Button reconnectBtn = new Button("Reconnect failed tokens");

        reconnectBtn.setStyle("-fx-background-color: -fx-positive;");

        reconnectBtn.setOnAction(event -> {
            reconnectBtn.setText("Reconnecting...");
            reconnectBtn.setStyle("-fx-background-color: -fx-component-color;");
            multiRequests.reconnectServer(lastServerJoinedInvite, lastServerJoinedID, reconnectBtn, 0, failedTokensLastServer).start();
        });

        return reconnectBtn;
    }

    private GridPane elementGrid(VBox leftElements, VBox rightElements) {
        GridPane elementGrid = new GridPane();
        elementGrid.setHgap(150);

        elementGrid.add(leftElements, 0, 1);
        elementGrid.add(rightElements, 1, 1);

        return elementGrid;
    }

    private Button startButton() {
        Button startButton = new Button("Enable");
        startButton.setStyle("-fx-background-color: -fx-positive;");

        startButton.setOnAction(event -> {
            if (startButtonPressed) {
                startButton.setText("Enable");
                startButton.setStyle("-fx-background-color: -fx-positive;");

                connectedCount.setText("Bots Connected: 0/" + TokenManager.tokensToUse().size());
                DiscordUserGateway.gatewayConnections = 0;
                captchaConnectionThread.stop();
                DiscordUserGateway.closeGateways(0);
            } else {
                startButton.setText("Connecting...");
                startButton.setStyle("-fx-background-color: -fx-component-color");
                captchaConnectionThread = multiRequests.connectGateway(startButton);
                captchaConnectionThread.start();
            }
            startButtonPressed = !startButtonPressed;
        });

        return startButton;
    }

    public HBox downloadProgressBox(ProgressBar progressBar) {
        HBox progressBox = new HBox();

        progressBar.setPrefWidth(750);

        progressBox.getChildren().add(progressBar);
        return progressBox;
    }

    private Button downloadBtn(ProgressBar progressBar) {
        Button downloadButton = new Button("Download libraries");

        downloadButton.setOnAction(event -> {
            downloadButton.setVisible(false);
            progressBar.setVisible(true);
            pandaLibrariesDir.mkdirs();
            SystemUtil.downloadZipFile("https://localhost.com/files/panda_libraries.zip", Main.MAIN_FILE_PATH + "/libraries/libraries.zip", progressBar);
        });

        downloadButton.setStyle("-fx-background-color: -fx-positive");
        return downloadButton;
    }

    public Label headingLabel() {
        Label headingLabel = new Label("Server-Captcha Verifier");
        return headingLabel;
    }

}
