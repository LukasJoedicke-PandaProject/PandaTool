package xyz.vitox.discordtool.tab.serverSpamComponents;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

public class DelaySlider {

    int sliderValue;


    public VBox delaySlider(String name, String unit, int jump, int start) {
        VBox delayElements = new VBox();
        Label delayInfo = new Label(name + ": 0" + unit);
        delayInfo.setStyle("-fx-text-fill: -fx-secondary-text-color;");
        Slider delaySlider = new Slider();

        if (start != 0) {
            delayInfo.setText(name + ": " + start + unit);
            delaySlider.adjustValue(start);
            setSliderValue(start);
        }

        delaySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            delayInfo.setText(name + ": " + newValue.intValue() * jump + unit);
            setSliderValue(newValue.intValue() * jump);
        });
        delayElements.getChildren().addAll(delaySlider, delayInfo);
        return delayElements;
    }

    public int getSliderValue() {
        return sliderValue;
    }

    public void setSliderValue(int sliderValue) {
        this.sliderValue = sliderValue;
    }
}
