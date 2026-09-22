type FetchMethod = "GET" | "POST";

type JsonBody = Record<string, unknown>;

type ApiError = {
  code: string,
  message: string,
  timestamp: string,
  uri: string
}

// Relative, the serving nginx (or the vite dev server) proxies /api to the backend
const apiPath = "/api/";

export async function fetchApi(relPath: string, method: FetchMethod,
  handleResponse: (arg0: Response) => void, handleError: (arg0: string | null) => void,
  handleLoading: (arg0: boolean) => void, body?: JsonBody): Promise<void> {

  const jsonBody = body ? JSON.stringify(body) : undefined;
  const requestHeaders = body ? {"Content-Type": "application/json"} : undefined;
  return fetch(apiPath + relPath, {
    method: method,
    credentials: "include",
    headers: requestHeaders,
    body: jsonBody,
  })
    .then(async response =>  {
    if (!response.ok) {
      // Not every error carries an ApiError body, e.g. a 403 from spring security has none
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
