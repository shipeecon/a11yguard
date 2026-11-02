using System.Text.Json;
using Microsoft.Playwright;
using OpenQA.Selenium;

namespace A11yGuard;

public static class Scanner
{
    private const string AxeScript = """
        async (tags) => {
          if (!window.axe) {
            await new Promise((resolve, reject) => {
              const script = document.createElement('script');
              script.src = 'https://cdnjs.cloudflare.com/ajax/libs/axe-core/4.13.0/axe.min.js';
              script.onload = resolve;
              script.onerror = reject;
              document.head.appendChild(script);
            });
          }
          return await window.axe.run({ runOnly: { type: 'tag', values: tags } });
        }
        """;

    public static async Task<AccessibilityResult> ScanAsync(IPage page, string testName, AccessibilityConfig? config = null)
    {
        var cfg = ConfigLoader.Load(config);
        var raw = await page.EvaluateAsync<JsonElement>(AxeScript, cfg.Tags);
        var result = Map("playwright-dotnet", testName, page.Url, cfg, raw);
        if (cfg.Screenshots)
        {
            var dir = Path.Combine(cfg.ReportDir, "screenshots");
            Directory.CreateDirectory(dir);
            var file = Path.Combine(dir, $"{testName}-page.png");
            await page.ScreenshotAsync(new PageScreenshotOptions { Path = file, FullPage = true });
            AttachPageShot(result, testName);
        }
        return Finish(result, cfg);
    }

    public static AccessibilityResult Scan(IWebDriver driver, string testName, AccessibilityConfig? config = null)
    {
        var cfg = ConfigLoader.Load(config);
        const string script = """
            var tags = arguments[0];
            var callback = arguments[arguments.length - 1];
            function run() {
              window.axe.run({ runOnly: { type: 'tag', values: tags } }).then(callback);
            }
            if (window.axe) { run(); }
            else {
              var s = document.createElement('script');
              s.src = 'https://cdnjs.cloudflare.com/ajax/libs/axe-core/4.13.0/axe.min.js';
              s.onload = run;
              document.head.appendChild(s);
            }
            """;
        var raw = (Dictionary<string, object>)((IJavaScriptExecutor)driver).ExecuteAsyncScript(script, cfg.Tags)!;
        var json = JsonSerializer.Serialize(raw);
        var result = Map("selenium-dotnet", testName, driver.Url, cfg, JsonDocument.Parse(json).RootElement);
        if (cfg.Screenshots && driver is ITakesScreenshot shot)
        {
            var dir = Path.Combine(cfg.ReportDir, "screenshots");
            Directory.CreateDirectory(dir);
            shot.GetScreenshot().SaveAsFile(Path.Combine(dir, $"{testName}-page.png"));
            AttachPageShot(result, testName);
        }
        return Finish(result, cfg);
    }

    private static AccessibilityResult Finish(AccessibilityResult result, AccessibilityConfig config)
    {
        result = Baseline.Apply(result, config);
        Reporter.Report(result, config.ReportDir, config.ReportFormats);
        Reporter.WriteExitCode(config.ReportDir, config.FailOn);
        if (config.FailOnViolations && !config.UpdateBaseline)
        {
            Reporter.AssertAccessible(result, config.FailOn);
        }
        return result;
    }

    private static void AttachPageShot(AccessibilityResult result, string testName)
    {
        var rel = $"screenshots/{testName}-page.png";
        result.PageScreenshot = rel;
        foreach (var violation in result.Violations)
            violation.Screenshot = rel;
    }

    private static AccessibilityResult Map(string framework, string testName, string url, AccessibilityConfig config, JsonElement raw)
    {
        return new AccessibilityResult
        {
            Framework = framework,
            TestName = testName,
            Url = url,
            Timestamp = DateTime.UtcNow.ToString("o"),
            Standard = config.Standard,
            Violations = MapRules(raw, "violations"),
            Incomplete = MapRules(raw, "incomplete")
        };
    }

    private static List<AccessibilityViolation> MapRules(JsonElement raw, string key)
    {
        var list = new List<AccessibilityViolation>();
        if (!raw.TryGetProperty(key, out var arr) || arr.ValueKind != JsonValueKind.Array) return list;
        foreach (var rule in arr.EnumerateArray())
        {
            var nodes = rule.TryGetProperty("nodes", out var n) ? n : default;
            var first = nodes.ValueKind == JsonValueKind.Array && nodes.GetArrayLength() > 0 ? nodes[0] : default;
            list.Add(new AccessibilityViolation
            {
                Id = rule.TryGetProperty("id", out var id) ? id.GetString() ?? "" : "",
                Impact = rule.TryGetProperty("impact", out var impact) ? impact.GetString() ?? "minor" : "minor",
                Description = rule.TryGetProperty("description", out var d) ? d.GetString() ?? "" : "",
                Help = rule.TryGetProperty("help", out var h) ? h.GetString() ?? "" : "",
                HelpUrl = rule.TryGetProperty("helpUrl", out var hu) ? hu.GetString() ?? "" : "",
                Target = first.ValueKind == JsonValueKind.Object && first.TryGetProperty("target", out var t)
                    ? t.EnumerateArray().Select(x => x.ToString()).ToList()
                    : [],
                Html = first.ValueKind == JsonValueKind.Object && first.TryGetProperty("html", out var html)
                    ? html.GetString() ?? ""
                    : ""
            });
        }
        return list;
    }
}
