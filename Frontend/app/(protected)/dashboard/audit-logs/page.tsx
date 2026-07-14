"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { apiClient } from "@/lib/api-client";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import {
    Search,
    ChevronLeft,
    ChevronRight,
    Trash2,
    AlertTriangle,
    Loader2,
    Calendar,
    Download,
    Filter,
    Eye,
    Activity,
    CheckCircle,
    XCircle,
    Clock,
} from "lucide-react";

export default function AuditLogsPage() {
    const { isAdmin, loading: authLoading } = useAuth();
    const router = useRouter();

    // Route Guard
    useEffect(() => {
        if (!authLoading && !isAdmin) {
            router.replace("/dashboard");
        }
    }, [isAdmin, authLoading, router]);

    // Data States
    const [logs, setLogs] = useState<any[]>([]);
    const [stats, setStats] = useState<any>(null);

    // Pagination & Filters
    const [page, setPage] = useState(0);
    const [pageSize, setPageSize] = useState(20);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    // Filters State
    const [usernameFilter, setUsernameFilter] = useState("");
    const [actionFilter, setActionFilter] = useState("");
    const [statusFilter, setStatusFilter] = useState("");
    const [resourceTypeFilter, setResourceTypeFilter] = useState("");
    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");

    // Loading States
    const [loading, setLoading] = useState(true);
    const [statsLoading, setStatsLoading] = useState(true);
    const [exporting, setExporting] = useState(false);

    // Selected Log for details modal
    const [selectedLog, setSelectedLog] = useState<any>(null);
    const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);

    // Bulk Delete state
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [deleteStartDate, setDeleteStartDate] = useState("");
    const [deleteEndDate, setDeleteEndDate] = useState("");

    const fetchLogs = async () => {
        if (!isAdmin) return;
        try {
            setLoading(true);

            const params: any = {
                page,
                size: pageSize,
                sort: "timestamp,desc",
            };

            if (usernameFilter) params.username = usernameFilter;
            if (actionFilter) params.action = actionFilter;
            if (statusFilter) params.status = statusFilter;
            if (resourceTypeFilter) params.resourceType = resourceTypeFilter;
            if (startDate) params.startDate = `${startDate}T00:00:00`;
            if (endDate) params.endDate = `${endDate}T23:59:59`;

            const res = await apiClient.get("/api/v1/admin/audit-logs", {
                params,
            });

            setLogs(res.data?.content || []);
            setTotalPages(res.data?.totalPages || 1);
            setTotalElements(res.data?.totalElements || 0);
        } catch {
            toast.error("Failed to load audit logs.");
        } finally {
            setLoading(false);
        }
    };

    const fetchStats = async () => {
        if (!isAdmin) return;
        try {
            setStatsLoading(true);
            const res = await apiClient.get(
                "/api/v1/admin/audit-logs/statistics",
            );
            setStats(res.data);
        } catch {
            toast.error("Failed to load audit statistics.");
        } finally {
            setStatsLoading(false);
        }
    };

    useEffect(() => {
        fetchLogs();
        fetchStats();
    }, [page, pageSize, isAdmin]);

    const handleFilterSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        setPage(0);
        fetchLogs();
    };

    const handleResetFilters = () => {
        setUsernameFilter("");
        setActionFilter("");
        setStatusFilter("");
        setResourceTypeFilter("");
        setStartDate("");
        setEndDate("");
        setPage(0);
        // Trigger refetch by running manual fetch
        setTimeout(() => {
            fetchLogs();
        }, 0);
    };

    // Export File logic (CSV / JSON)
    const handleExport = async (format: "csv" | "json") => {
        setExporting(true);
        try {
            const filterPayload: any = {
                page: 0,
                size: 10000, // Export up to 10k logs
                sort: "timestamp,desc",
            };
            if (usernameFilter) filterPayload.username = usernameFilter;
            if (actionFilter) filterPayload.action = actionFilter;
            if (statusFilter) filterPayload.status = statusFilter;
            if (resourceTypeFilter)
                filterPayload.resourceType = resourceTypeFilter;
            if (startDate) filterPayload.startDate = `${startDate}T00:00:00`;
            if (endDate) filterPayload.endDate = `${endDate}T23:59:59`;

            const res = await apiClient.post(
                `/api/v1/admin/audit-logs/export/${format}`,
                filterPayload,
                { responseType: "blob" },
            );

            // Create browser download link
            const blob = new Blob([res.data], {
                type: format === "csv" ? "text/csv" : "application/json",
            });
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = url;
            link.setAttribute(
                "download",
                `audit-logs-${new Date().toISOString().split("T")[0]}.${format}`,
            );
            document.body.appendChild(link);
            link.click();
            link.parentNode?.removeChild(link);
        } catch {
            toast.error("Failed to export logs. Please try again.");
        } finally {
            setExporting(false);
        }
    };

    // Bulk Delete logs
    const handleBulkDelete = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!deleteStartDate || !deleteEndDate) return;

        setLoading(true);
        try {
            await apiClient.delete("/api/v1/admin/audit-logs", {
                params: {
                    startDate: `${deleteStartDate}T00:00:00`,
                    endDate: `${deleteEndDate}T23:59:59`,
                },
            });

            setIsDeleteModalOpen(false);
            setDeleteStartDate("");
            setDeleteEndDate("");
            toast.success("Audit logs cleared successfully.");
            fetchLogs();
            fetchStats();
        } catch (err: any) {
            toast.error(
                err.response?.data?.message ||
                    err.message ||
                    "Failed to clear logs",
            );
        } finally {
            setLoading(false);
        }
    };

    const openDetailModal = (logItem: any) => {
        setSelectedLog(logItem);
        setIsDetailModalOpen(true);
    };

    if (authLoading || !isAdmin) {
        return (
            <div className="flex flex-col items-center justify-center min-h-[50vh] text-slate-400">
                <Loader2 className="w-8 h-8 animate-spin text-indigo-500 mb-2" />
                <p className="text-xs">Verifying Admin Permissions...</p>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {/* HEADER */}
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                <div>
                    <h1 className="text-xl font-bold dark:text-white">
                        Audit Log Viewer
                    </h1>
                    <p className="text-xs text-slate-400">
                        Track and analyze backend system events, database
                        updates, and authentication logs
                    </p>
                </div>
                <div className="flex flex-wrap gap-3">
                    <button
                        onClick={() => setIsDeleteModalOpen(true)}
                        className="inline-flex items-center gap-1.5 px-3 py-2 bg-red-600/10 hover:bg-red-600/20 text-red-500 text-xs font-semibold rounded-xl transition-all border border-red-500/10 cursor-pointer"
                    >
                        <Trash2 className="w-4 h-4" /> Clear Logs
                    </button>
                    <button
                        disabled={exporting}
                        onClick={() => handleExport("csv")}
                        className="inline-flex items-center gap-1.5 px-3 py-2 bg-white/5 hover:bg-white/10 text-slate-700 dark:text-white text-xs font-semibold rounded-xl border border-slate-200 dark:border-white/10 transition-all cursor-pointer"
                    >
                        <Download className="w-4 h-4" /> Export CSV
                    </button>
                    <button
                        disabled={exporting}
                        onClick={() => handleExport("json")}
                        className="inline-flex items-center gap-1.5 px-3 py-2 bg-white/5 hover:bg-white/10 text-slate-700 dark:text-white text-xs font-semibold rounded-xl border border-slate-200 dark:border-white/10 transition-all cursor-pointer"
                    >
                        <Download className="w-4 h-4" /> Export JSON
                    </button>
                </div>
            </div>

            {/* STATS OVERVIEW */}
            {!statsLoading && stats && (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                    <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-4 flex items-center gap-4">
                        <div className="w-10 h-10 bg-indigo-500/10 text-indigo-500 rounded-xl flex items-center justify-center">
                            <Activity className="w-5 h-5" />
                        </div>
                        <div>
                            <p className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider">
                                Total Actions
                            </p>
                            <h4 className="text-lg font-bold text-slate-800 dark:text-white mt-0.5">
                                {stats.totalLogsCount || 0}
                            </h4>
                        </div>
                    </div>

                    <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-4 flex items-center gap-4">
                        <div className="w-10 h-10 bg-emerald-500/10 text-emerald-500 rounded-xl flex items-center justify-center">
                            <CheckCircle className="w-5 h-5" />
                        </div>
                        <div>
                            <p className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider">
                                Successful Events
                            </p>
                            <h4 className="text-lg font-bold text-slate-800 dark:text-white mt-0.5">
                                {stats.successCount || 0}
                            </h4>
                        </div>
                    </div>

                    <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-4 flex items-center gap-4">
                        <div className="w-10 h-10 bg-red-500/10 text-red-500 rounded-xl flex items-center justify-center">
                            <XCircle className="w-5 h-5" />
                        </div>
                        <div>
                            <p className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider">
                                Failed Events
                            </p>
                            <h4 className="text-lg font-bold text-slate-800 dark:text-white mt-0.5">
                                {stats.failureCount || 0}
                            </h4>
                        </div>
                    </div>

                    <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-4 flex items-center gap-4">
                        <div className="w-10 h-10 bg-amber-500/10 text-amber-500 rounded-xl flex items-center justify-center">
                            <Clock className="w-5 h-5" />
                        </div>
                        <div>
                            <p className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider">
                                Error Rate
                            </p>
                            <h4 className="text-lg font-bold text-slate-800 dark:text-white mt-0.5">
                                {stats.totalLogsCount > 0
                                    ? `${((stats.failureCount / stats.totalLogsCount) * 100).toFixed(1)}%`
                                    : "0%"}
                            </h4>
                        </div>
                    </div>
                </div>
            )}

            {/* FILTER PANEL */}
            <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-5 shadow-sm">
                <div className="flex items-center gap-2 mb-4 text-slate-700 dark:text-slate-200">
                    <Filter className="w-4 h-4 text-indigo-500" />
                    <h3 className="text-sm font-bold">Filter System Logs</h3>
                </div>

                <form
                    onSubmit={handleFilterSubmit}
                    className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4"
                >
                    <div>
                        <label className="block text-[10px] font-semibold text-slate-400 mb-1">
                            Username
                        </label>
                        <input
                            type="text"
                            value={usernameFilter}
                            onChange={(e) => setUsernameFilter(e.target.value)}
                            className="w-full px-3 py-1.5 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs"
                            placeholder="e.g. admin"
                        />
                    </div>

                    <div>
                        <label className="block text-[10px] font-semibold text-slate-400 mb-1">
                            Action Type
                        </label>
                        <input
                            type="text"
                            value={actionFilter}
                            onChange={(e) => setActionFilter(e.target.value)}
                            className="w-full px-3 py-1.5 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs"
                            placeholder="e.g. CREATE_PRODUCT"
                        />
                    </div>

                    <div>
                        <label className="block text-[10px] font-semibold text-slate-400 mb-1">
                            Resource Type
                        </label>
                        <input
                            type="text"
                            value={resourceTypeFilter}
                            onChange={(e) =>
                                setResourceTypeFilter(e.target.value)
                            }
                            className="w-full px-3 py-1.5 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs"
                            placeholder="e.g. Product"
                        />
                    </div>

                    <div>
                        <label className="block text-[10px] font-semibold text-slate-400 mb-1">
                            Status
                        </label>
                        <select
                            value={statusFilter}
                            onChange={(e) => setStatusFilter(e.target.value)}
                            className="w-full px-3 py-1.5 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs"
                        >
                            <option value="">All Status</option>
                            <option value="SUCCESS">SUCCESS</option>
                            <option value="FAILURE">FAILURE</option>
                        </select>
                    </div>

                    <div>
                        <label className="block text-[10px] font-semibold text-slate-400 mb-1">
                            Start Date
                        </label>
                        <input
                            type="date"
                            value={startDate}
                            onChange={(e) => setStartDate(e.target.value)}
                            className="w-full px-3 py-1.5 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs"
                        />
                    </div>

                    <div>
                        <label className="block text-[10px] font-semibold text-slate-400 mb-1">
                            End Date
                        </label>
                        <input
                            type="date"
                            value={endDate}
                            onChange={(e) => setEndDate(e.target.value)}
                            className="w-full px-3 py-1.5 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs"
                        />
                    </div>

                    <div className="lg:col-span-6 flex justify-end gap-3 mt-2">
                        <button
                            type="button"
                            onClick={handleResetFilters}
                            className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-500 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs font-semibold cursor-pointer"
                        >
                            Reset Filters
                        </button>
                        <button
                            type="submit"
                            className="px-4 py-2 bg-indigo-600 hover:bg-indigo-505 text-white rounded-xl text-xs font-semibold shadow-md shadow-indigo-500/10 cursor-pointer"
                        >
                            Apply Filter
                        </button>
                    </div>
                </form>
            </div>

            {/* AUDIT LOG TABLE */}
            <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl shadow-sm overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left text-sm border-collapse">
                        <thead>
                            <tr className="border-b border-slate-200 dark:border-white/5 bg-slate-50/50 dark:bg-[#0a0a0f]/50 text-slate-400 text-xs font-semibold">
                                <th className="p-4">Timestamp</th>
                                <th className="p-4">Action</th>
                                <th className="p-4">Resource</th>
                                <th className="p-4">Username</th>
                                <th className="p-4">Status</th>
                                <th className="p-4">Correlation ID</th>
                                <th className="p-4 text-center">Details</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 dark:divide-white/5">
                            {loading ? (
                                Array.from({ length: 10 }).map((_, i) => (
                                    <tr key={i} className="animate-pulse">
                                        <td
                                            colSpan={7}
                                            className="p-4 h-12 bg-slate-50/10 dark:bg-[#0e0e13]/10"
                                        />
                                    </tr>
                                ))
                            ) : logs.length === 0 ? (
                                <tr>
                                    <td
                                        colSpan={7}
                                        className="p-8 text-center text-slate-400"
                                    >
                                        No system audit logs found.
                                    </td>
                                </tr>
                            ) : (
                                logs.map((item) => (
                                    <tr
                                        key={item.id}
                                        className="hover:bg-slate-50/50 dark:hover:bg-[#1c1c24]/10 text-slate-600 dark:text-slate-300"
                                    >
                                        <td className="p-4 text-xs font-medium text-slate-400">
                                            {new Date(
                                                item.timestamp,
                                            ).toLocaleString(undefined, {
                                                month: "short",
                                                day: "numeric",
                                                hour: "2-digit",
                                                minute: "2-digit",
                                                second: "2-digit",
                                            })}
                                        </td>
                                        <td className="p-4 font-semibold text-slate-800 dark:text-white text-xs">
                                            {item.action}
                                        </td>
                                        <td className="p-4 text-xs font-medium">
                                            {item.resourceType}
                                        </td>
                                        <td className="p-4 font-mono text-xs">
                                            {item.username}
                                        </td>
                                        <td className="p-4">
                                            <span
                                                className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded text-[10px] font-bold uppercase border ${
                                                    item.status === "SUCCESS"
                                                        ? "bg-emerald-500/10 text-emerald-500 border-emerald-500/20"
                                                        : "bg-red-500/10 text-red-500 border-red-500/20"
                                                }`}
                                            >
                                                {item.status}
                                            </span>
                                        </td>
                                        <td className="p-4 font-mono text-[10px] text-slate-400">
                                            {item.correlationId
                                                ? item.correlationId.substring(
                                                      0,
                                                      8,
                                                  ) + "..."
                                                : "-"}
                                        </td>
                                        <td className="p-4">
                                            <div className="flex items-center justify-center">
                                                <button
                                                    onClick={() =>
                                                        openDetailModal(item)
                                                    }
                                                    className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 bg-slate-50 dark:bg-[#0a0a0f] text-slate-400 hover:text-indigo-500 hover:bg-slate-100 transition-all cursor-pointer"
                                                >
                                                    <Eye className="w-4 h-4" />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>

                {/* PAGINATION */}
                <div className="p-4 border-t border-slate-200 dark:border-white/5 flex items-center justify-between text-xs text-slate-400 bg-slate-50/50 dark:bg-[#0a0a0f]/50">
                    <p>
                        Showing {logs.length} of {totalElements} logs
                    </p>
                    <div className="flex gap-2">
                        <button
                            disabled={page === 0 || loading}
                            onClick={() => setPage(page - 1)}
                            className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 disabled:opacity-50"
                        >
                            <ChevronLeft className="w-4 h-4" />
                        </button>
                        <span className="py-1 px-3 bg-slate-100 dark:bg-white/5 rounded-lg font-semibold text-slate-700 dark:text-white">
                            Page {page + 1} of {totalPages}
                        </span>
                        <button
                            disabled={page >= totalPages - 1 || loading}
                            onClick={() => setPage(page + 1)}
                            className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 disabled:opacity-50"
                        >
                            <ChevronRight className="w-4 h-4" />
                        </button>
                    </div>
                </div>
            </div>

            {/* DETAIL MODAL */}
            {isDetailModalOpen && selectedLog && (
                <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                    <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-lg p-6">
                        <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">
                            Audit Log Metadata
                        </h3>

                        <div className="space-y-4">
                            <div className="grid grid-cols-2 gap-4 text-xs">
                                <div>
                                    <span className="text-[10px] text-slate-400 uppercase tracking-wider block">
                                        Timestamp
                                    </span>
                                    <span className="font-semibold">
                                        {new Date(
                                            selectedLog.timestamp,
                                        ).toLocaleString()}
                                    </span>
                                </div>
                                <div>
                                    <span className="text-[10px] text-slate-400 uppercase tracking-wider block">
                                        Correlation ID
                                    </span>
                                    <span className="font-mono text-indigo-400">
                                        {selectedLog.correlationId || "None"}
                                    </span>
                                </div>
                                <div>
                                    <span className="text-[10px] text-slate-400 uppercase tracking-wider block">
                                        Action
                                    </span>
                                    <span className="font-semibold text-slate-800 dark:text-white">
                                        {selectedLog.action}
                                    </span>
                                </div>
                                <div>
                                    <span className="text-[10px] text-slate-400 uppercase tracking-wider block">
                                        Resource Type
                                    </span>
                                    <span className="font-semibold">
                                        {selectedLog.resourceType}
                                    </span>
                                </div>
                                <div>
                                    <span className="text-[10px] text-slate-400 uppercase tracking-wider block">
                                        User ID
                                    </span>
                                    <span className="font-mono">
                                        {selectedLog.userId || "None"}
                                    </span>
                                </div>
                                <div>
                                    <span className="text-[10px] text-slate-400 uppercase tracking-wider block">
                                        Username
                                    </span>
                                    <span className="font-semibold">
                                        {selectedLog.username}
                                    </span>
                                </div>
                            </div>

                            <div>
                                <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-1">
                                    Status
                                </span>
                                <span
                                    className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-bold uppercase border ${
                                        selectedLog.status === "SUCCESS"
                                            ? "bg-emerald-500/10 text-emerald-500 border-emerald-500/20"
                                            : "bg-red-500/10 text-red-500 border-red-500/20"
                                    }`}
                                >
                                    {selectedLog.status}
                                </span>
                            </div>

                            {selectedLog.errorMessage && (
                                <div>
                                    <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-1">
                                        Error Message
                                    </span>
                                    <p className="text-xs text-red-400 bg-red-500/5 p-3 rounded-xl border border-red-500/10">
                                        {selectedLog.errorMessage}
                                    </p>
                                </div>
                            )}
                        </div>

                        <div className="flex justify-end pt-6 border-t border-slate-200 dark:border-white/5 mt-6">
                            <button
                                onClick={() => setIsDetailModalOpen(false)}
                                className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-500 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs font-semibold"
                            >
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* CLEAR LOGS MODAL */}
            {isDeleteModalOpen && (
                <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                    <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-sm p-6">
                        <h3 className="text-lg font-bold dark:text-white mb-2 flex items-center gap-2 justify-center text-red-500">
                            <AlertTriangle className="w-5 h-5 animate-pulse" />{" "}
                            Bulk Clear System Logs
                        </h3>
                        <p className="text-xs text-slate-400 text-center mb-4">
                            Select the date range of logs to permanently delete
                            from the database.
                        </p>

                        <form onSubmit={handleBulkDelete} className="space-y-4">
                            <div>
                                <label className="block text-xs font-semibold text-slate-400 mb-1">
                                    Start Date *
                                </label>
                                <input
                                    type="date"
                                    required
                                    value={deleteStartDate}
                                    onChange={(e) =>
                                        setDeleteStartDate(e.target.value)
                                    }
                                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs"
                                />
                            </div>

                            <div>
                                <label className="block text-xs font-semibold text-slate-400 mb-1">
                                    End Date *
                                </label>
                                <input
                                    type="date"
                                    required
                                    value={deleteEndDate}
                                    onChange={(e) =>
                                        setDeleteEndDate(e.target.value)
                                    }
                                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs"
                                />
                            </div>

                            <div className="flex justify-end gap-3 pt-4 border-t border-slate-200 dark:border-white/5">
                                <button
                                    type="button"
                                    onClick={() => setIsDeleteModalOpen(false)}
                                    className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-500 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={
                                        loading ||
                                        !deleteStartDate ||
                                        !deleteEndDate
                                    }
                                    className="px-4 py-2 bg-red-600 hover:bg-red-505 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-red-500/10"
                                >
                                    {loading ? "Clearing..." : "Delete Logs"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}
