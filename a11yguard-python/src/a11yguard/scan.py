from __future__ import annotations

from datetime import datetime, timezone
from typing import Any, Dict, List

from pathlib import Path

from .baseline import apply_baseline
from .config import AccessibilityConfig, load_config
from .report import assert_accessible, report

AXE_RUN_JS = """
async (tags) => {
  const existing = document.querySelector('script[data-a11yguard-axe]');
  if (!window.axe) {
    await new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = 'https://cdnjs.cloudflare.com/ajax/libs/axe-core/4.13.0/axe.min.js';
      script.setAttribute('data-a11yguard-axe', 'true');
      script.onload = resolve;
      script.onerror = reject;
      document.head.appendChild(script);
    });
  }
  return await window.axe.run({ runOnly: { type: 'tag', values: tags } });
}
"""


def _map_rule(rule: Dict) -> Dict:
    nodes = rule.get("nodes") or []
    first = nodes[0] if nodes else {}
    return {
        "id": rule.get("id"),
        "impact": rule.get("impact") or "minor",
        "description": rule.get("description"),
        "help": rule.get("help"),
        "helpUrl": rule.get("helpUrl"),
        "target": first.get("target") or [],
        "html": first.get("html") or "",
    }


def _result(framework: str, test_name: str, url: str, config: AccessibilityConfig, raw: Dict) -> Dict:
    return {
        "framework": framework,
        "testName": test_name,
        "url": url,
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "standard": config.standard,
        "violations": [_map_rule(v) for v in raw.get("violations") or []],
        "incomplete": [_map_rule(v) for v in raw.get("incomplete") or []],
    }


def _finish(result: Dict, cfg: AccessibilityConfig) -> Dict:
    result = apply_baseline(result, cfg)
    report(result, cfg.report_dir, cfg.report_formats)
    Path(cfg.report_dir).mkdir(parents=True, exist_ok=True)
    failing = any(
        (v.get("impact") or "").lower() in {"critical", "serious"}
        for v in result.get("violations") or []
        if v
    )
    (Path(cfg.report_dir) / "a11yguard-exit-code").write_text(
        "1\n" if failing else "0\n", encoding="utf-8"
    )
    if cfg.fail_on_violations and not cfg.update_baseline:
        assert_accessible(result, cfg.fail_on)
    return result


def _attach_page_screenshot(page: Any, result: Dict, cfg: AccessibilityConfig, test_name: str) -> None:
    if not cfg.screenshots:
        return
    directory = Path(cfg.report_dir) / "screenshots"
    directory.mkdir(parents=True, exist_ok=True)
    shot = directory / f"{test_name}-page.png"
    page.screenshot(path=str(shot), full_page=True)
    rel = f"screenshots/{test_name}-page.png"
    result["pageScreenshot"] = rel
    for v in result.get("violations") or []:
        v["screenshot"] = rel


def scan_page(page: Any, test_name: str, config: AccessibilityConfig | None = None) -> Dict:
    cfg = config or load_config()
    raw = page.evaluate(AXE_RUN_JS, cfg.tags)
    result = _result("playwright-python", test_name, page.url, cfg, raw)
    _attach_page_screenshot(page, result, cfg, test_name)
    return _finish(result, cfg)


def scan_driver(driver: Any, test_name: str, config: AccessibilityConfig | None = None) -> Dict:
    cfg = config or load_config()
    script = """
        var tags = arguments[0];
        var callback = arguments[arguments.length - 1];
        function run() {
          window.axe.run({ runOnly: { type: 'tag', values: tags } }).then(callback);
        }
        if (window.axe) { run(); return; }
        var script = document.createElement('script');
        script.src = 'https://cdnjs.cloudflare.com/ajax/libs/axe-core/4.13.0/axe.min.js';
        script.onload = run;
        script.onerror = function() { callback({violations: [], incomplete: []}); };
        document.head.appendChild(script);
    """
    raw = driver.execute_async_script(script, cfg.tags) or {}
    result = _result("selenium-python", test_name, driver.current_url, cfg, raw)
    if cfg.screenshots:
        directory = Path(cfg.report_dir) / "screenshots"
        directory.mkdir(parents=True, exist_ok=True)
        driver.save_screenshot(str(directory / f"{test_name}-page.png"))
        rel = f"screenshots/{test_name}-page.png"
        result["pageScreenshot"] = rel
        for v in result.get("violations") or []:
            v["screenshot"] = rel
    return _finish(result, cfg)
