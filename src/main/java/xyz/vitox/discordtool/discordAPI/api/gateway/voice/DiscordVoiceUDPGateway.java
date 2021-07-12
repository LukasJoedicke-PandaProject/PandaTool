package xyz.vitox.discordtool.discordAPI.api.gateway.voice;

import com.google.gson.*;
import javafx.application.Platform;
import lombok.SneakyThrows;
import okhttp3.*;
import xyz.vitox.discordtool.discordAPI.api.DiscordAPI;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.sendSystem.SendHandler;
import xyz.vitox.discordtool.discordAPI.tokenManager.TokenManager;
import xyz.vitox.discordtool.tab.voiceSpamComponents.VoiceSpamInfo;
import xyz.vitox.discordtool.tab.voiceSpamComponents.VoiceSpamSettings;
import xyz.vitox.discordtool.util.IOUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.*;

public class DiscordVoiceUDPGateway extends WebSocketListener {

    private final String serverID;
    private final String sessionID;
    private final String endPoint;
    private final String voiceToken;
    private final String tokenID;
    private DatagramSocket udpSocket;

    private InetSocketAddress socketAddress;
    private final String channelID;
    private byte[] secretKey;
    private String encryptionMode;
    private int ssrc;

    public static ArrayList<WebSocket> openUDPGateways = new ArrayList<>();

    public DiscordVoiceUDPGateway(String serverID, String tokenID, String sessionID, String endPoint, String voiceToken, String channelID) {
        this.serverID = serverID;
        this.sessionID = sessionID;
        this.endPoint = endPoint;
        this.voiceToken = voiceToken;
        this.channelID = channelID;
        this.tokenID = tokenID;
    }

    public void run() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("wss://" + endPoint).build();
        WebSocket webSocket = client.newWebSocket(request, this);
        openUDPGateways.add(webSocket);
        client.dispatcher().executorService().shutdown();
    }

    @SneakyThrows
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        webSocket.send("{\n" +
                "  \"op\": 0,\n" +
                "  \"d\": {\n" +
                "    \"server_id\": \"" + serverID + "\",\n" +
                "    \"user_id\": \"" + tokenID + "\",\n" +
                "    \"session_id\": \"" + sessionID + "\",\n" +
                "    \"token\": \"" + voiceToken + "\"\n" +
                "  }\n" +
                "}");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        JsonElement element = new Gson().fromJson(text, JsonElement.class);
        JsonObject jsonObj = element.getAsJsonObject();
        JsonElement getEventName = jsonObj.get("op");
        JsonElement getInformation = jsonObj.get("d");

        if (!(getEventName instanceof JsonNull)) {
            if (getEventName.getAsInt() == 2) {
                String ip = getInformation.getAsJsonObject().get("ip").getAsString();
                int port = getInformation.getAsJsonObject().get("port").getAsInt();
                int ssrc = getInformation.getAsJsonObject().get("ssrc").getAsInt();
                this.ssrc = ssrc;
                InetSocketAddress externalIpAndPort = handleUdpDiscovery(new InetSocketAddress(ip, port), ssrc);

                this.encryptionMode = "xsalsa20_poly1305_suffix";

                webSocket.send("{\n" +
                        "    \"op\": 1,\n" +
                        "    \"d\": {\n" +
                        "        \"protocol\": \"udp\",\n" +
                        "        \"data\": {\n" +
                        "            \"address\": \"" + externalIpAndPort.getHostString() + "\",\n" +
                        "            \"port\": " + externalIpAndPort.getPort() + ",\n" +
                        "            \"mode\": \"" + encryptionMode + "\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}");

                establishUDPConnection(webSocket, externalIpAndPort.getAddress().getHostAddress(), externalIpAndPort.getPort(), ssrc);
            }

            if (getEventName.getAsInt() == 4) {
                JsonArray jsonArray = getInformation.getAsJsonObject().get("secret_key").getAsJsonArray();
                byte[] secretKey = new byte[32];
                for (int i = 0; i < jsonArray.size(); i++) {
                    secretKey[i] = jsonArray.get(i).getAsByte();
                }
                this.secretKey = secretKey;
                System.out.println("Voice connection finished.");

                if (AudioConnection.voiceConnections < TokenManager.tokensToUse().size() && VoiceSpamSettings.advancedConnection) {
                    AudioConnection.voiceConnections++;
                    Platform.runLater(() -> VoiceSpamInfo.botsConnected.setText("Bots connected: " + AudioConnection.voiceConnections + "/" + TokenManager.tokensToUse().size()));
                }

                SendHandler sendHandler = new SendHandler(false, null, true);
                DiscordAPI.sendHandlers.add(sendHandler);

                AudioConnection audioConnection = new AudioConnection(this.udpSocket, webSocket, socketAddress, secretKey, channelID, encryptionMode, ssrc, sendHandler);
                audioConnection.startSendSystem();
            }
        }
    }

    public void establishUDPConnection(WebSocket webSocket, String ip, int port, int ssrc) {

        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                while (true) {
                    webSocket.send("{\n" +
                            "    \"op\": 5,\n" +
                            "    \"d\": {\n" +
                            "        \"speaking\": 5,\n" +
                            "        \"delay\": 0,\n" +
                            "        \"ssrc\": " + ssrc + "\n" +
                            "    }\n" +
                            "}");
                    Thread.sleep(5000);
                }

            }
        }).start();

    }

    private InetSocketAddress handleUdpDiscovery(InetSocketAddress address, int ssrc) {
        //We will now send a packet to discord to punch a port hole in the NAT wall.
        //This is called UDP hole punching.
        try {
            //First close existing socket from possible previous attempts
            if (udpSocket != null)
                udpSocket.close();
            //Create new UDP socket for communication
            this.udpSocket = new DatagramSocket();
            //Create a byte array of length 70 containing our ssrc.
            ByteBuffer buffer = ByteBuffer.allocate(70);    //70 taken from documentation
            buffer.putShort((short) 1);                     // 1 = send (receive will be 2)
            buffer.putShort((short) 70);                    // length = 70 bytes (required)
            buffer.putInt(ssrc);                            // Put the ssrc that we were given into the packet to send back to discord.
            // rest of the bytes are used only in the response (address/port)

            //Construct our packet to be sent loaded with the byte buffer we store the ssrc in.
            DatagramPacket discoveryPacket = new DatagramPacket(buffer.array(), buffer.array().length, address);
            udpSocket.send(discoveryPacket);

            //Discord responds to our packet, returning a packet containing our external ip and the port we connected through.
            DatagramPacket receivedPacket = new DatagramPacket(new byte[70], 70);   //Give a buffer the same size as the one we sent.
            udpSocket.setSoTimeout(1000);
            udpSocket.receive(receivedPacket);

            //The byte array returned by discord containing our external ip and the port that we used
            //to connect to discord with.
            byte[] received = receivedPacket.getData();

            //Example string:"   121.83.253.66                                                   ��"
            //You'll notice that there are 4 leading nulls and a large amount of nulls between the the ip and
            // the last 2 bytes. Not sure why these exist.  The last 2 bytes are the port. More info below.

            //Take bytes between SSRC and PORT and put them into a string
            // null bytes at the beginning are skipped and the rest are appended to the end of the string
            String ourIP = new String(received, 4, received.length - 6);
            // Removes the extra nulls attached to the end of the IP string
            ourIP = ourIP.trim();

            //The port exists as the last 2 bytes in the packet data, and is encoded as an UNSIGNED short.
            //Furthermore, it is stored in Little Endian instead of normal Big Endian.
            //We will first need to convert the byte order from Little Endian to Big Endian (reverse the order)
            //Then we will need to deal with the fact that the bytes represent an unsigned short.
            //Java cannot deal with unsigned types, so we will have to promote the short to a higher type.
            //Get our port which is stored as little endian at the end of the packet
            // We AND it with 0xFFFF to ensure that it isn't sign extended
            int ourPort = (int) IOUtil.getShortBigEndian(received, received.length - 2) & 0xFFFF;
            this.socketAddress = address;
            return new InetSocketAddress(ourIP, ourPort);
        } catch (IOException e) {
            // We either timed out or the socket could not be created (firewall?)
            return null;
        }
    }

    public static void closeGateways(int delay) {
        if (openUDPGateways.size() > 0) {

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(5, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, queue, handler);

            openUDPGateways.forEach(session -> {
                executor.execute(() -> {
                    session.close(1000, "Closed by User.");
                });
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            openUDPGateways.clear();
        }
    }
}
