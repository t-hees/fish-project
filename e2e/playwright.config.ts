import { defineConfig, devices } from '@playwright/test';

/**
 * Runs against the stack from docker-compose.e2e.yml, see README.org for how to start it
 */
export default defineConfig({
  testDir: './tests',
  globalSetup: './global-setup.ts',
  // Journeys share one backend, every test registers its own user to stay independent
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? [['list'], ['html', { open: 'never' }]] : 'list',
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://localhost:3040',
    trace: 'retain-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
});
