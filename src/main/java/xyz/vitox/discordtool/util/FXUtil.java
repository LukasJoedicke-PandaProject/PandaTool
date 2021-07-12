package xyz.vitox.discordtool.util;

import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static javafx.scene.input.MouseEvent.*;

public class FXUtil {

    private static int winWidth = 1240, winHeight = 704;

    public static void movable(Stage stage, Pane pane) {
        AtomicReference<Double> xOffset = new AtomicReference<>(0D);
        AtomicReference<Double> yOffset = new AtomicReference<>(0D);

        pane.setOnMousePressed(e -> {
            xOffset.set(e.getSceneX());
            yOffset.set(e.getSceneY());
        });

        pane.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - xOffset.get());
            stage.setY(e.getScreenY() - yOffset.get());
        });
    }

    public static void windowActions(Stage stage, Pane min, Pane close) {
        min.setOnMouseClicked(e -> stage.setIconified(true));
        close.setOnMouseClicked(e -> System.exit(0));
    }

    public static void resizable(Stage stage) {
        ResizeListener resizeListener = new ResizeListener(stage, winWidth, winHeight);
        Scene scene = stage.getScene();

        EventType[] mouseEvents = new EventType[]{MOUSE_MOVED, MOUSE_PRESSED, MOUSE_DRAGGED, MOUSE_EXITED, MOUSE_EXITED_TARGET};
        Arrays.stream(mouseEvents).forEach(type -> scene.addEventHandler(type, resizeListener));
    }

    public static void tabSwitch(Node navigation, Pane tab, Pane navigationContainer, Pane tabContainer) {
        navigation.setOnMouseClicked(e -> {
            navigation.getStyleClass().add("selected");

            navigationContainer.getChildren().stream().filter(n -> !n.equals(navigation)).forEach(n -> n.getStyleClass().removeAll("selected"));

            tabContainer.getChildren().clear();

            tabContainer.getChildren().add(tab);
        });
    }

    public static FileChooser newFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("txt", "*"));
        return fileChooser;
    }

    public static DirectoryChooser newDirectoryChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        return directoryChooser;
    }

    public static void addTextLimiter(final TextArea tf, final int maxLength) {
        tf.textProperty().addListener((ov, oldValue, newValue) -> {
            if (tf.getText().length() > maxLength) {
                String s = tf.getText().substring(0, maxLength);
                tf.setText(s);
            }
        });
    }

    public static Alert createPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.showAndWait();
        return alert;
    }

    public static TextInputDialog createInputPopup(String header, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(null);
        dialog.setGraphic(null);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        return dialog;
    }


}
