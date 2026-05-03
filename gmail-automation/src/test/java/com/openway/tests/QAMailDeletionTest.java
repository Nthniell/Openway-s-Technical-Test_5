package com.openway.tests;

import com.openway.pages.GmailInboxPage;
import com.openway.pages.GmailLoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import java.util.ArrayList;

/**
 * Test class for Gmail mail deletion functionality
 */
public class QAMailDeletionTest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(QAMailDeletionTest.class);
    private GmailLoginPage loginPage;
    private GmailInboxPage inboxPage;

    /**
     * Test case: Login to Gmail and log the last unread email subject
     * This test verifies basic Gmail login and unread email retrieval
     */
    @Test(description = "Login to Gmail and retrieve last unread email subject")
    public void testLoginAndGetLastUnreadEmail() {
        logger.info("Starting test: Login to Gmail and get last unread email");

        try {
            // Initialize page objects
            loginPage = new GmailLoginPage(driver);
            inboxPage = new GmailInboxPage(driver);

            // Navigate to Gmail
            loginPage.navigateToGmail();

            // Get credentials from configuration
            String email = getTestEmail();
            String password = getTestPassword();

            Assert.assertNotNull(email, "Test email not configured. Please set test.email in config.properties");
            Assert.assertNotNull(password, "Test password not configured. Please set test.password in config.properties");

            logger.info("Starting Gmail login process");

            // Enter email
            loginPage.enterEmail(email);
            loginPage.clickEmailNext();

            // Enter password
            loginPage.enterPassword(password);
            loginPage.clickPasswordNext();

            // Check if verification is required
            if (loginPage.isVerificationCodeRequired()) {
                logger.warn("Gmail verification (2FA) required");
                String verificationCode = getVerificationCode();
                if (verificationCode != null && !verificationCode.isEmpty()) {
                    loginPage.enterVerificationCode(verificationCode);
                    loginPage.clickVerificationNext();
                } else {
                    logger.warn("Verification code not configured. Please enter code manually or set test.verification.code in config.properties");
                    // Wait for user to manually enter code
                    Thread.sleep(30000);
                }
            }

            // Wait for inbox to load
            loginPage.waitForInboxToLoad();
            Assert.assertTrue(loginPage.isLoginSuccessful(), "Gmail login failed");
            logger.info("Gmail login successful");

            // Wait for emails to load
            inboxPage.waitForEmailsToLoad();

            // Get email statistics
            int totalEmails = inboxPage.getTotalEmailCount();
            int unreadEmails = inboxPage.getUnreadEmailCount();
            logger.info("Inbox statistics - Total emails: " + totalEmails + ", Unread: " + unreadEmails);

            Assert.assertTrue(totalEmails > 0, "Inbox should contain at least one email");
            Assert.assertTrue(unreadEmails > 0, "Inbox should contain at least one unread email");

            // Get and log the last unread email subject
            String lastUnreadSubject = inboxPage.getLastUnreadEmailSubject();
            Assert.assertNotNull(lastUnreadSubject, "Failed to retrieve last unread email subject");

            logger.info("===== LAST UNREAD EMAIL SUBJECT: " + lastUnreadSubject + " =====");
            System.out.println("\n========== TEST RESULT ==========");
            System.out.println("Last Unread Email Subject: " + lastUnreadSubject);
            System.out.println("Total Emails in Inbox: " + totalEmails);
            System.out.println("Unread Emails: " + unreadEmails);
            System.out.println("==================================\n");

            logger.info("Test completed successfully");

        } catch (Exception e) {
            logger.error("Test failed with exception: " + e.getMessage(), e);
            throw new AssertionError("Test execution failed", e);
        }
    }

    /**
     * Test case: DEL-001 - Delete a single email from Inbox (Critical Priority)
     * Test Scenario: Verify that a single email can be deleted from the inbox
     * Expected Result: Email is removed and total email count decreases by 1
     */
    @Test(description = "DEL-001: Delete a single email from Inbox", enabled = false)
    public void testDEL001DeleteSingleEmail() {
        logger.info("Starting test: DEL-001 - Delete a single email from Inbox");

        try {
            loginPage = new GmailLoginPage(driver);
            inboxPage = new GmailInboxPage(driver);

            // Pre-condition: Login to Gmail
            logger.info("Pre-condition: Logging in to Gmail");
            loginPage.navigateToGmail();
            loginPage.enterEmail(getTestEmail());
            loginPage.clickEmailNext();
            loginPage.enterPassword(getTestPassword());
            loginPage.clickPasswordNext();

            if (loginPage.isVerificationCodeRequired()) {
                String verificationCode = getVerificationCode();
                if (verificationCode != null) {
                    loginPage.enterVerificationCode(verificationCode);
                    loginPage.clickVerificationNext();
                }
            }

            loginPage.waitForInboxToLoad();
            Assert.assertTrue(loginPage.isLoginSuccessful(), "Gmail login failed - Pre-condition failed");

            // Test Step 1: Wait for inbox to load with emails
            logger.info("Test Step 1: Wait for inbox to load");
            inboxPage.waitForEmailsToLoad();
            
            // Test Step 2: Get initial email count
            logger.info("Test Step 2: Get initial email count");
            int initialEmailCount = inboxPage.getTotalEmailCount();
            logger.info("Initial email count: " + initialEmailCount);
            
            // Verify at least one email exists
            Assert.assertTrue(initialEmailCount > 0, "Test Data Error - No emails in inbox to delete");
            
            // Test Step 3: Get first email subject (for verification)
            logger.info("Test Step 3: Get first email subject");
            String deletedEmailSubject = null;
            try {
                List<WebElement> emails = inboxPage.getAllEmails();
                if (!emails.isEmpty()) {
                    WebElement firstEmail = emails.get(0);
                    deletedEmailSubject = firstEmail.findElement(By.xpath(".//span[@class='bog']/span | .//span[@class='y6'] | .//div[contains(@class, 'aX8')]")).getText();
                    logger.info("Email to delete: " + deletedEmailSubject);
                }
            } catch (Exception e) {
                logger.warn("Could not get deleted email subject: " + e.getMessage());
            }
            
            // Test Step 4: Delete the first email
            logger.info("Test Step 4: Delete the first email");
            inboxPage.deleteEmail(0);
            
            // Test Step 5: Wait for deletion
            logger.info("Test Step 5: Wait for deletion to complete");
            Thread.sleep(5000); // Longer wait for Gmail to process deletion
            
            // Test Step 6: Create new inbox instance to re-fetch emails
            logger.info("Test Step 6: Refresh email list");
            inboxPage = new GmailInboxPage(driver); // Re-create to force re-fetch
            Thread.sleep(2000);
            
            // Test Step 7: Get final email count
            logger.info("Test Step 7: Get final email count");
            int finalEmailCount = inboxPage.getTotalEmailCount();
            logger.info("Final email count: " + finalEmailCount);
            
            // Expected Result: Verify email count decreased by 1
            logger.info("Expected Result: Email count should decrease by 1");
            logger.info("Initial count: " + initialEmailCount + ", Final count: " + finalEmailCount);
            Assert.assertEquals(finalEmailCount, initialEmailCount - 1, 
                    "Email should be deleted from inbox. Expected: " + (initialEmailCount - 1) + ", Got: " + finalEmailCount);
            
            logger.info("DEL-001 Test Result: PASSED ✓");
            logger.info("===== TEST SUMMARY =====");
            logger.info("Test Case: DEL-001 - Delete a single email from Inbox");
            logger.info("Status: PASSED");
            logger.info("Initial Email Count: " + initialEmailCount);
            logger.info("Final Email Count: " + finalEmailCount);
            logger.info("Deleted Email Subject: " + (deletedEmailSubject != null ? deletedEmailSubject : "N/A"));
            logger.info("========================");

        } catch (AssertionError ae) {
            logger.error("DEL-001 Test Result: FAILED ✗");
            logger.error("Assertion Error: " + ae.getMessage());
            throw ae;
        } catch (Exception e) {
            logger.error("DEL-001 Test Result: FAILED ✗");
            logger.error("Test failed with exception: " + e.getMessage(), e);
            throw new AssertionError("Test execution failed", e);
        }
    }

    /**
     * Test case: Verify deleted email is not visible in inbox
     */
    @Test(description = "Verify deleted email is not visible in inbox", enabled = false)
    public void testVerifyDeletedEmailNotInInbox() {
        logger.info("Starting test: Verify deleted email not in inbox");

        try {
            loginPage = new GmailLoginPage(driver);
            inboxPage = new GmailInboxPage(driver);

            // Login
            loginPage.navigateToGmail();
            loginPage.enterEmail(getTestEmail());
            loginPage.clickEmailNext();
            loginPage.enterPassword(getTestPassword());
            loginPage.clickPasswordNext();

            if (loginPage.isVerificationCodeRequired()) {
                String verificationCode = getVerificationCode();
                if (verificationCode != null) {
                    loginPage.enterVerificationCode(verificationCode);
                    loginPage.clickVerificationNext();
                }
            }

            loginPage.waitForInboxToLoad();
            Assert.assertTrue(loginPage.isLoginSuccessful(), "Gmail login failed");

            inboxPage.waitForEmailsToLoad();

            // Get first email subject before deletion
            String emailSubjectBeforeDeletion = inboxPage.getLastEmailSubject();
            logger.info("Email to delete: " + emailSubjectBeforeDeletion);

            // Delete the email
            inboxPage.deleteEmail(0);
            Thread.sleep(2000);

            // Refresh and verify
            inboxPage.refreshInbox();
            String lastEmailAfterDeletion = inboxPage.getLastEmailSubject();
            logger.info("Last email after deletion: " + lastEmailAfterDeletion);

            if (lastEmailAfterDeletion != null) {
                Assert.assertNotEquals(lastEmailAfterDeletion, emailSubjectBeforeDeletion,
                        "Deleted email should not be the last email anymore");
            }

            logger.info("Test completed successfully");

        } catch (Exception e) {
            logger.error("Test failed with exception: " + e.getMessage(), e);
            throw new AssertionError("Test execution failed", e);
        }
    }

    /**
     * Test case: DEL-002 - Verify deleted email is removed from Inbox view (High Priority)
     * Test Scenario: Verify that a deleted email is no longer visible in the Inbox
     * Expected Result: Deleted email subject is not found in email list
     */
    @Test(description = "DEL-002: Verify deleted email is removed from Inbox view")
    public void testDEL002VerifyDeletedEmailRemoved() {
        logger.info("Starting test: DEL-002 - Verify deleted email is removed from Inbox view");

        try {
            loginPage = new GmailLoginPage(driver);
            inboxPage = new GmailInboxPage(driver);

            // Pre-condition: Login to Gmail
            logger.info("Pre-condition: Logging in to Gmail");
            loginPage.navigateToGmail();
            loginPage.enterEmail(getTestEmail());
            loginPage.clickEmailNext();
            loginPage.enterPassword(getTestPassword());
            loginPage.clickPasswordNext();

            if (loginPage.isVerificationCodeRequired()) {
                String verificationCode = getVerificationCode();
                if (verificationCode != null) {
                    loginPage.enterVerificationCode(verificationCode);
                    loginPage.clickVerificationNext();
                }
            }

            loginPage.waitForInboxToLoad();
            Assert.assertTrue(loginPage.isLoginSuccessful(), "Gmail login failed - Pre-condition failed");

            // Test Step 1: Wait for inbox to load
            logger.info("Test Step 1: Wait for inbox to load");
            inboxPage.waitForEmailsToLoad();
            
            // Test Step 2: Get list of all email subjects before deletion
            logger.info("Test Step 2: Get initial email subjects");
            List<WebElement> emailsBefore = inboxPage.getAllEmails();
            List<String> subjectsBefore = new ArrayList<>();
            
            for (int i = 0; i < Math.min(5, emailsBefore.size()); i++) {
                try {
                    WebElement email = emailsBefore.get(i);
                    String subject = email.findElement(By.xpath(".//span[@class='bog']/span | .//span[@class='y6'] | .//div[contains(@class, 'aX8')]")).getText();
                    subjectsBefore.add(subject);
                    logger.info("Email " + (i+1) + " subject: " + subject);
                } catch (Exception e) {
                    logger.warn("Could not get subject for email " + (i+1));
                }
            }
            
            if (subjectsBefore.isEmpty()) {
                logger.warn("Could not retrieve any email subjects");
                Assert.fail("Unable to retrieve email subjects for comparison");
            }
            
            String deletedEmailSubject = subjectsBefore.get(0);
            logger.info("Email to delete: " + deletedEmailSubject);
            
            // Test Step 3: Delete the first email
            logger.info("Test Step 3: Delete the first email");
            inboxPage.deleteEmail(0);
            Thread.sleep(3000); // Wait for deletion
            
            // Test Step 4: Get updated email list
            logger.info("Test Step 4: Get updated email list after deletion");
            inboxPage = new GmailInboxPage(driver); // Refresh inbox page object
            Thread.sleep(1000);
            
            List<WebElement> emailsAfter = inboxPage.getAllEmails();
            List<String> subjectsAfter = new ArrayList<>();
            
            for (int i = 0; i < emailsAfter.size(); i++) {
                try {
                    WebElement email = emailsAfter.get(i);
                    String subject = email.findElement(By.xpath(".//span[@class='bog']/span | .//span[@class='y6'] | .//div[contains(@class, 'aX8')]")).getText();
                    subjectsAfter.add(subject);
                } catch (Exception e) {
                    logger.warn("Could not get subject for email after deletion");
                }
            }
            
            logger.info("Total emails after: " + emailsAfter.size());
            logger.info("Total subjects captured after: " + subjectsAfter.size());
            
            // Test Step 5: Verify deleted email is not in the list
            logger.info("Test Step 5: Verify deleted email subject is not in list");
            boolean deletedEmailFound = false;
            
            for (String subject : subjectsAfter) {
                if (subject.equals(deletedEmailSubject)) {
                    deletedEmailFound = true;
                    logger.error("FOUND: Deleted email still visible with subject: " + subject);
                    break;
                }
            }
            
            // Expected Result: Deleted email should not be found
            Assert.assertFalse(deletedEmailFound, 
                    "Deleted email should not be visible in Inbox. Deleted subject was: " + deletedEmailSubject);
            
            logger.info("DEL-002 Test Result: PASSED ✓");
            logger.info("===== TEST SUMMARY =====");
            logger.info("Test Case: DEL-002 - Verify deleted email is removed from Inbox view");
            logger.info("Status: PASSED");
            logger.info("Deleted Email Subject: " + deletedEmailSubject);
            logger.info("Emails Before Deletion: " + subjectsBefore.size());
            logger.info("Emails After Deletion: " + subjectsAfter.size());
            logger.info("Verification: Deleted email NOT found in list ✓");
            logger.info("========================");

        } catch (AssertionError ae) {
            logger.error("DEL-002 Test Result: FAILED ✗");
            logger.error("Assertion Error: " + ae.getMessage());
            throw ae;
        } catch (Exception e) {
            logger.error("DEL-002 Test Result: FAILED ✗");
            logger.error("Test failed with exception: " + e.getMessage(), e);
            throw new AssertionError("Test execution failed", e);
        }
    }
}
