import pytest

from .config import config_from_marker
from .scan import scan_driver, scan_page


def pytest_configure(config):
    config.addinivalue_line(
        "markers",
        "accessibility(standard='WCAG2AA'): run an A11yGuard scan after the test",
    )


@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_makereport(item, call):
    outcome = yield
    report = outcome.get_result()
    if call.when != "call" or report.skipped:
        return
    marker = item.get_closest_marker("accessibility")
    if marker is None:
        return
    cfg = config_from_marker(marker)
    if not cfg.enabled:
        return
    funcargs = getattr(item, "funcargs", {}) or {}
    page = funcargs.get("page")
    driver = funcargs.get("driver")
    name = item.name
    if page is not None:
        scan_page(page, name, cfg)
    elif driver is not None:
        scan_driver(driver, name, cfg)
    else:
        raise RuntimeError(
            "A11yGuard could not find a page or driver fixture on the test."
        )
