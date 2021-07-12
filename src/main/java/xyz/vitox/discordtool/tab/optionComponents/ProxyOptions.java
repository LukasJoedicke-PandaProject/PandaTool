package xyz.vitox.discordtool.tab.optionComponents;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.Main;
import xyz.vitox.discordtool.discordAPI.api.gateway.DiscordMassPing;
import xyz.vitox.discordtool.util.FXUtil;
import xyz.vitox.discordtool.util.SystemUtil;

import java.io.File;
import java.util.ArrayList;

public class ProxyOptions {

    public File proxyTxt;
    public static boolean isProxyLoaded;
    public static ArrayList<String> proxyList = new ArrayList<>();
    public static String selectedProxyType = "HTTP";

    public VBox contentArea() {
        VBox content = new VBox();

        Label infoLabel = new Label("Choose proxies:");
        content.getChildren().addAll(infoLabel, elements());

        content.setSpacing(10);
        content.setPadding(new Insets(10, 10, 10, 10));
        content.setStyle("-fx-background-color: #202834");
        return content;
    }

    private HBox elements() {
        HBox elements = new HBox();

        Label proxyCount = new Label();
        proxyCount.setTranslateY(4);
        TextField proxyPath = proxyPathField();
        Button selectTokenBtn = selectProxyBtn(proxyPath);
        ComboBox<String> proxyType = proxyType();

        elements.setSpacing(10);
        elements.getChildren().addAll(proxyPath, selectTokenBtn, proxyType, saveButton(proxyPath, proxyCount), proxyCount);
        return elements;
    }

    private TextField proxyPathField() {
        TextField proxyPathField = new TextField();

        if (!Main.getSettings().readSettingString("proxyPath").isEmpty()) {
            proxyPathField.setText(Main.getSettings().readSettingString("proxyPath"));
        }

        proxyPathField.setPromptText("Path to your proxies");
        proxyPathField.setPrefWidth(200);
        return proxyPathField;
    }

    private Button selectProxyBtn(TextField tokenPathField) {
        Button selectProxyBtn = new Button("Select proxies");

        selectProxyBtn.setOnAction(event -> {
            proxyTxt = FXUtil.newFileChooser().showOpenDialog(Main.getInstance().getStage());
            if (proxyTxt != null) {
                tokenPathField.setText(proxyTxt.getPath());
            }
            tokenPathField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (tokenPathField.getText().equals("")) {
                    selectProxyBtn.setText("Select proxies");
                    proxyTxt = null;
                }
            });
        });

        return selectProxyBtn;
    }

    private ComboBox<String> proxyType() {
        ComboBox<String> proxyType = new ComboBox<>();
        proxyType.getItems().add("HTTP");
        proxyType.getItems().add("SOCKS");
        proxyType.getSelectionModel().selectFirst();

        proxyType.setOnAction(event -> {
            selectedProxyType = proxyType.getValue();
        });
        return proxyType;
    }

    private Button saveButton(TextField tokenPathField, Label proxyCount) {
        Button saveButton = new Button("Load");

        saveButton.setStyle("-fx-background-color: -fx-positive;");

        saveButton.setOnAction(event -> {
            if (tokenPathField.getText().isEmpty()) {
                Main.getSettings().writeSettingString("proxyPath", "");
            } else {

                if (isProxyLoaded) {
                    saveButton.setText("Load");
                    saveButton.setStyle("-fx-background-color: -fx-positive;");
                    proxyList.clear();
                    proxyCount.setText("");
                } else {
                    saveButton.setText("Unload");
                    saveButton.setStyle("-fx-background-color: -fx-negative;");

                    proxyTxt = new File(tokenPathField.getText());

                    if (proxyTxt != null && proxyTxt.exists() && proxyTxt.getName().endsWith(".txt")) {
                        proxyList = SystemUtil.contentFromFileToArraylist(proxyTxt);
                        if (isProxy(proxyList.get(0))) {
                            proxyCount.setText("(" + proxyList.size() + ")");
                            Main.getSettings().writeSettingString("proxyPath", proxyTxt.getAbsolutePath());
                        } else {
                            proxyList.clear();
                        }
                    }
                }
            }

            isProxyLoaded = !isProxyLoaded;
        });

        return saveButton;
    }

    private boolean isProxy(String proxy) {
        try {
            String[] proxyInfo;
            proxyInfo = proxy.split(":");
            return proxyInfo[1] != null;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Not a proxy file.");
            return false;
        }
    }

}
