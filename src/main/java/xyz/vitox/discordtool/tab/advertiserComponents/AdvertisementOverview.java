package xyz.vitox.discordtool.tab.advertiserComponents;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import xyz.vitox.discordtool.discordAPI.api.DiscordAPI;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;
import xyz.vitox.discordtool.discordAPI.api.gateway.DiscordUserscrape;
import xyz.vitox.discordtool.discordAPI.tokenManager.Token;
import xyz.vitox.discordtool.discordAPI.tokenManager.TokenManager;
import xyz.vitox.discordtool.tab.serverSpamComponents.DelaySlider;

import java.io.IOException;

public class AdvertisementOverview {

    private DiscordMultiRequests multiRequests = new DiscordMultiRequests();
    private Token scrapeToken;


    public VBox contentArea() {
        VBox content = new VBox();

        content.getChildren().addAll(elements());

        content.setPadding(new Insets(10, 10, 10, 10));
        content.setStyle("-fx-background-color: -fx-card-color");
        return content;
    }

    public VBox elements() {
        VBox elements = new VBox();

        Label tokensLabel = new Label("Token to scrape with: ");
        tokensLabel.setTranslateY(3);
        Button importUserBtn = importUserButton();

        DelaySlider stopAtUserSliderElements = new DelaySlider();
        VBox stopAtUserSlider = stopAtUserSliderElements.delaySlider("Stop scraping at", " users", 100 , 0);

        ComboBox<Token> tokenSelector = tokenSelector();
        tokenSelector.getSelectionModel().selectFirst();

        HBox topElements = new HBox();
        topElements.setSpacing(10);
        topElements.getChildren().addAll(tokensLabel, tokenSelector);

        Button startBtn = startButton(stopAtUserSliderElements);

        HBox buttonElements = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonElements.getChildren().addAll(startBtn, spacer, importUserBtn);

        elements.setSpacing(10);
        elements.getChildren().addAll(topElements, stopAtUserSlider, buttonElements);
        return elements;
    }

    private ComboBox<Token> tokenSelector() {
        ObservableList<Token> tokenList = FXCollections.observableArrayList(TokenManager.tokensToUse());
        ComboBox<Token> cb = new ComboBox<>();
        cb.setEditable(true);

        FilteredList<Token> filteredItems = new FilteredList<>(tokenList, p -> true);

        // Add a listener to the textProperty of the combobox editor. The
        // listener will simply filter the list every time the input is changed
        // as long as the user hasn't selected an item in the list.
        cb.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            final TextField editor = cb.getEditor();
            final Token selected = cb.getSelectionModel().getSelectedItem();

            // This needs run on the GUI thread to avoid the error described
            // here: https://bugs.openjdk.java.net/browse/JDK-8081700.
            Platform.runLater(() -> {
                // If the no item in the list is selected or the selected item
                // isn't equal to the current input, we refilter the list.
                if (selected == null || !selected.getName() .equals(editor.getText())) {
                    filteredItems.setPredicate(item -> {
                        // We return true for any items that starts with the
                        // same letters as the input. We use toUpperCase to
                        // avoid case sensitivity.
                        if (item.getName().toUpperCase().startsWith(newValue.toUpperCase())) {
                            return true;
                        } else {
                            return false;
                        }
                    });
                }
            });
        });

        cb.setCellFactory(new Callback<ListView<Token>,ListCell<Token>>(){

            @Override
            public ListCell<Token> call(ListView<Token> p) {

                final ListCell<Token> cell = new ListCell<Token>(){

                    @Override
                    protected void updateItem(Token t, boolean bln) {
                        super.updateItem(t, bln);

                        if(t != null){
                            setText(t.getName() + "#" + t.getDiscriminator());
                        }else{
                            setText(null);
                        }
                    }

                };

                return cell;
            }
        });

        cb.setConverter(new StringConverter<Token>() {
            @Override
            public String toString(Token token) {
                if (token == null) {
                    return null;
                } else {
                    return token.getName();
                }
            }

            @Override
            public Token fromString(String productString)
            {
                return cb.getItems().stream().filter(item -> productString.equals(item.getName())).findFirst().orElse(null);
            }

        });

        cb.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> scrapeToken = newValue);

        cb.setItems(filteredItems);
        return cb;
    }

    private Button startButton(DelaySlider delaySlider) {
        Button startBtn = new Button("Start scraping");

//        startBtn.setOnAction(event -> {
//
//            try {
//                DiscordUserscrape discordUserscrape = new DiscordUserscrape(scrapeToken);
//                discordUserscrape.collectServerIDs();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });

        return startBtn;
    }

    private Button importUserButton() {
        Button importUserBtn = new Button("Import user list");
        return importUserBtn;
    }

}
