using A11yGuard;
using Microsoft.Playwright;
using NUnit.Framework;

public class HomePageTest
{
    IPlaywright? playwright;
    IBrowser? browser;
    IPage? page;

    [SetUp]
    public async Task SetUp()
    {
        playwright = await Playwright.CreateAsync();
        browser = await playwright.Chromium.LaunchAsync(new() { Headless = true });
        page = await browser.NewPageAsync();
    }

    [TearDown]
    public async Task TearDown()
    {
        if (browser is not null) await browser.DisposeAsync();
        playwright?.Dispose();
    }

    [Accessibility(Standard = "WCAG2AA", FailOn = "CRITICAL,SERIOUS")]
    [Test]
    public async Task HomePage()
    {
        await page!.GotoAsync("https://example.com");
        Assert.That(await page.TitleAsync(), Does.Contain("Example Domain"));
    }
}
