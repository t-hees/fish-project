import { describe, expect, it, vi } from 'vitest';
import { fetchApi } from './fetchApi';
import { apiPath, jsonResponse } from '../test/mockFetch';

function callFetchApi(response: Response | Promise<Response>, body?: Record<string, unknown>) {
  const fetchMock = vi.fn(() => Promise.resolve(response));
  vi.stubGlobal("fetch", fetchMock);
  vi.spyOn(console, "error").mockImplementation(() => {});
  const handlers = {
    handleResponse: vi.fn(),
    handleError: vi.fn(),
    handleLoading: vi.fn(),
  };
  const done = fetchApi("trips", body ? "POST" : "GET", handlers.handleResponse, handlers.handleError,
    handlers.handleLoading, body);
  return { fetchMock, done, ...handlers };
}

describe("fetchApi", () => {
  it("sends json bodies with credentials to the backend", async () => {
    const { fetchMock, done } = callFetchApi(jsonResponse({}), { id: 1 });
    await done;

    expect(fetchMock).toHaveBeenCalledWith(apiPath + "trips", {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ id: 1 }),
    });
  });

  it("sends form data as multipart", async () => {
    const fetchMock = vi.fn(() => Promise.resolve(jsonResponse({})));
    vi.stubGlobal("fetch", fetchMock);
    const form = new FormData();
    form.append("image", new Blob(["data"]), "photo.png");

    await fetchApi("trips/1/special-catches", "POST", vi.fn(), vi.fn(), vi.fn(), form);

    expect(fetchMock).toHaveBeenCalledWith(apiPath + "trips/1/special-catches", {
      method: "POST",
      credentials: "include",
      headers: undefined,
      body: form,
    });
  });

  it("passes successful responses on and stops loading", async () => {
    const response = jsonResponse({ trips: [] });
    const { done, handleResponse, handleError, handleLoading } = callFetchApi(response);
    await done;

    expect(handleResponse).toHaveBeenCalledWith(response);
    expect(handleError).not.toHaveBeenCalled();
    expect(handleLoading).toHaveBeenCalledWith(false);
  });

  it("reports api errors with code and message", async () => {
    const { done, handleResponse, handleError, handleLoading } = callFetchApi(jsonResponse(
      { code: "ENTITY_NOT_FOUND", message: "Failed to find Trip: 1", timestamp: "", uri: "" }, 404));
    await done;

    expect(handleResponse).not.toHaveBeenCalled();
    expect(handleError).toHaveBeenCalledWith("ENTITY_NOT_FOUND: Failed to find Trip: 1");
    expect(handleLoading).toHaveBeenCalledWith(false);
  });

  it("reports errors without a json body by status", async () => {
    const { done, handleError } = callFetchApi(new Response(null, { status: 401, statusText: "Unauthorized" }));
    await done;

    expect(handleError).toHaveBeenCalledWith("401: Unauthorized");
  });

  it("reports network failures", async () => {
    const { done, handleError, handleLoading } = callFetchApi(Promise.reject(new TypeError("Failed to fetch")));
    await done;

    expect(handleError).toHaveBeenCalledWith("Failed to fetch");
    expect(handleLoading).toHaveBeenCalledWith(false);
  });
});
