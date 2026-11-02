# a11yguard

Accessibility testing for Playwright and Selenium in JavaScript/TypeScript.

```bash
npm install a11yguard
```

Playwright Test:

```ts
import { test, expect } from "a11yguard/playwright";

test("home page", async ({ page }) => {
  await page.goto("https://example.com");
});
```

The Playwright `page` fixture scans automatically after the test. Reports go to `target/a11yguard-report`.

Selenium:

```js
const { scanSeleniumDriver } = require("a11yguard/selenium");
await scanSeleniumDriver(driver, "home page");
```
