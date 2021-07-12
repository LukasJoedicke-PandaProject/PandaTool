package xyz.vitox.discordtool.discordAPI.useragents.types;

import java.util.List;

public abstract class UserAgent {
    protected String browserName;
    abstract public void setBrowserVersions();
    abstract public void setBrowserAgents();

    public String getBrowserAgent() {
        return null;
    }

    public String getBrowserVersion() {
        return null;
    }

    public String getBrowserName() {
        return browserName;
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }

    public String randomListElement(List<String> list) {
        return list.get((int) Math.floor(Math.random() * list.size()));
    }

}
