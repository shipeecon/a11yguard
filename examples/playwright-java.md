# Playwright Java

Add `a11yguard-junit5` and `a11yguard-playwright-java`. Keep a `Page` field on the test instance so A11yGuard can scan after the test.

```java
import com.a11yguard.annotation.Accessibility;
import com.a11yguard.junit5.A11yJUnitExtension;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(A11yJUnitExtension.class)
class CheckoutTest {
    Playwright playwright;
    Browser browser;
    Page page;

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(true));
        page = browser.newPage();
    }

    @AfterEach
    void tearDown() {
        playwright.close();
    }

    @Accessibility
    @Test
    void checkout() {
        page.navigate("https://example.com/checkout");
    }
}
```

The Playwright adapter is separate from the Selenium adapter because they have different page/driver APIs.

## Reports

After the scan, A11yGuard writes reports to `target/a11yguard-report`.
