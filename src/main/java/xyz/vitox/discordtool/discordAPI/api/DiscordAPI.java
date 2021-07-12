package xyz.vitox.discordtool.discordAPI.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.*;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.sendSystem.SendHandler;
import xyz.vitox.discordtool.discordAPI.tokenManager.Token;
import xyz.vitox.discordtool.discordAPI.useragents.RandomUserAgent;
import xyz.vitox.discordtool.tab.optionComponents.ProxyOptions;
import xyz.vitox.discordtool.tab.voiceSpamComponents.musicManager.TokenAudioPlayer;
import xyz.vitox.discordtool.util.Utils;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

public class DiscordAPI {

    private final String discordApiRoute = "https://discord.com/api/v9";
    public static ArrayList<SendHandler> sendHandlers = new ArrayList<>();
    public static ArrayList<TokenAudioPlayer> audioPlayers = new ArrayList<>();

    public Gson gson = new Gson();
    public static OkHttpClient client = new OkHttpClient().newBuilder().connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS)).build();


    public String currentProxy;
    public static Proxy proxy;
    public static OkHttpClient proxyClient = new OkHttpClient.Builder().proxy(proxy).build();

    /**
     * Get the meta information of the token.
     *
     * @param token
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */

    public String getTokenInformation(String token, String fingerprint) throws IOException {
        return makeGETRequest("/users/@me", token, null, null, fingerprint);
    }

    public HashMap<String, String> getGuildMemberVerification(Token token, String inviteCode) throws IOException {
        HashMap<String, String> responseAndGuildID = new HashMap<>();
        String guildInformation = getGuildInformationFromInvite(token, inviteCode);

        JsonElement element = new Gson().fromJson(guildInformation, JsonElement.class);
        JsonObject jsonObj = element.getAsJsonObject();

        String guildID = jsonObj.get("guild").getAsJsonObject().get("id").getAsString();

        responseAndGuildID.put(guildID, makeGETRequest("/guilds/" + guildID + "/member-verification?with_guild=false&invite_code=" + inviteCode, token.getToken(), token, null, token.getFingerprint()));
        return responseAndGuildID;
    }

    public String sendVerificationToCommunityGuild(Token token, String guildID, String body) throws IOException {
        return makeExecutionRequest("PUT", "/guilds/" + guildID + "/requests/@me", token.getToken(), token, "application/json", body, null, null, token.getFingerprint());
    }

    public String getGuildInformation(Token token, String guildID) throws IOException {
        return makeGETRequest("/guilds/" + guildID, token.getToken(), token, null, token.getFingerprint());
    }

    public String getAllGuilds(Token token) throws IOException {
        return makeGETRequest("/users/@me/guilds", token.getToken(), token, null, token.getFingerprint());
    }

    public String getChannel(Token token, String channelID) throws IOException {
        return makeGETRequest("/channels/" + channelID, token.getToken(), token, null, token.getFingerprint());
    }

    public String getFingerprint() throws IOException {
        return makeGETRequest("/experiments", "undefined", null, null, null);
    }

    public String joinServer(Token token, String inviteCode) throws IOException {
        String guildInformation = getGuildInformationFromInvite(token, inviteCode);

        if (guildInformation.contains(inviteCode)) {
            JsonElement element = new Gson().fromJson(guildInformation, JsonElement.class);
            JsonObject jsonObj = element.getAsJsonObject();

            String guildID = jsonObj.get("guild").getAsJsonObject().get("id").getAsString();
            String channelID = jsonObj.get("channel").getAsJsonObject().get("id").getAsString();
            int channelType = jsonObj.get("channel").getAsJsonObject().get("type").getAsInt();

            String xContextProperty = "{\"location\":\"Join Guild\",\"location_guild_id\":\"" + guildID + "\",\"location_channel_id\":\"" + channelID + "\",\"location_channel_type\":" + channelType + "}";
            System.out.println(xContextProperty);
            return makeExecutionRequest("POST", "/invites/" + inviteCode, token.getToken(), token, "text/plain", "", null, xContextProperty, token.getFingerprint());
        } else {
            return "Couldnt connect to guild";
        }
    }

    public String getGuildInformationFromInvite(Token token, String inviteCode) throws IOException {
        return makeGETRequest("/invites/" + inviteCode, token.getToken(), token, null, token.getFingerprint());
    }

    public String getGuildChannels(Token token, String guildID) throws IOException {
        return makeGETRequest("/guilds/" + guildID + "/channels", token.getToken(), token, null, token.getFingerprint());
    }

    public void leaveServer(Token token, String serverID) throws IOException {
        makeExecutionRequest("DELETE", "/users/@me/guilds/" + serverID, token.getToken(), token, "text/plain", "", null, null, token.getFingerprint());
    }

    public void sendTyping(Token token, String channelID) throws IOException {
        makeExecutionRequest("POST", "/channels/" + channelID + "/typing", token.getToken(), token, "application/json", "", null, null, token.getFingerprint());
    }

    public String reactMessage(Token token, String channelID, String messageID, String emoji) throws IOException {
        return makeExecutionRequest("PUT", "/channels/" + channelID + "/messages/" + messageID + "/reactions/" + emoji + "/%40me", token.getToken(), token, "application/json", "", null, null, token.getFingerprint());
    }

    public String removeReaction(Token token, String channelID, String messageID, String emoji) throws IOException {
        return makeExecutionRequest("DELETE", "/channels/" + channelID + "/messages/" + messageID + "/reactions/" + emoji + "/%40me", token.getToken(), token, "application/json", "", null, null, token.getFingerprint());
    }

    public String sendFriendRequest(Token token, String userID) throws IOException {
        String xContextProperty = "{\"location\":\"User Profile\"}";
        return makeExecutionRequest("PUT", "/users/@me/relationships/" + userID, token.getToken(), token, "application/json", "{}", null, xContextProperty, token.getFingerprint());
    }

    public String removeFriendRequest(Token token, String userID) throws IOException {
        String xContextProperty = "{\"location\":\"Friends\"}";
        return makeExecutionRequest("DELETE", "/users/@me/relationships/" + userID, token.getToken(), token, "application/json", "{}", null, xContextProperty, token.getFingerprint());
    }

    public String changeProfilePicture(Token token, File profilePicture) throws IOException {
        String imageString;
        if (profilePicture != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            BufferedImage image = ImageIO.read(profilePicture);
            ImageIO.write(image, "png", baos);

            imageString = "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } else {
            imageString = null;
        }

        if (profilePicture != null) {
            return makeExecutionRequest("PATCH", "/users/@me", token.getToken(), token, "application/json", "{\"avatar\": \"" + imageString + "\"}", null, null, token.getFingerprint());
        } else {
            return makeExecutionRequest("PATCH", "/users/@me", token.getToken(), token, "application/json", "{\"avatar\": null }", null, null, token.getFingerprint());
        }
    }

    public String getFriendChannelID(Token token, String userID) throws IOException {
        String response = makeExecutionRequest("POST", "/users/@me/channels", token.getToken(), token, "application/json", "{\"recipients\":[\"" + userID + "\"]}", null, null, token.getFingerprint());
        JsonElement element = gson.fromJson(response, JsonElement.class);
        JsonObject jsonObj = element.getAsJsonObject();
        return jsonObj.get("id").getAsString();
    }

    public void writeMessage(Token token, String message, boolean tts, String channelID, File attachmentFile) throws IOException {

        if (message.contains("%random%")) {
            message = message.replaceAll("%random%", Utils.randomString(10));
        }

        if (attachmentFile == null) {
            makeExecutionRequest("POST", "/channels/" + channelID + "/messages", token.getToken(), token, "application/json",
                    "{\"content\": \"" + message + "\", \"tts\":\"" + tts + "\"}", null, null, token.getFingerprint());
        } else {

            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("content", message)
                    .addFormDataPart("file", "/" + attachmentFile.getName(),
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    new File("/" + attachmentFile.getPath()))).build();

            makeExecutionRequest("POST", "/channels/" + channelID + "/messages", token.getToken(), token, "", "", body, null, token.getFingerprint());
        }

    }

    private String makeGETRequest(String restRoute, String token, Token tokenObject, String xContextProperties, String xFingerprint) throws IOException {
        Request.Builder requestBuilder = standardRequest("GET", restRoute, token, tokenObject, null, xContextProperties, null);
        Request request = requestBuilder.build();

        Response response = getClient().newCall(request).execute();
        String responseBody = response.body().string();
        System.out.println(currentProxy + " -> " + responseBody);
        response.body().close();
        return responseBody;
    }

    private String makeExecutionRequest(String requestType, String restRoute, String token, Token tokenObject, String mediaType, String requestBody, RequestBody body, String xContextProperties, String xFingerprint) throws IOException {
        MediaType mediaTyp = MediaType.parse(mediaType);

        if (body == null) {
            body = RequestBody.create(mediaTyp, requestBody);
        }
        Request.Builder requestBuilder = standardRequest(requestType, restRoute, token, tokenObject, body, xContextProperties, xFingerprint);
        Request request = requestBuilder.build();

        Response response = getClient().newCall(request).execute();
        String responseBody = response.body().string();
        System.out.println(currentProxy + " -> " + responseBody);
        response.body().close();
        return responseBody;
    }

    public Request.Builder standardRequest(String requestMethod, String restRoute, String token, Token tokenObject, RequestBody body, String xContextProperties, String xFingerprint) {

        RandomUserAgent randomUserAgent;
        if (tokenObject == null) {
            randomUserAgent = new RandomUserAgent();
        } else {
            randomUserAgent = tokenObject.getRandomUserAgent();
        }
        Request.Builder request = new Request.Builder()
                .url(discordApiRoute + restRoute)
                .method(requestMethod, body)
                .addHeader("authorization", token)
                .addHeader("x-super-properties", encodeBase64("{\"os\":\"Windows\",\"browser\":\""+ randomUserAgent.getUserAgent().getBrowserName() +"\",\"device\":\"\",\"system_locale\":\"de-DE\",\"browser_user_agent\":\""+ randomUserAgent.getUserAgent().getBrowserAgent() +"\",\"browser_version\":\""+ randomUserAgent.getUserAgent().getBrowserVersion() +"\",\"os_version\":\"10\",\"referrer\":\"\",\"referring_domain\":\"\",\"referrer_current\":\"\",\"referring_domain_current\":\"\",\"release_channel\":\"stable\",\"client_build_number\":85732,\"client_event_source\":null}"))
                .addHeader("accept-language", "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7")
                .addHeader("referer", "https://discord.com/brand-new")
                .addHeader("accept", "*/*")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("authority", "discord.com")
                .addHeader("origin", "https://discord.com")
                .addHeader("User-Agent", randomUserAgent.getUserAgent().getBrowserAgent());

        if (restRoute.equals("/experiments")) {
            request.addHeader("x-track", encodeBase64("{\"os\":\"Windows\",\"browser\":\""+ randomUserAgent.getUserAgent().getBrowserName() +"\",\"device\":\"\",\"system_locale\":\"de-DE\",\"browser_user_agent\":\""+ randomUserAgent.getUserAgent().getBrowserAgent() +"\",\"browser_version\":\""+ randomUserAgent.getUserAgent().getBrowserVersion() +"\",\"os_version\":\"10\",\"referrer\":\"\",\"referring_domain\":\"\",\"referrer_current\":\"\",\"referring_domain_current\":\"\",\"release_channel\":\"stable\",\"client_build_number\":85732,\"client_event_source\":null}"));
        }

        if (randomUserAgent.getUserAgent().getBrowserName().equals("Chrome")) {
            request.addHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"90\", \"Google Chrome\";v=\"90\"");
        }

        if (xContextProperties != null) {
            request.addHeader("x-context-properties", encodeBase64(xContextProperties));
        }
        if (xFingerprint != null) {
            request.addHeader("x-fingerprint", xFingerprint);
        }
        return request;
    }

    public OkHttpClient getClient() {
        if (ProxyOptions.proxyList.size() == 0) {
            currentProxy = "No proxy";
            return new OkHttpClient().newBuilder().connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS)).proxy(proxy).build();
        } else {
            int randomNum = ThreadLocalRandom.current().nextInt(0, ProxyOptions.proxyList.size());
            String randomProxy = ProxyOptions.proxyList.get(randomNum);
            String[] proxyInformations = randomProxy.split(":");
            String proxyHost = proxyInformations[0];
            int proxyPort = Integer.parseInt(proxyInformations[1]);
            currentProxy = proxyHost;
            Proxy proxy = setProxy(proxyHost, proxyPort);
            if (proxyInformations.length == 4) {
                String proxyUsername = proxyInformations[2];
                String proxyPassword = proxyInformations[3];
                return new OkHttpClient().newBuilder().connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS)).proxy(proxy).proxyAuthenticator(proxyAuthenticator(proxyUsername, proxyPassword)).build();
            } else {
               return new OkHttpClient().newBuilder().connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS)).proxy(proxy).build();
            }
        }
    }

    public Proxy setProxy(String proxyHost, int proxyPort) {
        if (ProxyOptions.selectedProxyType.equals("HTTP")) {
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        } else if (ProxyOptions.selectedProxyType.equals("SOCKS")) {
            return new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));
        }
        return null;
    }

    public Authenticator proxyAuthenticator(String proxyUser, String proxyPassword) {
        return new Authenticator() {
            @Nullable
            @Override
            public Request authenticate(@Nullable Route route, Response response) throws IOException {
                String credential = Credentials.basic(proxyUser, proxyPassword);
                return response.request().newBuilder().header("Proxy-Authorization", credential).build();
            }
        };
    }

    public String encodeBase64(String stringToEncode) {
        return Base64.getEncoder().encodeToString(stringToEncode.getBytes(StandardCharsets.UTF_8));
    }
}
