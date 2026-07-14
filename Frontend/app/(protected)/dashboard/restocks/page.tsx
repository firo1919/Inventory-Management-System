"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { apiClient } from "@/lib/api-client";
import { toast } from "sonner";
import SearchableSelect from "@/components/ui/SearchableSelect";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import {
    Plus,
    ChevronLeft,
    ChevronRight,
    Trash2,
    AlertTriangle,
    Loader2,
    Eye,
} from "lucide-react";

const restockSchema = z.object({
    productId: z.string().min(1, "Product selection is required"),
    quantity: z
        .number()
        .int()
        .positive("Quantity must be a positive integer greater than zero"),
});

type RestockInput = z.infer<typeof restockSchema>;

export default function RestocksPage() {
    const { isAdmin } = useAuth();

    // Data States
    const [restocks, setRestocks] = useState<any[]>([]);
    const [productMap, setProductMap] = useState<Record<string, string>>({});

    // Pagination & Filters
    const [page, setPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    // Loading States
    const [loading, setLoading] = useState(true);

    // Modal States
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [isViewModalOpen, setIsViewModalOpen] = useState(false);

    // Selected Restock
    const [selectedRestock, setSelectedRestock] = useState<any>(null);

    const [formError, setFormError] = useState("");
    const [selectedProductDetails, setSelectedProductDetails] =
        useState<any>(null);

    // React Hook Form
    const {
        register,
        handleSubmit,
        setValue,
        watch,
        reset,
        formState: { errors },
    } = useForm<RestockInput>({
        resolver: zodResolver(restockSchema),
        defaultValues: {
            productId: "",
            quantity: 1,
        },
    });

    // Fetch product names for table display map
    useEffect(() => {
        async function fetchProductNames() {
            try {
                let allContent: any[] = [];
                let p = 0;
                let totalPgs = 1;
                while (p < totalPgs && p < 5) {
                    const res = await apiClient.get(
                        `/api/v1/products?page=${p}&size=20`,
                    );
                    allContent = allContent.concat(res.data?.content || []);
                    totalPgs = res.data?.totalPages || 1;
                    p++;
                }
                const mapping: Record<string, string> = {};
                allContent.forEach((pr: any) => {
                    mapping[pr.id] = pr.name;
                });
                setProductMap(mapping);
            } catch {
                // non-critical
            }
        }
        fetchProductNames();
    }, []);

    // Fetch Restocks Log
    const fetchRestocks = async (showToast = false) => {
        try {
            setLoading(true);
            const endpoint = isAdmin
                ? "/api/v1/admin/restocks"
                : "/api/v1/employee/restocks";
            const res = await apiClient.get(endpoint, {
                params: {
                    page,
                    size: pageSize,
                    sortBy: "timestamp",
                    sortDirection: "desc",
                },
            });

            setRestocks(res.data?.content || []);
            setTotalPages(res.data?.totalPages || 1);
            setTotalElements(res.data?.totalElements || 0);
        } catch {
            toast.error("Failed to fetch restocks.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchRestocks();
    }, [page, pageSize, isAdmin]);

    // Update selected product details from SearchableSelect raw data
    const handleProductSelect = (productId: string, rawProduct?: any) => {
        setValue("productId", productId);
        setSelectedProductDetails(rawProduct || null);
    };

    // Add Restock Submit
    const handleAddRestock = async (data: RestockInput) => {
        setFormError("");
        setLoading(true);
        try {
            await apiClient.post("/api/v1/restocks", data);
            setIsAddModalOpen(false);
            reset();
            setSelectedProductDetails(null);
            toast.success("Restock recorded successfully!");
            fetchRestocks();
        } catch (err: any) {
            toast.error(
                err.response?.data?.message ||
                    err.message ||
                    "Failed to record restock",
            );
            setFormError(
                err.response?.data?.message ||
                    err.message ||
                    "Failed to record restock",
            );
        } finally {
            setLoading(false);
        }
    };

    // Delete Restock Submit
    const handleDeleteRestock = async () => {
        setLoading(true);
        try {
            await apiClient.delete(
                `/api/v1/admin/restocks/${selectedRestock.id}`,
            );
            setIsDeleteModalOpen(false);
            toast.success("Restock record deleted.");
            fetchRestocks();
        } catch (err: any) {
            toast.error(
                err.response?.data?.message ||
                    err.message ||
                    "Failed to delete restock record",
            );
        } finally {
            setLoading(false);
        }
    };

    const openAddModal = () => {
        reset({
            productId: "",
            quantity: 1,
        });
        setFormError("");
        setSelectedProductDetails(null);
        setIsAddModalOpen(true);
    };

    const openDeleteModal = (restock: any) => {
        setSelectedRestock(restock);
        setIsDeleteModalOpen(true);
    };

    const openViewModal = (restock: any) => {
        setSelectedRestock(restock);
        setIsViewModalOpen(true);
    };

    return (
        <div className="space-y-6">
            {/* HEADER */}
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                <div>
                    <h1 className="text-xl font-bold dark:text-white">
                        Restocks Logs
                    </h1>
                    <p className="text-xs text-slate-400">
                        {isAdmin
                            ? "Audit all inbound stock logs across the system"
                            : "Log and view your product restock entries"}
                    </p>
                </div>
                <button
                    onClick={openAddModal}
                    className="inline-flex items-center gap-1.5 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-505 text-white text-xs font-semibold rounded-xl transition-all shadow-md shadow-indigo-500/10 cursor-pointer"
                >
                    <Plus className="w-4 h-4" /> Log A Restock
                </button>
            </div>

            {/* RESTOCKS LOG TABLE */}
            <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl shadow-sm overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left text-sm border-collapse">
                        <thead>
                            <tr className="border-b border-slate-200 dark:border-white/5 bg-slate-50/50 dark:bg-[#0a0a0f]/50 text-slate-400 text-xs font-semibold">
                                <th className="p-4">Transaction ID</th>
                                <th className="p-4">Product Name</th>
                                <th className="p-4 text-right">
                                    Quantity Restocked
                                </th>
                                <th className="p-4">Date Logged</th>
                                <th className="p-4 text-center">Actions</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 dark:divide-white/5">
                            {loading ? (
                                Array.from({ length: 5 }).map((_, i) => (
                                    <tr key={i} className="animate-pulse">
                                        <td
                                            colSpan={5}
                                            className="p-4 h-12 bg-slate-50/10 dark:bg-[#0e0e13]/10"
                                        />
                                    </tr>
                                ))
                            ) : restocks.length === 0 ? (
                                <tr>
                                    <td
                                        colSpan={5}
                                        className="p-8 text-center text-slate-400"
                                    >
                                        No restocks recorded yet.
                                    </td>
                                </tr>
                            ) : (
                                restocks.map((r) => (
                                    <tr
                                        key={r.id}
                                        onClick={() => openViewModal(r)}
                                        className="hover:bg-slate-50/50 dark:hover:bg-[#1c1c24]/10 text-slate-600 dark:text-slate-300 cursor-pointer"
                                    >
                                        <td className="p-4 font-mono text-xs text-indigo-400">
                                            {r.id.substring(0, 8)}...
                                        </td>
                                        <td className="p-4 font-semibold text-slate-800 dark:text-white">
                                            {productMap[r.productId] ||
                                                r.productName ||
                                                "Unknown Product"}
                                        </td>
                                        <td className="p-4 text-right font-medium text-emerald-500">
                                            +{r.quantity}
                                        </td>
                                        <td className="p-4 text-xs text-slate-400">
                                            {new Date(
                                                r.timestamp || r.restockDate,
                                            ).toLocaleString(undefined, {
                                                month: "short",
                                                day: "numeric",
                                                year: "numeric",
                                                hour: "2-digit",
                                                minute: "2-digit",
                                            })}
                                        </td>
                                        <td
                                            className="p-4"
                                            onClick={(e) => e.stopPropagation()}
                                        >
                                            <div className="flex items-center justify-center gap-1.5">
                                                <button
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        openViewModal(r);
                                                    }}
                                                    title="View Details"
                                                    className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 bg-slate-50 dark:bg-[#0a0a0f] text-slate-400 hover:text-indigo-500 hover:bg-slate-100 transition-colors cursor-pointer"
                                                >
                                                    <Eye className="w-4 h-4" />
                                                </button>
                                                {isAdmin && (
                                                    <button
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            openDeleteModal(r);
                                                        }}
                                                        title="Delete record"
                                                        className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 bg-slate-50 dark:bg-[#0a0a0f] text-slate-400 hover:text-red-500 hover:bg-slate-100 transition-colors cursor-pointer"
                                                    >
                                                        <Trash2 className="w-4 h-4" />
                                                    </button>
                                                )}
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
                        Showing {restocks.length} of {totalElements} restock
                        logs
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

            {/* CREATE RESTOCK MODAL */}
            {isAddModalOpen && (
                <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                    <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-md p-6 overflow-y-auto max-h-[90vh]">
                        <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">
                            Log Product Restock
                        </h3>
                        <form
                            onSubmit={handleSubmit(handleAddRestock)}
                            className="space-y-4"
                        >
                            {formError && (
                                <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-xs p-3 rounded-lg flex items-center gap-1.5">
                                    <AlertTriangle className="w-4 h-4" />
                                    <span>{formError}</span>
                                </div>
                            )}

                            <div>
                                <SearchableSelect
                                    endpoint="/api/v1/products"
                                    mapItem={(p: any) => ({
                                        value: p.id,
                                        label: p.name,
                                        subLabel: `${p.quantity} currently in stock · cost $${p.costPrice?.toFixed(2)}`,
                                        disabled: !p.active,
                                        raw: p,
                                    })}
                                    value={watch("productId")}
                                    onChange={handleProductSelect}
                                    placeholder="Search & select product..."
                                    label="Select Product"
                                    required
                                    pageSize={20}
                                />
                                {errors.productId && (
                                    <p className="text-[10px] text-red-400 mt-1">
                                        {errors.productId.message}
                                    </p>
                                )}
                            </div>

                            {selectedProductDetails && (
                                <div className="p-3 bg-indigo-500/5 border border-indigo-500/10 rounded-xl text-[11px] space-y-1 text-indigo-400">
                                    <p>
                                        • In Inventory:{" "}
                                        {selectedProductDetails.quantity} items
                                    </p>
                                    <p>
                                        • Cost Price: $
                                        {selectedProductDetails.costPrice?.toFixed(
                                            2,
                                        )}
                                    </p>
                                </div>
                            )}

                            <div>
                                <label className="block text-xs font-semibold text-slate-400 mb-1">
                                    Quantity Restocked *
                                </label>
                                <input
                                    type="number"
                                    {...register("quantity", {
                                        valueAsNumber: true,
                                    })}
                                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                                />
                                {errors.quantity && (
                                    <p className="text-[10px] text-red-400 mt-1">
                                        {errors.quantity.message}
                                    </p>
                                )}
                            </div>

                            <div className="flex justify-end gap-3 pt-4 border-t border-slate-200 dark:border-white/5">
                                <button
                                    type="button"
                                    onClick={() => setIsAddModalOpen(false)}
                                    className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-505 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={loading}
                                    className="px-4 py-2 bg-indigo-600 hover:bg-indigo-505 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-indigo-500/10 flex items-center gap-1.5 cursor-pointer"
                                >
                                    {loading ? (
                                        <>
                                            <Loader2 className="w-3.5 h-3.5 animate-spin" />{" "}
                                            Logging...
                                        </>
                                    ) : (
                                        "Record Restock"
                                    )}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* DELETE CONFIRMATION */}
            {isDeleteModalOpen && selectedRestock && (
                <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                    <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-sm p-6 text-center">
                        <AlertTriangle className="w-12 h-12 text-red-500 mx-auto mb-4" />
                        <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-2">
                            Delete Transaction Record
                        </h3>
                        <p className="text-xs text-slate-400 mb-6 font-medium">
                            Are you sure you want to delete restock transaction
                            ID:{" "}
                            <span className="font-mono text-indigo-400">
                                {selectedRestock.id.substring(0, 8)}...
                            </span>
                            ? This deletes the log only and will not roll back
                            inventory quantities automatically.
                        </p>
                        <div className="flex justify-center gap-3">
                            <button
                                onClick={() => setIsDeleteModalOpen(false)}
                                className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-505 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs"
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleDeleteRestock}
                                disabled={loading}
                                className="px-4 py-2 bg-red-600 hover:bg-red-500 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-red-500/10 cursor-pointer"
                            >
                                {loading ? "Deleting..." : "Delete Log"}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* RESTOCK DETAIL VIEW MODAL */}
            {isViewModalOpen && selectedRestock && (
                <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                    <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-md p-6">
                        <div className="flex items-center justify-between mb-5">
                            <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                                Restock Details
                            </h3>
                            <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase bg-indigo-500/10 text-indigo-450 border border-indigo-500/20">
                                RESTOCK
                            </span>
                        </div>

                        <div className="grid grid-cols-2 gap-4 text-xs mb-4">
                            <div className="col-span-2">
                                <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">
                                    Transaction ID
                                </span>
                                <span className="font-mono text-indigo-400">
                                    {selectedRestock.id}
                                </span>
                            </div>
                            <div>
                                <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">
                                    Product
                                </span>
                                <span className="font-semibold text-slate-800 dark:text-white">
                                    {productMap[selectedRestock.productId] ||
                                        selectedRestock.productName ||
                                        "Unknown"}
                                </span>
                            </div>
                            <div>
                                <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">
                                    Quantity Added
                                </span>
                                <span className="font-bold text-emerald-500 text-lg">
                                    +{selectedRestock.quantity}
                                </span>
                            </div>
                            <div className="col-span-2">
                                <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">
                                    Date Logged
                                </span>
                                <span>
                                    {new Date(
                                        selectedRestock.timestamp ||
                                            selectedRestock.restockDate ||
                                            selectedRestock.createdAt,
                                    ).toLocaleString()}
                                </span>
                            </div>
                            {selectedRestock.employeeName && (
                                <div>
                                    <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">
                                        Logged By
                                    </span>
                                    <span className="font-semibold">
                                        {selectedRestock.employeeName}
                                    </span>
                                </div>
                            )}
                        </div>

                        <div className="flex justify-end pt-4 border-t border-slate-200 dark:border-white/5">
                            <button
                                onClick={() => setIsViewModalOpen(false)}
                                className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-505 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs font-semibold"
                            >
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
