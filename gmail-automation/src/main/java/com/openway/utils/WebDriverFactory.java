package com.openway.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * WebDriver factory for creating and managing Selenium WebDriver instances
 */
public class WebDriverFactory {
    private static final Logger logger = LogManager.getLogger(WebDriverFactory.class);

    /**
     * Create a Chrome WebDriver instance
     * @return WebDriver instance
     */
    public static WebDriver createChromeDriver() {
        logger.info("Creating Chrome WebDriver instance");
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriver driver = new ChromeDriver(options);
        logger.info("Chrome WebDriver created successfully");
        return driver;
    }

    /**
     * Quit the WebDriver instance
     * @param driver WebDriver instance to quit
     */
    public static void quitDriver(WebDriver driver) {
        if (driver != null) {
            driver.quit();
            logger.info("WebDriver closed successfully");
        }
    }
}
