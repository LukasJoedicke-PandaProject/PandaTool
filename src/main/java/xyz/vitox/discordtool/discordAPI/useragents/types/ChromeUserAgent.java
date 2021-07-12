package xyz.vitox.discordtool.discordAPI.useragents.types;

import java.util.Arrays;
import java.util.List;

public class ChromeUserAgent extends UserAgent {

    private List<String> browserVersions;
    private List<String> browserAgents;
    private String randomBrowserVersion;

    public ChromeUserAgent() {
        setBrowserName("Chrome");
        setBrowserVersions();
        randomBrowserVersion = randomListElement(browserVersions);
        setBrowserAgents();
    }

    @Override
    public void setBrowserVersions() {
        browserVersions = Arrays.asList("89.0.4389", "87.0.4280", "85.0.4183", "86.0.4240");
    }

    @Override
    public void setBrowserAgents() {
        browserAgents = Arrays.asList(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/"+ randomBrowserVersion +" Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/"+ randomBrowserVersion+" Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/"+ randomBrowserVersion +" Safari/537.36"
        );
    }

    public String getBrowserAgent() {
        return randomListElement(browserAgents);
    }

    public String getBrowserVersion() {
        return randomBrowserVersion;
    }
}
