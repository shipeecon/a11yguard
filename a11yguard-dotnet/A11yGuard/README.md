# A11yGuard

Accessibility testing for Playwright and Selenium in .NET.

```bash
dotnet add package A11yGuard
```

```csharp
[Accessibility(Standard = "WCAG2AA", FailOn = "CRITICAL,SERIOUS")]
[Test]
public async Task HomePage()
{
    await page.GotoAsync("https://example.com");
}
```

Keep an `IPage` or `IWebDriver` field on the test fixture. The NUnit `[Accessibility]` attribute scans after the test. Reports go to `target/a11yguard-report`.
