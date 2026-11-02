from __future__ import annotations

import json
from pathlib import Path
from typing import Dict, List

from .config import AccessibilityConfig


def _target(v: Dict) -> str:
    return " | ".join(v.get("target") or [])


def _matches(rule_id: str, target: str, url: str, entry: Dict) -> bool:
    if (entry.get("id") or "").lower() != (rule_id or "").lower():
        return False
    wanted = entry.get("target") or ""
    target_ok = not wanted or wanted == "*" or wanted in target
    wanted_url = entry.get("url") or ""
    url_ok = not wanted_url or wanted_url == "*" or wanted_url == url
    return target_ok and url_ok


def apply_baseline(result: Dict, config: AccessibilityConfig) -> Dict:
    baseline: List[Dict] = []
    path = Path(config.baseline)
    if path.is_file():
        baseline = json.loads(path.read_text(encoding="utf-8")).get("violations") or []
    kept = []
    suppressed = []
    for v in result.get("violations") or []:
        target = _target(v)
        yaml_hit = any(_matches(v.get("id"), target, result.get("url"), s) for s in config.suppress)
        base_hit = any(_matches(v.get("id"), target, result.get("url"), s) for s in baseline)
        if yaml_hit or base_hit:
            suppressed.append(v)
        else:
            kept.append(v)
    result["violations"] = kept
    result["suppressed"] = suppressed
    if config.update_baseline:
        path.write_text(
            json.dumps(
                {
                    "violations": [
                        {"id": v.get("id"), "target": _target(v), "url": result.get("url")}
                        for v in kept
                    ]
                },
                indent=2,
            ),
            encoding="utf-8",
        )
    return result
