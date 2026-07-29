"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { auditLogsService, AuditLogParams } from "@/services/audit-logs";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import {
    Trash2,
    Download,
    Filter,
    Eye,
    Activity,
    CheckCircle,
    XCircle,
    Clock,
} from "lucide-react";
import { AuditLogDetailModal } from "./_components/AuditLogDetailModal";
import { BulkClearLogsModal } from "./_components/BulkClearLogsModal";
import { Pagination } from "@/components/ui/Pagination";
import { Loader } from "@/components/ui/Loader";
import { AuditLog } from "@/types";

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
    const [logs, setLogs] = useState<AuditLog[]>([]);
    const [stats, setStats] = useState<Record<string, number> | null>(null);

    // Pagination & Filters
    const [page, setPage] = useState(0);
    const [pageSize] = useState(20);
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
    const [clearLoading, setClearLoading] = useState(false);

    // Selected Log for details modal
    const [selectedLog, setSelectedLog] = useState<AuditLog | null>(null);
    const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);

    // Bulk Delete state
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

    const fetchLogs = async () => {
        if (!isAdmin) return;
        try {
            setLoading(true);

            const params: AuditLogParams = {
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

            const data = await auditLogsService.getAuditLogs(params);

            setLogs(data?.content || []);
            setTotalPages(data?.totalPages || 1);
            setTotalElements(data?.totalElements || 0);
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
            const data = await auditLogsService.getAuditStatistics();
            setStats(data);
        } catch {
            toast.error("Failed to load audit statistics.");
        } finally {
            setStatsLoading(false);
        }
    };

    useEffect(() => {
        const load = async () => {
            await fetchLogs();
            await fetchStats();
        };
        load();
    }, [page, pageSize, isAdmin]); // eslint-disable-line react-hooks/exhaustive-deps

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
        setTimeout(() => {
            fetchLogs();
        }, 0);
    };

    // Export File logic (CSV / JSON)
    const handleExport = async (format: "csv" | "json") => {
        setExporting(true);
        try {
            const filterPayload: Record<string, unknown> = {
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

            const data = await auditLogsService.exportAuditLogs(
                format,
                filterPayload,
            );

            // Create browser download link
            const blob = new Blob([data], {
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
    const handleBulkDelete = async (start: string, end: string) => {
        setClearLoading(true);
        try {
            await auditLogsService.deleteAuditLogs({
                startDate: `${start}T00:00:00`,
                endDate: `${end}T23:59:59`,
            });

            setIsDeleteModalOpen(false);
            toast.success("Audit logs cleared successfully.");
            fetchLogs();
            fetchStats();
        } catch (err: unknown) {
            toast.error(
                (err as { response?: { data?: { message?: string } } }).response
                    ?.data?.message ||
                    (err as Error).message ||
                    "Failed to clear logs",
            );
        } finally {
            setClearLoading(false);
        }
    };

    const openDetailModal = (logItem: AuditLog) => {
        setSelectedLog(logItem);
        setIsDetailModalOpen(true);
    };

    if (authLoading || !isAdmin) {
        return (
            <div className="flex flex-col items-center justify-center min-h-[50vh] text-slate-400">
                <Loader text="Verifying Admin Permissions..." />
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
                        onClick={() => {
                            console.log("Clear Logs button clicked!");
                            setIsDeleteModalOpen(true);
                        }}
                        className="inline-flex items-center gap-1.5 px-3 py-2 bg-red-600/10 hover:bg-red-600/20 text-red-500 text-xs font-semibold rounded-xl transition-all border border-red-500/10 cursor-pointer"
                    >
                        <Trash2 className="w-4 h-4" /> Clear Logs
                    </button>
                    <button
                        disabled={exporting}
                        onClick={() => handleExport("csv")}
                        className="inline-flex items-center gap-1.5 px-3 py-2 bg-white/5 hover:bg-white/10 text-slate-700 dark:text-white text-xs font-semibold rounded-xl border border-slate-200 dark:border-white/10 transition-all cursor-pointer disabled:opacity-50"
                    >
                        <Download className="w-4 h-4" /> Export CSV
                    </button>
                    <button
                        disabled={exporting}
                        onClick={() => handleExport("json")}
                        className="inline-flex items-center gap-1.5 px-3 py-2 bg-white/5 hover:bg-white/10 text-slate-700 dark:text-white text-xs font-semibold rounded-xl border border-slate-200 dark:border-white/10 transition-all cursor-pointer disabled:opacity-50"
                    >
                        <Download className="w-4 h-4" /> Export JSON
                    </button>
                </div>
            </div>

            {/* STATS OVERVIEW */}
            {!statsLoading && stats && (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 animate-fade-in">
                    <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-4 flex items-center gap-4">
                        <div className="w-10 h-10 bg-indigo-500/10 text-indigo-500 rounded-xl flex items-center justify-center">
                            <Activity className="w-5 h-5" />
                        </div>
                        <div>
                            <p className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider">
                                Total Actions
                            </p>
                            <h4 className="text-lg font-bold text-slate-800 dark:text-white mt-0.5">
                                {stats.totalLogs || 0}
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
                            className="w-full px-3 py-1.5 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white"
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
                            className="w-full px-3 py-1.5 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white"
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
                            className="w-full px-3 py-1.5 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white"
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
                            className="w-full px-3 py-1.5 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white font-semibold"
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
                            className="w-full px-3 py-1.5 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white"
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
                            className="w-full px-3 py-1.5 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white"
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
                            className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-xs font-semibold shadow-md shadow-indigo-500/10 cursor-pointer"
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
                                                item.timestamp || "",
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
                <Pagination
                    page={page}
                    pageSize={pageSize}
                    totalPages={totalPages}
                    totalElements={totalElements}
                    onPageChange={setPage}
                    loading={loading}
                    itemName="logs"
                />
            </div>

            {/* DETAIL MODAL */}
            <AuditLogDetailModal
                isOpen={isDetailModalOpen}
                onClose={() => setIsDetailModalOpen(false)}
                log={selectedLog}
            />

            {/* CLEAR LOGS MODAL */}
            <BulkClearLogsModal
                isOpen={isDeleteModalOpen}
                onClose={() => setIsDeleteModalOpen(false)}
                onConfirm={handleBulkDelete}
                loading={clearLoading}
            />
        </div>
    );
}
