# Test Case Documentation

## Part 1 – Components of a Test Case

A **test case** is a structured document that specifies inputs, execution conditions,
and expected outcomes for a particular software scenario.  It allows a QA engineer to
verify that a feature behaves as specified and to reproduce failures consistently.

### Standard Components

| # | Component | Description |
|---|-----------|-------------|
| 1 | **Test Case ID** | Unique identifier (e.g. `TC-001`).  Used to reference the case in bug reports and traceability matrices. |
| 2 | **Title / Name** | Short, descriptive name that summarises what is being tested. |
| 3 | **Objective / Description** | One- or two-sentence explanation of the purpose and scope of the test. |
| 4 | **Priority** | Importance relative to other tests (Critical / High / Medium / Low). |
| 5 | **Preconditions** | State the system must be in *before* the test begins (e.g. user logged in, database seeded). |
| 6 | **Test Data** | Specific input values, files, or configurations required (e.g. email address, password). |
| 7 | **Test Steps** | Numbered, unambiguous actions a tester performs in the exact order they must be executed. |
| 8 | **Expected Result** | Observable outcome after each step (or after all steps) that would indicate the system is working correctly. |
| 9 | **Actual Result** | What actually happened during execution — filled in at test time. |
| 10 | **Status** | Pass / Fail / Blocked / Not Run — recorded after execution. |
| 11 | **Environment** | OS, browser, application version, and any external dependencies. |
| 12 | **Notes / Attachments** | Screenshots, logs, or other supplementary information. |

---

### Example of a Well-Constructed Test Case

---

#### TC-001 – Permanently delete an email from Gmail Trash

| Field | Value |
|-------|-------|
| **Test Case ID** | TC-001 |
| **Title** | Permanently delete an email from Gmail Trash |
| **Objective** | Verify that a user can permanently delete a trashed email and that it is no longer retrievable from the Trash folder. |
| **Priority** | High |
| **Preconditions** | 1. A Google account exists with credentials `testuser@gmail.com` / `TestP@ss123`.<br>2. The user is **not** logged in (fresh browser session).<br>3. The Trash folder contains at least one email with the subject "Meeting Notes". |
| **Test Data** | Email: `testuser@gmail.com`<br>Password: `TestP@ss123`<br>Subject to delete: `"Meeting Notes"` |
| **Test Steps** | 1. Open Google Chrome and navigate to `https://mail.google.com/mail/`.<br>2. Enter `testuser@gmail.com` in the email field and click **Next**.<br>3. Enter `TestP@ss123` in the password field and click **Next**.<br>4. In the left sidebar, click **Trash**.<br>5. Locate the email with subject `"Meeting Notes"` and click the checkbox next to it.<br>6. Click the **Delete forever** button in the toolbar.<br>7. Confirm the deletion in the confirmation dialog by clicking **OK**. |
| **Expected Result** | 7a. The email with subject "Meeting Notes" disappears from the Trash list.<br>7b. A confirmation banner ("Conversation deleted forever.") is displayed.<br>7c. Reloading the Trash folder does **not** show the email. |
| **Actual Result** | *(to be filled in during test execution)* |
| **Status** | Not Run |
| **Environment** | OS: Windows 11 / Ubuntu 22.04<br>Browser: Google Chrome latest<br>Gmail: Production `mail.google.com` |
| **Notes** | If Google prompts for 2FA, complete it manually and resume from Step 4. |

---

## Part 2 – Automated Test: TC-002 – Log the Subject of the Last Unread Email

### TC-002-01 – Log the subject of the most recent unread email

| Field | Value |
|-------|-------|
| **Test Case ID** | TC-002-01 |
| **Title** | Log the subject of the most recent unread email |
| **Objective** | Open Gmail in a new Chrome window, sign in, and write the subject of the last (most recent) unread email to the log. |
| **Priority** | High |
| **Preconditions** | 1. A dedicated Google test account is registered.<br>2. The inbox contains **at least five** unread emails.<br>3. Credentials are available via `GMAIL_USERNAME` and `GMAIL_PASSWORD` environment variables.<br>4. Google Chrome is installed on the test machine.<br>5. Internet connectivity is available. |
| **Test Data** | `GMAIL_USERNAME` – full Gmail address of the test account.<br>`GMAIL_PASSWORD` – app password generated under Google account → Security → App Passwords. |
| **Test Steps** | 1. Open Google Chrome in a **new window**.<br>2. Navigate to `https://mail.google.com/mail/`.<br>3. Enter the email address from `GMAIL_USERNAME` and click **Next**.<br>4. Enter the password from `GMAIL_PASSWORD` and click **Next**.<br>5. Wait for the inbox to load.<br>6. Locate the topmost unread email row (`tr.zE`).<br>7. Extract the subject text from the unread row's subject span (`.y6 span`).<br>8. Write the subject to the test log at INFO level. |
| **Expected Result** | 8a. The test log contains a line with `LAST UNREAD EMAIL SUBJECT: <actual subject>`.<br>8b. The test passes without assertion errors. |
| **Actual Result** | *(to be filled in during test execution)* |
| **Status** | Not Run |
| **Environment** | OS: Any (Windows / Linux / macOS)<br>Browser: Google Chrome (latest)<br>Automation: Java 11, Selenium 4, TestNG 7, WebDriverManager 5 |
| **Notes** | Google may present a 2FA / OTP challenge. The test waits up to 120 s for a human or external tool to resolve it, then continues. Use an App Password to avoid interactive 2FA altogether. |

---

### TC-002-02 – Inbox precondition: at least five unread emails

| Field | Value |
|-------|-------|
| **Test Case ID** | TC-002-02 |
| **Title** | Inbox precondition: at least five unread emails present |
| **Objective** | Verify that the precondition of the test suite (≥ 5 unread emails) is satisfied. |
| **Priority** | Medium |
| **Preconditions** | Same as TC-002-01.  Depends on TC-002-01 passing first (`dependsOnMethods`). |
| **Test Steps** | 1. Count unread email rows (`tr.zE`) currently visible in the inbox.<br>2. Assert that the count is ≥ 5. |
| **Expected Result** | The unread count is 5 or higher. |
| **Actual Result** | *(to be filled in during test execution)* |
| **Status** | Not Run |
| **Environment** | Same as TC-002-01 |

---

## How to Run the Automated Tests

### Prerequisites

1. **Java 11+** installed (`java -version`).
2. **Maven 3.6+** installed (`mvn -version`).
3. **Google Chrome** installed on the machine.
4. A Google test account with at least **5 unread emails** in the inbox.
5. Credentials exported as environment variables:

```bash
export GMAIL_USERNAME=your-test-account@gmail.com
export GMAIL_PASSWORD=your-app-password
```

> **Tip:** Generate an App Password at  
> [https://myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)  
> to avoid interactive 2-Step Verification prompts during test runs.

### Running the tests

```bash
# From the repository root
mvn clean test
```

### Running in headless mode (e.g. CI)

```bash
mvn clean test -Dbrowser.headless=true
```

### Viewing the log output

After a run, the full log is saved to `test-output/gmail-test.log`.

---

## Project Structure

```
├── pom.xml                                             Maven project descriptor
├── TEST_CASES.md                                       This file
└── src/
    └── test/
        ├── java/
        │   └── com/openway/
        │       ├── pages/
        │       │   ├── LoginPage.java                  Page Object – Google Sign-In
        │       │   └── InboxPage.java                  Page Object – Gmail Inbox
        │       ├── tests/
        │       │   └── GmailUnreadEmailTest.java        TestNG test class
        │       └── utils/
        │           ├── ConfigReader.java               Reads config / env vars
        │           └── DriverFactory.java              Thread-safe ChromeDriver factory
        └── resources/
            ├── config.properties                       Default configuration
            ├── log4j2.xml                              Logging configuration
            └── testng.xml                              TestNG suite descriptor
```
