import { test, expect } from "../src/playwright";

test("home page", async ({ page }) => {
  await page.goto("https://example.com");
  await expect(page).toHaveTitle(/Example Domain/);
});
