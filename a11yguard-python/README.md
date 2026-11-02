# a11yguard

Accessibility testing for Playwright and Selenium in Python.

```bash
pip install "a11yguard[playwright]"
# or: pip install "a11yguard[selenium]"
```

```python
import pytest

@pytest.mark.accessibility(standard="WCAG2AA", fail_on=["CRITICAL", "SERIOUS"])
def test_home_page(page):
    page.goto("https://example.com")
```

The pytest plugin scans the `page` or `driver` fixture after the test. Reports are written to `target/a11yguard-report`.
