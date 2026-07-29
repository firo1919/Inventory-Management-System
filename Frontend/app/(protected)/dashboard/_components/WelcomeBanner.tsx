import React from "react";
import { Sparkles } from "lucide-react";
import Link from "next/link";

interface WelcomeBannerProps {
  userName: string;
  lowStockCount: number;
}

export function WelcomeBanner({ userName, lowStockCount }: WelcomeBannerProps) {
  return (
    <div className="relative p-6 md:p-8 rounded-3xl bg-gradient-to-r from-indigo-600 via-indigo-700 to-purple-800 dark:from-[#13112c] dark:via-[#1e1b4b] dark:to-[#171723] border border-indigo-500/30 dark:border-white/10 overflow-hidden shadow-xl shadow-indigo-500/15 backdrop-blur-md">
      <div className="absolute top-0 right-0 w-80 h-80 bg-white/10 dark:bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />
      <div className="relative z-10 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 bg-white/15 dark:bg-indigo-500/20 border border-white/20 dark:border-indigo-500/30 rounded-full text-white dark:text-indigo-300 text-xs font-semibold mb-3 backdrop-blur-sm">
            <Sparkles className="w-3.5 h-3.5 text-amber-300 dark:text-amber-400" /> Workspace Overview
          </div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight">
            Welcome back, {userName}!
          </h1>
          <p className="text-indigo-100 dark:text-slate-300 text-sm mt-1.5 max-w-md font-medium">
            Here is what is happening in your inventory database today. You have{" "}
            <span className="text-amber-300 dark:text-amber-400 font-bold underline decoration-amber-300/40 underline-offset-2">
              {lowStockCount} low stock alert{lowStockCount === 1 ? "" : "s"}
            </span>
            .
          </p>
        </div>
        <div className="flex gap-3">
          <Link
            href="/dashboard/sales"
            className="px-4 py-2.5 bg-white text-indigo-700 hover:bg-slate-100 font-bold rounded-xl text-xs transition-all shadow-md shadow-black/10 hover:shadow-lg cursor-pointer"
          >
            Log A Sale
          </Link>
          <Link
            href="/dashboard/restocks"
            className="px-4 py-2.5 bg-white/15 hover:bg-white/25 border border-white/30 text-white font-semibold rounded-xl text-xs transition-all backdrop-blur-sm cursor-pointer"
          >
            Log A Restock
          </Link>
        </div>
      </div>
    </div>
  );
}
