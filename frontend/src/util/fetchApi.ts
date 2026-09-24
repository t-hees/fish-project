type FetchMethod = "GET" | "POST" | "PUT" | "DELETE";

type JsonBody = Record<string, unknown>;

type ApiError = {
  code: string,
  message: string,
  timestamp: string,
  uri: string
}

// Relative, the serving nginx (or the vite dev server) proxies /api to the backend
const apiPath = "/api/";

/**
 * @param body Sent as json, or as multipart/form-data for FormData
 */
export async function fetchApi(relPath: string, method: FetchMethod,
  handleResponse: (arg0: Response) => void, handleError: (arg0: string | null) => void,
  handleLoading: (arg0: boolean) => void, body?: JsonBody | FormData): Promise<void> {

  const isJson = body !== undefined && !(body instanceof FormData);
  // FormData bodies get their multipart content type with boundary from fetch itself
  const requestHeaders = isJson ? {"Content-Type": "application/json"} : undefined;
  return fetch(apiPath + relPath, {
    method: method,
    credentials: "include",
    headers: requestHeaders,
    body: isJson ? JSON.stringify(body) : body,
  })
    .then(async response =>  {
    if (!response.ok) {
      // Not every error carries an ApiError body, e.g. a 401 from spring security has none
      const responseJson: ApiError | null = await response.json().catch(() => null);
      console.error(responseJson ?? response);
      throw new Error(responseJson
        ? responseJson.code + ": " + responseJson.message
        : response.status + ": " + (response.statusText || "Request failed"));
    }
    return response;
  })
    .then(response => handleResponse(response))
    .catch(err => handleError(err.message))
    .finally(() => handleLoading(false));
}
