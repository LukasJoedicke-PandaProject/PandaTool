package xyz.vitox.discordtool.tab.tokenSettingsComponents;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.Main;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;
import xyz.vitox.discordtool.util.FXUtil;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class ChangeProfilePicture {

    private Button changeButton;
    private DiscordMultiRequests multiRequests = new DiscordMultiRequests();
    public static ArrayList<File> picturesToChange = new ArrayList<>();

    public VBox contentArea() {
        VBox content = new VBox();

        HBox pictureCollection = pictureCollection();
        Button fileChooseBtn = fileChooseButton(pictureCollection);

        content.getChildren().addAll(headingLabel(), fileChooseBtn, pictureCollection, buttons());

        content.setSpacing(10);
        content.setPadding(new Insets(10, 10, 10, 10));
        content.setStyle("-fx-background-color: -fx-card-color");
        return content;
    }

    private Label headingLabel() {
        Label heading = new Label("Change profile picture: ");

        return heading;
    }

    private Button fileChooseButton(HBox pictureCollection) {
        Button fileChooseBtn = new Button("Choose");

        fileChooseBtn.setOnAction(e -> {
            List<File> file = FXUtil.newFileChooser().showOpenMultipleDialog(Main.getInstance().getStage());

            if (file != null) {
                for (File value : file) {
                    ImageView pictures = new ImageView();
                    Button removeButton = new Button("x");
                    removeButton.setStyle("-fx-background-color: -fx-negative;");

                    try {
                        pictures.setImage(new Image(value.toURI().toURL().toExternalForm()));
                        pictures.setFitWidth(75);
                        pictures.setFitHeight(75);

                        removeButton.setTranslateX(-20);
                        removeButton.setTranslateY(3);
                        pictureCollection.getChildren().add(pictures);
                        pictureCollection.getChildren().add(removeButton);

                        removeButton.setOnAction(event -> {
                            pictureCollection.getChildren().remove(pictures);
                            pictureCollection.getChildren().remove(removeButton);
                            picturesToChange.remove(value);
                            if (picturesToChange.size() <= 0) {
                                changeButton.setStyle("-fx-background-color: -fx-card-color;");
                            }
                        });

                        changeButton.setStyle("-fx-background-color: -fx-positive;");

                        picturesToChange.add(value);
                    } catch (MalformedURLException malformedURLException) {
                        malformedURLException.printStackTrace();
                    }
                }
            }
        });

        return fileChooseBtn;
    }

    private HBox pictureCollection() {
        HBox pictures = new HBox();

        return pictures;
    }

    private HBox buttons() {
        HBox buttons = new HBox();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttons.setSpacing(10);
        buttons.getChildren().addAll(changeButton(), spacer, removeButton());
        return buttons;
    }

    private Button changeButton() {
        changeButton = new Button("Change");

        changeButton.setOnAction(event -> {
            if (picturesToChange.size() > 0) {
                multiRequests.changeProfilePictures(picturesToChange.toArray(new File[0])).start();
            }
        });

        return changeButton;
    }

    private Button removeButton() {
        Button removeBtn = new Button("Remove");
        removeBtn.setStyle("-fx-background-color: -fx-negative;");
        removeBtn.setOnAction(event -> multiRequests.changeProfilePictures(null).start());

        return removeBtn;
    }

}
