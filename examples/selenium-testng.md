# Selenium + TestNG

Add:

```xml
<dependency>
  <groupId>io.github.shipeecon</groupId>
  <artifactId>a11yguard-testng</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Register once:

```java
import com.a11yguard.testng.A11yTestNGListener;
import org.testng.annotations.Listeners;

@Listeners(A11yTestNGListener.class)
public class BaseTest {
}
```

Test:

```java
import com.a11yguard.annotation.Accessibility;
import org.testng.annotations.Test;

class LoginTest extends BaseTest {

    WebDriver driver;

    @Accessibility
    @Test
    void login() {
        driver.get("https://example.com/login");
    }
}
```

For an entire suite, put the listener in `testng.xml` instead of using `@Listeners`.


## Reports

After the scan, A11yGuard automatically writes the configured reports to `target/a11yguard-report`.

All formats are enabled by default:

```text
a11yguard-report.html
a11yguard-report.json
a11yguard-report.xml
a11yguard-report.sarif
a11yguard-report.csv
a11yguard-report.md
```

To customize:

```java
@Accessibility(
    reportDir = "target/accessibility",
    reportFormats = {"HTML", "JSON", "SARIF"}
)
```
