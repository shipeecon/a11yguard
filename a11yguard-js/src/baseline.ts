import * as fs from "fs";
import { AccessibilityResult, AccessibilityViolation } from "./types";
import { LoadedConfig } from "./config";

interface Fingerprint {
  id: string;
  target?: string;
  url?: string;
}

function targetOf(v: AccessibilityViolation): string {
  return (v.target || []).join(" | ");
}

function matches(
  id: string,
  target: string,
  url: string,
  rule: { id: string; target?: string; url?: string }
): boolean {
  if (rule.id.toLowerCase() !== id.toLowerCase()) return false;
  const targetOk = !rule.target || rule.target === "*" || target.includes(rule.target);
  const urlOk = !rule.url || rule.url === "*" || url === rule.url;
  return targetOk && urlOk;
}

export function applyBaseline(
  result: AccessibilityResult,
  config: LoadedConfig
): AccessibilityResult {
  let baseline: Fingerprint[] = [];
  if (config.baseline && fs.existsSync(config.baseline)) {
    baseline = JSON.parse(fs.readFileSync(config.baseline, "utf8")).violations || [];
  }
  const kept: AccessibilityViolation[] = [];
  const suppressed: AccessibilityViolation[] = [];
  for (const v of result.violations) {
    const target = targetOf(v);
    const yamlHit = (config.suppress || []).some((s) => matches(v.id, target, result.url, s));
    const baseHit = baseline.some((s) => matches(v.id, target, result.url, s));
    if (yamlHit || baseHit) suppressed.push(v);
    else kept.push(v);
  }
  const next = { ...result, violations: kept, suppressed };
  if (config.updateBaseline) {
    fs.writeFileSync(
      config.baseline,
      JSON.stringify(
        {
          violations: kept.map((v) => ({
            id: v.id,
            target: targetOf(v),
            url: result.url
          }))
        },
        null,
        2
      )
    );
  }
  return next;
}
