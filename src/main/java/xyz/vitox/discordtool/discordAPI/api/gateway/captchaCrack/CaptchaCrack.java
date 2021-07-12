package xyz.vitox.discordtool.discordAPI.api.gateway.captchaCrack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.photo.Photo;
import xyz.vitox.discordtool.Main;
import xyz.vitox.discordtool.discordAPI.api.DiscordAPI;
import xyz.vitox.discordtool.discordAPI.tokenManager.Token;
import xyz.vitox.discordtool.tab.verifierComponents.ServerCaptchaVerifiy;
import xyz.vitox.discordtool.util.FXUtil;
import xyz.vitox.discordtool.util.SystemUtil;
import xyz.vitox.discordtool.util.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class CaptchaCrack {

    private JsonElement eventContent;
    private Token token;
    private final DiscordAPI discordAPI = new DiscordAPI();
    public static String CAPTCHA_BOT_ID = "512333785338216465";
    public static int CAPTCHA_LENGTH = 5;

    public CaptchaCrack(JsonElement eventContent, Token token) {
        this.eventContent = eventContent;
        this.token = token;
    }

    public void start() {
        if (isCorrectAuthor()) {
            String captchaURL = getCaptchaURL();
            if (!captchaURL.isEmpty()) {
                String getImageFromURL = getCaptchaURL();
                readCaptcha(getImageFromURL);
            }
        }
    }

    /**
     * Check if the Author of a created message is "Server Captcha Bot#3928"
     *
     * @return
     */
    public boolean isCorrectAuthor() {
        JsonElement messageAuthor = eventContent.getAsJsonObject().get("author");
        String authorID = messageAuthor.getAsJsonObject().get("id").getAsString();
        return authorID.equals(CAPTCHA_BOT_ID);
    }

    public String getCaptchaURL() {
        JsonArray embedObject = eventContent.getAsJsonObject().get("embeds").getAsJsonArray();

        if (embedObject.size() > 0) {
            JsonElement imageObject = embedObject.get(0).getAsJsonObject().get("image");

            if (imageObject == null) {
                return "";
            }
            if (!(imageObject instanceof JsonNull)) {
                return imageObject.getAsJsonObject().get("url").getAsString();
            } else {
                System.out.println("Error Json Captcha");
                return "";
            }
        } else {
            return "";
        }
    }

    public void readCaptcha(String url) {
        try {
            File denoisedImage = denoiseImage(url);
            Process process = new ProcessBuilder(Main.MAIN_FILE_PATH + "/libraries/darknet64/start.bat", denoisedImage.getAbsolutePath()).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();

            String[] lines = result.split("\\r?\\n");

            StringBuilder captchaSolution = new StringBuilder();
            int currentCaptchaLength = 0;
            for (String outputLine : lines) {
                if (outputLine.contains(":") && outputLine.contains("%")) {
                    currentCaptchaLength++;
                    if (currentCaptchaLength > CAPTCHA_LENGTH) {
                        break;
                    }
                    String captchaCharacter = outputLine.substring(0, 1);
                    captchaSolution.append(captchaCharacter);
                }
            }

            String captchaResult = captchaSolution.toString();
            System.out.println("Finished Captcha: " + captchaResult);
            denoisedImage.delete();
            sendCaptcha(captchaResult);
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCaptcha(String captchaSolution) {
        try {
            String userChannelID = discordAPI.getFriendChannelID(token, CAPTCHA_BOT_ID);
            discordAPI.writeMessage(token, captchaSolution, false, userChannelID, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File denoiseImage(String url) {
        File denoisedImage = null;
        try {
            File captchaImage = saveImageFromURL(url);
            Mat src = Imgcodecs.imread(captchaImage.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
            Mat dst = new Mat(src.rows(), src.cols(), src.type());
            Photo.fastNlMeansDenoising(src, dst, 11);
            Image img = HighGui.toBufferedImage(dst);

            captchaImage.delete();

            denoisedImage = new File(captchaImage.getAbsolutePath() + "-denoised.png");
            ImageIO.write((BufferedImage) img, "png", denoisedImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return denoisedImage;
    }

    public File saveImageFromURL(String url) {
        File imageFile = null;
        try {
            URL imageUrl = new URL(url);

            URLConnection openConnection = imageUrl.openConnection();
            openConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            openConnection.connect();
            BufferedImage rawCaptcha = ImageIO.read(openConnection.getInputStream());

            imageFile = new File(ServerCaptchaVerifiy.captchaDir.getAbsoluteFile() + "/" + Utils.randomString(5) + ".png");
            ImageIO.write(rawCaptcha, "png", imageFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageFile;
    }
}
