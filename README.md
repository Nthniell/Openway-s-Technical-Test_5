# Openway's Technical Test – QA Automation

## Overview

This repository contains the solution to the QA technical test for Openway.

It covers:

1. **Test case documentation** – components of a test case with a worked example  
   → see [`TEST_CASES.md`](TEST_CASES.md)

2. **Automated test (Scenario Option A)** – Java + Selenium + TestNG test that:
   - Opens Google Chrome in a new window
   - Navigates to `https://mail.google.com/mail/`
   - Signs in using credentials supplied via environment variables
   - Logs the subject of the most recent unread email in the mailbox

## Quick Start

### Prerequisites

| Tool | Version |
|------|---------|
| Java | 11+ |
| Maven | 3.6+ |
| Google Chrome | latest |

### Configure credentials

```bash
export GMAIL_USERNAME=your-test-account@gmail.com
export GMAIL_PASSWORD=your-app-password   # Google App Password recommended
```

> Generate an App Password at <https://myaccount.google.com/apppasswords>  
> to avoid interactive 2-FA prompts.

### Run the tests

```bash
mvn clean test
```

Log output (console + file) contains a clearly labelled line:

```
LAST UNREAD EMAIL SUBJECT: <subject of the most recent unread email>
```

The full log is also saved to `test-output/gmail-test.log`.

### Headless mode (CI)

```bash
mvn clean test -Dbrowser.headless=true
```

## Project Structure

```
├── pom.xml                                  Maven project (Java 11, Selenium 4, TestNG 7)
├── TEST_CASES.md                            Test case documentation
└── src/test/
    ├── java/com/openway/
    │   ├── pages/
    │   │   ├── LoginPage.java               Page Object – Google Sign-In
    │   │   └── InboxPage.java               Page Object – Gmail Inbox
    │   ├── tests/
    │   │   └── GmailUnreadEmailTest.java    TestNG test class (TC-002-01, TC-002-02)
    │   └── utils/
    │       ├── ConfigReader.java            Env-var / properties config reader
    │       └── DriverFactory.java           Thread-safe ChromeDriver factory
    └── resources/
        ├── config.properties               Default configuration
        ├── log4j2.xml                      Logging configuration
        └── testng.xml                      TestNG suite descriptor
```

