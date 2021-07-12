package xyz.vitox.discordtool.discordAPI.api.gateway.voice;

import com.iwebpp.crypto.TweetNaclFast;
import okhttp3.WebSocket;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.packets.AudioPacket;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.packets.OpusPacket;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.sendSystem.DefaultSendSystem;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.sendSystem.SendHandler;
import xyz.vitox.discordtool.util.IOUtil;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

public class AudioConnection {

    public static final long MAX_UINT_32 = 4294967295L;

    private final InetSocketAddress socketAddress;
    private final DatagramSocket udpConnection;
    private SendHandler sendHandler;
    private final byte[] secretKey;
    private final String channelID;
    private String encryptionMode;
    private int ssrc;
    private static final ByteBuffer silenceBytes = ByteBuffer.wrap(new byte[] {(byte)0xF8, (byte)0xFF, (byte)0xFE});
    private boolean sentSilenceOnConnect = false;
    private char seq = 0;           //Sequence of audio packets. Used to determine the order of the packets.
    private int timestamp = 0;      //Used to sync up our packets within the same timeframe of other people talking.
    private TweetNaclFast.SecretBox boxer;
    private long nonce = 0;
    private ByteBuffer buffer = ByteBuffer.allocate(512);
    private ByteBuffer encryptionBuffer = ByteBuffer.allocate(512);
    private final byte[] nonceBuffer = new byte[TweetNaclFast.SecretBox.nonceLength];
    private volatile int silenceCounter = 0;
    private volatile boolean speaking = false;
    private int speakingDelay = 10;
    private WebSocket webSocket;
    public static int voiceConnections = 0;

    public AudioConnection(DatagramSocket udpConnection, WebSocket webSocket, InetSocketAddress socketAddress, byte[] secretKey, String channelID, String encryptionMode, int ssrc, SendHandler sendHandler) {
        this.socketAddress = socketAddress;
        this.secretKey = secretKey;
        this.channelID = channelID;
        this.encryptionMode = encryptionMode;
        this.ssrc = ssrc;
        this.boxer = new TweetNaclFast.SecretBox(secretKey);
        this.udpConnection = udpConnection;
        this.sendHandler = sendHandler;
        this.webSocket = webSocket;
    }

    public void startSendSystem() {
        try {
            DefaultSendSystem defaultSendSystem = new DefaultSendSystem(this.udpConnection, this);
            defaultSendSystem.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DatagramPacket getNextPacket(boolean changeTalking)
    {
        ByteBuffer buffer = getNextPacketRaw(changeTalking);
        return buffer == null ? null : getDatagramPacket(buffer);
    }

    public ByteBuffer getNextPacketRaw(boolean changeTalking)
    {
        ByteBuffer nextPacket = null;
        try
        {
            cond: if (sentSilenceOnConnect && sendHandler != null && sendHandler.isCanProvide())
            {
                silenceCounter = -1;
                ByteBuffer rawAudio = sendHandler.getProvide20MsAudio();
                if (rawAudio != null && !rawAudio.hasArray())
                {
                    // we can't use the boxer without an array so encryption would not work
                }
                if (rawAudio == null || !rawAudio.hasRemaining() || !rawAudio.hasArray())
                {
                    if (speaking && changeTalking)
                        sendSilentPackets();
                }
                else
                {

                    nextPacket = getPacketData(rawAudio);
                    if (!speaking)
                    speaking = true;

                    if (seq + 1 > Character.MAX_VALUE)
                        seq = 0;
                    else
                        seq++;
                }
            }
            else if (silenceCounter > -1)
            {
                nextPacket = getPacketData(silenceBytes);
                if (seq + 1 > Character.MAX_VALUE)
                    seq = 0;
                else
                    seq++;

                silenceCounter++;
                //If we have sent our 10 silent packets on initial connect, or if we have sent enough silent packets
                // to satisfy the speaking delay, stop transmitting silence.
                if ((!sentSilenceOnConnect && silenceCounter > 10) || silenceCounter > speakingDelay)
                {
                    if (sentSilenceOnConnect)
                    silenceCounter = -1;
                    sentSilenceOnConnect = true;
                }
            }
            else if (speaking && changeTalking)
            {
                sendSilentPackets();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (nextPacket != null)
            timestamp += OpusPacket.OPUS_FRAME_SIZE;

        return nextPacket;
    }

    private void sendSilentPackets()
    {
        silenceCounter = 0;
    }

    private DatagramPacket getDatagramPacket(ByteBuffer b)
    {
        byte[] data = b.array();
        int offset = b.arrayOffset() + b.position();
        int length = b.remaining();
        return new DatagramPacket(data, offset, length, socketAddress);
    }

    private ByteBuffer getPacketData(ByteBuffer rawAudio)
    {
        ensureEncryptionBuffer(rawAudio);
        AudioPacket packet = new AudioPacket(encryptionBuffer, seq, timestamp, ssrc, rawAudio);
        int nlen = 0;
        switch (encryptionMode.toUpperCase())
        {
            case "XSALSA20_POLY1305":
                nlen = 0;
                break;
            case "XSALSA20_POLY1305_LITE":
                if (nonce >= MAX_UINT_32)
                    loadNextNonce(nonce = 0);
                else
                    loadNextNonce(++nonce);
                nlen = 4;
                break;
            case "XSALSA20_POLY1305_SUFFIX":
                ThreadLocalRandom.current().nextBytes(nonceBuffer);
                nlen = TweetNaclFast.SecretBox.nonceLength;
                break;
        }
        return buffer = packet.asEncryptedPacket(boxer, buffer, nonceBuffer, nlen);
    }

    private void ensureEncryptionBuffer(ByteBuffer data)
    {
        ((Buffer) encryptionBuffer).clear();
        int currentCapacity = encryptionBuffer.remaining();
        int requiredCapacity = AudioPacket.RTP_HEADER_BYTE_LENGTH + data.remaining();
        if (currentCapacity < requiredCapacity)
            encryptionBuffer = ByteBuffer.allocate(requiredCapacity);
    }

    private void loadNextNonce(long nonce)
    {
        IOUtil.setIntBigEndian(nonceBuffer, 0, (int) nonce);
    }


}
