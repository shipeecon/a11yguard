using System.Text.Json;

namespace A11yGuard;

public static class ConfigLoader
{
    public static AccessibilityConfig Load(AccessibilityConfig? overrides = null)
    {
        var config = new AccessibilityConfig();
        var file = Find();
        if (file is not null)
        {
            Apply(config, Parse(file));
        }
        if (overrides is not null)
        {
            ApplyOverride(config, overrides);
        }
        return config;
    }

    private static string? Find()
    {
        var dir = new DirectoryInfo(Directory.GetCurrentDirectory());
        while (dir is not null)
        {
            foreach (var name in new[] { "a11yguard.yaml", "a11yguard.yml", "a11yguard.json" })
            {
                var candidate = Path.Combine(dir.FullName, name);
                if (File.Exists(candidate)) return candidate;
            }
            dir = dir.Parent;
        }
        return null;
    }

    private static Dictionary<string, object?> Parse(string path)
    {
        var text = File.ReadAllText(path);
        if (path.EndsWith(".json", StringComparison.OrdinalIgnoreCase))
        {
            return JsonSerializer.Deserialize<Dictionary<string, object?>>(text) ?? [];
        }
        var data = new Dictionary<string, object?>(StringComparer.OrdinalIgnoreCase);
        string? current = null;
        foreach (var raw in text.Split('\n'))
        {
            var line = raw.Split('#')[0];
            var item = line.Trim();
            if (item.StartsWith("- ") && current is not null)
            {
                if (data[current] is not List<string> list)
                {
                    list = [];
                    data[current] = list;
                }
                list.Add(item[2..].Trim().Trim('"', '\''));
                continue;
            }
            if (!line.Contains(':') || line.StartsWith(' ')) continue;
            var parts = line.Split(':', 2);
            var key = parts[0].Trim();
            var value = parts[1].Trim();
            if (value is "" or "[]")
            {
                current = key;
                data[key] = new List<string>();
            }
            else if (value is "true" or "false")
            {
                current = null;
                data[key] = value == "true";
            }
            else
            {
                current = null;
                data[key] = value.Trim('"', '\'');
            }
        }
        return data;
    }

    private static void Apply(AccessibilityConfig config, Dictionary<string, object?> data)
    {
        if (GetString(data, "standard") is { Length: > 0 } standard) config.Standard = standard;
        if (GetList(data, "failOn", "fail_on") is { Count: > 0 } failOn) config.FailOn = [.. failOn];
        if (GetList(data, "tags") is { Count: > 0 } tags) config.Tags = [.. tags];
        if (GetList(data, "exclude") is { } exclude) config.Exclude = [.. exclude];
        if (GetList(data, "reportFormats", "report_formats") is { Count: > 0 } formats) config.ReportFormats = [.. formats];
        if (GetString(data, "reportDir", "report_dir") is { Length: > 0 } dir) config.ReportDir = dir;
        if (GetBool(data, "screenshots") is { } shots) config.Screenshots = shots;
        if (GetString(data, "baseline") is { Length: > 0 } baseline) config.Baseline = baseline;
        if (GetBool(data, "updateBaseline", "update_baseline") is { } update) config.UpdateBaseline = update;
        if (GetBool(data, "failOnViolations", "fail_on_violations") is { } fail) config.FailOnViolations = fail;
        config.Suppress = GetObjects(data, "suppress");
    }

    private static void ApplyOverride(AccessibilityConfig config, AccessibilityConfig overrides)
    {
        if (!string.IsNullOrWhiteSpace(overrides.Standard)) config.Standard = overrides.Standard;
        if (overrides.FailOn.Length > 0) config.FailOn = overrides.FailOn;
        if (overrides.Tags.Length > 0) config.Tags = overrides.Tags;
        if (overrides.Exclude.Length > 0) config.Exclude = overrides.Exclude;
        if (overrides.ReportFormats.Length > 0) config.ReportFormats = overrides.ReportFormats;
        if (!string.Equals(overrides.ReportDir, "target/a11yguard-report", StringComparison.Ordinal))
            config.ReportDir = overrides.ReportDir;
        config.Screenshots = overrides.Screenshots;
        if (!string.IsNullOrWhiteSpace(overrides.Baseline)) config.Baseline = overrides.Baseline;
        config.UpdateBaseline = overrides.UpdateBaseline || config.UpdateBaseline;
        config.FailOnViolations = overrides.FailOnViolations;
        if (overrides.Suppress.Count > 0) config.Suppress = overrides.Suppress;
    }

    private static string? GetString(Dictionary<string, object?> data, params string[] keys)
    {
        foreach (var key in keys)
        {
            if (data.TryGetValue(key, out var value) && value is not null)
                return value.ToString();
        }
        return null;
    }

    private static bool? GetBool(Dictionary<string, object?> data, params string[] keys)
    {
        foreach (var key in keys)
        {
            if (data.TryGetValue(key, out var value))
            {
                if (value is bool b) return b;
                if (value is string s && bool.TryParse(s, out var parsed)) return parsed;
            }
        }
        return null;
    }

    private static List<string>? GetList(Dictionary<string, object?> data, params string[] keys)
    {
        foreach (var key in keys)
        {
            if (data.TryGetValue(key, out var value) && value is List<string> list)
                return list;
        }
        return null;
    }

    private static List<Suppression> GetObjects(Dictionary<string, object?> data, string key)
    {
        if (!data.TryGetValue(key, out var value) || value is not List<string> items)
            return [];
        return items
            .Select(item => new Suppression { Id = item })
            .ToList();
    }
}
