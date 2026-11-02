import { AxeBuilder } from "@axe-core/playwright";
import type { Page } from "@playwright/test";
import { test as base } from "@playwright/test";
import * as fs from "fs";
import * as path from "path";
import {
  AccessibilityConfig,
  AccessibilityResult,
  DEFAULT_CONFIG
} from "./types";
import { applyBaseline } from "./baseline";
import { loadConfig } from "./config";
import { assertAccessible, report, writeExitCode } from "./report";

type AxeNode = { html?: string; target?: string[] };
type AxeRule = {
  id: string;
  impact?: string;
  description: string;
  help: string;
  helpUrl: string;
  nodes?: AxeNode[];
};

function mapRule(rule: AxeRule) {
  return {
    id: rule.id,
    impact: rule.impact || "minor",
    description: rule.description,
    help: rule.help,
    helpUrl: rule.helpUrl,
    target: rule.nodes?.[0]?.target || [],
    html: rule.nodes?.[0]?.html || ""
  };
}

export async function scanPlaywrightPage(
  page: Page,
  testName: string,
  options: AccessibilityConfig = {}
): Promise<AccessibilityResult> {
  const config = loadConfig(options);
  let builder = new AxeBuilder({ page });
  if (config.tags?.length) builder = builder.withTags(config.tags);
  for (const selector of config.exclude || []) builder = builder.exclude(selector);
  const raw = await builder.analyze();
  let result: AccessibilityResult = {
    framework: "playwright-js",
    testName,
    url: page.url(),
    timestamp: new Date().toISOString(),
    standard: config.standard,
    violations: (raw.violations as AxeRule[]).map(mapRule),
    incomplete: (raw.incomplete as AxeRule[]).map(mapRule)
  };
  if (config.screenshots) {
    const dir = path.join(config.reportDir, "screenshots");
    fs.mkdirSync(dir, { recursive: true });
    const pageFile = path.join(dir, `${testName}-page.png`);
    await page.screenshot({ path: pageFile, fullPage: true });
    result.pageScreenshot = `screenshots/${testName}-page.png`;
    result.violations = result.violations.map((v) => ({
      ...v,
      screenshot: result.pageScreenshot
    }));
  }
  result = applyBaseline(result, config);
  report(result, config.reportDir, config.reportFormats);
  writeExitCode(config.reportDir, config.failOn);
  if (config.failOnViolations && !config.updateBaseline) {
    assertAccessible(result, config.failOn);
  }
  return result;
}

export const test = base.extend<{ a11y: AccessibilityConfig | false }>({
  a11y: [DEFAULT_CONFIG, { option: true }],
  page: async ({ page, a11y }, use, testInfo) => {
    await use(page);
    if (a11y === false || a11y.enabled === false) return;
    await scanPlaywrightPage(page, testInfo.title, a11y);
  }
});

export { expect } from "@playwright/test";
