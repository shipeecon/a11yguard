import * as fs from "fs";
import * as path from "path";
import { AccessibilityConfig, DEFAULT_CONFIG } from "./types";

export interface LoadedConfig extends Required<AccessibilityConfig> {
  screenshots: boolean;
  baseline: string;
  updateBaseline: boolean;
  failOnViolations: boolean;
  suppress: { id: string; target?: string; reason?: string }[];
}

function findConfig(): string | undefined {
  let dir = process.cwd();
  while (true) {
    for (const name of ["a11yguard.yaml", "a11yguard.yml", "a11yguard.json"]) {
      const candidate = path.join(dir, name);
      if (fs.existsSync(candidate)) return candidate;
    }
    const parent = path.dirname(dir);
    if (parent === dir) return undefined;
    dir = parent;
  }
}

function parseYamlLike(text: string): Record<string, unknown> {
  try {
    return JSON.parse(text);
  } catch {
    const out: Record<string, unknown> = {};
    const lines = text.split(/\r?\n/);
    let currentList: string | undefined;
    const lists: Record<string, string[]> = {};
    for (const raw of lines) {
      const line = raw.replace(/#.*$/, "");
      const listItem = line.match(/^\s+-\s+(.+)$/);
      if (listItem && currentList) {
        lists[currentList].push(listItem[1].trim().replace(/^["']|["']$/g, ""));
        continue;
      }
      const kv = line.match(/^([A-Za-z0-9_]+):\s*(.*)$/);
      if (!kv) continue;
      currentList = undefined;
      const key = kv[1];
      const value = kv[2].trim();
      if (value === "" || value === "[]") {
        currentList = key;
        lists[key] = [];
        out[key] = lists[key];
      } else if (value === "true" || value === "false") {
        out[key] = value === "true";
      } else {
        out[key] = value.replace(/^["']|["']$/g, "");
      }
    }
    return out;
  }
}

export function loadConfig(overrides: AccessibilityConfig = {}): LoadedConfig {
  const file = findConfig();
  const fromFile = file ? parseYamlLike(fs.readFileSync(file, "utf8")) : {};
  return {
    ...DEFAULT_CONFIG,
    screenshots: true,
    baseline: "a11yguard-baseline.json",
    updateBaseline: false,
    failOnViolations: true,
    suppress: [],
    ...fromFile,
    ...overrides
  } as LoadedConfig;
}
