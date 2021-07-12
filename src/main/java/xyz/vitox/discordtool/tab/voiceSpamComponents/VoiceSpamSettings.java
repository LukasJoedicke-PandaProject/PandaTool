package xyz.vitox.discordtool.tab.voiceSpamComponents;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;
import xyz.vitox.discordtool.tab.serverSpamComponents.DelaySlider;
import xyz.vitox.discordtool.tab.voiceSpamComponents.musicManager.TokenAudioPlayer;

import java.util.Arrays;

public class VoiceSpamSettings {

    private DiscordMultiRequests discordMultiRequests = new DiscordMultiRequests();
    public static boolean advancedConnection = false;
    public static int joinDelay = 0;
    public static int leaveDelay = 0;

    public VBox contentArea() {
        VBox content = new VBox();

        content.setSpacing(20);
        content.setPadding(new Insets(10, 10, 10, 10));
        content.setStyle("-fx-background-color: #202834");
        content.getChildren().addAll(advancedConnectionCheckbox(content), sliders());
        return content;
    }

    private VBox sliders() {
        VBox sliders = new VBox();

        VBox joinSliderElements = joinSlider("Join Delay", "ms", 500, 0);
        VBox leaveSliderElements = leaveSlider("Leave Delay", "ms", 500, 0);

        sliders.setSpacing(10);
        sliders.getChildren().addAll(joinSliderElements, leaveSliderElements);
        return sliders;
    }

    private CheckBox advancedConnectionCheckbox(VBox content) {
        CheckBox advancedConnectionBox = new CheckBox("Enable Full Voice Connection");

        advancedConnectionBox.setOnAction(event -> {
            if (advancedConnectionBox.isSelected()) {
                content.getChildren().remove(1);
                content.getChildren().addAll(voiceConnectedSettings(), sliders());
                VoiceChannel.enableFullVoiceConnectionBox.setVisible(false);
                advancedConnection = true;
            } else {
                content.getChildren().remove(1);
                content.getChildren().remove(1);
                content.getChildren().addAll(sliders());
                VoiceChannel.enableFullVoiceConnectionBox.setVisible(true);
                advancedConnection = false;
            }
        });

        return advancedConnectionBox;
    }

    public VBox voiceConnectedSettings() {
        VBox voiceConnectedSettings = new VBox();
        Button stopBtn = new Button("Stop Track");

        stopBtn.setStyle("-fx-background-color: -fx-negative;");

        stopBtn.setOnAction(event -> discordMultiRequests.stopMusic().start());

        VBox volumeSliderElements = volumeSlider("Volume of Soundtrack", "%", 1, 50);
        VBox trackStartDelaySliderElements = trackStartDelaySliderElements("Delay of songs", "ms", 100, 0);

        voiceConnectedSettings.setSpacing(10);
        voiceConnectedSettings.getChildren().addAll(volumeSliderElements, trackStartDelaySliderElements, stopBtn);

        return voiceConnectedSettings;
    }

    public VBox leaveSlider(String name, String unit, int jump, int start) {
        VBox delayElements = new VBox();
        Label delayInfo = new Label(name + ": 0" + unit);
        delayInfo.setStyle("-fx-text-fill: -fx-secondary-text-color;");
        Slider delaySlider = new Slider();

        if (start != 0) {
            delayInfo.setText(name + ": " + start + unit);
            delaySlider.adjustValue(start);
        }

        delaySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            delayInfo.setText(name + ": " + newValue.intValue() * jump + unit);
        });
        delayElements.getChildren().addAll(delaySlider, delayInfo);
        return delayElements;
    }

    public VBox joinSlider(String name, String unit, int jump, int start) {
        VBox delayElements = new VBox();
        Label delayInfo = new Label(name + ": 0" + unit);
        delayInfo.setStyle("-fx-text-fill: -fx-secondary-text-color;");
        Slider delaySlider = new Slider();

        if (start != 0) {
            delayInfo.setText(name + ": " + start + unit);
            delaySlider.adjustValue(start);
        }

        delaySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            delayInfo.setText(name + ": " + newValue.intValue() * jump + unit);
            DiscordMultiRequests.joinVoiceChannelDelay = newValue.intValue() * jump;
        });
        delayElements.getChildren().addAll(delaySlider, delayInfo);
        return delayElements;
    }

    public VBox trackStartDelaySliderElements(String name, String unit, int jump, int start) {
        VBox delayElements = new VBox();
        Label delayInfo = new Label(name + ": 0" + unit);
        delayInfo.setStyle("-fx-text-fill: -fx-secondary-text-color;");
        Slider delaySlider = new Slider();

        if (start != 0) {
            delayInfo.setText(name + ": " + start + unit);
            delaySlider.adjustValue(start);
        }

        delaySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            delayInfo.setText(name + ": " + newValue.intValue() * jump + unit);
            DiscordMultiRequests.songDelay = newValue.intValue() * jump;
        });
        delayElements.getChildren().addAll(delaySlider, delayInfo);
        return delayElements;
    }

    public VBox volumeSlider(String name, String unit, int jump, int start) {
        VBox delayElements = new VBox();
        Label delayInfo = new Label(name + ": 0" + unit);
        delayInfo.setStyle("-fx-text-fill: -fx-secondary-text-color;");
        Slider delaySlider = new Slider();

        if (start != 0) {
            delayInfo.setText(name + ": " + start + unit);
            delaySlider.adjustValue(start);
        }

        delaySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            delayInfo.setText(name + ": " + newValue.intValue() * jump + unit);

            Arrays.stream(TokenAudioPlayer.players.toArray(new AudioPlayer[0])).parallel().forEach(player -> {
                player.setVolume(newValue.intValue() * jump * 10);
            });
        });
        delayElements.getChildren().addAll(delaySlider, delayInfo);
        return delayElements;
    }

}
