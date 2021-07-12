package xyz.vitox.discordtool.discordAPI.api.gateway.voice.packets;

import java.util.Arrays;
import java.util.Objects;

public final class OpusPacket implements Comparable<OpusPacket>
{
    /** (Hz) We want to use the highest of qualities! All the bandwidth! */
    public static final int OPUS_SAMPLE_RATE = 48000;
    /** An opus frame size of 960 at 48000hz represents 20 milliseconds of audio. */
    public static final int OPUS_FRAME_SIZE = 960;
    /** This is 20 milliseconds. We are only dealing with 20ms opus packets. */
    public static final int OPUS_FRAME_TIME_AMOUNT = 20;
    /** We want to use stereo. If the audio given is mono, the encoder promotes it to Left and Right mono (stereo that is the same on both sides) */
    public static final int OPUS_CHANNEL_COUNT = 2;

    private final long userId;
    private final byte[] opusAudio;
    private final AudioPacket rawPacket;

    private short[] decoded;
    private boolean triedDecode;

    public OpusPacket(AudioPacket packet, long userId)
    {
        this.rawPacket = packet;
        this.userId = userId;
        this.opusAudio = packet.getEncodedAudio().array();
    }

    /**
     * The sequence number of this packet. This is used as ordering key for {@link #compareTo(OpusPacket)}.
     * <br>A char represents an unsigned short value in this case.
     *
     * <p>Note that packet sequence is important for decoding. If a packet is out of sequence the decode
     * step will fail.
     *
     * @return The sequence number of this packet
     *
     * @see    <a href="http://www.rfcreader.com/#rfc3550_line548" target="_blank">RTP Header</a>
     */
    public char getSequence()
    {
        return rawPacket.getSequence();
    }

    /**
     * The timestamp for this packet. As specified by the RTP header.
     *
     * @return The timestamp
     *
     * @see    <a href="http://www.rfcreader.com/#rfc3550_line548" target="_blank">RTP Header</a>
     */
    public int getTimestamp()
    {
        return rawPacket.getTimestamp();
    }

    /**
     * The synchronization source identifier (SSRC) for the user that sent this audio packet.
     *
     * @return The SSRC
     *
     * @see    <a href="http://www.rfcreader.com/#rfc3550_line548" target="_blank">RTP Header</a>
     */
    public int getSSRC()
    {
        return rawPacket.getSSRC();
    }

    public long getUserId()
    {
        return userId;
    }

    /**
     * The raw opus audio, copied to a new array.
     *
     * @return The raw opus audio
     */
    public byte[] getOpusAudio()
    {
        //prevent write access to backing array
        return Arrays.copyOf(opusAudio, opusAudio.length);
    }


    /**
     * Decodes and adjusts the opus audio for the specified volume.
     * <br>The provided volume should be a double precision floating point in the interval from 0 to 1.
     * In this case 0.5 would represent 50% volume for instance.
     *
     * @param  decoded
     *         The decoded audio data
     * @param  volume
     *         The volume
     *
     * @throws java.lang.IllegalArgumentException
     *         If {@code decoded} is null
     *
     */
    @SuppressWarnings("ConstantConditions") // the null case is handled with an exception
    public static byte[] getAudioData(short[] decoded, double volume)
    {
        if (decoded == null)
            throw new IllegalArgumentException("Cannot get audio data from null");
        int byteIndex = 0;
        byte[] audio = new byte[decoded.length * 2];
        for (short s : decoded)
        {
            if (volume != 1.0)
                s = (short) (s * volume);

            byte leftByte  = (byte) ((s >>> 8) & 0xFF);
            byte rightByte = (byte)  (s        & 0xFF);
            audio[byteIndex] = leftByte;
            audio[byteIndex + 1] = rightByte;
            byteIndex += 2;
        }
        return audio;
    }

    @Override
    public int compareTo(OpusPacket o)
    {
        return getSequence() - o.getSequence();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getSequence(), getTimestamp(), getOpusAudio());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof OpusPacket))
            return false;
        OpusPacket other = (OpusPacket) obj;
        return getSequence() == other.getSequence()
            && getTimestamp() == other.getTimestamp()
            && getSSRC() == other.getSSRC();
    }
}