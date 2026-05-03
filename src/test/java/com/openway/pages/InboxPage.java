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
import java.util.List;

/**
 * Page Object for the Gmail Inbox.
 *
 * <p>Provides methods to query the list of unread messages and read their subjects.
 */
public class InboxPage {

    private static final Logger LOG = LoggerFactory.getLogger(InboxPage.class);

    private final WebDriver driver;
    private final WebDriverWait wait;

    /**
     * Selector for individual unread email rows.
     *
     * <p>In Gmail's HTML, unread messages carry the {@code zE} CSS class on the
     * {@code <tr>} table row, while read messages use {@code yO}.  We target the
     * subject/snippet span that is a direct child of an unread row.
     *
     * <ul>
     *   <li>{@code tr.zE} – unread email row</li>
     *   <li>{@code .y6 span} – the visible subject text inside that row</li>
     * </ul>
     */
    private static final By UNREAD_EMAIL_SUBJECT = By.cssSelector("tr.zE .y6 span");

    /** Fallback selector when the primary subject selector does not match anything. */
    private static final By UNREAD_ROW_FALLBACK = By.cssSelector("tr.zE");

    public InboxPage(WebDriver driver) {
        this.driver = driver;
        int timeoutSeconds = ConfigReader.getInt("browser.timeout.seconds", 30);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    /**
     * Returns the subject line of the <em>most recent</em> (topmost) unread email
     * visible in the inbox, or {@code null} if no unread emails are present.
     *
     * <p>Gmail renders emails in reverse-chronological order, so the first matching
     * element corresponds to the newest unread message.
     *
     * @return subject text of the last (newest) unread email, or {@code null}
     */
    public String getLastUnreadEmailSubject() {
        LOG.info("Looking for unread emails in the inbox...");

        // Wait until at least one unread row exists (or timeout gracefully)
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(UNREAD_EMAIL_SUBJECT));
        } catch (org.openqa.selenium.TimeoutException e) {
            LOG.warn("No unread emails found with primary selector '{}'. "
                    + "The inbox may be empty or Gmail's HTML structure may have changed.",
                    UNREAD_EMAIL_SUBJECT);
            return null;
        }

        List<WebElement> subjects = driver.findElements(UNREAD_EMAIL_SUBJECT);
        if (subjects.isEmpty()) {
            LOG.warn("Subject elements list is empty even after wait. Returning null.");
            return null;
        }

        // The first element in the list corresponds to the newest unread email
        String subject = subjects.get(0).getText().trim();
        LOG.info("Found {} unread email(s). Subject of the most recent one: '{}'",
                subjects.size(), subject);
        return subject;
    }

    /**
     * Returns the total number of unread email rows currently visible in the inbox.
     *
     * @return count of unread rows, or {@code 0} if none are found
     */
    public int getUnreadEmailCount() {
        List<WebElement> rows = driver.findElements(UNREAD_ROW_FALLBACK);
        LOG.debug("Unread email row count: {}", rows.size());
        return rows.size();
    }
}
