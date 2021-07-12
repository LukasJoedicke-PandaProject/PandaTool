package xyz.vitox.discordtool.discordAPI.api.gateway;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class DiscordGateway {

    public static ArrayList<WebsocketClientEndpoint> openGateways = new ArrayList<>();
    public WebsocketClientEndpoint gatewayEndpoint;

    public WebsocketClientEndpoint initGateway(String token) {
        try {
            gatewayEndpoint = new WebsocketClientEndpoint(new URI("wss://gateway.discord.gg/?v=6&encoding=json"));
            openGateways.add(gatewayEndpoint);
            sendAuthentification(token);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return gatewayEndpoint;
    }

    public void sendAuthentification(String token) {
        gatewayEndpoint.sendMessage("{\n" +
                "  \"op\": 2,\n" +
                "  \"d\": {\n" +
                "    \"token\": \"" + token + "\",\n" +
                "    \"properties\": {\n" +
                "      \"$os\": \"linux\",\n" +
                "      \"$browser\": \"my_library\",\n" +
                "      \"$device\": \"my_library\"\n" +
                "    }\n" +
                "  }\n" +
                "}");
    }

    public void initHeartbeat(WebsocketClientEndpoint clientEndPoint) {
        new Thread(() -> {
            while (true) {
                clientEndPoint.sendMessage("{\n" +
                        "    \"op\": 1,\n" +
                        "    \"d\": 251\n" +
                        "}");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    System.out.println("Failed send heartbeat to token. Invalid token?");
                }
            }
        }).start();
    }

    public static void closeGateways(int delay) {
        if (openGateways.size() > 0) {

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(5, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, queue, handler);

            openGateways.forEach(session -> {
                executor.execute(() -> {
                    try {
                        session.userSession.close();
                    } catch (IOException e) {
                        System.out.println("Failed to close connection to token. Invalid token?");
                    }
                });
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            openGateways.clear();
        }
    }
}
