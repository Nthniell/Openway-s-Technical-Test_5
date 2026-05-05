# Gmail Mail Deletion Automation

Java, Selenium WebDriver, and TestNG automation for Gmail deletion scenarios `DEL-001` through `DEL-005`.

Each deletion scenario targets the newest unread email at the top of the Inbox, so the test always deletes an unread message when the mailbox has unread test data.

## Automated Scenarios

- `DEL-001` - Delete a single email from Inbox
- `DEL-002` - Verify deleted email is removed from Inbox view
- `DEL-003` - Delete multiple emails sequentially
- `DEL-004` - Verify Undo after deletion
- `DEL-005` - Verify deleted email is visible in Trash

## Setup

Use a dedicated Gmail test account only. Configure credentials in:

```text
src/test/resources/config.properties
```

Supported configuration keys:

```properties
test.email=your-test-account@gmail.com
test.password=your-password
test.verification.code=
```

The same values can also be supplied with Maven system properties or environment variables:

```bash
mvn test -Dtest.email=... -Dtest.password=...
TEST_EMAIL=... TEST_PASSWORD=... mvn test
```

## Run

```bash
mvn test
```

The TestNG suite is defined in:

```text
src/test/resources/testng.xml
```

Logs are written to:

```text
logs/gmail-automation.log
logs/gmail-automation-error.log
```

## Notes

These tests are destructive by design. Running the full suite deletes unread emails from the configured Gmail account, except for `DEL-004`, which restores the deleted email using Undo. Keep enough unread emails in the Inbox before running the suite.
