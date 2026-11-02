using System.Reflection;
using Microsoft.Playwright;
using NUnit.Framework;
using NUnit.Framework.Interfaces;
using OpenQA.Selenium;

namespace A11yGuard;

[AttributeUsage(AttributeTargets.Method | AttributeTargets.Class)]
public sealed class AccessibilityAttribute : TestActionAttribute
{
    public string Standard { get; set; } = "WCAG2AA";
    public string FailOn { get; set; } = "CRITICAL,SERIOUS";
    public string Tags { get; set; } = "wcag2a,wcag2aa";
    public string ReportDir { get; set; } = "target/a11yguard-report";
    public bool Screenshots { get; set; } = true;
    public string Baseline { get; set; } = "a11yguard-baseline.json";
    public bool UpdateBaseline { get; set; }
    public bool FailOnViolations { get; set; } = true;

    public override void AfterTest(ITest test)
    {
        if (test.Fixture is null) return;
        var config = ConfigLoader.Load(new AccessibilityConfig
        {
            Standard = Standard,
            FailOn = FailOn.Split(',', StringSplitOptions.TrimEntries | StringSplitOptions.RemoveEmptyEntries),
            Tags = Tags.Split(',', StringSplitOptions.TrimEntries | StringSplitOptions.RemoveEmptyEntries),
            ReportDir = ReportDir,
            Screenshots = Screenshots,
            Baseline = Baseline,
            UpdateBaseline = UpdateBaseline,
            FailOnViolations = FailOnViolations
        });

        var page = Find<IPage>(test.Fixture);
        var driver = Find<IWebDriver>(test.Fixture);
        var name = test.MethodName ?? test.Name;

        if (page is not null)
        {
            Scanner.ScanAsync(page, name, config).GetAwaiter().GetResult();
            return;
        }
        if (driver is not null)
        {
            Scanner.Scan(driver, name, config);
            return;
        }

        throw new InvalidOperationException(
            "A11yGuard could not find an IPage or IWebDriver field/property on the test fixture.");
    }

    private static T? Find<T>(object instance) where T : class
    {
        for (var type = instance.GetType(); type != null; type = type.BaseType)
        {
            foreach (var field in type.GetFields(BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic))
            {
                if (typeof(T).IsAssignableFrom(field.FieldType) && field.GetValue(instance) is T value)
                    return value;
            }
            foreach (var prop in type.GetProperties(BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic))
            {
                if (prop.CanRead && typeof(T).IsAssignableFrom(prop.PropertyType) && prop.GetValue(instance) is T value)
                    return value;
            }
        }
        return null;
    }
}
