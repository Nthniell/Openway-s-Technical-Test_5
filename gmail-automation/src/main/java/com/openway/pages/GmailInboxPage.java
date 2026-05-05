package com.openway.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Page Object Model for Gmail Inbox and deletion-related views.
 */
public class GmailInboxPage {
    private static final Logger logger = LogManager.getLogger(GmailInboxPage.class);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Actions actions;

    private final By emailRows = By.xpath("//tr[contains(@class, 'zA') or @role='row' or @data-message-id]");
    private final By subjectInRow = By.xpath(".//span[@class='bog']/span | .//span[contains(@class, 'bog')]");
    private final By rowCheckbox = By.xpath(".//div[@role='checkbox'] | .//span[@role='checkbox'] | .//input[@type='checkbox']");
    private final By toolbarDeleteButton = By.xpath(
            "//div[@role='button' and (contains(@aria-label, 'Delete') or contains(@data-tooltip, 'Delete'))]"
                    + " | //button[contains(@aria-label, 'Delete') or contains(@data-tooltip, 'Delete')]");
    private final By undoButton = By.xpath("//span[text()='Undo'] | //button[contains(., 'Undo')]");
    private final By searchBox = By.xpath("//input[@aria-label='Search mail' or @name='q']");
    private final By noConversationMessage = By.xpath("//*[contains(text(), 'No conversations')]");

    public static class DeletedEmail {
        private final String subject;
        private final boolean removedFromCurrentView;

        public DeletedEmail(String subject, boolean removedFromCurrentView) {
            this.subject = subject;
            this.removedFromCurrentView = removedFromCurrentView;
        }

        public String getSubject() {
            return subject;
        }

        public boolean isRemovedFromCurrentView() {
            return removedFromCurrentView;
        }

        @Override
        public String toString() {
            return subject;
        }
    }

    public GmailInboxPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.actions = new Actions(driver);
        logger.info("GmailInboxPage initialized");
    }

    public void waitForEmailsToLoad() {
        logger.info("Waiting for Gmail email list");
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(emailRows),
                ExpectedConditions.presenceOfElementLocated(noConversationMessage)));
    }

    private List<WebElement> getAllEmails() {
        return driver.findElements(emailRows);
    }

    public List<WebElement> getUnreadEmails() {
        List<WebElement> unreadEmails = new ArrayList<>();
        for (WebElement email : getAllEmails()) {
            if (isUnreadRow(email)) {
                unreadEmails.add(email);
            }
        }
        logger.info("Total visible unread email rows: {}", unreadEmails.size());
        return unreadEmails;
    }

    public int getTotalEmailCount() {
        int count = getAllEmails().size();
        logger.info("Total visible email rows: {}", count);
        return count;
    }

    private List<String> getVisibleSubjects() {
        List<String> subjects = new ArrayList<>();
        for (WebElement email : getAllEmails()) {
            String subject = getSubject(email);
            if (!subject.isBlank()) {
                subjects.add(subject);
            }
        }
        logger.info("Captured visible subjects: {}", subjects);
        return subjects;
    }

    public DeletedEmail deleteNewestUnreadEmail() {
        WebElement newestUnreadEmail = getNewestUnreadEmailRow();
        String subject = getSubject(newestUnreadEmail);
        logger.info("Deleting newest unread email: {}", subject);
        selectEmailRow(newestUnreadEmail);
        boolean removed = clickToolbarDelete(newestUnreadEmail);
        return new DeletedEmail(subject, removed);
    }

    public List<DeletedEmail> deleteNewestUnreadEmails(int count) {
        List<DeletedEmail> deletedEmails = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            waitForEmailsToLoad();
            deletedEmails.add(deleteNewestUnreadEmail());
            waitForInboxUpdate();
        }
        return deletedEmails;
    }

    public void clickUndo() {
        logger.info("Clicking Gmail Undo after deletion");
        wait.until(ExpectedConditions.elementToBeClickable(undoButton)).click();
        waitForInboxUpdate();
    }

    public boolean isEmailSubjectVisible(String subject) {
        if (subject == null || subject.isBlank()) {
            return false;
        }

        for (String visibleSubject : getVisibleSubjects()) {
            if (visibleSubject.equals(subject) || visibleSubject.contains(subject)) {
                logger.info("Subject is visible: {}", subject);
                return true;
            }
        }

        logger.info("Subject is not visible: {}", subject);
        return false;
    }

    public boolean isEmailSubjectInTrash(String subject) {
        logger.info("Searching Trash for subject: {}", subject);
        searchMail("in:trash \"" + subject + "\"");
        return isEmailSubjectVisible(subject);
    }

    private void searchMail(String query) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(searchBox));
        input.clear();
        input.sendKeys(query);
        input.submit();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(emailRows),
                ExpectedConditions.presenceOfElementLocated(noConversationMessage)));
    }

    public void refreshInbox() {
        logger.info("Refreshing Gmail");
        driver.navigate().refresh();
        waitForEmailsToLoad();
    }

    private void waitForInboxUpdate() {
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'moved to Trash')]")),
                ExpectedConditions.presenceOfElementLocated(emailRows),
                ExpectedConditions.presenceOfElementLocated(noConversationMessage)));
    }

    private WebElement getNewestUnreadEmailRow() {
        waitForEmailsToLoad();
        List<WebElement> unreadEmails = getUnreadEmails();
        if (unreadEmails.isEmpty()) {
            throw new IllegalStateException("No visible unread emails found in Gmail Inbox.");
        }
        WebElement newestUnreadEmail = unreadEmails.get(0);
        logger.info("Newest unread candidate selected: {}", getSubject(newestUnreadEmail));
        return newestUnreadEmail;
    }

    private void selectEmailRow(WebElement emailRow) {
        actions.moveToElement(emailRow).perform();
        WebElement checkbox = emailRow.findElement(rowCheckbox);
        checkbox.click();
        logger.info("Email row selected");
    }

    private boolean clickToolbarDelete(WebElement deletedRow) {
        WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(toolbarDeleteButton));
        deleteButton.click();
        boolean removed = wait.until(ExpectedConditions.or(
                ExpectedConditions.stalenessOf(deletedRow),
                ExpectedConditions.invisibilityOf(deletedRow),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'moved to Trash')]"))));
        logger.info("Toolbar Delete clicked");
        return removed;
    }

    private String getSubject(WebElement emailRow) {
        try {
            return emailRow.findElement(subjectInRow).getText().trim();
        } catch (Exception e) {
            logger.warn("Could not read subject element, falling back to row text: {}", e.getMessage());
            return emailRow.getText().trim();
        }
    }

    private boolean isUnreadRow(WebElement emailRow) {
        String rowClass = emailRow.getAttribute("class");
        if (rowClass != null && rowClass.contains("yO")) {
            logger.info("Unread row by class: {}", getSubject(emailRow));
            return true;
        }

        try {
            WebElement subject = emailRow.findElement(subjectInRow);
            String fontWeight = subject.getCssValue("font-weight");
            boolean boldSubject = isBoldFontWeight(fontWeight);
            if (boldSubject) {
                logger.info("Unread row by bold subject: {} (font-weight={})", getSubject(emailRow), fontWeight);
                return true;
            }
        } catch (Exception e) {
            logger.warn("Could not inspect subject font weight: {}", e.getMessage());
        }

        return false;
    }

    private boolean isBoldFontWeight(String fontWeight) {
        if (fontWeight == null || fontWeight.isBlank()) {
            return false;
        }
        if ("bold".equalsIgnoreCase(fontWeight) || "bolder".equalsIgnoreCase(fontWeight)) {
            return true;
        }
        try {
            return Integer.parseInt(fontWeight) >= 600;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
