package xyz.vitox.discordtool.discordAPI.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import okhttp3.WebSocket;
import xyz.vitox.discordtool.discordAPI.api.gateway.DiscordUserGateway;
import xyz.vitox.discordtool.discordAPI.api.gateway.voice.DiscordVoiceGateway;
import xyz.vitox.discordtool.discordAPI.tokenManager.Token;
import xyz.vitox.discordtool.discordAPI.tokenManager.TokenManager;
import xyz.vitox.discordtool.tab.serverSpamComponents.FriendRequest;
import xyz.vitox.discordtool.tab.serverSpamComponents.JoinLeave;
import xyz.vitox.discordtool.tab.serverSpamComponents.ReactEmoji;
import xyz.vitox.discordtool.tab.verifierComponents.CommunityVerify;
import xyz.vitox.discordtool.tab.verifierComponents.ServerCaptchaVerifiy;
import xyz.vitox.discordtool.tab.voiceSpamComponents.VoiceChannel;
import xyz.vitox.discordtool.tab.voiceSpamComponents.VoiceSpamSettings;
import xyz.vitox.discordtool.tab.voiceSpamComponents.musicManager.TokenAudioPlayer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class is used to execute requests from DiscordAPI.class with multiple Threads for
 */
public class DiscordMultiRequests {

    private final DiscordAPI discordAPI = new DiscordAPI();
    private int numRunnables = 10;

    /**
     * Get Token informations and increase progressbar
     * @param discordTokens
     * @param progressBar
     * @return
     */
    public HashMap<String, String> getTokenInformation(ArrayList<String> discordTokens, ProgressBar progressBar) {

        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

        HashMap<String, String> tokenAndInfo = new HashMap<>();

        AtomicInteger processedTokens = new AtomicInteger();
        AtomicReference<AtomicInteger> prevProcessedTokens = new AtomicReference<>(new AtomicInteger());
        discordTokens.forEach(token -> executor.execute(() -> {
            try {
                prevProcessedTokens.set(processedTokens);
                processedTokens.getAndIncrement();
                float procTokens = processedTokens.floatValue();

                String fingerPrintResponse = discordAPI.getFingerprint();

                JsonElement element = new Gson().fromJson(fingerPrintResponse, JsonElement.class);
                JsonObject jsonObjFingerprint = element.getAsJsonObject();
                String fingerprint = jsonObjFingerprint.get("fingerprint").getAsString();

                tokenAndInfo.put(token + ";" + fingerprint, discordAPI.getTokenInformation(token, fingerprint));

                if (!(prevProcessedTokens.get().floatValue() < processedTokens.floatValue())) {
                    progressBar.setProgress(procTokens / (float) discordTokens.size());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        shutdownExecutor(executor);
        progressBar.setProgress(1);
        return tokenAndInfo;
    }

    public Thread reconnectServer(String inviteCode, String serverID, Button reconnectButton, int delay, ArrayList<Token> failedTokens) {
        return new Thread(() -> {

            BlockingQueue<Runnable> queue2 = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler2 = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor2 = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue2, handler2);

            failedTokens.forEach(token -> {
                executor2.execute(() -> {
                    try {
                        discordAPI.leaveServer(token, serverID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                setDelay(delay);
            });

            shutdownExecutor(executor2);

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

            failedTokens.forEach(token -> {
                executor.execute(() -> {
                    try {
                        discordAPI.joinServer(token, inviteCode);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                setDelay(delay);
            });

            shutdownExecutor(executor);
            ServerCaptchaVerifiy.failedTokensLastServer.clear();
            Platform.runLater(() -> {
                reconnectButton.setText("Reconnect failed tokens");
                reconnectButton.setStyle("-fx-background-color: -fx-positive;");
            });
        });
    }

    public Thread joinDiscordServer(String inviteCode, int delay, Button startButton) {

        return new Thread(() -> {
            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

            AtomicInteger joinedCount = new AtomicInteger();

            TokenManager.tokensToUse().forEach(token -> {
                executor.execute(() -> {
                    try {
                        String response = discordAPI.joinServer(token, inviteCode);
                        if (!response.equals("Couldnt connect to guild")) {
                            joinedCount.getAndIncrement();
                            Platform.runLater(() -> JoinLeave.joinedCountLabel.setText("Joined: " + joinedCount.get() + "/" + TokenManager.tokensToUse().size()));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            });
            shutdownExecutor(executor);

            Platform.runLater(() -> {
                JoinLeave.isJoinButtonPressed = false;
                startButton.setText("Join");
                startButton.setStyle("-fx-background-color: -fx-positive;");
            });
        });

    }

    public Thread leaveDiscordServer(String serverID, int delay, Button leaveButton) {

        return new Thread(() -> {

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

            TokenManager.tokensToUse().forEach(token -> {
                executor.execute(() -> {
                    try {
                        discordAPI.leaveServer(token, serverID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                setDelay(delay);
            });

            shutdownExecutor(executor);
            Platform.runLater(() -> {
                JoinLeave.isLeaveButtonPressed = false;
                leaveButton.setText("Leave");
                leaveButton.setStyle("-fx-background-color: -fx-positive;");
            });
        });

    }

    public Thread reactToMessage(String channelID, String messageID, String emoji, int delay, Button reactButton) {

        return new Thread(() -> {

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

            AtomicInteger reactionCount = new AtomicInteger();
            TokenManager.tokensToUse().forEach(token -> {
                executor.execute(() -> {
                    try {
                        String response = discordAPI.reactMessage(token, channelID, messageID, emoji);
                        if (!response.contains("{\"message\": \"Unknown Message\", \"code\": 10008}") && !response.contains("You need to verify your account in order to perform this action.")) {
                            reactionCount.getAndIncrement();
                            Platform.runLater(() -> ReactEmoji.reactionCount.setText("Reacted: " + reactionCount.get() + "/" + TokenManager.tokensToUse().size()));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                setDelay(delay);
            });

            shutdownExecutor(executor);
            Platform.runLater(() -> {
                ReactEmoji.isReactPressed = false;
                reactButton.setText("React");
                reactButton.setStyle("-fx-background-color: -fx-positive;");
            });
        });

    }

    public Thread removeReaction(String channelID, String messageID, String emoji, int delay) {

        return new Thread(() -> {

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

            TokenManager.tokensToUse().forEach(token -> {
                executor.execute(() -> {
                    try {
                        discordAPI.removeReaction(token, channelID, messageID, emoji);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                setDelay(delay);
            });

            shutdownExecutor(executor);
//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
//                    JoinLeave.isLeaveButtonPressed = false;
//                    leaveButton.setText("Leave");
//                    leaveButton.setStyle("-fx-background-color: -fx-positive;");
//                }
//            });
        });

    }

    public Thread sendFriendRequest(String userID, int delay, Button sendButton) {

        return new Thread(() -> {

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

            TokenManager.tokensToUse().forEach(token -> {
                executor.execute(() -> {
                    try {
                        discordAPI.sendFriendRequest(token, userID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                setDelay(delay);
            });

            shutdownExecutor(executor);
            Platform.runLater(() -> {
                FriendRequest.isSendButtonPressed = false;
                sendButton.setText("Send");
                sendButton.setStyle("-fx-background-color: -fx-positive;");
            });
        });

    }

    public Thread removeFriendRequest(String userID, int delay, Button removeButton) {

        return new Thread(() -> {

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

            TokenManager.tokensToUse().forEach(token -> {
                executor.execute(() -> {
                    try {
                        discordAPI.removeFriendRequest(token, userID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                setDelay(delay);
            });

            shutdownExecutor(executor);
            Platform.runLater(() -> {
                FriendRequest.isRemoveButtonPressed = false;
                removeButton.setText("Remove");
            });
        });

    }

    public Thread changeProfilePictures(File[] profilePicture) {

        return new Thread(() -> {

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

            TokenManager.tokensToUse().forEach(token -> {
                Random r = new Random();
                executor.execute(() -> {
                    try {
                        if (profilePicture != null) {
                            discordAPI.changeProfilePicture(token, profilePicture[r.nextInt(profilePicture.length)]);
                        } else {
                            discordAPI.changeProfilePicture(token, null);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            });

            shutdownExecutor(executor);
        });

    }

    public Thread friendRequestLoop(String userID, int delay) {

        return new Thread(() -> {

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

            while (true) {
                TokenManager.tokensToUse().forEach(token -> {
                    executor.execute(() -> {
                        try {
                            discordAPI.removeFriendRequest(token, userID);
                            setDelay(delay);
                            discordAPI.sendFriendRequest(token, userID);
                            setDelay(delay);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                });
            }
        });

    }

    public Thread leaveAllGuilds() {

        return new Thread(() -> {

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

                TokenManager.tokensToUse().forEach(token -> {
                    executor.execute(() -> {
                        try {
                            String allGuilds = discordAPI.getAllGuilds(token);

                            Gson gson = new Gson();
                            JsonElement element = gson.fromJson(allGuilds, JsonElement.class);
                            JsonArray guildObjects = element.getAsJsonArray();

                            for (int guildCount = 0; guildCount < guildObjects.size(); guildCount++) {
                                String id = guildObjects.get(guildCount).getAsJsonObject().get("id").getAsString();
                                discordAPI.leaveServer(token, id);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                });
        });

    }

    public Thread verifyCommunityGuild(String inviteCode, Button startBtn) {

        return new Thread(() -> {

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

            final int[] approvedCount = {0};
            TokenManager.tokensToUse().forEach(token -> {
                executor.execute(() -> {
                    try {
                        HashMap<String, String> verifyRequirementsAndGuildID = discordAPI.getGuildMemberVerification(token, inviteCode);

                        JsonElement element = new Gson().fromJson(verifyRequirementsAndGuildID.values().toArray()[0].toString(), JsonElement.class);
                        JsonObject jsonObj = element.getAsJsonObject();
                        JsonArray formFields = jsonObj.getAsJsonArray("form_fields");
                        formFields.get(0).getAsJsonObject().addProperty("response", true);
                        String responseFromGuild = discordAPI.sendVerificationToCommunityGuild(token, verifyRequirementsAndGuildID.keySet().toArray()[0].toString(), jsonObj.toString());

                        Platform.runLater(() -> {
                            if (responseFromGuild.contains("APPROVED")) {
                                approvedCount[0]++;
                                CommunityVerify.communityVerifiedCountLabel.setText("Verified (" + approvedCount[0] + "/" + TokenManager.tokensToUse().size() + ")");
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            });

            shutdownExecutor(executor);
            Platform.runLater(() -> {
                approvedCount[0] = 0;
                startBtn.setText("Start");
                startBtn.setStyle("-fx-background-color: -fx-positive");
            });
        });

    }

    public Thread stopMusic() {
        return new Thread(() -> {

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

            DiscordAPI.audioPlayers.forEach(audioPlayer -> {
                executor.execute(() -> {
                    audioPlayer.stopSong();
                });
            });
        });
    }

    public static int songDelay = 0;
    public Thread playMusic(String url, String guildID, Button playBtn) {
       return new Thread(() -> {

           BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
           RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
           ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

            if (DiscordAPI.audioPlayers.size() == 0) {
                DiscordAPI.sendHandlers.forEach(sendHandler -> {
                    executor.execute(() -> {
                        TokenAudioPlayer tokenAudioPlayer = new TokenAudioPlayer(guildID, sendHandler);
                        DiscordAPI.audioPlayers.add(tokenAudioPlayer);
                        tokenAudioPlayer.loadSong(url);
                    });
                    setDelay(songDelay);
                });
            } else {
                DiscordAPI.audioPlayers.forEach(audioPlayer -> {
                    executor.execute(() -> {
                        audioPlayer.stopSong();
                        audioPlayer.loadSong(url);
                    });
                    setDelay(songDelay);
                });
            }

           shutdownExecutor(executor);
           Platform.runLater(() -> {
               VoiceChannel.canClickPlayButton = true;
               playBtn.setStyle("-fx-background-color: -fx-positive;");
               playBtn.setText("Play");
           });
        });
    }

    public static int joinVoiceChannelDelay = 0;
    public Thread joinVoiceChannel(String serverID, String channelID, Button joinBtn) {

        return new Thread(() -> {

            if (VoiceSpamSettings.advancedConnection) {
                joinVoiceChannelDelay = 725;
            }

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

            if (DiscordVoiceGateway.openGateways.size() == 0) {

                TokenManager.tokensToUse().forEach(token -> {
                    executor.execute(() -> {
                        DiscordVoiceGateway gatewayTest = new DiscordVoiceGateway(token.getToken(), token.getId(), serverID, channelID);
                        WebSocket webSocket = gatewayTest.newGateay();
                        gatewayTest.initHeartbeat(webSocket);
                    });
                    setDelay(joinVoiceChannelDelay);
                });
            } else {

                for (WebSocket gateWay : DiscordVoiceGateway.openGateways) {
                    executor.execute(() -> {
                        DiscordVoiceGateway gatewayTest = new DiscordVoiceGateway(serverID, channelID);
                        gatewayTest.requestVoiceServer(gateWay, serverID, channelID);
                    });
                    setDelay(joinVoiceChannelDelay);
                }
            }

            shutdownExecutor(executor);
            Platform.runLater(() -> {
                VoiceChannel.canClickJoinButton = true;
                joinBtn.setStyle("-fx-background-color: -fx-positive;");
                joinBtn.setText("Join");
            });
        });

    }

    public Thread connectGateway(Button startBtn) {

        return new Thread(() -> {

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

            if (DiscordUserGateway.openUserGateways.size() == 0) {
                TokenManager.tokensToUse().forEach(token -> {
                    executor.execute(() -> {
                        DiscordUserGateway discordUserGateway = new DiscordUserGateway(token);
                        WebSocket webSocket = discordUserGateway.newGateay();
                        discordUserGateway.initHeartbeat(webSocket);
                    });
                    setDelay(1000);
                });
            } else {

//                for (WebSocket gateWay : DiscordUserGateway.openUserGateways) {
//                    executor.execute(() -> {
//                        DiscordVoiceGateway gatewayTest = new DiscordVoiceGateway(serverID, channelID);
//                        gatewayTest.requestVoiceServer(gateWay, serverID, channelID);
//                    });
//                }
            }

            shutdownExecutor(executor);
            Platform.runLater(() -> {
                ServerCaptchaVerifiy.startButtonPressed = true;
                startBtn.setStyle("-fx-background-color: -fx-negative;");
                startBtn.setText("Disable");
            });
        });

    }

    public Token checkIfTokenIsInGuild(String guildID) {

        Optional<Token> firstToken = Arrays.stream(TokenManager.tokensToUse().toArray(new Token[0])).filter(discordToken -> {
            try {
                String guildInformation = discordAPI.getGuildInformation(discordToken, guildID);
                System.out.println(guildInformation);
                return guildInformation.contains(guildID) && !guildInformation.contains("Missing Access");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }).findFirst();

        return firstToken.orElse(null);

    }

    public String getGuildIDByChannelID(String channelID) {

        try {
            for (int tokenIndex = 0; tokenIndex < TokenManager.tokensToUse().size(); tokenIndex++) {
                String channelInformation = discordAPI.getChannel(TokenManager.tokensToUse().get(tokenIndex), channelID);
                if (channelInformation.contains("Unknown Channel") || !channelInformation.contains("guild_id")) {
                    continue;
                }
                JsonElement element = new Gson().fromJson(channelInformation, JsonElement.class);
                JsonObject jsonObj = element.getAsJsonObject();
                return jsonObj.get("guild_id").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public Thread writeMessage(String message, String channelID, boolean tts, boolean typing, File attachmentFile, int delay) {

        return new Thread(() -> {

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

            while (true) {
                TokenManager.tokensToUse().forEach(token -> {
                    executor.execute(() -> {
                        try {
                            discordAPI.writeMessage(token, message, tts, channelID, attachmentFile);
                            if (typing) {
                                discordAPI.sendTyping(token, channelID);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    setDelay(delay);
                });
            }
        });
    }

    public Thread writeUser(String message, String channelID, boolean tts, boolean typing, File attachmentFile, int delay) {

        return new Thread(() -> {

            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

//            while (true) {
//                TokenManager.tokensToUse().forEach(token -> {
//                    executor.execute(() -> {
//                        try {
//                            String userChannelID = discordAPI.getFriendChannelID(token, channelID);
//                            discordAPI.writeMessage(token, message, tts, userChannelID, attachmentFile);
//                            if (typing) {
//                                discordAPI.sendTyping(token, userChannelID);
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    });
//                    setDelay(delay);
//                });
//            }
        });
    }

    public static ArrayList<Thread> massPingThread = new ArrayList<>();
    public Thread writeMessageNoThread(String message, String channelID, boolean tts, boolean typing, File attachmentFile, int delay) {

        return new Thread(() -> {
            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(numRunnables, true);
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            ExecutorService executor = new ThreadPoolExecutor(numRunnables, numRunnables, 0L, TimeUnit.MILLISECONDS, queue, handler);

            while (true) {
                TokenManager.tokensToUse().forEach(token -> {
                    try {
                        discordAPI.writeMessage(token, message, tts, channelID, attachmentFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setDelay(delay);
                });
            }
        });
    }

    private void setDelay(int delay) {
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(50);
            } catch (Exception ignored) {

            }
        }
    }

}
