package xyz.vitox.discordtool.tab.serverRecon;

public class ServerEmoji {

    public String emojiName;
    public String emojiID;

    public ServerEmoji(String roleName, String roleID) {
        this.emojiName = roleName;
        this.emojiID = roleID;
    }

    public String getEmojiName() {
        return emojiName;
    }

    public void setEmojiName(String emojiName) {
        this.emojiName = emojiName;
    }

    public String getEmojiID() {
        return emojiID;
    }

    public void setEmojiID(String emojiID) {
        this.emojiID = emojiID;
    }
}
