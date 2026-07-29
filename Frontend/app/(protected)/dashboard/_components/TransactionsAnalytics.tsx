"use client";

import React, { useEffect, useState } from "react";
import {
    AreaChart,
    Area,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    Legend,
} from "recharts";
import { Loader2, TrendingUp, AlertCircle, RotateCcw } from "lucide-react";
import { analyticsService, TrendPoint } from "@/services/analytics";

interface TransactionsAnalyticsProps {
    salesCount: number;
    restocksCount: number;
}

function daysAgo(days: number): string {
    const d = new Date();
    d.setDate(d.getDate() - days);
    return d.toISOString().split("T")[0];
}

export function TransactionsAnalytics({
    salesCount,
    restocksCount,
}: TransactionsAnalyticsProps) {
    const [data, setData] = useState<TrendPoint[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [period, setPeriod] = useState<"7D" | "30D" | "90D">("30D");
    const [reloadKey, setReloadKey] = useState(0);

    useEffect(() => {
        let ignore = false;

        async function loadData() {
            setLoading(true);
            setError(null);
            let days = 30;
            if (period === "7D") days = 7;
            else if (period === "90D") days = 90;

            const endDate = new Date().toISOString().split("T")[0];
            const startDate = daysAgo(days);
            const granularity = period === "90D" ? "WEEKLY" : "DAILY";

            try {
                const res = await analyticsService.getTrends(
                    granularity,
                    startDate,
                    endDate,
                );
                if (ignore) return;
                if (res && Array.isArray(res)) {
                    setData(res);
                } else {
                    setError("No analytics data returned from server");
                }
            } catch (err) {
                if (ignore) return;
                console.error(err);
                const errorObj = err as {
                    response?: { data?: { message?: string } };
                };
                setError(
                    errorObj?.response?.data?.message ||
                        "Failed to load transaction analytics",
                );
            } finally {
                if (!ignore) {
                    setLoading(false);
                }
            }
        }

        loadData();

        return () => {
            ignore = true;
        };
    }, [period, reloadKey]);

    const handleRetry = () => {
        setReloadKey((prev) => prev + 1);
    };

    const formatYAxis = (value: number) => {
        if (value >= 1000) {
            return `$${(value / 1000).toFixed(1)}k`;
        }
        return `$${value}`;
    };

    return (
        <div className="lg:col-span-2 bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-5 shadow-sm">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-6 gap-4">
                <div>
                    <h3 className="font-bold text-slate-900 dark:text-white text-base flex items-center gap-2">
                        Transactions Analytics{" "}
                        <TrendingUp className="w-4 h-4 text-indigo-500" />
                    </h3>
                    <p className="text-xs text-slate-400 mt-0.5">
                        Sales revenue vs Restocks volume
                    </p>
                </div>

                <div className="flex items-center gap-1 bg-slate-100 dark:bg-white/5 p-1 rounded-lg">
                    {(["7D", "30D", "90D"] as const).map((p) => (
                        <button
                            key={p}
                            onClick={() => setPeriod(p)}
                            className={`px-3 py-1 text-xs font-medium rounded-md transition-all ${
                                period === p
                                    ? "bg-white dark:bg-slate-800 text-slate-900 dark:text-white shadow-sm"
                                    : "text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200"
                            }`}
                        >
                            {p}
                        </button>
                    ))}
                </div>
            </div>

            <div className="flex gap-6 text-sm mb-6 px-2">
                <div className="flex flex-col">
                    <span className="text-slate-500 dark:text-slate-400 text-xs flex items-center gap-1.5">
                        <span className="w-2 h-2 rounded-full bg-emerald-500 block" />{" "}
                        Sales
                    </span>
                    <span className="font-semibold text-slate-800 dark:text-slate-100 mt-1">
                        {salesCount} total
                    </span>
                </div>
                <div className="flex flex-col">
                    <span className="text-slate-500 dark:text-slate-400 text-xs flex items-center gap-1.5">
                        <span className="w-2 h-2 rounded-full bg-purple-500 block" />{" "}
                        Restocks
                    </span>
                    <span className="font-semibold text-slate-800 dark:text-slate-100 mt-1">
                        {restocksCount} total
                    </span>
                </div>
            </div>

            <div className="h-72 w-full relative">
                {loading ? (
                    <div className="absolute inset-0 flex items-center justify-center bg-white/50 dark:bg-[#13131a]/50 z-10 backdrop-blur-sm rounded-xl">
                        <Loader2 className="w-8 h-8 text-indigo-500 animate-spin" />
                    </div>
                ) : error ? (
                    <div className="h-full w-full flex flex-col items-center justify-center gap-3 rounded-xl border border-rose-500/20 bg-rose-500/5 dark:bg-rose-500/10 p-6 text-center">
                        <div className="w-12 h-12 rounded-2xl bg-rose-500/10 text-rose-500 flex items-center justify-center">
                            <AlertCircle className="w-6 h-6" />
                        </div>
                        <div>
                            <h4 className="text-sm font-semibold text-slate-900 dark:text-white">
                                Failed to Load Analytics
                            </h4>
                            <p className="text-xs text-slate-400 mt-1 max-w-xs">
                                {error}
                            </p>
                        </div>
                        <button
                            onClick={handleRetry}
                            className="mt-1 inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium bg-rose-500/10 hover:bg-rose-500/20 text-rose-600 dark:text-rose-400 rounded-lg transition-colors"
                        >
                            <RotateCcw className="w-3.5 h-3.5" /> Retry
                        </button>
                    </div>
                ) : data.length === 0 ? (
                    <div className="h-full w-full flex flex-col items-center justify-center gap-2 rounded-xl border border-dashed border-slate-200 dark:border-white/10 text-center p-6">
                        <AlertCircle className="w-8 h-8 text-slate-400" />
                        <p className="text-sm font-medium text-slate-600 dark:text-slate-300">
                            No Transaction Data Available
                        </p>
                        <p className="text-xs text-slate-400">
                            No transactions recorded for the selected date
                            range.
                        </p>
                    </div>
                ) : (
                    <ResponsiveContainer width="100%" height="100%">
                        <AreaChart
                            data={data}
                            margin={{ top: 5, right: 0, left: -20, bottom: 0 }}
                        >
                            <defs>
                                <linearGradient
                                    id="colorSales"
                                    x1="0"
                                    y1="0"
                                    x2="0"
                                    y2="1"
                                >
                                    <stop
                                        offset="5%"
                                        stopColor="#10b981"
                                        stopOpacity={0.3}
                                    />
                                    <stop
                                        offset="95%"
                                        stopColor="#10b981"
                                        stopOpacity={0}
                                    />
                                </linearGradient>
                                <linearGradient
                                    id="colorRestocks"
                                    x1="0"
                                    y1="0"
                                    x2="0"
                                    y2="1"
                                >
                                    <stop
                                        offset="5%"
                                        stopColor="#a855f7"
                                        stopOpacity={0.3}
                                    />
                                    <stop
                                        offset="95%"
                                        stopColor="#a855f7"
                                        stopOpacity={0}
                                    />
                                </linearGradient>
                            </defs>
                            <CartesianGrid
                                strokeDasharray="3 3"
                                vertical={false}
                                stroke="rgba(150, 150, 150, 0.1)"
                            />
                            <XAxis
                                dataKey="period"
                                axisLine={false}
                                tickLine={false}
                                tick={{ fontSize: 12, fill: "#94a3b8" }}
                                dy={10}
                            />
                            <YAxis
                                yAxisId="left"
                                axisLine={false}
                                tickLine={false}
                                tick={{ fontSize: 12, fill: "#94a3b8" }}
                                tickFormatter={formatYAxis}
                            />
                            <YAxis
                                yAxisId="right"
                                orientation="right"
                                axisLine={false}
                                tickLine={false}
                                tick={{ fontSize: 12, fill: "#94a3b8" }}
                            />
                            <Tooltip
                                contentStyle={{
                                    backgroundColor: "#1c1c24",
                                    borderColor: "rgba(255,255,255,0.1)",
                                    borderRadius: "8px",
                                    color: "#fff",
                                    boxShadow:
                                        "0 4px 6px -1px rgba(0, 0, 0, 0.1)",
                                }}
                                itemStyle={{
                                    color: "#e2e8f0",
                                    fontSize: "13px",
                                }}
                                labelStyle={{
                                    color: "#94a3b8",
                                    fontSize: "12px",
                                    marginBottom: "4px",
                                }}
                            />
                            <Legend
                                iconType="circle"
                                wrapperStyle={{ fontSize: "12px" }}
                            />
                            <Area
                                yAxisId="left"
                                type="monotone"
                                dataKey="salesRevenue"
                                name="Sales Revenue"
                                stroke="#10b981"
                                strokeWidth={2}
                                fillOpacity={1}
                                fill="url(#colorSales)"
                                activeDot={{
                                    r: 6,
                                    strokeWidth: 0,
                                    fill: "#10b981",
                                }}
                            />
                            <Area
                                yAxisId="right"
                                type="monotone"
                                dataKey="unitsRestocked"
                                name="Units Restocked"
                                stroke="#a855f7"
                                strokeWidth={2}
                                fillOpacity={1}
                                fill="url(#colorRestocks)"
                                activeDot={{
                                    r: 6,
                                    strokeWidth: 0,
                                    fill: "#a855f7",
                                }}
                            />
                        </AreaChart>
                    </ResponsiveContainer>
                )}
            </div>
        </div>
    );
}
