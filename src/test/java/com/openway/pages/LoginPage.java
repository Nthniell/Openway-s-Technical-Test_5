package com.openway.pages;

import com.openway.utils.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Page Object for the Google Sign-In flow.
 *
 * <p>Google's sign-in page is split into two steps:
 * <ol>
 *   <li>Enter email address → click "Next"</li>
 *   <li>Enter password → click "Next"</li>
 * </ol>
 *
 * <p><strong>Note on 2-Factor Authentication:</strong> Google may challenge with an OTP
 * or additional verification step.  When this happens the test pauses and waits for the
 * inbox URL to become active (up to {@code MANUAL_VERIFICATION_WAIT_SECONDS} seconds),
 * giving a human time to complete the challenge before the test resumes.  In fully
 * automated pipelines, use a dedicated test account whose security settings allow
 * sign-in with just a username + app-password (Google → Security → App Passwords).
 */
public class LoginPage {

    private static final Logger LOG = LoggerFactory.getLogger(LoginPage.class);

    /** Extra seconds to wait so a human can complete a 2FA challenge interactively. */
    private static final int MANUAL_VERIFICATION_WAIT_SECONDS = 120;

    private static final String GMAIL_URL = "https://mail.google.com/mail/";

    private final WebDriver driver;
    private final WebDriverWait wait;

    // --- Locators ---
    private final By emailField     = By.id("identifierId");
    private final By emailNextBtn   = By.id("identifierNext");
    private final By passwordField  = By.name("Passwd");
    private final By passwordNextBtn = By.id("passwordNext");
    /** Selector that is present only when the inbox has loaded successfully. */
    private final By inboxIndicator = By.cssSelector("div[gh='tl']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        int timeoutSeconds = ConfigReader.getInt("browser.timeout.seconds", 30);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    /**
     * Navigates to the Gmail sign-in page.
     *
     * @return this page object (fluent API)
     */
    public LoginPage open() {
        LOG.info("Navigating to {}", GMAIL_URL);
        driver.get(GMAIL_URL);
        return this;
    }

    /**
     * Performs the full sign-in sequence using credentials from the environment /
     * config file.
     *
     * @return an {@link InboxPage} once the inbox is confirmed to have loaded
     * @throws IllegalStateException if credentials are not configured
     */
    public InboxPage login() {
        String username = ConfigReader.get("gmail.username");
        String password = ConfigReader.get("gmail.password");

        if (username == null || username.isBlank()) {
            throw new IllegalStateException(
                    "Gmail username is not set. Supply it via the GMAIL_USERNAME environment variable "
                    + "or the 'gmail.username' property in config.properties.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Gmail password is not set. Supply it via the GMAIL_PASSWORD environment variable "
                    + "or the 'gmail.password' property in config.properties.");
        }

        LOG.info("Entering email address for user '{}'.", username);
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField));
        driver.findElement(emailField).sendKeys(username);
        driver.findElement(emailNextBtn).click();

        LOG.info("Entering password.");
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField));
        driver.findElement(passwordField).sendKeys(password);
        driver.findElement(passwordNextBtn).click();

        waitForInboxOrVerification();

        return new InboxPage(driver);
    }

    /**
     * Waits for the inbox to appear.  If Google asks for additional verification
     * (OTP, device approval, etc.) the test will wait up to
     * {@value #MANUAL_VERIFICATION_WAIT_SECONDS} seconds so the challenge can be
     * resolved manually or by an external automation mechanism, then checks again.
     */
    private void waitForInboxOrVerification() {
        LOG.info("Waiting for inbox to load (or for additional verification prompt)...");
        try {
            new WebDriverWait(driver, Duration.ofSeconds(
                    ConfigReader.getInt("browser.timeout.seconds", 30)))
                    .until(ExpectedConditions.or(
                            ExpectedConditions.urlContains("mail.google.com/mail"),
                            ExpectedConditions.visibilityOfElementLocated(inboxIndicator)
                    ));
        } catch (org.openqa.selenium.TimeoutException te) {
            // Google may have triggered a verification step.
            LOG.warn("Standard timeout elapsed. Google may be requesting additional verification. "
                    + "Waiting up to {} seconds for manual/external completion...",
                    MANUAL_VERIFICATION_WAIT_SECONDS);
            // Give extra time (human or external tool can intervene)
            new WebDriverWait(driver, Duration.ofSeconds(MANUAL_VERIFICATION_WAIT_SECONDS))
                    .until(ExpectedConditions.visibilityOfElementLocated(inboxIndicator));
        }
        LOG.info("Inbox loaded successfully.");
    }
}
