package tests;

import com.a11yguard.annotation.Accessibility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HomePageAccessibilityTest extends BaseAccessibilityTest {

    @Accessibility(standard = "WCAG2AA", failOn = {"CRITICAL", "SERIOUS"})
    @Test
    void homePage() {
        driver.get("https://example.com");
        assertTrue(driver.getTitle().toLowerCase().contains("example"),
            "Home page should load");
    }
}
