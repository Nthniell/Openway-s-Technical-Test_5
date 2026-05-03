package com.openway.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.time.Duration;

/**
 * Page Object Model for Gmail Login Page
 */
public class GmailLoginPage {
    private static final Logger logger = LogManager.getLogger(GmailLoginPage.class);
    private WebDriver driver;
    private WebDriverWait wait;

    // Locators
    private final By emailInput = By.id("identifierId");
    private final By nextButton = By.id("identifierNext");
    private final By passwordInput = By.xpath("//input[@type='password']");
    private final By passwordNextButton = By.id("passwordNext");
    private final By verificationCodeInput = By.name("verificationCode");
    private final By verificationNextButton = By.id("verificationCode");
    private final By notNowButton = By.xpath("//button[contains(text(), 'Not now')]");

    /**
     * Constructor
     * @param driver WebDriver instance
     */
    public GmailLoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        logger.info("GmailLoginPage initialized");
    }

    /**
     * Navigate to Gmail login page
     */
    public void navigateToGmail() {
        logger.info("Navigating to Gmail login page");
        driver.get("https://mail.google.com/mail/");
        wait.until(ExpectedConditions.presenceOfElementLocated(emailInput));
        logger.info("Gmail login page loaded");
    }

    /**
     * Enter email address
     * @param email Email address
     */
    public void enterEmail(String email) {
        logger.info("Entering email: " + email);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(emailInput));
        emailField.clear();
        emailField.sendKeys(email);
    }

    /**
     * Click next button after email entry
     */
    public void clickEmailNext() {
        logger.info("Clicking next button for email");
        WebElement nextBtn = wait.until(ExpectedConditions.elementToBeClickable(nextButton));
        nextBtn.click();
        // Wait for password field to appear (with longer timeout)
        try {
            Thread.sleep(2000); // Wait for page transition
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        wait.until(ExpectedConditions.presenceOfElementLocated(passwordInput));
    }

    /**
     * Enter password
     * @param password Password
     */
    public void enterPassword(String password) {
        logger.info("Entering password");
        // Retry logic to handle stale elements
        int maxRetries = 3;
        int retries = 0;
        while (retries < maxRetries) {
            try {
                WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(passwordInput));
                passwordField.clear();
                passwordField.sendKeys(password);
                logger.info("Password entered successfully");
                return;
            } catch (Exception e) {
                retries++;
                logger.warn("Attempt " + retries + " to enter password failed: " + e.getMessage());
                if (retries >= maxRetries) {
                    throw e;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Click next button after password entry
     */
    public void clickPasswordNext() {
        logger.info("Clicking next button for password");
        WebElement nextBtn = wait.until(ExpectedConditions.elementToBeClickable(passwordNextButton));
        nextBtn.click();
        // Wait a bit for next screen
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Handle "Not now" button if it appears (for passkey/2FA setup)
        handleNotNowButton();
    }

    /**
     * Click "Not now" button if it appears (passkey setup screen)
     */
    private void handleNotNowButton() {
        try {
            WebElement notNow = wait.until(ExpectedConditions.elementToBeClickable(notNowButton));
            logger.info("Passkey setup screen detected - clicking 'Not now'");
            notNow.click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            logger.info("'Not now' button not found - continuing normally");
        }
    }

    /**
     * Check if verification code input is present (for 2FA)
     * @return true if verification code input is visible
     */
    public boolean isVerificationCodeRequired() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(verificationCodeInput));
            logger.info("Verification code required - 2FA enabled");
            return true;
        } catch (Exception e) {
            logger.info("No verification code required");
            return false;
        }
    }

    /**
     * Enter verification code
     * @param code Verification code
     */
    public void enterVerificationCode(String code) {
        logger.info("Entering verification code");
        WebElement codeField = wait.until(ExpectedConditions.presenceOfElementLocated(verificationCodeInput));
        codeField.clear();
        codeField.sendKeys(code);
    }

    /**
     * Click next button after verification code entry
     */
    public void clickVerificationNext() {
        logger.info("Clicking next button for verification code");
        WebElement nextBtn = wait.until(ExpectedConditions.elementToBeClickable(verificationNextButton));
        nextBtn.click();
    }

    /**
     * Wait for Gmail inbox to load
     */
    public void waitForInboxToLoad() {
        logger.info("Waiting for Gmail inbox to load");
        // Wait for email rows which indicates inbox is loaded
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tr[@role='row'] | //tr[contains(@class, 'zA')] | //tr[@data-message-id]")));
            logger.info("Gmail inbox loaded - email rows found");
        } catch (Exception e) {
            logger.warn("Could not find email rows, trying alternative");
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@aria-label, 'Inbox')]")));
                logger.info("Gmail inbox loaded");
            } catch (Exception e2) {
                logger.warn("Inbox load verification failed: " + e2.getMessage());
            }
        }
    }

    /**
     * Check if login was successful by verifying inbox is displayed
     * @return true if inbox is visible
     */
    public boolean isLoginSuccessful() {
        try {
            // Try to find email rows
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tr[@role='row'] | //tr[contains(@class, 'zA')] | //tr[@data-message-id]")));
            logger.info("Login successful - inbox displayed with email rows");
            return true;
        } catch (Exception e1) {
            try {
                // Fallback: check for inbox container
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@aria-label, 'Inbox')]")));
                logger.info("Login successful - inbox displayed");
                return true;
            } catch (Exception e2) {
                logger.error("Login verification failed: " + e2.getMessage());
                return false;
            }
        }
    }
}
