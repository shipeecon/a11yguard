using System.Text;
using System.Text.Json;

namespace A11yGuard;

public static class Reporter
{
    private static readonly List<AccessibilityResult> Results = [];

    public static void Report(AccessibilityResult result, string reportDir, IEnumerable<string> formats)
    {
        Results.Add(result);
        Directory.CreateDirectory(reportDir);
        foreach (var format in formats)
        {
            switch (format.ToUpperInvariant())
            {
                case "HTML": WriteHtml(reportDir); break;
                case "JSON": File.WriteAllText(Path.Combine(reportDir, "a11yguard-report.json"), JsonSerializer.Serialize(Results, new JsonSerializerOptions { WriteIndented = true })); break;
                case "MARKDOWN": WriteMarkdown(reportDir); break;
                case "CSV": WriteCsv(reportDir); break;
                case "JUNIT_XML": WriteJunit(reportDir); break;
                case "SARIF": WriteSarif(reportDir); break;
            }
        }
    }

    public static void WriteExitCode(string reportDir, IEnumerable<string> failOn)
    {
        Directory.CreateDirectory(reportDir);
        var fail = Results.Any(r => r.Violations.Any(v => failOn.Contains(Severity(v.Impact), StringComparer.OrdinalIgnoreCase)));
        File.WriteAllText(Path.Combine(reportDir, "a11yguard-exit-code"), fail ? "1\n" : "0\n");
    }

    public static void AssertAccessible(AccessibilityResult result, IEnumerable<string> failOn)
    {
        var fail = failOn.ToHashSet(StringComparer.OrdinalIgnoreCase);
        var failing = result.Violations.Where(v => fail.Contains(Severity(v.Impact))).ToList();
        if (failing.Count == 0) return;
        var details = string.Join("\n", failing.Select(v => $"[{v.Impact}] {v.Id} - {v.Help} - {string.Join(" | ", v.Target)}"));
        throw new AssertionException($"Accessibility violations found at {result.Url}\n{details}");
    }

    private static string Severity(string impact) => impact?.ToLowerInvariant() switch
    {
        "critical" => "CRITICAL",
        "serious" => "SERIOUS",
        "moderate" => "MODERATE",
        _ => "MINOR"
    };

    private static void WriteHtml(string dir)
    {
        var sb = new StringBuilder();
        sb.Append("<!doctype html><html><head><meta charset=\"utf-8\"><title>A11yGuard Accessibility Report</title></head><body>");
        sb.Append($"<h1>A11yGuard Accessibility Report</h1><p>Tests: {Results.Count}</p><p>Violations: {Results.Sum(r => r.Violations.Count)}</p>");
        sb.Append("<table border='1'><tr><th>Framework</th><th>Test</th><th>URL</th><th>Standard</th><th>Violations</th></tr>");
        foreach (var r in Results)
            sb.Append($"<tr><td>{r.Framework}</td><td>{r.TestName}</td><td>{r.Url}</td><td>{r.Standard}</td><td>{r.Violations.Count}</td></tr>");
        sb.Append("</table>");
        foreach (var r in Results)
        {
            if (!string.IsNullOrWhiteSpace(r.PageScreenshot))
                sb.Append($"<p><b>{r.TestName}</b><br><img src='{r.PageScreenshot}' alt='page screenshot' style='max-width:100%'></p>");
        }
        sb.Append("</body></html>");
        File.WriteAllText(Path.Combine(dir, "a11yguard-report.html"), sb.ToString());
    }

    private static void WriteMarkdown(string dir)
    {
        var sb = new StringBuilder("# A11yGuard Accessibility Report\n\n");
        sb.Append($"| Tests | {Results.Count} |\n| Violations | {Results.Sum(r => r.Violations.Count)} |\n");
        File.WriteAllText(Path.Combine(dir, "a11yguard-report.md"), sb.ToString());
    }

    private static void WriteCsv(string dir)
    {
        var sb = new StringBuilder("framework,testName,url,standard,violations\n");
        foreach (var r in Results)
            sb.AppendLine($"{r.Framework},{r.TestName},{r.Url},{r.Standard},{r.Violations.Count}");
        File.WriteAllText(Path.Combine(dir, "a11yguard-report.csv"), sb.ToString());
    }

    private static void WriteJunit(string dir)
    {
        var failures = Results.Count(r => r.Violations.Count > 0);
        var sb = new StringBuilder($"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<testsuite name=\"A11yGuard\" tests=\"{Results.Count}\" failures=\"{failures}\">\n");
        foreach (var r in Results)
        {
            sb.Append($"  <testcase name=\"{r.TestName}\">");
            if (r.Violations.Count > 0) sb.Append("<failure message=\"Accessibility violations found\"/>");
            sb.AppendLine("</testcase>");
        }
        sb.AppendLine("</testsuite>");
        File.WriteAllText(Path.Combine(dir, "a11yguard-report.xml"), sb.ToString());
    }

    private static void WriteSarif(string dir)
    {
        var payload = new
        {
            schema = "https://json.schemastore.org/sarif-2.1.0.json",
            version = "2.1.0",
            runs = new[] { new { tool = new { driver = new { name = "A11yGuard" } }, results = Results.SelectMany(r => r.Violations.Select(v => new { ruleId = v.Id, message = new { text = v.Help } })).ToArray() } }
        };
        File.WriteAllText(Path.Combine(dir, "a11yguard-report.sarif"), JsonSerializer.Serialize(payload, new JsonSerializerOptions { WriteIndented = true }));
    }
}

public sealed class AssertionException : Exception
{
    public AssertionException(string message) : base(message) { }
}
