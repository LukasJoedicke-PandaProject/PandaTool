package xyz.vitox.discordtool.tab.optionComponents;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.Main;

public class UseVerifiedTokens {

    public VBox contentArea() {
        VBox content = new VBox();

        content.getChildren().addAll(useValidTokens());

        content.setSpacing(10);
        content.setPadding(new Insets(10, 10, 10, 10));
        content.setStyle("-fx-background-color: #202834");
        return content;
    }

    public CheckBox useValidTokens() {
        CheckBox checkBox = new CheckBox("Only use verified tokens");

        checkBox.setOnAction(event -> {
            Main.getSettings().writeSettingBoolean("onlyValidTokens", checkBox.isSelected());
        });

        checkBox.setSelected(Main.getSettings().readSettingBoolean("onlyValidTokens"));

        return checkBox;
    }

}
