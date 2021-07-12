package xyz.vitox.discordtool.discordAPI.api.gateway.voice.sendSystem;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;

public class SendHandler {

    public static AudioFormat INPUT_FORMAT = new AudioFormat(48000f, 16, 2, true, true);

    private boolean canProvide;
    private boolean isOpus;
    private ByteBuffer provide20MsAudio;

    public SendHandler(boolean canProvide, ByteBuffer provide20MsAudio, boolean isOpus) {
        this.canProvide = canProvide;
        this.provide20MsAudio = provide20MsAudio;
        this.isOpus = isOpus;
    }

    public boolean isCanProvide() {
        return canProvide;
    }

    public void setCanProvide(boolean canProvide) {
        this.canProvide = canProvide;
    }

    public boolean isOpus() {
        return isOpus;
    }

    public void setOpus(boolean opus) {
        isOpus = opus;
    }

    public ByteBuffer getProvide20MsAudio() {
        return provide20MsAudio;
    }

    public void setProvide20MsAudio(ByteBuffer provide20MsAudio) {
        this.provide20MsAudio = provide20MsAudio;
    }

}
