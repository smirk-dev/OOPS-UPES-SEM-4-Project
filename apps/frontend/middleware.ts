import { NextRequest, NextResponse } from "next/server";

const rolePaths: Record<string, string> = {
  STUDENT: "/student",
  VENDOR: "/vendor",
  ADMIN: "/admin",
};

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const session = request.cookies.get("upes_session")?.value;
  const role = request.cookies.get("upes_role")?.value ?? "";

  const isProtected = pathname.startsWith("/dashboard") || pathname.startsWith("/student") || pathname.startsWith("/vendor") || pathname.startsWith("/admin");
  if (isProtected && !session) {
    return NextResponse.redirect(new URL("/login", request.url));
  }

  if (pathname === "/login" && session) {
    return NextResponse.redirect(new URL(rolePaths[role] ?? "/dashboard", request.url));
  }

  if (role === "STUDENT" && pathname.startsWith("/vendor")) {
    return NextResponse.redirect(new URL("/student", request.url));
  }

  if (role === "STUDENT" && pathname.startsWith("/admin")) {
    return NextResponse.redirect(new URL("/student", request.url));
  }

  if (role === "VENDOR" && (pathname.startsWith("/student") || pathname.startsWith("/admin"))) {
    return NextResponse.redirect(new URL("/vendor", request.url));
  }

  if (role === "ADMIN" && (pathname.startsWith("/student") || pathname.startsWith("/vendor"))) {
    return NextResponse.redirect(new URL("/admin", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/login", "/dashboard/:path*", "/student/:path*", "/vendor/:path*", "/admin/:path*"],
};
