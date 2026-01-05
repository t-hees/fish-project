type FetchMethod = "GET" | "POST";

type JsonBody = Record<string, unknown>;

const apiPath = `http://${import.meta.env.VITE_BACKEND_HOST}:${import.meta.env.VITE_BACKEND_PORT}/api/`;

export async function fetchApi(relPath: string, method: FetchMethod,
  handleResponse: (arg0: Response) => void, handleError: (arg0: string | null) => void,
  handleLoading: (arg0: boolean) => void, body?: JsonBody): Promise<void> {

  const jsonBody = body ? JSON.stringify(body) : undefined;
  const authToken = localStorage.getItem("auth-token");
  const requestHeaders = body
    ? {
      "Content-Type": "application/json",
      ...(authToken && { "Authorization": `Bearer ${authToken}` }),
  }
    : (authToken ? { "Authorization": `Bearer ${authToken}` } : undefined);

  return fetch(apiPath + relPath, {
    method: method,
    credentials: "include",
    headers: requestHeaders,
    body: jsonBody,
  })
    .then(async response =>  {
    if (!response.ok) {
      throw new Error(await response.text());
    }
    return response;
  })
    .then(response => handleResponse(response))
    .catch(err => handleError(String(err)))
    .finally(() => handleLoading(false));
}
