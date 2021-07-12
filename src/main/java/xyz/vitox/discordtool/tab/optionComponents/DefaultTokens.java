package xyz.vitox.discordtool.tab.optionComponents;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.Main;
import xyz.vitox.discordtool.util.FXUtil;

import java.io.File;

public class DefaultTokens {

    public File tokenTxt;

    public VBox contentArea() {
        VBox content = new VBox();

        Label infoLabel = new Label("Default token list:");
        content.getChildren().addAll(infoLabel, elements());

        content.setSpacing(10);
        content.setPadding(new Insets(10, 10, 10, 10));
        content.setStyle("-fx-background-color: #202834");
        return content;
    }

    private HBox elements() {
        HBox elements = new HBox();

        TextField tokenPath = tokenPathField();
        Button selectTokenBtn = selectTokenBtn(tokenPath);

        elements.setSpacing(10);
        elements.getChildren().addAll(tokenPath, selectTokenBtn, saveButton(tokenPath));
        return elements;
    }

    private TextField tokenPathField() {
        TextField tokenPathField = new TextField();

        tokenPathField.setPromptText("Path to your tokens");
        tokenPathField.setPrefWidth(200);
        return tokenPathField;
    }

    private Button selectTokenBtn(TextField tokenPathField) {
        Button selectTokenBtn = new Button("Select tokens");

        selectTokenBtn.setOnAction(event -> {
            tokenTxt = FXUtil.newFileChooser().showOpenDialog(Main.getInstance().getStage());
            if (tokenTxt != null) {
                tokenPathField.setText(tokenTxt.getPath());
            }
            tokenPathField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (tokenPathField.getText().equals("")) {
                    selectTokenBtn.setText("Select Tokens");
                    tokenTxt = null;
                }
            });
        });

        return selectTokenBtn;
    }

    private Button saveButton(TextField tokenPathField) {
        Button saveButton = new Button("Save");

        saveButton.setStyle("-fx-background-color: -fx-positive;");

        saveButton.setOnAction(event -> {
            if (tokenPathField.getText().isEmpty()) {
                Main.getSettings().writeSettingString("tokenPath", "");
            } else {
                tokenTxt = new File(tokenPathField.getText());

                if (tokenTxt != null && tokenTxt.exists()) {
                    Main.getSettings().writeSettingString("tokenPath", tokenTxt.getAbsolutePath());
                }
            }

            saveButton.setText("Saved!");
        });

        return saveButton;
    }

}
