import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

export async function middleware(request: NextRequest) {
  const sessionToken = request.cookies.get("better-auth.session_token")?.value;
  const url = request.nextUrl.clone();

  const isAuthPage = url.pathname.startsWith("/auth");
  const isDashboardPage = url.pathname.startsWith("/dashboard");

  // gatekeeper protection
  if (isDashboardPage && !sessionToken) {
    url.pathname = "/auth/login";
    return NextResponse.redirect(url);
  }

  if (isAuthPage && sessionToken) {
    url.pathname = "/dashboard";
    return NextResponse.redirect(url);
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/dashboard/:path*", "/auth/:path*"],
};
