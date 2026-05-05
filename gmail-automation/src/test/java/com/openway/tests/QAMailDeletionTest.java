package com.openway.tests;

import com.openway.pages.GmailInboxPage;
import com.openway.pages.GmailLoginPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Automated Gmail deletion tests for DEL-001 through DEL-005.
 */
public class QAMailDeletionTest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(QAMailDeletionTest.class);

    private GmailInboxPage inboxPage;

    @Test(description = "DEL-001: Delete a single email from Inbox")
    public void testDEL001DeleteSingleEmailFromInbox() {
        logger.info("Starting DEL-001");
        openGmailInbox();

        GmailInboxPage.DeletedEmail deletedEmail = inboxPage.deleteNewestUnreadEmail();

        Assert.assertTrue(deletedEmail.isRemovedFromCurrentView(),
                "Deleted email row should disappear from the current Inbox view: " + deletedEmail.getSubject());
        logger.info("DEL-001 passed. Deleted subject: {}", deletedEmail.getSubject());
    }

    @Test(description = "DEL-002: Verify deleted email is removed from Inbox view")
    public void testDEL002VerifyDeletedEmailRemovedFromInbox() {
        logger.info("Starting DEL-002");
        openGmailInbox();

        GmailInboxPage.DeletedEmail deletedEmail = inboxPage.deleteNewestUnreadEmail();

        Assert.assertTrue(deletedEmail.isRemovedFromCurrentView(),
                "Deleted email row should be removed from Inbox view: " + deletedEmail.getSubject());
        logger.info("DEL-002 passed. Removed subject: {}", deletedEmail.getSubject());
    }

    @Test(description = "DEL-003: Delete multiple emails sequentially")
    public void testDEL003DeleteMultipleEmailsSequentially() {
        logger.info("Starting DEL-003");
        openGmailInbox();

        int emailsToDelete = 3;
        Assert.assertTrue(inboxPage.getUnreadEmails().size() >= emailsToDelete,
                "Inbox must contain at least " + emailsToDelete + " unread emails.");

        List<GmailInboxPage.DeletedEmail> deletedEmails = inboxPage.deleteNewestUnreadEmails(emailsToDelete);

        for (GmailInboxPage.DeletedEmail deletedEmail : deletedEmails) {
            Assert.assertTrue(deletedEmail.isRemovedFromCurrentView(),
                    "Deleted email row should not remain in Inbox view: " + deletedEmail.getSubject());
        }
        logger.info("DEL-003 passed. Deleted subjects: {}", deletedEmails);
    }

    @Test(description = "DEL-004: Verify undo functionality after deletion")
    public void testDEL004UndoAfterDeletion() {
        logger.info("Starting DEL-004");
        openGmailInbox();

        GmailInboxPage.DeletedEmail deletedEmail = inboxPage.deleteNewestUnreadEmail();
        inboxPage.clickUndo();
        inboxPage.refreshInbox();

        Assert.assertTrue(inboxPage.isEmailSubjectVisible(deletedEmail.getSubject()),
                "Email should be restored to Inbox after Undo: " + deletedEmail.getSubject());
        logger.info("DEL-004 passed. Restored subject: {}", deletedEmail.getSubject());
    }

    @Test(description = "DEL-005: Verify Trash folder visibility after deletion")
    public void testDEL005VerifyTrashFolderVisibilityAfterDeletion() {
        logger.info("Starting DEL-005");
        openGmailInbox();

        GmailInboxPage.DeletedEmail deletedEmail = inboxPage.deleteNewestUnreadEmail();

        Assert.assertTrue(inboxPage.isEmailSubjectInTrash(deletedEmail.getSubject()),
                "Deleted email should be visible in Trash: " + deletedEmail.getSubject());
        logger.info("DEL-005 passed. Subject found in Trash: {}", deletedEmail.getSubject());
    }

    private void openGmailInbox() {
        GmailLoginPage loginPage = new GmailLoginPage(driver);
        inboxPage = new GmailInboxPage(driver);

        loginPage.navigateToGmail();

        String email = getTestEmail();
        String password = getTestPassword();
        Assert.assertTrue(email != null && !email.isBlank(),
                "Test email is not configured. Set test.email, -Dtest.email, or TEST_EMAIL.");
        Assert.assertTrue(password != null && !password.isBlank(),
                "Test password is not configured. Set test.password, -Dtest.password, or TEST_PASSWORD.");

        loginPage.enterEmail(email);
        loginPage.clickEmailNext();
        loginPage.enterPassword(password);
        loginPage.clickPasswordNext();

        if (loginPage.isVerificationCodeRequired()) {
            String verificationCode = getVerificationCode();
            Assert.assertTrue(verificationCode != null && !verificationCode.isBlank(),
                    "Google verification is required. Set test.verification.code, "
                            + "-Dtest.verification.code, or TEST_VERIFICATION_CODE.");
            loginPage.enterVerificationCode(verificationCode);
            loginPage.clickVerificationNext();
        }

        loginPage.waitForInboxToLoad();
        Assert.assertTrue(loginPage.isLoginSuccessful(), "Gmail login failed or Inbox did not load.");
        inboxPage.waitForEmailsToLoad();
        Assert.assertTrue(inboxPage.getTotalEmailCount() > 0, "Inbox must contain at least one email.");
        Assert.assertTrue(inboxPage.getUnreadEmails().size() > 0, "Inbox must contain at least one unread email.");
    }
}
