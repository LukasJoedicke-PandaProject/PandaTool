package xyz.vitox.discordtool.tab;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.Main;
import xyz.vitox.discordtool.controller.DashboardController;
import xyz.vitox.discordtool.discordAPI.TokenLoader;
import xyz.vitox.discordtool.tab.homeComponents.TokenCard;
import xyz.vitox.discordtool.tab.homeComponents.TokenCheckerComponent;
import xyz.vitox.discordtool.util.FXUtil;
import xyz.vitox.discordtool.util.RequestAPI;
import xyz.vitox.discordtool.util.Validator;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class Home extends StackPane {

    public File tokenTxt;

    public Home() {
        this.getChildren().addAll(tokenCheckerComponent(), contentArea());
    }

    public VBox contentArea() {
        VBox content = new VBox();
        HBox initElements = new HBox();

        ProgressBar progressBar = progressBar();

        Button startButton = startButton(progressBar);
        TextField tokenInputField = tokenInput(startButton);
        Button selectButton = selectTokenBtn(tokenInputField);

        initElements.setAlignment(Pos.CENTER);
        initElements.setSpacing(10);
        initElements.getChildren().addAll(tokenInputField, selectButton, startButton);

        content.setAlignment(Pos.CENTER);
        content.setSpacing(10);
        content.getChildren().addAll(initElements, progressBar);

        try {
            Validator validator = new Validator();
            if (validator.validateKey(Validator.LICENSE_KEY)) {
                System.out.println("INFO: Panda v" + Main.getInstance().getVersion() + " started succesfully.");
            } else {
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * Initialize the "Token-Checker" which is on top of the Home Screen
     * @return HBox
     */
    public HBox tokenCheckerComponent() {
        TokenCheckerComponent tokenCheckerComponent = new TokenCheckerComponent();
        return tokenCheckerComponent.contentArea();
    }

    public ProgressBar progressBar() {
        ProgressBar progressBar = new ProgressBar(0.0);
        progressBar.setPrefWidth(375);
        return progressBar;
    }

    /**
     * Inputfield for the token path
     * Changes the color of the "Start" Button
     * @param startButton
     * @return Button
     */
    public TextField tokenInput(Button startButton) {
        TextField tokenInputField = new TextField();
        tokenInputField.setPrefWidth(200);

        if (Main.getSettings().readSettingString("tokenPath") != null) {
            tokenInputField.setText(Main.getSettings().readSettingString("tokenPath"));
            tokenTxt = new File(Main.getSettings().readSettingString("tokenPath"));
            startButton.setStyle(tokenTxt.exists() ? "-fx-background-color: -fx-positive;" : "-fx-background-color: -fx-card-color;");
        }

        tokenInputField.textProperty().addListener((observable, oldValue, newValue) -> {
            tokenTxt = new File(tokenInputField.getText());
            startButton.setStyle(tokenTxt.exists() ? "-fx-background-color: -fx-positive;" : "-fx-background-color: -fx-card-color;");
        });

        return tokenInputField;
    }

    /**
     * "Select Tokens" Button
     * Select the token.txt and set the path of the input field
     * @param tokenPath
     * @return Button
     */
    public Button selectTokenBtn(TextField tokenPath) {
        Button tokenBtn = new Button("Select Tokens");

        tokenBtn.setOnAction(event -> {
            tokenTxt = FXUtil.newFileChooser().showOpenDialog(Main.getInstance().getStage());
            if (tokenTxt != null) {
                tokenPath.setText(tokenTxt.getPath());
            }
        });

        return tokenBtn;
    }

    /**
     * "Start" Button
     * Handing over the token.txt file to the backend
     * @return Button
     */
    public Button startButton(ProgressBar progressBar) {
        Button startButton = new Button("Start");

        AtomicInteger clickCount = new AtomicInteger();

        startButton.setOnAction(event -> {
            if (clickCount.get() <= 0) {
                if (tokenTxt != null && tokenTxt.exists()) {
                    startButton.setText("Loading...");
                    startButton.setStyle("-fx-background-color: -fx-card-color;");
                    clickCount.getAndIncrement();

                    TokenLoader tokenLoader = new TokenLoader();
                    tokenLoader.loadRawTokens(tokenTxt, progressBar);
                }
            }
        });

        return startButton;
    }

    /**
     * Removing the current "Token-Selection" Screen
     * Add the new "Token-Overview" Screen
     */
    public void changeHomeView() {
        getChildren().remove(0, 1);

        getChildren().add(tokenCheckerComponent());

        TokenCard tokenCard = new TokenCard();
        getChildren().add(tokenCard.discordUserPane());
    }

    /**
     * Adding back the standard Homeview
     * Triggered by "unload" button
     */
    public void restoreHomeView() {
        getChildren().removeAll(this.getChildren());
        getChildren().add(tokenCheckerComponent());
        getChildren().add(contentArea());

        DashboardController.getVoiceSpam().restoreView();
        DashboardController.getTokenSettings().getChildren().add(DashboardController.getTokenSettings().loadAccountsFirst());
        DashboardController.getVoiceSpam().getChildren().add(DashboardController.getVoiceSpam().loadAccountsFirst());
        DashboardController.getServerSpam().getChildren().add(DashboardController.getServerSpam().loadAccountsFirst());
        DashboardController.getServerRecon().getChildren().add(DashboardController.getServerRecon().loadAccountsFirst());
        DashboardController.getVerifier().getChildren().add(DashboardController.getVerifier().loadAccountsFirst());
    }
}
