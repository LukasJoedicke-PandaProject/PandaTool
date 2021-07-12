package xyz.vitox.discordtool.discordAPI.api.gateway.voice.sendSystem;

import xyz.vitox.discordtool.discordAPI.api.gateway.voice.AudioConnection;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.packets.OpusPacket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;

public class DefaultSendSystem {

    private Thread sendThread;
    private final DatagramSocket udpSocket;
    private final AudioConnection audioConnection;

    public static ArrayList<Thread> sendingThreads = new ArrayList<>();
    public static ArrayList<DatagramSocket> sendingSockets = new ArrayList<>();

    public DefaultSendSystem(DatagramSocket udpSocket, AudioConnection audioConnection) {
        this.udpSocket = udpSocket;
        this.audioConnection = audioConnection;
    }

    public void start() {
        sendThread = new Thread(() -> {
            boolean sentPacket = true;
            long lastFrameSent = System.currentTimeMillis();
            sendingThreads.add(sendThread);
            sendingSockets.add(udpSocket);
            while (!udpSocket.isClosed() && !sendThread.isInterrupted()) {
                try {
                    DatagramPacket packet = audioConnection.getNextPacket(false);

                    sentPacket = packet != null;
                    if (sentPacket) {
                        udpSocket.send(packet);
                    }
                } catch (Exception e) {

                } finally {
                    long sleepTime = (OpusPacket.OPUS_FRAME_TIME_AMOUNT) - (System.currentTimeMillis() - lastFrameSent);
                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    if (System.currentTimeMillis() < lastFrameSent + 60)
                    {
                        lastFrameSent += OpusPacket.OPUS_FRAME_TIME_AMOUNT;
                    } else {
                        lastFrameSent = System.currentTimeMillis();
                    }
                }
            }
        });

        sendThread.start();
    }

}
