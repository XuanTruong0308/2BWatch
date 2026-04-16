import type { ApiResponse } from "@/lib/api/types";

const JSON_HEADERS = {
  Accept: "application/json",
  "Content-Type": "application/json",
};

const CSRF_ENDPOINT = "/api/v1/auth/csrf";

let csrfRefreshPromise: Promise<void> | null = null;

function readCookie(name: string) {
  return document.cookie
    .split("; ")
    .find((row) => row.startsWith(`${name}=`))
    ?.split("=")[1];
}

async function parseResponse<T>(response: Response): Promise<T> {
  const isJson = response.headers.get("content-type")?.includes("application/json");
  const payload = isJson ? await response.json() : null;

  if (!response.ok) {
    const message =
      payload?.message ??
      (response.status === 401
        ? "Vui lòng đăng nhập để tiếp tục."
        : response.status === 403
          ? "Phiên làm việc đã hết hạn hoặc bạn không có quyền thực hiện thao tác này."
          : "Yêu cầu thất bại");
    throw new Error(message);
  }

  if (payload && typeof payload.success === "boolean" && payload.success === false) {
    throw new Error(payload.message ?? "Yêu cầu thất bại");
  }

  return payload as T;
}

function buildHeaders(headers?: HeadersInit) {
  const mergedHeaders = new Headers(headers);
  const csrfToken = readCookie("XSRF-TOKEN");

  if (csrfToken) {
    mergedHeaders.set("X-XSRF-TOKEN", decodeURIComponent(csrfToken));
  }

  return mergedHeaders;
}

async function refreshCsrfCookie(force = false) {
  if (!force && readCookie("XSRF-TOKEN")) {
    return;
  }

  if (!csrfRefreshPromise) {
    csrfRefreshPromise = fetch(CSRF_ENDPOINT, {
      credentials: "include",
      headers: { Accept: "application/json" },
    })
      .then(async (response) => {
        if (!response.ok) {
          throw new Error("Không thể làm mới CSRF token.");
        }

        if (response.headers.get("content-type")?.includes("application/json")) {
          await response.json();
        }
      })
      .finally(() => {
        csrfRefreshPromise = null;
      });
  }

  await csrfRefreshPromise;
}

async function requestJson<T>(
  url: string,
  init: RequestInit = {},
  options: { ensureCsrf?: boolean } = {},
): Promise<T> {
  const { ensureCsrf = false } = options;

  if (ensureCsrf) {
    await refreshCsrfCookie();
  }

  const execute = () =>
    fetch(url, {
      credentials: "include",
      ...init,
      headers: buildHeaders(init.headers),
    });

  let response = await execute();

  if (ensureCsrf && response.status === 403) {
    await refreshCsrfCookie(true);
    response = await execute();
  }

  return parseResponse<T>(response);
}

export async function getJson<T>(url: string): Promise<T> {
  return requestJson<T>(url);
}

export async function postJson<T>(url: string, body?: unknown): Promise<T> {
  return requestJson<T>(
    url,
    {
      method: "POST",
      headers: JSON_HEADERS,
      body: body === undefined ? undefined : JSON.stringify(body),
    },
    { ensureCsrf: true },
  );
}

export async function putJson<T>(url: string, body?: unknown): Promise<T> {
  return requestJson<T>(
    url,
    {
      method: "PUT",
      headers: JSON_HEADERS,
      body: body === undefined ? undefined : JSON.stringify(body),
    },
    { ensureCsrf: true },
  );
}

export async function deleteJson<T>(url: string): Promise<T> {
  return requestJson<T>(
    url,
    {
      method: "DELETE",
    },
    { ensureCsrf: true },
  );
}

export async function postFormData<T>(url: string, formData: FormData): Promise<T> {
  return requestJson<T>(
    url,
    {
      method: "POST",
      body: formData,
    },
    { ensureCsrf: true },
  );
}

export type JsonApiResponse<T> = ApiResponse<T>;
