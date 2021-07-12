package xyz.vitox.discordtool.tab.homeComponents.selenium;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import xyz.vitox.discordtool.Main;
import xyz.vitox.discordtool.discordAPI.tokenManager.Token;
import xyz.vitox.discordtool.util.FXUtil;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ChromeUtil {

    File chromeDriverPath = new File(Main.MAIN_FILE_PATH + "/chromedriver.exe");

    public void checkIfChromeDriverExists(Token token) {
        if (chromeDriverPath.exists()) {
            loginToDiscord(token);
        } else {
            Alert alert = FXUtil.createPopup("No ChromeDriver was found. Manually check Chrome version and download it?");
            if (alert.getResult() == ButtonType.YES) {
                checkChromeVersion("C:\\Program Files (x86)\\Google\\Chrome\\Application", token);
            }
        }
    }

    public void checkChromeVersion(String path, Token token) {
        File chromeDir = new File(path);

        if (chromeDir.exists()) {
            if (chromeDir.isDirectory()) {
                File[] directories = new File(chromeDir.getAbsolutePath()).listFiles(File::isDirectory);
                for (File file : directories) {
                    String[] splittedPath = file.getPath().split("\\\\");
                    if (isNumeric(splittedPath[splittedPath.length - 1])) {
                        downloadChromedriver(splittedPath[splittedPath.length - 1], token);
                        return;
                    }
                }
            } else {
                Alert alert = FXUtil.createPopup("Please select the installation directory, not the .exe. Try again?");
                if (alert.getResult() == ButtonType.YES) {
                    checkChromeVersion("C:\\Program Files (x86)\\Google\\Chrome\\Application", token);
                }
            }
        } else {
            TextInputDialog dialog = FXUtil.createInputPopup("Couldn't find a chrome installation.", "Please select your chrome installation path.");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> checkChromeVersion(name, token));
        }
    }

    public void downloadChromedriver(String chromeDriverVersion, Token token) {
        Alert alert = FXUtil.createPopup("Found Chrome Version: " + chromeDriverVersion +". Download ChromeDriver now?");

        if (alert.getResult() == ButtonType.YES) {
            try {

                File chromedriverZip = new File(System.getProperty("user.dir") + "/chromedriver_win32.zip");
                URL website = new URL("https://chromedriver.storage.googleapis.com/"+ getChromedriverDownloadVersion(chromeDriverVersion) + "/chromedriver_win32.zip");
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(chromedriverZip);
                fos.getChannel().transferFrom(rbc, 0, 2147483647);
                fos.close();

                try {
                    unzipChromedriver();
                } finally {
                    chromedriverZip.delete();
                    FXUtil.createPopup("Succesfully downloaded chromedriver.");
                    loginToDiscord(token);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getChromedriverDownloadVersion(String chromeDriverVersion) {
        String downloadURL = "";
        try {
            String[] getMajorVersion = chromeDriverVersion.split("\\.");
            URL url = new URL("https://chromedriver.storage.googleapis.com/LATEST_RELEASE_" + getMajorVersion[0]);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            downloadURL = reader.readLine();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return downloadURL;
    }

    public void unzipChromedriver() {
        try {
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(System.getProperty("user.dir") + "/chromedriver_win32.zip"));
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = chromeDriverPath.getAbsolutePath();
                if (!entry.isDirectory()) {
                    extractFile(zipIn, filePath);
                } else {
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            zipIn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    public void loginToDiscord(Token token) {
        new Thread(() -> {
            SeleniumDiscord seleniumDiscord = new SeleniumDiscord();
            seleniumDiscord.init(token.getToken());
        }).start();
    }

    public static boolean isNumeric(String str) {
        return str.matches(".*\\d.*");
    }
}

