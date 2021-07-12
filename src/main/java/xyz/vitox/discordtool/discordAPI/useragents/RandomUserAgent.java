package xyz.vitox.discordtool.discordAPI.useragents;

import xyz.vitox.discordtool.discordAPI.useragents.types.ChromeUserAgent;
import xyz.vitox.discordtool.discordAPI.useragents.types.FirefoxUserAgent;
import xyz.vitox.discordtool.discordAPI.useragents.types.UserAgent;

import java.util.ArrayList;
import java.util.List;

public class RandomUserAgent {

    private UserAgent userAgent;

    private List<UserAgent> userAgentList = new ArrayList<>();

    public RandomUserAgent() {
        initUseragents();
        this.userAgent = randomListElement(userAgentList);
    }

    public UserAgent getUserAgent() {
        return userAgent;
    }

    private void initUseragents() {
        userAgentList.add(new ChromeUserAgent());
        userAgentList.add(new FirefoxUserAgent());
    }

    private UserAgent randomListElement(List<UserAgent> list) {
        return list.get((int) Math.floor(Math.random() * list.size()));
    }
}
