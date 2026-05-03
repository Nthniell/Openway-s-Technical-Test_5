package com.openway.tests;

import com.openway.pages.InboxPage;
import com.openway.pages.LoginPage;
import com.openway.utils.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * <h2>TC-002 – Read and log the subject of the most recent unread email</h2>
 *
 * <h3>Test Scenario (Scenario Option A)</h3>
 * <p>This test covers the automated part of Scenario Option A from the QA technical
 * test.  It:
 * <ol>
 *   <li>Opens Google Chrome in a new window.</li>
 *   <li>Navigates to {@code https://mail.google.com/mail/}.</li>
 *   <li>Enters the configured login and password.</li>
 *   <li>Writes the subject of the last unread email in the mailbox to the log.</li>
 * </ol>
 *
 * <h3>Credentials</h3>
 * <p>Set the following environment variables before running the test:
 * <pre>
 *   export GMAIL_USERNAME=your-test-account@gmail.com
 *   export GMAIL_PASSWORD=your-app-password
 * </pre>
 * <p>Google recommends using an <em>App Password</em> (account → Security → App Passwords)
 * rather than the real account password, especially when 2-Step Verification is enabled.
 * This ensures the test is portable and works on any machine without manual 2FA interaction.
 *
 * <h3>Preconditions</h3>
 * <ul>
 *   <li>A Google test account exists and its credentials are provided via the environment
 *       variables {@code GMAIL_USERNAME} and {@code GMAIL_PASSWORD}.</li>
 *   <li>The inbox contains at least five unread emails.</li>
 *   <li>Google Chrome is installed on the test machine.</li>
 *   <li>Internet connectivity is available.</li>
 * </ul>
 */
public class GmailUnreadEmailTest {

    private static final Logger LOG = LoggerFactory.getLogger(GmailUnreadEmailTest.class);

    private WebDriver driver;
    private InboxPage inboxPage;

    // -------------------------------------------------------------------------
    // Setup / Teardown
    // -------------------------------------------------------------------------

    /**
     * Opens a new Chrome window and logs into Gmail before any test in this class runs.
     */
    @BeforeClass
    public void setUp() {
        LOG.info("=== Test setup: launching Chrome and logging in to Gmail ===");
        driver = DriverFactory.getDriver();

        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        inboxPage = loginPage.login();

        LOG.info("=== Setup complete – inbox is open ===");
    }

    /**
     * Quits the Chrome driver after all tests in this class have run.
     */
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        LOG.info("=== Test teardown: closing Chrome ===");
        DriverFactory.quitDriver();
    }

    // -------------------------------------------------------------------------
    // Test Methods
    // -------------------------------------------------------------------------

    /**
     * <strong>TC-002-01:</strong> Verify that the inbox contains at least one unread
     * email and log the subject of the most recent one.
     *
     * <p><strong>Steps:</strong>
     * <ol>
     *   <li>Open Gmail inbox (done in {@link #setUp()}).</li>
     *   <li>Retrieve the subject of the most recent unread email.</li>
     *   <li>Assert that the subject is not {@code null} or empty.</li>
     *   <li>Write the subject to the log.</li>
     * </ol>
     *
     * <p><strong>Expected result:</strong> The subject of the most recent unread email
     * is printed to the log and the test passes.
     */
    @Test(description = "TC-002-01: Log the subject of the most recent unread email")
    public void testLogLastUnreadEmailSubject() {
        LOG.info("--- TC-002-01: Retrieving the subject of the most recent unread email ---");

        String subject = inboxPage.getLastUnreadEmailSubject();

        Assert.assertNotNull(subject,
                "Expected at least one unread email in the inbox, but none were found. "
                + "Please ensure the test account has at least five unread emails.");

        Assert.assertFalse(subject.isBlank(),
                "The unread email subject is blank, which is unexpected.");

        // *** PRIMARY DELIVERABLE: log the subject ***
        LOG.info("==============================================================");
        LOG.info("LAST UNREAD EMAIL SUBJECT: {}", subject);
        LOG.info("==============================================================");
    }

    /**
     * <strong>TC-002-02:</strong> Verify that the inbox contains at least five unread
     * emails (precondition check).
     *
     * <p>This test validates the precondition stated in the task: the mailbox must
     * contain at least five unread emails.
     */
    @Test(description = "TC-002-02: Inbox contains at least five unread emails (precondition check)",
          dependsOnMethods = "testLogLastUnreadEmailSubject")
    public void testInboxHasAtLeastFiveUnreadEmails() {
        LOG.info("--- TC-002-02: Verifying inbox has at least 5 unread emails ---");

        int unreadCount = inboxPage.getUnreadEmailCount();
        LOG.info("Total unread emails visible: {}", unreadCount);

        Assert.assertTrue(unreadCount >= 5,
                "The inbox must contain at least 5 unread emails but found: " + unreadCount
                + ". Please add more unread emails to the test account and retry.");
    }
}
