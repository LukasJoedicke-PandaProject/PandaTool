package xyz.vitox.discordtool.tab.voiceSpamComponents.musicManager;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import javafx.application.Platform;
import xyz.vitox.discordtool.discordAPI.api.DiscordMultiRequests;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.packets.OpusPacket;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.sendSystem.SendHandler;
import xyz.vitox.discordtool.tab.voiceSpamComponents.VoiceSpamInfo;
import xyz.vitox.discordtool.tab.voiceSpamComponents.VoiceSpamSettings;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class TokenAudioPlayer {

    private DiscordMultiRequests discordMultiRequests = new DiscordMultiRequests();
    private AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    public static ArrayList<AudioPlayer> players = new ArrayList<>();
    private String guildID;
    private SendHandler sendHandler;
    private AudioPlayer player;
    private ByteBuffer buffer;
    private MutableAudioFrame frame;
    private boolean isPlaying;

    public TokenAudioPlayer(String guildID, SendHandler sendHandler) {
        this.guildID = guildID;
        this.sendHandler = sendHandler;
        AudioSourceManagers.registerLocalSource(playerManager);
        AudioSourceManagers.registerRemoteSources(playerManager);
        player = playerManager.createPlayer();
        players.add(player);
        buffer = ByteBuffer.allocate(1024);
        frame = new MutableAudioFrame();
        frame.setBuffer(buffer);
    }

    public void loadSong(String song) {

        playerManager.loadItem(song, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                Platform.runLater(() -> {
                    VoiceSpamInfo.currentlyPlaying.setText("Currently Playing: " + track.getInfo().title);
                });
                player.playTrack(track);
                player.setVolume(250);
                isPlaying = true;
                    long lastFrameSent = System.currentTimeMillis();
                    while (isPlaying) {
                        try {
                            boolean canProvide = player.provide(frame);
                            if (canProvide) {
                                sendHandler.setCanProvide(true);
                                sendHandler.setProvide20MsAudio((ByteBuffer) buffer.flip());
                            } else {
                                sendHandler.setCanProvide(false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            long sleepTime = (OpusPacket.OPUS_FRAME_TIME_AMOUNT) - (System.currentTimeMillis() - lastFrameSent);
                            if (sleepTime > 0) {
                                try {
                                    Thread.sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                            if (System.currentTimeMillis() < lastFrameSent + 60) {
                                lastFrameSent += OpusPacket.OPUS_FRAME_TIME_AMOUNT;
                            } else {
                                lastFrameSent = System.currentTimeMillis();
                            }
                        }
                    }

            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
            }

            @Override
            public void noMatches() {
                System.out.println("dd");
            }

            @Override
            public void loadFailed(FriendlyException throwable) {
                throwable.printStackTrace();
                System.out.println("Loading failed");
            }
        });
    }

    public void stopSong() {
        ByteBuffer buffer =  ByteBuffer.allocate(1024);
        sendHandler.setProvide20MsAudio(buffer);
        player.getPlayingTrack().stop();
        isPlaying = false;
    }
}
