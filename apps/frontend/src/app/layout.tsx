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
      <body className="min-h-screen">
        <div className="mx-auto max-w-6xl px-4 pb-12 pt-4">
          <AppHeader />
          {children}
        </div>
      </body>
    </html>
  );
}
