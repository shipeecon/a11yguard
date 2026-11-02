export type Severity = "CRITICAL" | "SERIOUS" | "MODERATE" | "MINOR";
export type ReportFormat = "HTML" | "JSON" | "JUNIT_XML" | "SARIF" | "CSV" | "MARKDOWN";

export interface AccessibilityConfig {
  standard?: string;
  failOn?: Severity[];
  tags?: string[];
  exclude?: string[];
  reportFormats?: ReportFormat[];
  reportDir?: string;
  enabled?: boolean;
  screenshots?: boolean;
  baseline?: string;
  updateBaseline?: boolean;
  failOnViolations?: boolean;
}

export interface AccessibilityViolation {
  id: string;
  impact: string;
  description: string;
  help: string;
  helpUrl: string;
  target: string[];
  html: string;
  screenshot?: string;
}

export interface AccessibilityResult {
  framework: string;
  testName: string;
  url: string;
  timestamp: string;
  standard: string;
  violations: AccessibilityViolation[];
  incomplete: AccessibilityViolation[];
  suppressed?: AccessibilityViolation[];
  pageScreenshot?: string;
}

export const DEFAULT_CONFIG: Required<AccessibilityConfig> = {
  standard: "WCAG2AA",
  failOn: ["CRITICAL", "SERIOUS"],
  tags: ["wcag2a", "wcag2aa"],
  exclude: [],
  reportFormats: ["HTML", "JSON", "JUNIT_XML", "SARIF", "CSV", "MARKDOWN"],
  reportDir: "target/a11yguard-report",
  enabled: true,
  screenshots: true,
  baseline: "a11yguard-baseline.json",
  updateBaseline: false,
  failOnViolations: true
};

export function severityOf(impact: string | undefined): Severity {
  switch ((impact || "").toLowerCase()) {
    case "critical":
      return "CRITICAL";
    case "serious":
      return "SERIOUS";
    case "moderate":
      return "MODERATE";
    default:
      return "MINOR";
  }
}
