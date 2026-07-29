"use client";

export const dynamic = "force-dynamic";

import React, { useEffect, useState } from "react";
import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip as RechartsTooltip,
    ResponsiveContainer,
    Legend,
    AreaChart,
    Area,
    ComposedChart,
    Line,
} from "recharts";
import {
    Loader2,
    Calendar,
    TrendingUp,
    DollarSign,
    ShoppingBag,
    Package,
    AlertCircle,
    RotateCcw,
} from "lucide-react";
import {
    analyticsService,
    TransactionSummary,
    TrendPoint,
    TopProduct,
} from "@/services/analytics";

function daysAgo(days: number): string {
    const d = new Date();
    d.setDate(d.getDate() - days);
    return d.toISOString().split("T")[0];
}

export default function AnalyticsPage() {
    const [period, setPeriod] = useState<"7D" | "30D" | "90D" | "1Y">("30D");
    const [loading, setLoading] = useState(true);
    const [reloadKey, setReloadKey] = useState(0);

    const [summary, setSummary] = useState<TransactionSummary | null>(null);
    const [trends, setTrends] = useState<TrendPoint[]>([]);
    const [topProducts, setTopProducts] = useState<TopProduct[]>([]);

    const [summaryError, setSummaryError] = useState<string | null>(null);
    const [trendsError, setTrendsError] = useState<string | null>(null);
    const [topProductsError, setTopProductsError] = useState<string | null>(
        null,
    );

    useEffect(() => {
        let ignore = false;

        async function loadData() {
            setLoading(true);
            setSummaryError(null);
            setTrendsError(null);
            setTopProductsError(null);

            let days = 30;
            if (period === "7D") days = 7;
            else if (period === "90D") days = 90;
            else if (period === "1Y") days = 365;

            const endDate = new Date().toISOString().split("T")[0];
            const startDate = daysAgo(days);
            let granularity: "DAILY" | "WEEKLY" | "MONTHLY" = "DAILY";
            if (period === "90D") granularity = "WEEKLY";
            if (period === "1Y") granularity = "MONTHLY";

            const [summaryRes, trendsRes, topRes] = await Promise.allSettled([
                analyticsService.getSummary(startDate, endDate),
                analyticsService.getTrends(granularity, startDate, endDate),
                analyticsService.getTopProducts(
                    10,
                    startDate,
                    endDate,
                    "revenue",
                ),
            ]);

            if (ignore) return;

            if (summaryRes.status === "fulfilled" && summaryRes.value) {
                setSummary(summaryRes.value);
            } else {
                const err =
                    summaryRes.status === "rejected" ? summaryRes.reason : null;
                setSummaryError(
                    err?.response?.data?.message ||
                        "Failed to load analytics summary",
                );
            }

            if (
                trendsRes.status === "fulfilled" &&
                trendsRes.value &&
                Array.isArray(trendsRes.value)
            ) {
                setTrends(trendsRes.value);
            } else {
                const err =
                    trendsRes.status === "rejected" ? trendsRes.reason : null;
                setTrendsError(
                    err?.response?.data?.message ||
                        "Failed to load trend analytics",
                );
            }

            if (
                topRes.status === "fulfilled" &&
                topRes.value &&
                Array.isArray(topRes.value)
            ) {
                setTopProducts(topRes.value);
            } else {
                const err = topRes.status === "rejected" ? topRes.reason : null;
                setTopProductsError(
                    err?.response?.data?.message ||
                        "Failed to load top products analytics",
                );
            }

            setLoading(false);
        }

        loadData();

        return () => {
            ignore = true;
        };
    }, [period, reloadKey]);

    const handleRetry = () => {
        setReloadKey((prev) => prev + 1);
    };

    const formatCurrency = (val: number) => `$${val.toLocaleString()}`;
    const formatCompact = (val: number) => {
        if (val >= 1000) return `$${(val / 1000).toFixed(1)}k`;
        return `$${val}`;
    };

    return (
        <div className="space-y-8 pb-10">
            {/* HEADER */}
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-slate-900 dark:text-white">
                        Analytics Overview
                    </h1>
                    <p className="text-slate-500 dark:text-slate-400 mt-1">
                        Deep dive into your sales and inventory performance
                    </p>
                </div>
                <div className="flex items-center gap-2 bg-white dark:bg-[#13131a] p-1.5 rounded-xl border border-slate-200 dark:border-white/5 shadow-sm">
                    <Calendar className="w-4 h-4 text-slate-400 ml-2" />
                    <div className="flex gap-1 ml-2">
                        {(["7D", "30D", "90D", "1Y"] as const).map((p) => (
                            <button
                                key={p}
                                onClick={() => setPeriod(p)}
                                className={`px-4 py-1.5 text-sm font-medium rounded-lg transition-all ${
                                    period === p
                                        ? "bg-indigo-500/10 text-indigo-600 dark:text-indigo-400"
                                        : "text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-200"
                                }`}
                            >
                                {p}
                            </button>
                        ))}
                    </div>
                </div>
            </div>

            {/* SUMMARY METRICS */}
            <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
                <MetricCard
                    title="Total Revenue"
                    value={
                        summary
                            ? formatCurrency(summary.totalSalesRevenue)
                            : "-"
                    }
                    icon={<DollarSign className="w-5 h-5 text-emerald-500" />}
                    colorClass="bg-emerald-500/10"
                    loading={loading}
                    error={summaryError}
                />
                <MetricCard
                    title="Sales Count"
                    value={summary?.totalSalesCount.toLocaleString() || "-"}
                    icon={<TrendingUp className="w-5 h-5 text-indigo-500" />}
                    colorClass="bg-indigo-500/10"
                    loading={loading}
                    error={summaryError}
                />
                <MetricCard
                    title="Units Sold"
                    value={summary?.totalUnitsSold.toLocaleString() || "-"}
                    icon={<ShoppingBag className="w-5 h-5 text-amber-500" />}
                    colorClass="bg-amber-500/10"
                    loading={loading}
                    error={summaryError}
                />
                <MetricCard
                    title="Units Restocked"
                    value={summary?.totalUnitsRestocked.toLocaleString() || "-"}
                    icon={<Package className="w-5 h-5 text-purple-500" />}
                    colorClass="bg-purple-500/10"
                    loading={loading}
                    error={summaryError}
                />
            </div>

            {/* MAIN TREND CHART */}
            <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-6 shadow-sm relative">
                <div className="flex items-center justify-between mb-6">
                    <div>
                        <h3 className="font-bold text-slate-900 dark:text-white text-base">
                            Revenue vs Restock Volume Trends
                        </h3>
                        <p className="text-xs text-slate-500 mt-1">
                            Comparing incoming stock volume with outgoing sales
                            revenue
                        </p>
                    </div>
                    {trendsError && (
                        <button
                            onClick={handleRetry}
                            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium bg-rose-500/10 hover:bg-rose-500/20 text-rose-600 dark:text-rose-400 rounded-lg transition-colors"
                        >
                            <RotateCcw className="w-3.5 h-3.5" /> Retry
                        </button>
                    )}
                </div>

                {loading ? (
                    <div className="h-80 w-full flex items-center justify-center bg-white/50 dark:bg-[#13131a]/50 backdrop-blur-sm rounded-2xl">
                        <Loader2 className="w-8 h-8 text-indigo-500 animate-spin" />
                    </div>
                ) : trendsError ? (
                    <div className="h-80 w-full flex flex-col items-center justify-center gap-3 rounded-xl border border-rose-500/20 bg-rose-500/5 dark:bg-rose-500/10 p-6 text-center">
                        <div className="w-12 h-12 rounded-2xl bg-rose-500/10 text-rose-500 flex items-center justify-center">
                            <AlertCircle className="w-6 h-6" />
                        </div>
                        <div>
                            <h4 className="text-sm font-semibold text-slate-900 dark:text-white">
                                Unable to Load Trend Chart
                            </h4>
                            <p className="text-xs text-slate-400 mt-1 max-w-sm">
                                {trendsError}
                            </p>
                        </div>
                    </div>
                ) : trends.length === 0 ? (
                    <div className="h-80 w-full flex flex-col items-center justify-center gap-2 rounded-xl border border-dashed border-slate-200 dark:border-white/10 text-center p-6">
                        <AlertCircle className="w-8 h-8 text-slate-400" />
                        <p className="text-sm font-medium text-slate-600 dark:text-slate-300">
                            No Trend Data Found
                        </p>
                        <p className="text-xs text-slate-400">
                            No sales or restocks recorded for this period.
                        </p>
                    </div>
                ) : (
                    <div className="h-80 w-full">
                        <ResponsiveContainer width="100%" height="100%">
                            <AreaChart
                                data={trends}
                                margin={{
                                    top: 10,
                                    right: 0,
                                    left: -20,
                                    bottom: 0,
                                }}
                            >
                                <defs>
                                    <linearGradient
                                        id="areaSales"
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
                                        id="areaRestocks"
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
                                    tickFormatter={formatCompact}
                                />
                                <YAxis
                                    yAxisId="right"
                                    orientation="right"
                                    axisLine={false}
                                    tickLine={false}
                                    tick={{ fontSize: 12, fill: "#94a3b8" }}
                                />
                                <RechartsTooltip
                                    contentStyle={{
                                        backgroundColor: "#1c1c24",
                                        borderColor: "rgba(255,255,255,0.1)",
                                        borderRadius: "8px",
                                        color: "#fff",
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
                                <Legend iconType="circle" />
                                <Area
                                    yAxisId="left"
                                    type="monotone"
                                    dataKey="salesRevenue"
                                    name="Revenue"
                                    stroke="#10b981"
                                    strokeWidth={2}
                                    fill="url(#areaSales)"
                                    activeDot={{
                                        r: 6,
                                        fill: "#10b981",
                                        strokeWidth: 0,
                                    }}
                                />
                                <Area
                                    yAxisId="right"
                                    type="monotone"
                                    dataKey="unitsRestocked"
                                    name="Units Restocked"
                                    stroke="#a855f7"
                                    strokeWidth={2}
                                    fill="url(#areaRestocks)"
                                    activeDot={{
                                        r: 6,
                                        fill: "#a855f7",
                                        strokeWidth: 0,
                                    }}
                                />
                            </AreaChart>
                        </ResponsiveContainer>
                    </div>
                )}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* TOP PRODUCTS CHART */}
                <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-6 shadow-sm relative">
                    <div className="flex items-center justify-between mb-6">
                        <div>
                            <h3 className="font-bold text-slate-900 dark:text-white text-base">
                                Top Products by Revenue
                            </h3>
                            <p className="text-xs text-slate-500 mt-1">
                                Best performing items in selected period
                            </p>
                        </div>
                        {topProductsError && (
                            <button
                                onClick={handleRetry}
                                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium bg-rose-500/10 hover:bg-rose-500/20 text-rose-600 dark:text-rose-400 rounded-lg transition-colors"
                            >
                                <RotateCcw className="w-3.5 h-3.5" /> Retry
                            </button>
                        )}
                    </div>

                    {loading ? (
                        <div className="h-80 w-full flex items-center justify-center bg-white/50 dark:bg-[#13131a]/50 backdrop-blur-sm rounded-2xl">
                            <Loader2 className="w-8 h-8 text-indigo-500 animate-spin" />
                        </div>
                    ) : topProductsError ? (
                        <div className="h-80 w-full flex flex-col items-center justify-center gap-3 rounded-xl border border-rose-500/20 bg-rose-500/5 dark:bg-rose-500/10 p-6 text-center">
                            <div className="w-12 h-12 rounded-2xl bg-rose-500/10 text-rose-500 flex items-center justify-center">
                                <AlertCircle className="w-6 h-6" />
                            </div>
                            <div>
                                <h4 className="text-sm font-semibold text-slate-900 dark:text-white">
                                    Unable to Load Top Products
                                </h4>
                                <p className="text-xs text-slate-400 mt-1 max-w-xs">
                                    {topProductsError}
                                </p>
                            </div>
                        </div>
                    ) : topProducts.length === 0 ? (
                        <div className="h-80 w-full flex flex-col items-center justify-center gap-2 rounded-xl border border-dashed border-slate-200 dark:border-white/10 text-center p-6">
                            <AlertCircle className="w-8 h-8 text-slate-400" />
                            <p className="text-sm font-medium text-slate-600 dark:text-slate-300">
                                No Product Data
                            </p>
                            <p className="text-xs text-slate-400">
                                No product sales recorded for this period.
                            </p>
                        </div>
                    ) : (
                        <div className="h-80 w-full">
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart
                                    data={topProducts}
                                    layout="vertical"
                                    margin={{
                                        top: 0,
                                        right: 30,
                                        left: 20,
                                        bottom: 0,
                                    }}
                                >
                                    <CartesianGrid
                                        strokeDasharray="3 3"
                                        horizontal={true}
                                        vertical={false}
                                        stroke="rgba(150, 150, 150, 0.1)"
                                    />
                                    <XAxis
                                        type="number"
                                        axisLine={false}
                                        tickLine={false}
                                        tick={{ fontSize: 12, fill: "#94a3b8" }}
                                        tickFormatter={formatCompact}
                                    />
                                    <YAxis
                                        dataKey="productName"
                                        type="category"
                                        axisLine={false}
                                        tickLine={false}
                                        tick={{ fontSize: 12, fill: "#94a3b8" }}
                                        width={120}
                                    />
                                    <RechartsTooltip
                                        cursor={{
                                            fill: "rgba(255,255,255,0.05)",
                                        }}
                                        contentStyle={{
                                            backgroundColor: "#1c1c24",
                                            borderColor:
                                                "rgba(255,255,255,0.1)",
                                            borderRadius: "8px",
                                            color: "#fff",
                                        }}
                                        itemStyle={{
                                            color: "#e2e8f0",
                                            fontSize: "13px",
                                        }}
                                    />
                                    <Bar
                                        dataKey="totalRevenue"
                                        name="Revenue"
                                        fill="#10b981"
                                        radius={[0, 4, 4, 0]}
                                        barSize={20}
                                    />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    )}
                </div>

                {/* VOLUME COMPARISON (Sales vs Restocks Count) */}
                <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-6 shadow-sm relative">
                    <div className="flex items-center justify-between mb-6">
                        <div>
                            <h3 className="font-bold text-slate-900 dark:text-white text-base">
                                Transaction Volume
                            </h3>
                            <p className="text-xs text-slate-500 mt-1">
                                Number of sales vs restocks events
                            </p>
                        </div>
                        {trendsError && (
                            <button
                                onClick={handleRetry}
                                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium bg-rose-500/10 hover:bg-rose-500/20 text-rose-600 dark:text-rose-400 rounded-lg transition-colors"
                            >
                                <RotateCcw className="w-3.5 h-3.5" /> Retry
                            </button>
                        )}
                    </div>

                    {loading ? (
                        <div className="h-80 w-full flex items-center justify-center bg-white/50 dark:bg-[#13131a]/50 backdrop-blur-sm rounded-2xl">
                            <Loader2 className="w-8 h-8 text-indigo-500 animate-spin" />
                        </div>
                    ) : trendsError ? (
                        <div className="h-80 w-full flex flex-col items-center justify-center gap-3 rounded-xl border border-rose-500/20 bg-rose-500/5 dark:bg-rose-500/10 p-6 text-center">
                            <div className="w-12 h-12 rounded-2xl bg-rose-500/10 text-rose-500 flex items-center justify-center">
                                <AlertCircle className="w-6 h-6" />
                            </div>
                            <div>
                                <h4 className="text-sm font-semibold text-slate-900 dark:text-white">
                                    Unable to Load Transaction Volume
                                </h4>
                                <p className="text-xs text-slate-400 mt-1 max-w-xs">
                                    {trendsError}
                                </p>
                            </div>
                        </div>
                    ) : trends.length === 0 ? (
                        <div className="h-80 w-full flex flex-col items-center justify-center gap-2 rounded-xl border border-dashed border-slate-200 dark:border-white/10 text-center p-6">
                            <AlertCircle className="w-8 h-8 text-slate-400" />
                            <p className="text-sm font-medium text-slate-600 dark:text-slate-300">
                                No Volume Data
                            </p>
                            <p className="text-xs text-slate-400">
                                No sales or restocks events recorded for this
                                period.
                            </p>
                        </div>
                    ) : (
                        <div className="h-80 w-full">
                            <ResponsiveContainer width="100%" height="100%">
                                <ComposedChart
                                    data={trends}
                                    margin={{
                                        top: 10,
                                        right: 0,
                                        left: -20,
                                        bottom: 0,
                                    }}
                                >
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
                                        axisLine={false}
                                        tickLine={false}
                                        tick={{ fontSize: 12, fill: "#94a3b8" }}
                                    />
                                    <RechartsTooltip
                                        contentStyle={{
                                            backgroundColor: "#1c1c24",
                                            borderColor:
                                                "rgba(255,255,255,0.1)",
                                            borderRadius: "8px",
                                            color: "#fff",
                                        }}
                                        itemStyle={{
                                            color: "#e2e8f0",
                                            fontSize: "13px",
                                        }}
                                    />
                                    <Legend iconType="circle" />
                                    <Bar
                                        dataKey="salesCount"
                                        name="Sales Count"
                                        fill="#6366f1"
                                        radius={[4, 4, 0, 0]}
                                        maxBarSize={40}
                                    />
                                    <Line
                                        type="monotone"
                                        dataKey="restocksCount"
                                        name="Restocks Count"
                                        stroke="#a855f7"
                                        strokeWidth={3}
                                        dot={{ r: 4, fill: "#a855f7" }}
                                        activeDot={{ r: 6 }}
                                    />
                                </ComposedChart>
                            </ResponsiveContainer>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

function MetricCard({
    title,
    value,
    icon,
    colorClass,
    loading,
    error,
}: {
    title: string;
    value: string;
    icon: React.ReactNode;
    colorClass: string;
    loading: boolean;
    error?: string | null;
}) {
    return (
        <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-5 shadow-sm relative overflow-hidden group">
            <div className="absolute -right-4 -top-4 w-24 h-24 bg-linear-to-br from-transparent to-black/5 dark:to-white/5 rounded-full blur-2xl group-hover:scale-150 transition-transform duration-500" />
            <div className="flex items-center gap-4 relative z-10">
                <div
                    className={`w-12 h-12 rounded-xl flex items-center justify-center shrink-0 ${colorClass}`}
                >
                    {error ? (
                        <AlertCircle className="w-5 h-5 text-rose-500" />
                    ) : (
                        icon
                    )}
                </div>
                <div>
                    <p className="text-sm font-medium text-slate-500 dark:text-slate-400">
                        {title}
                    </p>
                    {loading ? (
                        <div className="h-7 w-24 bg-slate-200 dark:bg-white/10 rounded animate-pulse mt-1" />
                    ) : error ? (
                        <span className="text-xs text-rose-500 font-medium mt-1 block">
                            Failed to load
                        </span>
                    ) : (
                        <h4 className="text-2xl font-bold text-slate-900 dark:text-white mt-0.5">
                            {value}
                        </h4>
                    )}
                </div>
            </div>
        </div>
    );
}
