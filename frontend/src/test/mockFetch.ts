import { vi } from 'vitest';

export const apiPath = "/api/";

type MockRoute = (body: unknown) => Response;

// Json bodies are parsed, FormData is kept as is
function parseBody(body: RequestInit["body"]): unknown {
  if (body instanceof FormData) return body;
  return body ? JSON.parse(body as string) : undefined;
}

export function jsonResponse(data: unknown, status = 200): Response {
  return new Response(JSON.stringify(data), { status, headers: { "Content-Type": "application/json" } });
}

/**
 * Splits a route key like "PUT trips/5/catches" into method and path, a key without method matches any
 */
function parseRouteKey(key: string): { method?: string, path: string } {
  const [first, ...rest] = key.split(" ");
  return rest.length > 0 ? { method: first, path: rest.join(" ") } : { path: first };
}

/**
 * Replaces the global fetch with a mock answering requests by relative api path prefix
 * @param routes Maps an optional method and a relative api path prefix (e.g. "GET trips/5/catches")
 * to a response factory receiving the parsed json request body
 */
export function mockFetch(routes: Record<string, MockRoute>) {
  const fetchMock = vi.fn(async (url: string, init?: RequestInit) => {
    const relPath = url.replace(apiPath, "");
    const route = Object.keys(routes).find(key => {
      const { method, path } = parseRouteKey(key);
      return relPath.startsWith(path) && (!method || method === (init?.method ?? "GET"));
    });
    if (!route) throw new Error(`Unexpected request: ${init?.method} ${url}`);
    return routes[route](parseBody(init?.body));
  });
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

/**
 * Returns the bodies (parsed json, FormData or undefined without body) of all requests sent to the given
 * relative api path
 */
export function requestBodies(fetchMock: ReturnType<typeof mockFetch>, relPath: string, method?: string): unknown[] {
  return fetchMock.mock.calls
    .filter(([url, init]) => url === apiPath + relPath && (!method || init?.method === method))
    .map(([, init]) => parseBody(init?.body));
}
