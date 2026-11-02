using System.Text.Json;

namespace A11yGuard;

public static class Baseline
{
    public static AccessibilityResult Apply(AccessibilityResult result, AccessibilityConfig config)
    {
        var baseline = new List<Suppression>();
        if (File.Exists(config.Baseline))
        {
            using var doc = JsonDocument.Parse(File.ReadAllText(config.Baseline));
            if (doc.RootElement.TryGetProperty("violations", out var arr) && arr.ValueKind == JsonValueKind.Array)
            {
                foreach (var item in arr.EnumerateArray())
                {
                    baseline.Add(new Suppression
                    {
                        Id = item.TryGetProperty("id", out var id) ? id.GetString() ?? "" : "",
                        Target = item.TryGetProperty("target", out var target) ? target.GetString() ?? "" : "",
                        Url = item.TryGetProperty("url", out var url) ? url.GetString() ?? "" : ""
                    });
                }
            }
        }

        var kept = new List<AccessibilityViolation>();
        var suppressed = new List<AccessibilityViolation>();
        foreach (var violation in result.Violations)
        {
            var target = string.Join(" | ", violation.Target);
            var yamlHit = config.Suppress.Any(s => Matches(violation.Id, target, result.Url, s));
            var baseHit = baseline.Any(s => Matches(violation.Id, target, result.Url, s));
            if (yamlHit || baseHit) suppressed.Add(violation);
            else kept.Add(violation);
        }
        result.Violations = kept;
        result.Suppressed = suppressed;

        if (config.UpdateBaseline)
        {
            var payload = new
            {
                violations = kept.Select(v => new
                {
                    id = v.Id,
                    target = string.Join(" | ", v.Target),
                    url = result.Url
                })
            };
            File.WriteAllText(config.Baseline, JsonSerializer.Serialize(payload, new JsonSerializerOptions { WriteIndented = true }));
        }
        return result;
    }

    private static bool Matches(string id, string target, string url, Suppression rule)
    {
        if (!string.Equals(rule.Id, id, StringComparison.OrdinalIgnoreCase)) return false;
        var targetOk = string.IsNullOrWhiteSpace(rule.Target) || rule.Target == "*" || target.Contains(rule.Target);
        var urlOk = string.IsNullOrWhiteSpace(rule.Url) || rule.Url == "*" || rule.Url == url;
        return targetOk && urlOk;
    }
}
