package xyz.vitox.discordtool.tab.homeComponents.selenium;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import xyz.vitox.discordtool.Main;

public class SeleniumDiscord {

    public void init(String token) {
        System.setProperty("webdriver.chrome.driver", Main.MAIN_FILE_PATH + "/chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        loginToDiscord(driver, token);
    }

    public static void loginToDiscord(WebDriver driver, String token) {
        try {
            driver.get("https://discord.com/");
            executeScript("setInterval(() => {\n" +
                    "document.body.appendChild(document.createElement `iframe`).contentWindow.localStorage.token = `\""+ token +"\"`\n" +
                    "}, 50);\n" +
                    "setTimeout(() => {\n" +
                    "location.reload();\n" +
                    "}, 2500);", driver);

            Thread.sleep(2000);

            driver.get("https://discord.com/channels/@me");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void executeScript(String script, WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(script);
    }

}
