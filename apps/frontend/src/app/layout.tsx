import type { Metadata } from "next";
import type { ReactNode } from "react";
import "./globals.css";
import { AppHeader } from "@/components/ui/AppHeader";

export const metadata: Metadata = {
  title: "UPES Campus Delivery",
  description: "Campus-first grocery and food ordering platform",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <body className="min-h-screen antialiased">
        <div className="mx-auto max-w-7xl px-4 pb-14 pt-4 sm:px-6 lg:px-8">
          <AppHeader />
          <div className="mt-4">{children}</div>
        </div>
      </body>
    </html>
  );
}
