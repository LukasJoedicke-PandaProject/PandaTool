package xyz.vitox.discordtool.controller;

import xyz.vitox.discordtool.Main;
import xyz.vitox.discordtool.tab.*;
import xyz.vitox.discordtool.util.FXUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private BorderPane root;

    @FXML
    private StackPane stckTopBar;

    @FXML
    private VBox vbxMenuNavigation, vbxMenuTabs;

    @FXML
    private Label lblVersion;

    public static Pane[] tabs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblVersion.setText(Main.getInstance().getVersion());
        FXUtil.movable(Main.getInstance().getStage(), stckTopBar);

        tabs = new Pane[]{
                new Home(),
                new TokenSettings(),
                new ServerSpam(),
                new VoiceSpam(),
                new ServerRecon(),
                new Verifier(),
                new Advertiser(),
                new Options()};

        vbxMenuTabs.getChildren().add(tabs[0]);

        for (int i = 0; i < tabs.length; ++i) {
            VBox.setVgrow(tabs[i], Priority.ALWAYS);
            FXUtil.tabSwitch(vbxMenuNavigation.getChildren().get(i), tabs[i], vbxMenuNavigation, vbxMenuTabs);
        }
    }

    public static Home getHome() {
        return (Home) tabs[0];
    }

    public static TokenSettings getTokenSettings() {
        return (TokenSettings) tabs[1];
    }

    public static ServerSpam getServerSpam() {
        return (ServerSpam) tabs[2];
    }

    public static VoiceSpam getVoiceSpam() {
        return (VoiceSpam) tabs[3];
    }

    public static ServerRecon getServerRecon() {
        return (ServerRecon) tabs[4];
    }

    public static Verifier getVerifier() {
        return (Verifier) tabs[5];
    }

    public static Advertiser getAdvertiser() {
        return (Advertiser) tabs[6];
    }

    public static Options getOptions() {
        return (Options) tabs[7];
    }

}
