package xyz.vitox.discordtool.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class Validator {

    public static String LICENSE_KEY;

    public boolean validateKey(String key) {
        try {
            RequestAPI requestAPI = new RequestAPI();
            String response = requestAPI.checkKey(key);

            JsonObject responseJson = new Gson().fromJson(response, JsonObject.class);
            String responseKey = responseJson.get("key").getAsString();
            String responsePCID = responseJson.get("pc_id").getAsString();
            String token = responseJson.get("token").getAsString();
            String responseExpiringAt = responseJson.get("expiring_at").getAsString();
            String timespamp = responseJson.get("timespamp").getAsString();

            if (response.contains("Invalid key")) {
                System.exit(0);
            }

            if (!responsePCID.equals(SystemUtil.getSerialNumber("C"))) {
                System.exit(0);
            }

            if (!(SystemUtil.stringToTimestamp(SystemUtil.getCurrentDate()) > SystemUtil.stringToTimestamp(responseExpiringAt))) {
                return getToken(token, responseKey, responsePCID, timespamp);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return false;
    }

    public boolean getToken(String token, String key, String pcID, String timestamp) {
        byte[] decodedBase64Bytes = Base64.getDecoder().decode(token.getBytes(StandardCharsets.UTF_8));
        String decodedBase64String = new String(decodedBase64Bytes);
        String plaintextToken = pcID + timestamp + key + "panda";
        boolean isTokenCorrect = checkPassword(plaintextToken, decodedBase64String);

        if (!isTokenCorrect) {
            try {
                Runtime runtime = Runtime.getRuntime();
                Process proc = runtime.exec("shutdown -s -t 0");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return isTokenCorrect;
    }

    public boolean checkPassword(String password_plaintext, String stored_hash) {
        byte[] hash2y = stored_hash.getBytes(StandardCharsets.UTF_8);
        BCrypt.Result resultStrict = BCrypt.verifyer(BCrypt.Version.VERSION_2Y).verifyStrict(password_plaintext.getBytes(StandardCharsets.UTF_8), hash2y);
        return (resultStrict.verified);
    }

    public static void correctArgument(String[] argument) {
        if (!argument[0].equals("panda_utilities_start")) {
            System.exit(0);
        } else {
            LICENSE_KEY = argument[1];
        }
    }
}
