import { NextRequest, NextResponse } from "next/server";

const rolePaths: Record<string, string> = {
  STUDENT: "/student",
  VENDOR: "/vendor",
  ADMIN: "/admin",
};

const JWT_SECRET =
  process.env.JWT_SECRET ?? "change-me-to-a-strong-32-byte-minimum-secret";

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const token = request.cookies.get("upes_session")?.value;
  const claims = token ? await verifyJwt(token) : null;
  const role = typeof claims?.role === "string" ? claims.role : "";
  const hasSession = claims !== null;

  const isProtected = pathname.startsWith("/dashboard") || pathname.startsWith("/student") || pathname.startsWith("/vendor") || pathname.startsWith("/admin");
  if (isProtected && !hasSession) {
    return NextResponse.redirect(new URL("/login", request.url));
  }

  if (pathname === "/login" && hasSession) {
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

async function verifyJwt(token: string): Promise<Record<string, unknown> | null> {
  const parts = token.split(".");
  if (parts.length !== 3) {
    return null;
  }

  const [encodedHeader, encodedPayload, encodedSignature] = parts;
  try {
    const headerBytes = base64UrlDecode(encodedHeader);
    const header = JSON.parse(new TextDecoder().decode(headerBytes)) as {
      alg?: string;
    };

    if (header.alg !== "HS256") {
      return null;
    }

    const key = await crypto.subtle.importKey(
      "raw",
      new TextEncoder().encode(JWT_SECRET),
      { name: "HMAC", hash: "SHA-256" },
      false,
      ["verify"]
    );

    const data = new TextEncoder().encode(`${encodedHeader}.${encodedPayload}`);
    const signature = base64UrlDecode(encodedSignature);
    const isValid = await crypto.subtle.verify("HMAC", key, signature, data);
    if (!isValid) {
      return null;
    }

    const payloadBytes = base64UrlDecode(encodedPayload);
    const payload = JSON.parse(new TextDecoder().decode(payloadBytes)) as Record<string, unknown>;

    const exp = payload.exp;
    if (typeof exp === "number" && Date.now() >= exp * 1000) {
      return null;
    }

    return payload;
  } catch {
    return null;
  }
}

function base64UrlDecode(input: string): Uint8Array {
  const base64 = input.replace(/-/g, "+").replace(/_/g, "/");
  const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, "=");
  const binary = atob(padded);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i += 1) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes;
}

export const config = {
  matcher: ["/login", "/dashboard/:path*", "/student/:path*", "/vendor/:path*", "/admin/:path*"],
};
