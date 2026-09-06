[🏠 Back to Home](README.md) | [🧪 Test Automation Master Guide](test_automation_master_guide.md) | [🥒 View Full Cucumber BDD Guide](cucumber.md)

# 🥒 Cucumber BDD Architecture & Cheatsheet

> [!NOTE]
> This guide has been upgraded to a comprehensive, scenario-rich guide with full Java step definitions, hooks lifecycle, data tables, and REST API testing. Please refer to [`test_automation_master_guide.md`](test_automation_master_guide.md) for the unified enterprise guide and [`cucumber.md`](cucumber.md) for the dedicated Cucumber guide.

## Quick Summary
- **Gherkin Grammar:** `Feature`, `Background`, `Scenario`, `Scenario Outline`, `Examples`, `Given`, `When`, `Then`, `And`, `But`.
- **Cucumber Expressions:** Modern, readable parameter matching (`{int}`, `{string}`, `{float}`, `{word}`).
- **Hooks Lifecycle:** Order setup with `@Before(order = 1)` and clean teardown with `@After`.
- **Failure Auditing:** Attach failure screenshots automatically via `scenario.attach(bytes, "image/png", "Error")`.
- **Parallel Testing:** Multi-threaded execution configured via `junit-platform.properties` and PicoContainer.

👉 **[Read the Full Test Automation Master Guide](test_automation_master_guide.md)** | **[Read the Full Cucumber BDD Guide](cucumber.md)**