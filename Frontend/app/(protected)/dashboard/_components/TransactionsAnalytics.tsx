import React from "react";
import { BarChart2 } from "lucide-react";

interface TransactionsAnalyticsProps {
  salesCount: number;
  restocksCount: number;
}

export function TransactionsAnalytics({ salesCount, restocksCount }: TransactionsAnalyticsProps) {
  return (
    <div className="lg:col-span-2 bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-5 shadow-sm">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h3 className="font-bold text-slate-900 dark:text-white text-base">
            Transactions Analytics
          </h3>
          <p className="text-xs text-slate-400 mt-0.5">
            Weekly sales &amp; restock trends
          </p>
        </div>
      </div>

      <div className="h-72 w-full flex flex-col items-center justify-center gap-4 rounded-xl border border-dashed border-slate-200 dark:border-white/10 bg-slate-50/50 dark:bg-white/2">
        <div className="w-12 h-12 rounded-2xl bg-indigo-500/10 flex items-center justify-center">
          <BarChart2 className="w-6 h-6 text-indigo-500" />
        </div>
        <div className="text-center">
          <p className="text-sm font-semibold text-slate-700 dark:text-white">
            Analytics Coming Soon
          </p>
          <p className="text-xs text-slate-400 mt-1 max-w-xs">
            Real-time transaction charts will appear here once the backend analytics aggregation endpoint
            is available. Your data is being recorded in the Sales and Restocks logs.
          </p>
        </div>
        <div className="flex gap-6 text-xs text-slate-400">
          <span className="flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-indigo-500 block" /> {salesCount} total sales
          </span>
          <span className="flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-purple-500 block" /> {restocksCount} total restocks
          </span>
        </div>
      </div>
    </div>
  );
}
