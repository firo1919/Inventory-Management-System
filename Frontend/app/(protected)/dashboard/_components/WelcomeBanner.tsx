import React from "react";
import { Sparkles } from "lucide-react";
import Link from "next/link";

interface WelcomeBannerProps {
  userName: string;
  lowStockCount: number;
}

export function WelcomeBanner({ userName, lowStockCount }: WelcomeBannerProps) {
  return (
    <div className="relative p-6 md:p-8 rounded-3xl bg-linear-to-r from-purple-900/40 to-indigo-900/40 border border-white/5 overflow-hidden backdrop-blur-md">
      <div className="absolute top-0 right-0 w-80 h-80 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />
      <div className="relative z-10 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 bg-indigo-500/10 border border-indigo-500/20 rounded-full text-indigo-400 text-xs font-semibold mb-3">
            <Sparkles className="w-3.5 h-3.5" /> Workspace Overview
          </div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight">
            Welcome back, {userName}!
          </h1>
          <p className="text-slate-400 text-sm mt-1.5 max-w-md">
            Here is what is happening in your inventory database today. You have{" "}
            <span className="text-amber-400 font-semibold">
              {lowStockCount} low stock alerts
            </span>
            .
          </p>
        </div>
        <div className="flex gap-3">
          <Link
            href="/dashboard/sales"
            className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-xs font-semibold transition-all shadow-lg shadow-indigo-500/20"
          >
            Log A Sale
          </Link>
          <Link
            href="/dashboard/restocks"
            className="px-4 py-2 bg-white/5 hover:bg-white/10 border border-white/10 text-white rounded-xl text-xs font-semibold transition-all"
          >
            Log A Restock
          </Link>
        </div>
      </div>
    </div>
  );
}
