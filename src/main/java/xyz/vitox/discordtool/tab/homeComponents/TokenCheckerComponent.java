package xyz.vitox.discordtool.tab.homeComponents;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import xyz.vitox.discordtool.Main;
import xyz.vitox.discordtool.controller.DashboardController;
import xyz.vitox.discordtool.discordAPI.TokenLoader;
import xyz.vitox.discordtool.discordAPI.tokenManager.TokenManager;
import xyz.vitox.discordtool.util.FXUtil;
import xyz.vitox.discordtool.util.SystemUtil;

import java.io.File;


public class TokenCheckerComponent {

    public HBox contentArea() {
        HBox content = new HBox();

        content.setTranslateY(-28);
        content.setSpacing(10);
        content.setAlignment(Pos.TOP_CENTER);
        content.getChildren().addAll(unloadButton(), verifiedTokensLabel(), unverifiedTokensLabel(), invalidTokensLabel(), exportButton());
        return content;
    }

    public Label verifiedTokensLabel() {
        Label verifiedTokensLabel = new Label("Verified Tokens: " + TokenManager.verifiedTokens.size());
        verifiedTokensLabel.setStyle("-fx-text-fill: -fx-positive;");

        return verifiedTokensLabel;
    }

    public static Label unverifiedTokensLabel() {
        Label unverifiedTokensLabel = new Label("Unverified Tokens: " + TokenManager.unverifiedTokens.size());
        unverifiedTokensLabel.setStyle("-fx-text-fill: #a1793a;");

        return unverifiedTokensLabel;
    }

    public Label invalidTokensLabel() {
        Label invalidTokensLabel = new Label("Invalid Tokens: " + TokenManager.invalidTokens.size());
        invalidTokensLabel.setStyle("-fx-text-fill: -fx-negative;");

        return invalidTokensLabel;
    }

    public Button unloadButton() {
        Button unloadButton = new Button("Unload");
        unloadButton.setTranslateY(-4);

        unloadButton.setOnAction(event -> {
            TokenManager.tokenList.clear();
            TokenManager.invalidTokens.clear();
            TokenManager.unverifiedTokens.clear();
            TokenManager.verifiedTokens.clear();
            TokenLoader.discordTokens.clear();
            TokenManager.tokensToUse().clear();

            DashboardController.getHome().restoreHomeView();
        });

        return unloadButton;
    }

    public Button exportButton() {
        Button export = new Button("Export");
        export.setTranslateY(-4);

        export.setOnAction(event -> {
            File exportDir = FXUtil.newDirectoryChooser().showDialog(Main.getInstance().getStage());
            if (exportDir != null) {
                File verifiedTxt = new File(exportDir + "/verified.txt");
                File unverifiedTxt = new File(exportDir + "/unverified.txt");
                File invalidTxt = new File(exportDir + "/invalid.txt");

                SystemUtil.writeFileToken(verifiedTxt, TokenManager.verifiedTokens);
                SystemUtil.writeFileToken(unverifiedTxt, TokenManager.unverifiedTokens);
                SystemUtil.writeFile(invalidTxt, TokenManager.invalidTokens);
            }
        });

        return export;
    }

}
