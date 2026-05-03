package com.openway.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory that creates and manages a single {@link WebDriver} instance per thread.
 *
 * <p>Using {@link ThreadLocal} ensures that parallel test execution does not share
 * driver instances across test threads.
 */
public final class DriverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DriverFactory.class);

    /** Thread-local storage so parallel tests each get their own driver. */
    private static final ThreadLocal<WebDriver> DRIVER_HOLDER = new ThreadLocal<>();

    private DriverFactory() {
        // utility class
    }

    /**
     * Returns the {@link WebDriver} for the current thread, creating one if needed.
     *
     * @return a ready-to-use {@link ChromeDriver} instance
     */
    public static WebDriver getDriver() {
        if (DRIVER_HOLDER.get() == null) {
            LOG.info("Setting up ChromeDriver via WebDriverManager...");
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();

            boolean headless = ConfigReader.getBoolean("browser.headless", false);
            if (headless) {
                LOG.info("Launching Chrome in headless mode.");
                options.addArguments("--headless=new");
            }

            // Recommended flags for stable CI / container execution
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            // Open in a new window (not a tab in an existing window)
            options.addArguments("--new-window");

            WebDriver driver = new ChromeDriver(options);
            DRIVER_HOLDER.set(driver);
            LOG.info("ChromeDriver instance created for thread '{}'.", Thread.currentThread().getName());
        }
        return DRIVER_HOLDER.get();
    }

    /**
     * Quits the {@link WebDriver} for the current thread and removes it from the
     * thread-local store.
     */
    public static void quitDriver() {
        WebDriver driver = DRIVER_HOLDER.get();
        if (driver != null) {
            LOG.info("Quitting ChromeDriver for thread '{}'.", Thread.currentThread().getName());
            driver.quit();
            DRIVER_HOLDER.remove();
        }
    }
}
