const { Builder } = require("selenium-webdriver");
const { scanSeleniumDriver } = require("a11yguard/selenium");

describe("home page", function () {
  let driver;

  beforeEach(async function () {
    driver = await new Builder().forBrowser("chrome").build();
  });

  afterEach(async function () {
    if (this.currentTest && driver) {
      await scanSeleniumDriver(driver, this.currentTest.title);
    }
    if (driver) await driver.quit();
  });

  it("opens the home page", async function () {
    await driver.get("https://example.com");
  });
});
