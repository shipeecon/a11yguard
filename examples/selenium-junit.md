# Selenium + JUnit 5

Add:

```xml
<dependency>
  <groupId>io.github.shipeecon</groupId>
  <artifactId>a11yguard-junit5</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Test:

```java
import com.a11yguard.annotation.Accessibility;
import com.a11yguard.junit5.A11yJUnitExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

@ExtendWith(A11yJUnitExtension.class)
class LoginTest {

    WebDriver driver;

    @Accessibility
    @Test
    void login() {
        driver.get("https://example.com/login");
    }
}
```


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
