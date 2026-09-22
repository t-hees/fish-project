import { vi } from 'vitest';

export const apiPath = "/api/";

type MockRoute = (body: unknown) => Response;

export function jsonResponse(data: unknown, status = 200): Response {
  return new Response(JSON.stringify(data), { status, headers: { "Content-Type": "application/json" } });
}

/**
 * Replaces the global fetch with a mock answering requests by relative api path prefix
 * @param routes Maps a relative api path prefix (e.g. "trip/get-catches") to a response factory
 * receiving the parsed json request body
 */
export function mockFetch(routes: Record<string, MockRoute>) {
  const fetchMock = vi.fn(async (url: string, init?: RequestInit) => {
    const relPath = url.replace(apiPath, "");
    const route = Object.keys(routes).find(prefix => relPath.startsWith(prefix));
    if (!route) throw new Error(`Unexpected request: ${url}`);
    return routes[route](init?.body ? JSON.parse(init.body as string) : undefined);
  });
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

/**
 * Returns the parsed json bodies of all requests sent to the given relative api path
 */
export function requestBodies(fetchMock: ReturnType<typeof mockFetch>, relPath: string): unknown[] {
  return fetchMock.mock.calls
    .filter(([url]) => url === apiPath + relPath)
    .map(([, init]) => JSON.parse(init!.body as string));
}
