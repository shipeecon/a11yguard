namespace A11yGuard;

public sealed class AccessibilityConfig
{
    public string Standard { get; set; } = "WCAG2AA";
    public string[] FailOn { get; set; } = ["CRITICAL", "SERIOUS"];
    public string[] Tags { get; set; } = ["wcag2a", "wcag2aa"];
    public string[] Exclude { get; set; } = [];
    public string[] ReportFormats { get; set; } = ["HTML", "JSON", "JUNIT_XML", "SARIF", "CSV", "MARKDOWN"];
    public string ReportDir { get; set; } = "target/a11yguard-report";
    public bool Screenshots { get; set; } = true;
    public string Baseline { get; set; } = "a11yguard-baseline.json";
    public bool UpdateBaseline { get; set; }
    public bool FailOnViolations { get; set; } = true;
    public List<Suppression> Suppress { get; set; } = [];
}

public sealed class Suppression
{
    public string Id { get; set; } = "";
    public string Target { get; set; } = "";
    public string Url { get; set; } = "";
    public string Reason { get; set; } = "";
}

public sealed class AccessibilityViolation
{
    public string Id { get; set; } = "";
    public string Impact { get; set; } = "";
    public string Description { get; set; } = "";
    public string Help { get; set; } = "";
    public string HelpUrl { get; set; } = "";
    public List<string> Target { get; set; } = [];
    public string Html { get; set; } = "";
    public string Screenshot { get; set; } = "";
}

public sealed class AccessibilityResult
{
    public string Framework { get; set; } = "";
    public string TestName { get; set; } = "";
    public string Url { get; set; } = "";
    public string Timestamp { get; set; } = "";
    public string Standard { get; set; } = "";
    public List<AccessibilityViolation> Violations { get; set; } = [];
    public List<AccessibilityViolation> Incomplete { get; set; } = [];
    public List<AccessibilityViolation> Suppressed { get; set; } = [];
    public string PageScreenshot { get; set; } = "";
}
