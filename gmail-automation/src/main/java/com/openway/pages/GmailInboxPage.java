package com.openway.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.interactions.Actions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.time.Duration;
import java.util.List;

/**
 * Page Object Model for Gmail Inbox Page
 */
public class GmailInboxPage {
    private static final Logger logger = LogManager.getLogger(GmailInboxPage.class);
    private WebDriver driver;
    private WebDriverWait wait;

    // Locators for emails in inbox
    private final By emailRows = By.xpath("//tr[@role='row'] | //tr[contains(@class, 'zA')] | //tr[@data-message-id]");
    private final By unreadEmails = By.xpath("//tr[@role='row'][contains(@class, 'yO')] | //tr[contains(@class, 'zA')][contains(@class, 'yO')]");
    private final By emailSubject = By.xpath(".//span[@class='bog']/span | .//span[@class='y6'] | .//div[contains(@class, 'aX8')]");
    private final By unreadIndicator = By.xpath(".//span[@aria-label and contains(@aria-label, 'not opened')]");

    /**
     * Constructor
     * @param driver WebDriver instance
     */
    public GmailInboxPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        logger.info("GmailInboxPage initialized");
    }

    /**
     * Wait for inbox to load with emails
     */
    public void waitForEmailsToLoad() {
        logger.info("Waiting for emails to load in inbox");
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(emailRows));
            logger.info("Emails loaded in inbox");
        } catch (Exception e) {
            logger.warn("Timeout waiting for emails: " + e.getMessage());
        }
    }

    /**
     * Get all unread emails from inbox
     * @return List of unread email subjects
     */
    public List<WebElement> getUnreadEmails() {
        logger.info("Retrieving unread emails from inbox");
        try {
            List<WebElement> unreadElements = driver.findElements(unreadEmails);
            logger.info("Found " + unreadElements.size() + " unread emails");
            return unreadElements;
        } catch (Exception e) {
            logger.error("Error retrieving unread emails: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get all emails currently displayed in inbox
     * @return List of email row elements
     */
    public List<WebElement> getAllEmails() {
        logger.info("Retrieving all emails from inbox");
        try {
            List<WebElement> emails = driver.findElements(emailRows);
            logger.info("Found " + emails.size() + " emails in inbox");
            return emails;
        } catch (Exception e) {
            logger.error("Error retrieving all emails: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get the subject of the last unread email
     * @return Subject of the last unread email or null if none found
     */
    public String getLastUnreadEmailSubject() {
        logger.info("Getting subject of last unread email");
        try {
            List<WebElement> unreadEmails = getUnreadEmails();
            if (unreadEmails.isEmpty()) {
                logger.warn("No unread emails found");
                return null;
            }

            WebElement lastUnreadEmail = unreadEmails.get(unreadEmails.size() - 1);
            String subject = lastUnreadEmail.getText().trim();
            logger.info("Last unread email subject: " + subject);
            return subject;
        } catch (Exception e) {
            logger.error("Error getting last unread email subject: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get the subject of the last email in inbox (whether read or unread)
     * @return Subject of the last email
     */
    public String getLastEmailSubject() {
        logger.info("Getting subject of last email in inbox");
        try {
            List<WebElement> emails = getAllEmails();
            if (emails.isEmpty()) {
                logger.warn("No emails found in inbox");
                return null;
            }

            WebElement lastEmail = emails.get(emails.size() - 1);
            WebElement subjectElement = lastEmail.findElement(emailSubject);
            String subject = subjectElement.getText().trim();
            logger.info("Last email subject: " + subject);
            return subject;
        } catch (Exception e) {
            logger.error("Error getting last email subject: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get count of unread emails
     * @return Number of unread emails
     */
    public int getUnreadEmailCount() {
        try {
            List<WebElement> unreadEmails = getUnreadEmails();
            int count = unreadEmails.size();
            logger.info("Unread email count: " + count);
            return count;
        } catch (Exception e) {
            logger.error("Error getting unread email count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get count of total emails in inbox
     * @return Number of total emails
     */
    public int getTotalEmailCount() {
        try {
            List<WebElement> emails = getAllEmails();
            int count = emails.size();
            logger.info("Total email count: " + count);
            return count;
        } catch (Exception e) {
            logger.error("Error getting total email count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Select an email by index (0-based)
     * @param index Index of email to select
     */
    public void selectEmail(int index) {
        logger.info("Selecting email at index: " + index);
        try {
            List<WebElement> emails = getAllEmails();
            if (index >= 0 && index < emails.size()) {
                emails.get(index).click();
                logger.info("Email selected successfully");
            } else {
                logger.warn("Email index out of bounds: " + index);
            }
        } catch (Exception e) {
            logger.error("Error selecting email: " + e.getMessage());
        }
    }

    /**
     * Delete email at specified index
     * @param index Index of email to delete
     */
    public void deleteEmail(int index) {
        logger.info("Deleting email at index: " + index);
        try {
            List<WebElement> emails = getAllEmails();
            if (index < 0 || index >= emails.size()) {
                logger.warn("Email index out of bounds: " + index);
                return;
            }
            
            WebElement email = emails.get(index);
            
            // Method 1: Try clicking checkbox then delete button
            try {
                // Find checkbox in the email row
                WebElement checkbox = email.findElement(By.xpath(".//input[@type='checkbox'] | .//span[@role='checkbox']"));
                checkbox.click();
                logger.info("Email selected with checkbox");
                Thread.sleep(1000);
                
                // Look for delete button that appears after selection
                WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(@aria-label, 'Delete') or contains(@aria-label, 'delete')]")));
                deleteButton.click();
                logger.info("Email deleted using delete button");
                return;
            } catch (Exception e1) {
                logger.warn("Method 1 (checkbox + button) failed: " + e1.getMessage());
            }
            
            // Method 2: Click email then use keyboard shortcut
            try {
                email.click();
                Thread.sleep(1000);
                
                // Send Delete key using Actions
                Actions actions = new Actions(driver);
                actions.sendKeys(org.openqa.selenium.Keys.DELETE).perform();
                logger.info("Email deleted using Delete key");
                return;
            } catch (Exception e2) {
                logger.warn("Method 2 (Delete key) failed: " + e2.getMessage());
            }
            
            // Method 3: Right-click for context menu
            try {
                Actions actions = new Actions(driver);
                actions.contextClick(email).perform();
                Thread.sleep(500);
                
                WebElement deleteOption = driver.findElement(By.xpath("//span[contains(text(), 'Delete') or contains(text(), 'delete')]"));
                deleteOption.click();
                logger.info("Email deleted using context menu");
                return;
            } catch (Exception e3) {
                logger.warn("Method 3 (context menu) failed: " + e3.getMessage());
            }
            
            logger.error("All delete methods failed for email at index " + index);
            
        } catch (Exception e) {
            logger.error("Error in deleteEmail: " + e.getMessage());
        }
    }

    /**
     * Refresh inbox to reload emails
     */
    public void refreshInbox() {
        logger.info("Refreshing inbox");
        try {
            // Use keyboard shortcut to refresh (R key in Gmail)
            WebElement emailElement = driver.findElement(emailRows);
            emailElement.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.SHIFT, "r"));
            
            // Wait for refresh to complete
            Thread.sleep(2000);
            
            // Wait for emails to load
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(emailRows));
            logger.info("Inbox refreshed successfully");
        } catch (Exception e) {
            logger.warn("Error refreshing inbox with keyboard shortcut: " + e.getMessage());
            try {
                // Fallback: navigate refresh
                driver.navigate().refresh();
                Thread.sleep(2000);
                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(emailRows));
                logger.info("Inbox refreshed with page refresh");
            } catch (Exception e2) {
                logger.error("Error refreshing inbox: " + e2.getMessage());
            }
        }
    }
}
