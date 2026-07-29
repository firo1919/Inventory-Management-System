import React from "react";
import { Package } from "lucide-react";
import Link from "next/link";

import { Product } from "@/types";

interface LowStockWarningsProps {
    products: Product[];
    loading: boolean;
}

export function LowStockWarnings({ products, loading }: LowStockWarningsProps) {
    return (
        <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-5 shadow-sm flex flex-col">
            <div className="flex items-center justify-between mb-4">
                <div>
                    <h3 className="font-bold text-slate-900 dark:text-white text-base">
                        Low Stock Warnings
                    </h3>
                    <p className="text-xs text-slate-400 mt-0.5">
                        Products below threshold
                    </p>
                </div>
                <Link
                    href="/dashboard/products"
                    className="text-xs text-indigo-500 hover:text-indigo-400 font-semibold"
                >
                    View All
                </Link>
            </div>

            <div className="flex-1 space-y-3.5 overflow-y-auto max-h-72 pr-1">
                {loading ? (
                    Array.from({ length: 3 }).map((_, i) => (
                        <div
                            key={i}
                            className="h-14 bg-slate-50 dark:bg-slate-900/30 rounded-xl animate-pulse"
                        />
                    ))
                ) : products.length === 0 ? (
                    <div className="flex flex-col items-center justify-center h-full text-slate-400 py-10">
                        <Package className="w-10 h-10 opacity-35 mb-2 text-indigo-500" />
                        <p className="text-xs font-semibold">
                            All products fully stocked
                        </p>
                    </div>
                ) : (
                    products.map((p) => (
                        <div
                            key={p.id}
                            className="flex items-center justify-between p-3.5 bg-slate-50 dark:bg-[#1a1a24]/50 border border-slate-200/50 dark:border-white/5 rounded-xl hover:border-indigo-500/20 transition-all"
                        >
                            <div className="min-w-0">
                                <p className="text-xs font-semibold text-slate-800 dark:text-white truncate">
                                    {p.name}
                                </p>
                                <p className="text-[10px] text-slate-400 font-medium truncate mt-0.5">
                                    SKU: {p.sku} • Threshold:{" "}
                                    {p.lowStockThreshold || 10}
                                </p>
                            </div>
                            <div className="text-right shrink-0">
                                <span className="text-xs font-bold text-red-500 bg-red-500/10 px-2 py-1 rounded-lg">
                                    {p.quantity} left
                                </span>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}
