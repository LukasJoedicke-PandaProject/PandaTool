package xyz.vitox.discordtool.discordAPI.tokenManager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import xyz.vitox.discordtool.Main;
import xyz.vitox.discordtool.controller.DashboardController;
import xyz.vitox.discordtool.discordAPI.api.DiscordAPI;
import xyz.vitox.discordtool.discordAPI.useragents.RandomUserAgent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TokenManager {

    public static ArrayList<Token> tokenList = new ArrayList<>();
    public static ArrayList<Token> verifiedTokens = new ArrayList<>();
    public static ArrayList<Token> verifiedAndUnverifiedTokens = new ArrayList<>();
    public static ArrayList<Token> unverifiedTokens = new ArrayList<>();
    public static ArrayList<String> invalidTokens = new ArrayList<>();
    private final HashMap<String, String> tokenAndInfo;

    private DiscordAPI discordAPI = new DiscordAPI();

    public TokenManager(HashMap<String, String> tokenAndInfo) {
        this.tokenAndInfo = tokenAndInfo;
    }

    public void parseTokensToObject() {
        for (Map.Entry<String, String> tokens : tokenAndInfo.entrySet()) {

            String[] tokenAndFingerprint = tokens.getKey().split(";");

            String token = tokenAndFingerprint[0];
            String fingerPrint = tokenAndFingerprint[1];

            String tokenInformations = tokens.getValue();

            if (tokenInformations == null || tokenInformations.contains("Unauthorized")) {
                invalidTokens.add(token);
                continue;
            }

            if (tokenInformations.contains("You are being rate limited.")) {
                continue;
            }

            JsonElement element = new Gson().fromJson(tokenInformations, JsonElement.class);
            JsonObject jsonObj = element.getAsJsonObject();

            RandomUserAgent randomUserAgent = new RandomUserAgent();

            String username = jsonObj.get("username").getAsString();
            String discriminator = jsonObj.get("discriminator").getAsString();
            String id = jsonObj.get("id").getAsString();
            String email = jsonObj.get("email").toString();
            String avatar = jsonObj.get("avatar").toString();
            String phone = jsonObj.get("phone").toString();
            String verified = jsonObj.get("verified").getAsString();

            email = email.replaceAll("\"", "");

            if (verified.equals("true")) {
                if (!phone.contains("null")) {
                    verifiedTokens.add(new Token(username, discriminator, id, email, avatar, phone, verified, token, fingerPrint, randomUserAgent));
                    verifiedAndUnverifiedTokens.add(new Token(username, discriminator, id, email, avatar, phone, verified, token, fingerPrint, randomUserAgent));
                } else {
                    verifiedAndUnverifiedTokens.add(new Token(username, discriminator, id, email, avatar, phone, verified, token, fingerPrint, randomUserAgent));
                    unverifiedTokens.add(new Token(username, discriminator, id, email, avatar, phone, verified, token, fingerPrint, randomUserAgent));
                }
            } else {
                verifiedAndUnverifiedTokens.add(new Token(username, discriminator, id, email, avatar, phone, verified, token, fingerPrint, randomUserAgent));
                unverifiedTokens.add(new Token(username, discriminator, id, email, avatar, phone, verified, token, fingerPrint, randomUserAgent));
            }

            if (!phone.equals("null")) {
                phone = phone.replaceAll("\"", "");
            }
            if (!avatar.equals("null")) {
                avatar = avatar.replaceAll("\"", "");
            }

            tokenList.add(new Token(username, discriminator, id, email, avatar, phone, verified, token, fingerPrint, randomUserAgent));
        }
        removeBlocker();
        changeHomeView();
    }

    public static ArrayList<Token> tokensToUse() {
        if (Main.getSettings().readSettingBoolean("onlyValidTokens")) {
            return verifiedTokens;
        } else {
            return verifiedAndUnverifiedTokens;
        }
    }

    /**
     * Once all tokens are loaded, this function will remove the select screen in Home.class
     * And add a view to see all tokens as cards
     */
    public void changeHomeView() {
        Platform.runLater(() -> {
            DashboardController.getHome().changeHomeView();
        });
    }

    public void removeBlocker() {
        Platform.runLater(() -> {
            DashboardController.getTokenSettings().getChildren().remove(1);
            DashboardController.getVoiceSpam().getChildren().remove(1);
            DashboardController.getServerSpam().getChildren().remove(1);
            DashboardController.getServerRecon().getChildren().remove(1);
            DashboardController.getVerifier().getChildren().remove(1);

            DashboardController.getAdvertiser().getChildren().remove(0);
            DashboardController.getAdvertiser().getChildren().add(DashboardController.getAdvertiser().contentArea());
        });
    }

}
