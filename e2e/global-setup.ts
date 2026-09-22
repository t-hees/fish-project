import type { FullConfig } from '@playwright/test';

/**
 * Waits until the backend is reachable through the frontend proxy, it starts a lot slower than nginx
 */
export default async function globalSetup(config: FullConfig) {
  const baseURL = config.projects[0].use.baseURL!;
  const deadline = Date.now() + 120_000;
  let lastStatus = 'no response';
  while (Date.now() < deadline) {
    try {
      // Unauthenticated, so a running backend answers 403 while the proxy answers 502 without one
      const response = await fetch(`${baseURL}/api/user/name`);
      if (response.status === 403) return;
      lastStatus = String(response.status);
    } catch (e) {
      lastStatus = String(e);
    }
    await new Promise(resolve => setTimeout(resolve, 2000));
  }
  throw new Error(`Stack at ${baseURL} not ready, last status: ${lastStatus}. Is docker-compose.e2e.yml up?`);
}
