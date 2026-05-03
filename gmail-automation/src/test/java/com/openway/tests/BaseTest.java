package com.openway.tests;

import com.openway.config.ConfigManager;
import com.openway.utils.WebDriverFactory;
import org.openqa.selenium.WebDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import java.time.Duration;

/**
 * Base test class for all Gmail automation tests
 */
public class BaseTest {
    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected WebDriver driver;

    /**
     * Setup before each test method
     */
    @BeforeMethod
    public void setUp() {
        logger.info("========== Test Setup Started ==========");
        driver = WebDriverFactory.createChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
        logger.info("WebDriver initialized and window maximized");
    }

    /**
     * Teardown after each test method
     */
    @AfterMethod
    public void tearDown() {
        logger.info("========== Test Teardown Started ==========");
        if (driver != null) {
            WebDriverFactory.quitDriver(driver);
        }
        logger.info("========== Test Completed ==========");
    }

    /**
     * Get test email from configuration
     * @return Test email address
     */
    protected String getTestEmail() {
        return ConfigManager.getProperty("test.email");
    }

    /**
     * Get test password from configuration
     * @return Test password
     */
    protected String getTestPassword() {
        return ConfigManager.getProperty("test.password");
    }

    /**
     * Get verification code from configuration (for 2FA)
     * @return Verification code or null if not configured
     */
    protected String getVerificationCode() {
        return ConfigManager.getProperty("test.verification.code");
    }
}
