import { NextRequest, NextResponse } from "next/server";
import { auth } from "@/lib/auth";

async function handleProxy(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
) {
  const { path } = await params;
  const pathStr = path.join("/");

  // 1. Get the session to retrieve the access token
  const session = await auth.api.getSession({
    headers: request.headers,
  });

  const sessionObj = session?.session as any;
  const accessToken =
    sessionObj?.accessToken ||
    sessionObj?.additionalFields?.accessToken ||
    request.cookies.get("backend_access_token")?.value;
  const refreshToken =
    sessionObj?.refreshToken ||
    sessionObj?.additionalFields?.refreshToken ||
    request.cookies.get("backend_refresh_token")?.value;

  const backendUrl = process.env.BACKEND_URL || "http://localhost:8080";
  const urlObj = new URL(request.url);
  const searchParams = urlObj.search;

  const targetUrl = `${backendUrl}/api/v1/${pathStr}${searchParams}`;

  // 2. Prepare headers
  const headers = new Headers();
  request.headers.forEach((value, key) => {
    // Avoid forwarding browser-specific host headers that could confuse the backend
    if (key.toLowerCase() !== "host" && key.toLowerCase() !== "content-length") {
      headers.set(key, value);
    }
  });

  if (accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  // 3. Prepare body
  let body: any = null;
  if (request.method !== "GET" && request.method !== "HEAD") {
    if (pathStr === "auth/logout") {
      // For logout, map token to body
      const rToken = refreshToken || request.cookies.get("backend_refresh_token")?.value;
      if (rToken) {
        body = new Blob([JSON.stringify({ refreshToken: rToken })], {
          type: "application/json",
        });
        headers.set("Content-Type", "application/json");
      }
    } else {
      try {
        body = await request.blob();
      } catch (e) {
        // ignore
      }
    }
  }

  // 4. Send request to backend
  let response = await fetch(targetUrl, {
    method: request.method,
    headers,
    body,
  });

  // 5. Handle Token Refresh if backend returns 401 Unauthorized
  if (response.status === 401 && refreshToken) {
    console.log(
      `[Proxy] Token expired for path /api/v1/${pathStr}. Attempting server-side silent refresh...`
    );
    try {
      const refreshRes = await fetch(`${backendUrl}/api/v1/auth/refresh`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ refreshToken }),
      });

      if (refreshRes.ok) {
        const refreshData = await refreshRes.json();
        const newAccessToken = refreshData.accessToken;
        const newRefreshToken = refreshData.refreshToken;

        console.log(
          `[Proxy] Token refresh succeeded. Retrying request to /api/v1/${pathStr}...`
        );

        // Retry original request with new access token
        headers.set("Authorization", `Bearer ${newAccessToken}`);
        response = await fetch(targetUrl, {
          method: request.method,
          headers,
          body,
        });

        // Set new cookies on the response so they are saved in the browser
        const nextResponse = new NextResponse(response.body, {
          status: response.status,
          headers: response.headers,
        });

        nextResponse.cookies.set("backend_access_token", newAccessToken, {
          httpOnly: true,
          secure: process.env.NODE_ENV === "production",
          sameSite: "lax",
          path: "/",
          maxAge: 15 * 60, // 15 mins
        });

        nextResponse.cookies.set("backend_refresh_token", newRefreshToken, {
          httpOnly: true,
          secure: process.env.NODE_ENV === "production",
          sameSite: "lax",
          path: "/",
          maxAge: 7 * 24 * 60 * 60, // 7 days
        });

        return nextResponse;
      } else {
        console.warn("[Proxy] Token refresh failed with status", refreshRes.status);
      }
    } catch (refreshErr) {
      console.error("[Proxy] Token refresh exception:", refreshErr);
    }
  }

  // 6. Return response
  const finalResponse = new NextResponse(response.body, {
    status: response.status,
    headers: response.headers,
  });

  // Sync cookies on login/logout
  if (pathStr === "auth/logout") {
    finalResponse.cookies.set("backend_access_token", "", { maxAge: 0, path: "/" });
    finalResponse.cookies.set("backend_refresh_token", "", { maxAge: 0, path: "/" });
  }

  return finalResponse;
}

export async function GET(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> }
) {
  return handleProxy(request, context);
}

export async function POST(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> }
) {
  return handleProxy(request, context);
}

export async function PUT(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> }
) {
  return handleProxy(request, context);
}

export async function DELETE(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> }
) {
  return handleProxy(request, context);
}
