package xyz.vitox.discordtool.util;

import okhttp3.*;

import java.util.Arrays;

public class RequestAPI {

    OkHttpClient client = new OkHttpClient().newBuilder().connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS)).build();

    public String checkHWID() {
        return makePOSTRequest("/v1/checkHWID" +
                        "",
                "{\n" +
                        "   \"cid\":"+ SystemUtil.getSerialNumber("C") +",\n" +
                        "   \"client\":\"Panda-App\",\n" +
                        "   \"pcName\":\""+ SystemUtil.getPCName() +"\"\n" +
                        "}");
    }

    public static String lastCurrentDate;

    public String checkKey(String key) {
        lastCurrentDate = SystemUtil.getCurrentDate();
        return makePOSTRequest("/v1/checkKey" +
                        "",
                "{\n" +
                        "   \"requestDate\":\""+ lastCurrentDate +"\",\n" +
                        "   \"key\":\""+ key +"\",\n" +
                        "   \"cid\":"+ SystemUtil.getSerialNumber("C") +",\n" +
                        "   \"client\":\"Panda-App\",\n" +
                        "   \"pcName\":\""+ SystemUtil.getPCName() +"\",\n" +
                        "   \"token\":\""+ SystemUtil.generateOTP(SystemUtil.getSerialNumber("C"), lastCurrentDate, "") +"\"\n" +
                        "}");
    }

    public String makePOSTRequest(String restRoute, String bodyText) {
        try {
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, bodyText);
            Request request = new Request.Builder()
                    .url("https://api.localhost.com" + restRoute)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", "PandaLauncher6988")
                    .addHeader("Authorization", "Basic cGFuZGFsYXVuY2hlcjpid3RlcENvZUU0M2xjSWw2b21XelR2c2tjbUhXUzFEOUl5bk9jSmRoNkQySEZTUWwxSXZNVkZKcg==")
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error";
    }

}
