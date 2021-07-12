package xyz.vitox.discordtool.discordAPI;

import javafx.scene.control.ProgressBar;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;
import xyz.vitox.discordtool.discordAPI.tokenManager.TokenManager;
import xyz.vitox.discordtool.util.SystemUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class TokenLoader {

    public static ArrayList<String> discordTokens = new ArrayList<>();

    /**
     * Load tokens into ArrayList
     * Call getTokenInformation and handover tokens
     * @param tokenFile
     */
    public void loadRawTokens(File tokenFile, ProgressBar progressBar) {
        discordTokens = SystemUtil.contentFromFileToArraylist(tokenFile);
        getTokenInformation(discordTokens, progressBar);
    }

    /**
     * Get the token and the token information (response json) as a HashMap and parseTokens to an Object
     * @param discordTokens
     */
    public void getTokenInformation(ArrayList<String> discordTokens, ProgressBar progressBar) {
        new Thread(() -> {
            DiscordMultiRequests discordMultiRequests = new DiscordMultiRequests();
            HashMap<String, String> tokenAndInfo = discordMultiRequests.getTokenInformation(discordTokens, progressBar);

            parseTokens(tokenAndInfo);
        }).start();
    }

    public void parseTokens(HashMap<String, String> tokenAndInfo) {
        TokenManager tokenManager = new TokenManager(tokenAndInfo);
        tokenManager.parseTokensToObject();
    }

}
