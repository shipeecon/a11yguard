import pytest
from playwright.sync_api import Page


@pytest.mark.accessibility(standard="WCAG2AA", fail_on=["CRITICAL", "SERIOUS"])
def test_home_page(page: Page):
    page.goto("https://example.com")
    assert "Example Domain" in page.title()
