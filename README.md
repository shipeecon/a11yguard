# A11yGuard

A cross-framework accessibility testing library built around a single annotation:

```java
@Accessibility
@Test
void checkout() {
    page.navigate("https://example.com/checkout");
}
```

Supported languages and runners:

- **Java** — Selenium + Playwright, JUnit 5 and TestNG (`@Accessibility`)
- **JavaScript / TypeScript** — Playwright Test and Selenium WebDriver
- **Python** — pytest + Playwright or Selenium (`@pytest.mark.accessibility`)
- **C# / .NET** — NUnit + Playwright or Selenium (`[Accessibility]`)

The same scan-and-report model is used in every language. Java keeps the `@Accessibility` annotation; the other packages use the closest native hook.

## Important design constraint

Java annotations cannot magically execute code in every test runner. JUnit 5 and TestNG require a one-time runner/listener bootstrap. After that bootstrap, individual tests only need `@Accessibility`.

This project therefore has:

```text
A11yGuard Core
       |
       +-- @Accessibility
       |
       +-- Selenium adapter
       +-- Playwright Java adapter
       +-- JUnit 5 integration
       +-- TestNG integration
       +-- reporting/configuration
```

The accessibility engine is axe-core. The current scaffold pins the Deque Java integrations to axe-core 4.13.0.

## Maven modules

- `a11yguard-core`
- `a11yguard-selenium`
- `a11yguard-playwright-java`
- `a11yguard-junit5`
- `a11yguard-testng`

## Use as a dependency

Public coordinates after a `v0.1.0` release:

| Ecosystem | Install |
|---|---|
| Java / Maven | `com.a11yguard:a11yguard-junit5:0.1.0` |
| JavaScript | `npm install a11yguard` |
| Python | `pip install a11yguard` |
| .NET | `dotnet add package A11yGuard` |

Until the first public upload finishes, install from this repo:

```bash
mvn clean install -pl "!tests"
```

That publishes these coordinates to `~/.m2/repository` (Windows: `%USERPROFILE%\.m2\repository`):

| If you use | Artifact |
|---|---|
| Selenium + JUnit 5 | `com.a11yguard:a11yguard-junit5:0.1.0-SNAPSHOT` |
| Selenium + TestNG | `com.a11yguard:a11yguard-testng:0.1.0-SNAPSHOT` |
| Playwright Java | `com.a11yguard:a11yguard-playwright-java:0.1.0-SNAPSHOT` |

Core and Selenium are pulled in transitively. Keep `selenium-java` (or Playwright) as your own test dependency.

### Maven

JUnit 5:

```xml
<dependency>
  <groupId>com.a11yguard</groupId>
  <artifactId>a11yguard-junit5</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <scope>test</scope>
</dependency>
```

TestNG: use `a11yguard-testng`. Playwright: use `a11yguard-playwright-java`.

### Gradle (Groovy)

```groovy
repositories {
  mavenLocal()
}
dependencies {
  testImplementation "com.a11yguard:a11yguard-junit5:0.1.0-SNAPSHOT"
}
```

### Gradle (Kotlin)

```kotlin
repositories {
  mavenLocal()
}
dependencies {
  testImplementation("com.a11yguard:a11yguard-junit5:0.1.0-SNAPSHOT")
}
```

### sbt

```scala
resolvers += Resolver.mavenLocal
libraryDependencies += "com.a11yguard" % "a11yguard-junit5" % "0.1.0-SNAPSHOT" % Test
```

### Ivy

```xml
<dependency org="com.a11yguard" name="a11yguard-junit5" rev="0.1.0-SNAPSHOT" conf="test->default"/>
```

### JAR file (no Maven/Gradle)

After `mvn clean install -pl "!tests"`, the jars are under each module’s `target/` folder, for example:

- `a11yguard-junit5/target/a11yguard-junit5-0.1.0-SNAPSHOT.jar`
- `a11yguard-core/target/a11yguard-core-0.1.0-SNAPSHOT.jar`
- `a11yguard-selenium/target/a11yguard-selenium-0.1.0-SNAPSHOT.jar`

Add those jars plus their transitive dependencies (`axe-core`, Jackson, JUnit/TestNG, Selenium) to the test classpath. Prefer Maven or Gradle so those transitives are resolved automatically.

### Sharing with another laptop or team

1. **Same machine / already installed:** `mavenLocal()` / local `.m2` is enough.
2. **Another Windows/Mac laptop:** zip this repo, run `mvn clean install -pl "!tests"` there, then add the dependency above.
3. **Company Nexus / Artifactory / GitHub Packages:** deploy the modules, then point other projects at that repository instead of `mavenLocal()`.
4. **Maven Central (public):** tag `v0.1.0` after adding the GitHub Actions secrets in the Publishing section.

## JavaScript / TypeScript

From `a11yguard-js`:

```bash
cd a11yguard-js
npm install
npm run build
```

Playwright Test:

```ts
import { test, expect } from "a11yguard/playwright";

test("home page", async ({ page }) => {
  await page.goto("https://example.com");
  await expect(page).toHaveTitle(/Example Domain/);
});
```

The Playwright `page` fixture scans automatically after the test. Reports go to `target/a11yguard-report`.

Selenium (Mocha/Jest):

```js
const { scanSeleniumDriver } = require("a11yguard/selenium");
await scanSeleniumDriver(driver, "home page");
```

Install into another JS project with `npm install ./a11yguard-js` (or publish `a11yguard` to npm).

## Python

```bash
pip install -e "./a11yguard-python[playwright]"
# or: pip install -e "./a11yguard-python[selenium]"
```

```python
import pytest

@pytest.mark.accessibility(standard="WCAG2AA", fail_on=["CRITICAL", "SERIOUS"])
def test_home_page(page):
    page.goto("https://example.com")
```

The pytest plugin scans the `page` or `driver` fixture after the test.

## C# / .NET

Add the `a11yguard-dotnet/A11yGuard` project (or pack it as `A11yGuard`).

```csharp
[Accessibility(Standard = "WCAG2AA", FailOn = "CRITICAL,SERIOUS")]
[Test]
public async Task HomePage()
{
    await page.GotoAsync("https://example.com");
}
```

Keep an `IPage` or `IWebDriver` field on the test fixture. The NUnit `[Accessibility]` attribute scans after the test.

## Selenium

```java
import com.a11yguard.annotation.Accessibility;
import org.junit.jupiter.api.Test;

@Accessibility
@Test
void homePage() {
    driver.get("https://example.com");
}
```

One-time JUnit registration:

```java
@ExtendWith(A11yJUnitExtension.class)
class MyTest {
    ...
}
```

or register the extension through your test framework's common base class.

## TestNG

```java
@Accessibility
@Test
public void homePage() {
    driver.get("https://example.com");
}
```

One-time TestNG registration:

```java
@Listeners(A11yTestNGListener.class)
public class BaseTest {
}
```

or use `testng.xml`.

## Playwright Java

```java
@Accessibility
@Test
void checkout() {
    page.navigate("https://example.com/checkout");
}
```

Keep a `com.microsoft.playwright.Page` field on the test instance and add `a11yguard-playwright-java` next to `a11yguard-junit5`. The JUnit/TestNG hook finds that `Page` field and runs axe-core after the test.

## Configuration

Per-test:

```java
@Accessibility(
    standard = "WCAG2AA",
    failOn = {"CRITICAL", "SERIOUS"},
    screenshots = true,
    baseline = "a11yguard-baseline.json"
)
```

Global defaults live in `a11yguard.yaml` at the project root (also accepts `a11yguard.yml` or `a11yguard.json`):

```yaml
standard: WCAG2AA
failOn: [CRITICAL, SERIOUS]
tags: [wcag2a, wcag2aa]
screenshots: true
baseline: a11yguard-baseline.json
updateBaseline: false
failOnViolations: true
suppress:
  - id: color-contrast
    target: ".brand-banner"
    reason: "Approved brand color"
```

Known issues in `a11yguard-baseline.json` are ignored. Set `updateBaseline: true` once to snapshot the current violations, then turn it off.

## Screenshots

When `screenshots: true`, each scan writes a page PNG under `target/a11yguard-report/screenshots/` and embeds it in the HTML report. Element shots are captured when the axe target can be located.

## CI

Tests already fail on CRITICAL/SERIOUS violations. After a run, `target/a11yguard-report/a11yguard-exit-code` contains `0` or `1`. A GitHub Actions workflow is in `.github/workflows/a11y.yml`.

To fail a pipeline from a saved report:

```bash
java -cp a11yguard-core/target/a11yguard-core-0.1.0-SNAPSHOT.jar:$(mvn -pl a11yguard-core -q dependency:build-classpath -DincludeScope=runtime) \
  com.a11yguard.ci.A11yGuardCi target/a11yguard-report/a11yguard-report.json
```

## Publishing

A GitHub Actions workflow in `.github/workflows/publish.yml` publishes **all four** ecosystems when you push a version tag such as `v0.1.0`.

Package names `a11yguard` / `A11yGuard` are currently free on npm, PyPI, and NuGet.

### 1. Create the publisher accounts

Use any email you own. Each site emails a confirmation link.

1. **GitHub** — https://github.com/signup (needed for the public repo and the release workflow)
2. **npm** — https://www.npmjs.com/signup then https://www.npmjs.com/settings/~/tokens → **Granular Access Token** with permission to publish
3. **PyPI** — https://pypi.org/account/register/ then https://pypi.org/manage/account/token/ → API token
4. **NuGet** — https://www.nuget.org/users/account/LogOn (sign in with Microsoft) then https://www.nuget.org/account/apikeys → API key with **Push**
5. **Maven Central** — https://central.sonatype.com/ → register, then claim namespace `com.a11yguard` (needs the `a11yguard.com` domain) **or** `io.github.<your-github-username>` (needs a GitHub repo). Create a portal user token under Account → View User Token. Generate a GPG key for signing: `gpg --full-generate-key`

### 2. Put the tokens in GitHub Actions secrets

In the GitHub repo: **Settings → Secrets and variables → Actions**

| Secret | From |
|---|---|
| `NPM_TOKEN` | npm granular token |
| `PYPI_API_TOKEN` | PyPI API token (`pypi-...`) |
| `NUGET_API_KEY` | NuGet API key |
| `CENTRAL_USERNAME` | Maven Central user-token username |
| `CENTRAL_TOKEN` | Maven Central user-token password |
| `GPG_PRIVATE_KEY` | `gpg --armor --export-secret-keys YOUR_KEY_ID` |
| `GPG_PASSPHRASE` | passphrase for that GPG key |

### 3. Publish

```bash
git tag v0.1.0
git push origin v0.1.0
```

That runs `.github/workflows/publish.yml` and uploads:

- Java → Maven Central (`com.a11yguard:*:0.1.0`)
- JS/TS → npm (`a11yguard`)
- Python → PyPI (`a11yguard`)
- .NET → NuGet (`A11yGuard`)

If a secret is missing, that ecosystem is skipped and the others still publish.

Local fallback before the repo is public:

```bash
mvn clean install -pl "!tests"
cd a11yguard-js && npm install && npm run build
pip install -e "./a11yguard-python[playwright]"
```

## Reports

The core result model supports:

- HTML
- JSON
- JUnit XML
- SARIF

## Reporting

A11yGuard generates detailed reports automatically after every `@Accessibility` scan. By default it creates:

- **HTML** — dashboard, test summary, violation details, targets, HTML snippets and manual-review/incomplete items.
- **JSON** — machine-readable full result model.
- **JUnit XML** — CI/test-report compatible failures and incomplete counts.
- **SARIF 2.1.0** — static-analysis/security tooling compatible output.
- **CSV** — flat violation/test data for Excel, BI and data pipelines.
- **Markdown** — readable report for pull requests and documentation.

Reports are aggregated across scans in the current JVM under `target/a11yguard-report` by default. Configure them directly on the annotation:

```java
@Accessibility(
    reportDir = "target/a11yguard",
    reportFormats = {"HTML", "JSON", "JUNIT_XML", "SARIF", "CSV", "MARKDOWN"}
)
```

The same reporting facade is public for custom integrations:

```java
AccessibilityReporter.report(result, Path.of("target/a11yguard"),
    EnumSet.allOf(ReportFormat.class));
```

## Scope

axe-core is an automated accessibility engine; automated checks do not replace manual accessibility testing. axe-core documents that it can identify many WCAG issues but some cases are returned as `incomplete` and require manual review.

