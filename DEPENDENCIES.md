# Dependency baseline

The project uses axe-core 4.13.0 through Deque's Java integrations.

- Selenium integration: `com.deque.html.axe-core:selenium:4.13.0`
- Playwright Java integration: `com.deque.html.axe-core:playwright:4.13.0`
- Playwright Java: `com.microsoft.playwright:playwright:1.63.0`

Review and pin dependencies according to your organization's security policy before publishing.


## Reporting

The JSON and SARIF reporters use Jackson Databind from the core module. HTML, JUnit XML, CSV and Markdown reports have no additional reporting dependency.
