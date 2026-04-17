import { test, expect } from '@playwright/test';

test.describe('Smart Stadium Routing E2E', () => {
  test.beforeEach(async ({ page }) => {
    // Navigates to the app. In CI, this would be the deployed URL or local server.
    await page.goto('/');
  });

  test('should display the dashboard and load telemetry data', async ({ page }) => {
    await expect(page.locator('header')).toBeVisible();
    await expect(page.locator('text=Smart Stadium')).toBeVisible();
    
    // Wait for at least one zone card to appear
    const zoneCard = page.locator('.zone-card').first();
    await expect(zoneCard).toBeVisible();
  });

  test('should calculate a route between zones', async ({ page }) => {
    // Select source zone
    await page.selectOption('select[aria-label="Select start zone"]', 'GATE_A');
    
    // Select destination zone
    await page.selectOption('select[aria-label="Select destination zone"]', 'VIP_LOUNGE');
    
    // Check if the route is displayed
    const routeSummary = page.locator('.route-summary');
    await expect(routeSummary).toBeVisible();
    
    // Verify path elements
    const pathSteps = page.locator('.path-node');
    const count = await pathSteps.count();
    expect(count).toBeGreaterThan(1);
  });
});
