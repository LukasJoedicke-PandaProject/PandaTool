package xyz.vitox.discordtool;

import javafx.scene.image.Image;
import org.opencv.core.Core;
import xyz.vitox.discordtool.saving.Settings;
import xyz.vitox.discordtool.util.FXUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.*;
import xyz.vitox.discordtool.util.Validator;

import java.io.File;

@Getter
public class Main extends Application {

    private final String name = "Panda", version = "2.5";

    public static String MAIN_FILE_PATH = "./PandaAssets/";

    @Getter
    private static Main instance;

    private Stage stage;

    @Setter
    private Scene scene;

    private static Settings settings = new Settings();

    public void start(Stage stage) {
        try {
            instance = this;
            this.stage = stage;

            Parent root = FXMLLoader.load(getClass().getResource("/xyz/vitox/discordtool/fxml/dashboard.fxml"));
            scene = new Scene(root);
            scene.setRoot(root);
            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);

            stage.initStyle(StageStyle.TRANSPARENT);
            FXUtil.resizable(stage);

            stage.setWidth(1600);
            stage.setHeight(800);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            Validator.correctArgument(args);
            loadLibraries();
            getSettings().createSettings();
            launch(args);
        } catch (Exception e) {
            System.exit(0);
        }
    }

    public static void loadLibraries() {
        File libraryDir = new File(MAIN_FILE_PATH + "/libraries/data");
        if (libraryDir.exists()) {
            System.loadLibrary(MAIN_FILE_PATH + "/libraries/opencv/" + Core.NATIVE_LIBRARY_NAME);
        }
    }

    public static Settings getSettings() {
        return settings;
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.exit(0);
    }

}
