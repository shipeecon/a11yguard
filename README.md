# A11yGuard

Accessibility checks for Selenium and Playwright. You write a normal test; A11yGuard runs axe-core after the page loads and fails on CRITICAL/SERIOUS issues.

- **Java** — `@Accessibility` with JUnit 5 or TestNG
- **JavaScript / TypeScript** — Playwright Test or Selenium
- **Python** — `@pytest.mark.accessibility`
- **.NET** — `[Accessibility]` with NUnit

## Install

```bash
npm install a11yguard
pip install "a11yguard[playwright]"   # or: pip install "a11yguard[selenium]"
dotnet add package A11yGuard
```

Java is not on Maven Central yet. From this repo:

```bash
mvn clean install -pl "!tests"
```

Then add one test dependency: `a11yguard-junit5`, `a11yguard-testng`, or `a11yguard-playwright-java` (group `io.github.shipeecon`, version `0.1.0`).

## Java

Register the hook once on a base class, then annotate tests.

JUnit 5:

```java
@ExtendWith(A11yJUnitExtension.class)
class BaseTest {
    WebDriver driver; // or Playwright Page
}

@Accessibility
@Test
void homePage() {
    driver.get("https://example.com");
}
```

TestNG: `@Listeners(A11yTestNGListener.class)` on the base class (or in `testng.xml`).

Keep a `WebDriver` or `com.microsoft.playwright.Page` field on the test instance.

## JavaScript / TypeScript

```ts
import { test } from "a11yguard/playwright";

test("home page", async ({ page }) => {
  await page.goto("https://example.com");
});
```

The Playwright `page` fixture scans after the test.

Selenium:

```js
const { scanSeleniumDriver } = require("a11yguard/selenium");
await scanSeleniumDriver(driver, "home page");
```

## Python

```python
import pytest

@pytest.mark.accessibility(standard="WCAG2AA", fail_on=["CRITICAL", "SERIOUS"])
def test_home_page(page):
    page.goto("https://example.com")
```

The plugin scans the `page` or `driver` fixture after the test.

## .NET

```csharp
[Accessibility(Standard = "WCAG2AA", FailOn = "CRITICAL,SERIOUS")]
[Test]
public async Task HomePage()
{
    await page.GotoAsync("https://example.com");
}
```

Keep an `IPage` or `IWebDriver` field on the fixture.

## Configuration

Per test (Java):

```java
@Accessibility(
    standard = "WCAG2AA",
    failOn = {"CRITICAL", "SERIOUS"},
    reportDir = "target/a11yguard-report",
    screenshots = true
)
```

Optional project file `a11yguard.yaml` (also `.yml` / `.json`). Installing the library does not create this file:

```yaml
standard: WCAG2AA
failOn: [CRITICAL, SERIOUS]
tags: [wcag2a, wcag2aa]
reportDir: target/a11yguard-report
screenshots: true
baseline: a11yguard-baseline.json
updateBaseline: false
failOnViolations: true
suppress:
  - id: color-contrast
    target: ".brand-banner"
    reason: "Approved brand color"
```

The test annotation wins over YAML when you set it. Known issues in `a11yguard-baseline.json` are ignored. Set `updateBaseline: true` once to snapshot current violations, then turn it off.

## Reports

After each scan, files go to `target/a11yguard-report` (or `reportDir`):

- HTML, JSON, JUnit XML, SARIF, CSV, Markdown
- `screenshots/`
- `a11yguard-exit-code` (`0` or `1` for CI)

Open `a11yguard-report.html` for the dashboard.

Automated axe-core checks do not replace manual accessibility testing. Some rules come back as `incomplete` and need a human review.
