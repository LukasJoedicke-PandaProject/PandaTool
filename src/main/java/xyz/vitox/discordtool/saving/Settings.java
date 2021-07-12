package xyz.vitox.discordtool.saving;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import xyz.vitox.discordtool.Main;

import java.io.*;

public class Settings {

    public String settingsPath = Main.MAIN_FILE_PATH + "/settings.json";
    public JsonObject settingJsonObject = new JsonObject();
    public Gson gson = new Gson();

    public void createSettings() {
        try {
            File settingsJson = new File(settingsPath);
            if (settingsJson.createNewFile()) {
                gson = new GsonBuilder().setPrettyPrinting().create();
                Writer writer = new FileWriter(settingsPath, true);
                gson.toJson(settingJsonObject, writer);
                writer.close();
            } else {
                JsonReader reader = new JsonReader(new FileReader(settingsPath));
                JsonElement el = new JsonParser().parse(reader);
                settingJsonObject = el.getAsJsonObject();
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeSettingString(String settingName, String settingValue) {
        try {
            settingJsonObject.addProperty(settingName, settingValue);
            gson = new GsonBuilder().setPrettyPrinting().create();
            Writer writer = new FileWriter(settingsPath);
            gson.toJson(settingJsonObject, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeSettingBoolean(String settingName, Boolean settingValue) {
        try {
            settingJsonObject.addProperty(settingName, settingValue);
            gson = new GsonBuilder().setPrettyPrinting().create();
            Writer writer = new FileWriter(settingsPath);
            gson.toJson(settingJsonObject, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readSettingString(String settingName) {

        if (settingJsonObject.get(settingName) == null) {
            writeSettingString(settingName, "");
        }

        return settingJsonObject.get(settingName).getAsString();
    }

    public Boolean readSettingBoolean(String settingName) {

        if (settingJsonObject.get(settingName) == null) {
            writeSettingBoolean(settingName, false);
        }

        return settingJsonObject.get(settingName).getAsBoolean();
    }

}