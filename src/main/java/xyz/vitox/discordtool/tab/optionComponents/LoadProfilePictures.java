package xyz.vitox.discordtool.tab.optionComponents;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.Main;

public class LoadProfilePictures {

    public VBox contentArea() {
        VBox content = new VBox();

        content.getChildren().addAll(loadPP());

        content.setSpacing(10);
        content.setPadding(new Insets(10, 10, 10, 10));
        content.setStyle("-fx-background-color: #202834");
        return content;
    }

    public CheckBox loadPP() {
        CheckBox checkBox = new CheckBox("Load profile pictures in Dashboard");

        checkBox.setOnAction(event -> {
            Main.getSettings().writeSettingBoolean("loadProfilePictures", checkBox.isSelected());
        });

        checkBox.setSelected(Main.getSettings().readSettingBoolean("loadProfilePictures"));

        return checkBox;
    }

}
