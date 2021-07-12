package xyz.vitox.discordtool.util;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.shape.SVGPath;

import java.awt.*;
import java.net.URL;

public class InfoButton {

    public Button content(String link) {
        Button infoBtn = new Button();
        infoBtn.setStyle("-fx-background-color: transparent");

        infoBtn.setTranslateX(5);
        infoBtn.setTranslateY(-5);
        Group svg = new Group(
                createPath("M20 424.229h20V279.771H20c-11.046 0-20-8.954-20-20V212c0-11.046 8.954-20 20-20h112c11.046 0 20 8.954 20 20v212.229h20c11.046 0 20 8.954 20 20V492c0 11.046-8.954 20-20 20H20c-11.046 0-20-8.954-20-20v-47.771c0-11.046 8.954-20 20-20zM96 0C56.235 0 24 32.235 24 72s32.235 72 72 72 72-32.235 72-72S135.764 0 96 0z")
        );

        Bounds bounds = svg.getBoundsInParent();
        double scale = Math.min(15/bounds.getWidth(), 15 / bounds.getHeight());

        svg.setScaleX(scale);
        svg.setScaleY(scale);
        infoBtn.setMaxSize(25, 25);
        infoBtn.setMinSize(25, 25);
        infoBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        infoBtn.setGraphic(svg);

        infoBtn.setOnAction(event -> openWebpage(link));

        return infoBtn;
    }

    private static SVGPath createPath(String d) {
        SVGPath path = new SVGPath();
        path.getStyleClass().add("help-icon");
        path.setContent(d);
        return path;
    }

    public void openWebpage(String urlString) {
        try {
            Desktop.getDesktop().browse(new URL(urlString).toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
