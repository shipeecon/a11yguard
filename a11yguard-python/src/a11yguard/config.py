from __future__ import annotations

import json
from dataclasses import dataclass, field
from pathlib import Path
from typing import List


@dataclass
class AccessibilityConfig:
    standard: str = "WCAG2AA"
    fail_on: List[str] = field(default_factory=lambda: ["CRITICAL", "SERIOUS"])
    tags: List[str] = field(default_factory=lambda: ["wcag2a", "wcag2aa"])
    exclude: List[str] = field(default_factory=list)
    report_formats: List[str] = field(
        default_factory=lambda: ["HTML", "JSON", "JUNIT_XML", "SARIF", "CSV", "MARKDOWN"]
    )
    report_dir: str = "target/a11yguard-report"
    enabled: bool = True
    screenshots: bool = True
    baseline: str = "a11yguard-baseline.json"
    update_baseline: bool = False
    fail_on_violations: bool = True
    suppress: List[dict] = field(default_factory=list)


DEFAULT_CONFIG = AccessibilityConfig()


def _find_config() -> Path | None:
    here = Path.cwd()
    for folder in [here, *here.parents]:
        for name in ("a11yguard.yaml", "a11yguard.yml", "a11yguard.json"):
            candidate = folder / name
            if candidate.is_file():
                return candidate
    return None


def _load_file() -> dict:
    path = _find_config()
    if path is None:
        return {}
    text = path.read_text(encoding="utf-8")
    if path.suffix == ".json":
        return json.loads(text)
    data = {}
    current = None
    for raw in text.splitlines():
        line = raw.split("#", 1)[0]
        item = line.strip()
        if item.startswith("- ") and current:
            data.setdefault(current, []).append(item[2:].strip().strip("\"'"))
            continue
        if ":" in line and not line.startswith(" "):
            key, value = line.split(":", 1)
            key, value = key.strip(), value.strip()
            if value in ("", "[]"):
                current = key
                data[key] = []
            elif value in ("true", "false"):
                current = None
                data[key] = value == "true"
            else:
                current = None
                data[key] = value.strip("\"'")
    return data


def load_config() -> AccessibilityConfig:
    file_data = _load_file()
    cfg = AccessibilityConfig()
    mapping = {
        "standard": "standard",
        "failOn": "fail_on",
        "fail_on": "fail_on",
        "tags": "tags",
        "exclude": "exclude",
        "reportFormats": "report_formats",
        "report_formats": "report_formats",
        "reportDir": "report_dir",
        "report_dir": "report_dir",
        "screenshots": "screenshots",
        "baseline": "baseline",
        "updateBaseline": "update_baseline",
        "update_baseline": "update_baseline",
        "failOnViolations": "fail_on_violations",
        "fail_on_violations": "fail_on_violations",
        "suppress": "suppress",
        "enabled": "enabled",
    }
    for src, dest in mapping.items():
        if src in file_data:
            setattr(cfg, dest, file_data[src])
    return cfg


def config_from_marker(marker) -> AccessibilityConfig:
    kwargs = marker.kwargs if marker else {}
    base = load_config()
    for name in (
        "standard",
        "fail_on",
        "tags",
        "exclude",
        "report_formats",
        "report_dir",
        "enabled",
        "screenshots",
        "baseline",
        "update_baseline",
        "fail_on_violations",
        "suppress",
    ):
        if name in kwargs:
            setattr(base, name, kwargs[name])
    return base
