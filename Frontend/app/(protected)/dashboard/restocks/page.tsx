"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { restocksService } from "@/services/restocks";
import { productsService } from "@/services/products";
import { toast } from "sonner";
import { Plus, Trash2, Eye } from "lucide-react";
import { RestockFormModal } from "./_components/RestockFormModal";
import { DeleteRestockModal } from "./_components/DeleteRestockModal";
import { RestockViewModal } from "./_components/RestockViewModal";
import { Pagination } from "@/components/ui/Pagination";
import { RestockInput } from "./schema";
import { Restock, Product } from "@/types";

export default function RestocksPage() {
  const { isAdmin } = useAuth();

  // Data States
  const [restocks, setRestocks] = useState<Restock[]>([]);
  const [productMap, setProductMap] = useState<Record<string, string>>({});

  // Pagination & Filters
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Loading States
  const [loading, setLoading] = useState(true);
  const [modalLoading, setModalLoading] = useState(false);

  // Modal States
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);

  // Selected Restock
  const [selectedRestock, setSelectedRestock] = useState<Restock | null>(null);
  const [formError, setFormError] = useState("");

  // Fetch product names for table display map
  useEffect(() => {
    async function fetchProductNames() {
      try {
        let allContent: Product[] = [];
        let p = 0;
        let totalPgs = 1;
        while (p < totalPgs && p < 5) {
          const data = await productsService.getProducts({
            page: p,
            size: 20,
          });
          allContent = allContent.concat(data?.content || []);
          totalPgs = data?.totalPages || 1;
          p++;
        }
        const mapping: Record<string, string> = {};
        allContent.forEach((pr: Product) => {
          mapping[pr.id] = pr.name;
        });
        setProductMap(mapping);
      } catch {
        // non-critical
      }
    }
    fetchProductNames();
  }, []);

  // Fetch Restocks Logs
  const fetchRestocks = async () => {
    try {
      setLoading(true);
      const data = await restocksService.getRestocks(isAdmin, {
        page,
        size: pageSize,
        sortBy: "timestamp",
        sortDirection: "desc",
      });

      setRestocks(data?.content || []);
      setTotalPages(data?.totalPages || 1);
      setTotalElements(data?.totalElements || 0);
    } catch {
      toast.error("Failed to load restock records.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const load = async () => {
      await fetchRestocks();
    };
    load();
  }, [page, pageSize, isAdmin]); // eslint-disable-line react-hooks/exhaustive-deps

  // Log Restock Submit
  const handleAddRestock = async (data: RestockInput) => {
    setFormError("");
    setModalLoading(true);
    try {
      await restocksService.createRestock(data);
      setIsAddModalOpen(false);
      toast.success("Restock transaction recorded!");
      fetchRestocks();
    } catch (err: unknown) {
      const errorMsg = (err as { response?: { data?: { message?: string } } }).response?.data?.message || (err as Error).message || "Failed to record restock";
      toast.error(errorMsg);
      setFormError(errorMsg);
    } finally {
      setModalLoading(false);
    }
  };

  // Delete Restock Record
  const handleDeleteRestock = async () => {
    setModalLoading(true);
    try {
      if (!selectedRestock) return;
      await restocksService.deleteRestock(selectedRestock.id);
      setIsDeleteModalOpen(false);
      toast.success("Restock record deleted.");
      fetchRestocks();
    } catch (err: unknown) {
      toast.error((err as { response?: { data?: { message?: string } } }).response?.data?.message || (err as Error).message || "Failed to delete restock record");
    } finally {
      setModalLoading(false);
    }
  };

  const openAddModal = () => {
    setFormError("");
    setIsAddModalOpen(true);
  };

  const openDeleteModal = (restock: Restock) => {
    setSelectedRestock(restock);
    setIsDeleteModalOpen(true);
  };

  const openViewModal = (restock: Restock) => {
    setSelectedRestock(restock);
    setIsViewModalOpen(true);
  };

  return (
    <div className="space-y-6">
      {/* HEADER */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold dark:text-white">Restocks Logs</h1>
          <p className="text-xs text-slate-400">View and record all inward incoming restocks for catalog products</p>
        </div>
        <button
          onClick={openAddModal}
          className="inline-flex items-center gap-1.5 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold rounded-xl transition-all shadow-md shadow-indigo-500/10 cursor-pointer"
        >
          <Plus className="w-4 h-4" /> Log Restock
        </button>
      </div>

      {/* RESTOCKS TABLE */}
      <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm border-collapse">
            <thead>
              <tr className="border-b border-slate-200 dark:border-white/5 bg-slate-50/50 dark:bg-[#0a0a0f]/50 text-slate-400 text-xs font-semibold">
                <th className="p-4">Transaction ID</th>
                <th className="p-4">Product Name</th>
                <th className="p-4">Quantity Added</th>
                <th className="p-4">Date Logged</th>
                <th className="p-4 text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-white/5">
              {loading ? (
                Array.from({ length: 3 }).map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    <td colSpan={5} className="p-4 h-12 bg-slate-50/10 dark:bg-[#0e0e13]/10" />
                  </tr>
                ))
              ) : restocks.length === 0 ? (
                <tr>
                  <td colSpan={5} className="p-8 text-center text-slate-400">
                    No restocks logged yet.
                  </td>
                </tr>
              ) : (
                restocks.map((restock) => (
                  <tr
                    key={restock.id}
                    onClick={() => openViewModal(restock)}
                    className="hover:bg-slate-50/50 dark:hover:bg-[#1c1c24]/10 text-slate-650 dark:text-slate-300 cursor-pointer"
                  >
                    <td className="p-4 font-mono text-xs text-indigo-400">
                      {restock.id.substring(0, 8)}...
                    </td>
                    <td className="p-4 font-semibold text-slate-800 dark:text-white">
                      {productMap[restock.productId] || restock.productName || "Unknown"}
                    </td>
                    <td className="p-4 font-bold text-emerald-500">
                      +{restock.quantity} units
                    </td>
                    <td className="p-4 text-xs">
                      {new Date(
                        restock.timestamp || restock.restockDate || restock.createdAt || ""
                      ).toLocaleString()}
                    </td>
                    <td className="p-4" onClick={(e) => e.stopPropagation()}>
                      <div className="flex items-center justify-center gap-1.5">
                        <button
                          onClick={() => openViewModal(restock)}
                          title="View Details"
                          className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 bg-slate-50 dark:bg-[#0a0a0f] text-slate-400 hover:text-indigo-500 hover:bg-slate-100 transition-colors cursor-pointer"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                        {isAdmin && (
                          <button
                            onClick={() => openDeleteModal(restock)}
                            title="Delete Transaction Record"
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
        <Pagination
          page={page}
          pageSize={pageSize}
          totalPages={totalPages}
          totalElements={totalElements}
          onPageChange={setPage}
          loading={loading}
          itemName="restocks records"
        />
      </div>

      {/* CREATE RESTOCK MODAL */}
      <RestockFormModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onSubmit={handleAddRestock}
        loading={modalLoading}
        formError={formError}
      />

      {/* DELETE CONFIRMATION */}
      <DeleteRestockModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        onConfirm={handleDeleteRestock}
        restockId={selectedRestock?.id || ""}
        loading={modalLoading}
      />

      {/* RESTOCK DETAIL VIEW MODAL */}
      <RestockViewModal
        isOpen={isViewModalOpen}
        onClose={() => setIsViewModalOpen(false)}
        restock={selectedRestock}
        productMap={productMap}
      />
    </div>
  );
}
