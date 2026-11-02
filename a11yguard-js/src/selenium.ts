import * as fs from "fs";
import * as path from "path";
import AxeBuilder from "@axe-core/webdriverjs";
import { AccessibilityConfig, AccessibilityResult } from "./types";
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

export async function scanSeleniumDriver(
  driver: {
    getCurrentUrl(): Promise<string>;
    takeScreenshot?: () => Promise<string>;
  },
  testName: string,
  options: AccessibilityConfig = {}
): Promise<AccessibilityResult> {
  const config = loadConfig(options);
  let builder = new AxeBuilder(driver);
  if (config.tags?.length) builder = builder.withTags(config.tags);
  for (const selector of config.exclude || []) builder = builder.exclude(selector);
  const raw = await builder.analyze();
  let result: AccessibilityResult = {
    framework: "selenium-js",
    testName,
    url: await driver.getCurrentUrl(),
    timestamp: new Date().toISOString(),
    standard: config.standard,
    violations: (raw.violations as AxeRule[]).map(mapRule),
    incomplete: (raw.incomplete as AxeRule[]).map(mapRule)
  };
  if (config.screenshots && driver.takeScreenshot) {
    const dir = path.join(config.reportDir, "screenshots");
    fs.mkdirSync(dir, { recursive: true });
    const png = Buffer.from(await driver.takeScreenshot(), "base64");
    fs.writeFileSync(path.join(dir, `${testName}-page.png`), png);
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
