from __future__ import annotations

import json
from html import escape
from pathlib import Path
from typing import Dict, List

_RESULTS: List[Dict] = []


def _target(v: Dict) -> str:
    return " | ".join(v.get("target") or [])


def _severity(impact: str | None) -> str:
    return {
        "critical": "CRITICAL",
        "serious": "SERIOUS",
        "moderate": "MODERATE",
    }.get((impact or "").lower(), "MINOR")


def report(result: Dict, report_dir: str, formats: List[str]) -> None:
    _RESULTS.append(result)
    directory = Path(report_dir)
    directory.mkdir(parents=True, exist_ok=True)
    writers = {
        "HTML": _html,
        "JSON": _json,
        "MARKDOWN": _markdown,
        "CSV": _csv,
        "JUNIT_XML": _junit,
        "SARIF": _sarif,
    }
    for fmt in formats:
        writers[fmt](_RESULTS, directory)


def assert_accessible(result: Dict, fail_on: List[str]) -> None:
    failing = [
        v for v in result.get("violations", []) if _severity(v.get("impact")) in fail_on
    ]
    if not failing:
        return
    details = "\n".join(
        f"[{v.get('impact')}] {v.get('id')} - {v.get('help')} - {_target(v)}"
        for v in failing
    )
    raise AssertionError(
        f"Accessibility violations found at {result.get('url')}\n{details}"
    )


def _html(all_results: List[Dict], directory: Path) -> None:
    violations = sum(len(r.get("violations", [])) for r in all_results)
    incomplete = sum(len(r.get("incomplete", [])) for r in all_results)
    pages = len({r.get("url") for r in all_results})
    rows = "".join(
        f"<tr><td>{escape(str(r.get('framework')))}</td><td>{escape(str(r.get('testName')))}</td>"
        f"<td>{escape(str(r.get('url')))}</td><td>{escape(str(r.get('standard')))}</td>"
        f"<td>{len(r.get('violations', []))}</td><td>{len(r.get('incomplete', []))}</td>"
        f"<td>{escape(str(r.get('timestamp')))}</td></tr>"
        for r in all_results
    )
    details = []
    for r in all_results:
        shot = (
            f"<p><img src='{escape(str(r.get('pageScreenshot')))}' alt='page screenshot' style='max-width:100%'></p>"
            if r.get("pageScreenshot")
            else ""
        )
        vrows = "".join(
            "<tr><td>{}</td><td>{}</td><td>{}</td><td>{}</td><td><code>{}</code></td><td><code>{}</code></td><td>{}</td></tr>".format(
                escape(str(v.get("impact"))),
                escape(str(v.get("id"))),
                escape(str(v.get("description"))),
                escape(str(v.get("help"))),
                escape(_target(v)),
                escape(str(v.get("html"))),
                (
                    f"<img src='{escape(str(v.get('screenshot')))}' alt='{escape(str(v.get('id')))}' style='max-width:240px'>"
                    if v.get("screenshot")
                    else ""
                ),
            )
            for v in r.get("violations", [])
        )
        details.append(
            f"<details><summary><b>{escape(str(r.get('testName')))}</b> — {escape(str(r.get('url')))} "
            f"({len(r.get('violations', []))})</summary>{shot}"
            f"<table><tr><th>Impact</th><th>Rule</th><th>Description</th><th>Help</th><th>Target</th><th>HTML</th><th>Screenshot</th></tr>"
            f"{vrows}</table></details>"
        )
    html = f"""<!doctype html><html><head><meta charset="utf-8"><title>A11yGuard Accessibility Report</title>
<style>
body{{font-family:Arial,sans-serif;margin:0;background:#f6f7f9;color:#222}}
header{{padding:28px;background:#1f2937;color:#fff}} main{{padding:24px;max-width:1400px;margin:auto}}
.cards{{display:grid;grid-template-columns:repeat(4,1fr);gap:14px}}.card{{background:#fff;padding:18px;border-radius:10px}}
.card b{{font-size:28px;display:block;margin-top:6px}} table{{width:100%;border-collapse:collapse;background:#fff;margin:12px 0 28px}}
th,td{{padding:10px;border-bottom:1px solid #eee;text-align:left;vertical-align:top}}th{{background:#f0f2f5}}
</style></head><body><header><h1>A11yGuard Accessibility Report</h1></header><main>
<div class="cards"><div class="card">Tests<b>{len(all_results)}</b></div><div class="card">Violations<b>{violations}</b></div>
<div class="card">Incomplete / manual review<b>{incomplete}</b></div><div class="card">Pages scanned<b>{pages}</b></div></div>
<h2>Test executions</h2><table><tr><th>Framework</th><th>Test</th><th>URL</th><th>Standard</th><th>Violations</th><th>Incomplete</th><th>Timestamp</th></tr>{rows}</table>
<h2>Violation details</h2>{''.join(details)}
</main></body></html>"""
    (directory / "a11yguard-report.html").write_text(html, encoding="utf-8")


def _json(all_results: List[Dict], directory: Path) -> None:
    (directory / "a11yguard-report.json").write_text(
        json.dumps(all_results, indent=2), encoding="utf-8"
    )


def _markdown(all_results: List[Dict], directory: Path) -> None:
    violations = sum(len(r.get("violations", [])) for r in all_results)
    incomplete = sum(len(r.get("incomplete", [])) for r in all_results)
    lines = [
        "# A11yGuard Accessibility Report",
        "",
        "| Metric | Count |",
        "|---|---:|",
        f"| Tests | {len(all_results)} |",
        f"| Violations | {violations} |",
        f"| Incomplete / manual review | {incomplete} |",
        "",
        "## Test executions",
        "",
        "| Framework | Test | URL | Standard | Violations | Incomplete |",
        "|---|---|---|---|---:|---:|",
    ]
    for r in all_results:
        lines.append(
            f"| {r.get('framework')} | {r.get('testName')} | {r.get('url')} | {r.get('standard')} | {len(r.get('violations', []))} | {len(r.get('incomplete', []))} |"
        )
    lines += ["", "## Violation details", ""]
    for r in all_results:
        for v in r.get("violations", []):
            lines.append(f"### {v.get('id')} — {v.get('impact')}")
            lines.append(f"- **Test:** {r.get('testName')}")
            lines.append(f"- **Description:** {v.get('description')}")
            lines.append(f"- **Target:** `{_target(v)}`")
            lines.append("")
    (directory / "a11yguard-report.md").write_text("\n".join(lines), encoding="utf-8")


def _csv(all_results: List[Dict], directory: Path) -> None:
    def cell(value) -> str:
        return '"' + str(value or "").replace('"', '""') + '"'

    rows = [
        "framework,testName,url,standard,timestamp,status,type,impact,ruleId,description,help,helpUrl,target,html"
    ]
    for r in all_results:
        if not r.get("violations") and not r.get("incomplete"):
            rows.append(
                f"{cell(r.get('framework'))},{cell(r.get('testName'))},{cell(r.get('url'))},{cell(r.get('standard'))},{cell(r.get('timestamp'))},PASS,,,,,,,,"
            )
        for v in r.get("violations", []):
            rows.append(
                ",".join(
                    [
                        cell(r.get("framework")),
                        cell(r.get("testName")),
                        cell(r.get("url")),
                        cell(r.get("standard")),
                        cell(r.get("timestamp")),
                        "FAIL",
                        "VIOLATION",
                        cell(v.get("impact")),
                        cell(v.get("id")),
                        cell(v.get("description")),
                        cell(v.get("help")),
                        cell(v.get("helpUrl")),
                        cell(_target(v)),
                        cell(v.get("html")),
                    ]
                )
            )
    (directory / "a11yguard-report.csv").write_text("\n".join(rows) + "\n", encoding="utf-8")


def _junit(all_results: List[Dict], directory: Path) -> None:
    failures = sum(1 for r in all_results if r.get("violations"))
    parts = [
        '<?xml version="1.0" encoding="UTF-8"?>',
        f'<testsuite name="A11yGuard" tests="{len(all_results)}" failures="{failures}" skipped="0">',
    ]
    for r in all_results:
        parts.append(
            f'  <testcase classname="{escape(str(r.get("framework")))}" name="{escape(str(r.get("testName")))}">'
        )
        if r.get("violations"):
            body = "".join(
                f"[{v.get('impact')}] {v.get('id')} - {v.get('help')} - {_target(v)}\n"
                for v in r["violations"]
            )
            parts.append(f'    <failure message="Accessibility violations found"><![CDATA[{body}]]></failure>')
        parts.append("  </testcase>")
    parts.append("</testsuite>")
    (directory / "a11yguard-report.xml").write_text("\n".join(parts) + "\n", encoding="utf-8")


def _sarif(all_results: List[Dict], directory: Path) -> None:
    results = []
    for r in all_results:
        for v in r.get("violations", []):
            level = "error" if (v.get("impact") or "").lower() in {"critical", "serious"} else "warning"
            results.append(
                {
                    "ruleId": v.get("id"),
                    "level": level,
                    "message": {"text": v.get("help") or v.get("description")},
                    "locations": [
                        {"physicalLocation": {"artifactLocation": {"uri": r.get("url") or ""}}}
                    ],
                    "helpUri": v.get("helpUrl"),
                    "properties": {
                        "target": _target(v),
                        "html": v.get("html"),
                        "framework": r.get("framework"),
                        "testName": r.get("testName"),
                    },
                }
            )
    payload = {
        "$schema": "https://json.schemastore.org/sarif-2.1.0.json",
        "version": "2.1.0",
        "runs": [{"tool": {"driver": {"name": "A11yGuard"}}, "results": results}],
    }
    (directory / "a11yguard-report.sarif").write_text(json.dumps(payload, indent=2), encoding="utf-8")
