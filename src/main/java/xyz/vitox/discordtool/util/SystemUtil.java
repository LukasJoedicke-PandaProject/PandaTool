package xyz.vitox.discordtool.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import org.apache.commons.io.IOUtils;
import xyz.vitox.discordtool.Main;
import xyz.vitox.discordtool.discordAPI.tokenManager.Token;
import xyz.vitox.discordtool.tab.verifierComponents.ServerCaptchaVerifiy;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class SystemUtil {

    public static void writeFile(File file, ArrayList<String> information) {
        try {
            if (information.size() > 0) {
                BufferedWriter invalidWriter;
                invalidWriter = new BufferedWriter(new FileWriter(file));
                for (String verifiedToken : information) {
                    invalidWriter.write(verifiedToken);
                    invalidWriter.newLine();
                }
                invalidWriter.flush();
                invalidWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeFileToken(File file, ArrayList<Token> information) {
        try {
            if (information.size() > 0) {
                BufferedWriter invalidWriter;
                invalidWriter = new BufferedWriter(new FileWriter(file));
                for (Token token : information) {
                    invalidWriter.write(token.getToken());
                    invalidWriter.newLine();
                }
                invalidWriter.flush();
                invalidWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> contentFromFileToArraylist(File tokenFile) {
        ArrayList<String> tokens = new ArrayList<>();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(tokenFile));
            String line;
            while ((line = br.readLine()) != null) {
                tokens.add(line);
            }
            br.close();
            return tokens;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSerialNumber(String drive) {
        String result = "";
        try {
            File file = File.createTempFile("realhowto", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new FileWriter(file);

            String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                    + "Set colDrives = objFSO.Drives\n" + "Set objDrive = colDrives.item(\"" + drive + "\")\n"
                    + "Wscript.Echo objDrive.SerialNumber"; // see note
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.trim();
    }

    public static void downloadZipFile(String fromUrl, String localFileName, ProgressBar progressBar) {
        Task worker = downloadTask(fromUrl, localFileName);
        progressBar.progressProperty().bind(worker.progressProperty());

        Thread downloadFileThread = new Thread(worker);
        downloadFileThread.start();
    }

    public static Task downloadTask(String fromUrl, String localFileName) {
        return new Task() {
            @Override
            protected Void call() {
                try {
                    File localFile = new File(localFileName);
                    if (localFile.exists()) {
                        localFile.delete();
                    }
                    localFile.createNewFile();
                    URL url = new URL(fromUrl);
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(localFileName));
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    conn.setRequestProperty("User-Agent", "Panda");
                    int fileSize = conn.getContentLength();
                    InputStream in = conn.getInputStream();
                    byte[] buffer = new byte[54 * 1024 * 1024];

                    int numRead;
                    int totalProccessed = 0;

                    while ((numRead = in.read(buffer)) != -1) {
                        totalProccessed += numRead;
                        out.write(buffer, 0, numRead);
                        updateProgress(totalProccessed, fileSize);
                    }
                    in.close();
                    out.close();
                    conn.disconnect();
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    File zipFile = new File(localFileName);
                    unzipFile(zipFile);
                }
                return null;
            }
        };
    }

    public static String getCurrentDate() {
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dt.format(date);
    }

    public static long stringToTimestamp(String dateString) {
        dateString = dateString.replaceAll("\\s+", "T");
        LocalDateTime dateTime = LocalDateTime.parse(dateString);
        return Timestamp.valueOf(dateTime).getTime() / 1000L;
    }

    public static String generateOTP(String deviceID, String currentTimestamp, String custom) {
        String oneTimePassword = deviceID + "panda" + currentTimestamp + "security" + custom;
        String bCryptedPassword = BCrypt.with(BCrypt.Version.VERSION_2Y).hashToString(12, oneTimePassword.toCharArray());

        return Base64.getEncoder().encodeToString(bCryptedPassword.getBytes(StandardCharsets.UTF_8));
    }

    public static void unzipFile(File fileZip) {
        try {
            try (java.util.zip.ZipFile zipFile = new ZipFile(fileZip.getAbsolutePath())) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    File entryDestination = new File(Main.MAIN_FILE_PATH + "/libraries/", entry.getName());
                    if (entry.isDirectory()) {
                        entryDestination.mkdirs();
                    } else {
                        entryDestination.getParentFile().mkdirs();
                        try (InputStream in = zipFile.getInputStream(entry);
                             OutputStream out = new FileOutputStream(entryDestination)) {
                            IOUtils.copy(in, out);
                        }
                    }
                }
            } finally {
                Platform.runLater(() -> {
                    ServerCaptchaVerifiy.infoLabel.setStyle("-fx-text-fill: #198517");
                    ServerCaptchaVerifiy.infoLabel.setText("Success. Please restart Panda.");
                });
                fileZip.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getPCName() {
        String hostname = "Unknown";

        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException ex) {

        }
        return hostname;
    }
}
