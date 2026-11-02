import pytest
from selenium import webdriver
from selenium.webdriver.chrome.options import Options


@pytest.fixture
def driver():
    options = Options()
    options.add_argument("--headless=new")
    browser = webdriver.Chrome(options=options)
    yield browser
    browser.quit()


@pytest.mark.accessibility(standard="WCAG2AA", fail_on=["CRITICAL", "SERIOUS"])
def test_home_page(driver):
    driver.get("https://example.com")
    assert "Example Domain" in driver.title
