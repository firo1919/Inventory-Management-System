"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { salesService } from "@/services/sales";
import { productsService } from "@/services/products";
import { toast } from "sonner";
import { Plus, Eye, Trash2 } from "lucide-react";
import { SaleFormModal } from "./_components/SaleFormModal";
import { DeleteSaleModal } from "./_components/DeleteSaleModal";
import { SaleViewModal } from "./_components/SaleViewModal";
import { Pagination } from "@/components/ui/Pagination";
import { SaleInput } from "./schema";

export default function SalesPage() {
  const { isAdmin } = useAuth();

  // Data States
  const [sales, setSales] = useState<any[]>([]);
  const [productMap, setProductMap] = useState<Record<string, string>>({});

  // Pagination & Filters
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Loading States
  const [loading, setLoading] = useState(true);
  const [modalLoading, setModalLoading] = useState(false);

  // Modal States
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);

  // Selected Sale
  const [selectedSale, setSelectedSale] = useState<any>(null);
  const [formError, setFormError] = useState("");

  // Fetch all products for productMap (table name lookups) — small page of names
  useEffect(() => {
    async function fetchProductNames() {
      try {
        let allContent: any[] = [];
        let p = 0;
        let totalPgs = 1;
        while (p < totalPgs && p < 5) {
          // max 5 pages = 100 products for the map
          const data = await productsService.getProducts({
            page: p,
            size: 20,
          });
          allContent = allContent.concat(data?.content || []);
          totalPgs = data?.totalPages || 1;
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

  // Fetch Sales Log
  const fetchSales = async () => {
    try {
      setLoading(true);
      const data = await salesService.getSales(isAdmin, {
        page,
        size: pageSize,
        sortBy: "timestamp",
        sortDirection: "desc",
      });

      setSales(data?.content || []);
      setTotalPages(data?.totalPages || 1);
      setTotalElements(data?.totalElements || 0);
    } catch {
      toast.error("Failed to fetch sales.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSales();
  }, [page, pageSize, isAdmin]);

  // Add Sale Submit
  const handleAddSale = async (data: SaleInput) => {
    setFormError("");
    setModalLoading(true);
    try {
      await salesService.createSale(data);
      setIsAddModalOpen(false);
      toast.success("Sale recorded successfully!");
      fetchSales();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to record sale");
      setFormError(err.response?.data?.message || err.message || "Failed to record sale");
    } finally {
      setModalLoading(false);
    }
  };

  // Delete Sale Submit
  const handleDeleteSale = async () => {
    setModalLoading(true);
    try {
      await salesService.deleteSale(selectedSale.id);
      setIsDeleteModalOpen(false);
      toast.success("Sale record deleted.");
      fetchSales();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to delete sale record");
    } finally {
      setModalLoading(false);
    }
  };

  const openAddModal = () => {
    setFormError("");
    setIsAddModalOpen(true);
  };

  const openDeleteModal = (sale: any) => {
    setSelectedSale(sale);
    setIsDeleteModalOpen(true);
  };

  const openViewModal = (sale: any) => {
    setSelectedSale(sale);
    setIsViewModalOpen(true);
  };

  return (
    <div className="space-y-6">
      {/* HEADER */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold dark:text-white">Sales Logs</h1>
          <p className="text-xs text-slate-400">View and record all outward customer sales transactions</p>
        </div>
        <button
          onClick={openAddModal}
          className="inline-flex items-center gap-1.5 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold rounded-xl transition-all shadow-md shadow-indigo-500/10 cursor-pointer"
        >
          <Plus className="w-4 h-4" /> Record New Sale
        </button>
      </div>

      {/* SALES TABLE */}
      <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm border-collapse">
            <thead>
              <tr className="border-b border-slate-200 dark:border-white/5 bg-slate-50/50 dark:bg-[#0a0a0f]/50 text-slate-400 text-xs font-semibold">
                <th className="p-4">Transaction ID</th>
                <th className="p-4">Product</th>
                <th className="p-4 text-center">Qty Sold</th>
                <th className="p-4">Price / Revenue</th>
                <th className="p-4">Logged At</th>
                <th className="p-4 text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-white/5">
              {loading ? (
                Array.from({ length: 3 }).map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    <td colSpan={6} className="p-4 h-12 bg-slate-50/10 dark:bg-[#0e0e13]/10" />
                  </tr>
                ))
              ) : sales.length === 0 ? (
                <tr>
                  <td colSpan={6} className="p-8 text-center text-slate-400">
                    No sales recorded yet.
                  </td>
                </tr>
              ) : (
                sales.map((sale) => (
                  <tr
                    key={sale.id}
                    onClick={() => openViewModal(sale)}
                    className="hover:bg-slate-50/50 dark:hover:bg-[#1c1c24]/10 text-slate-600 dark:text-slate-300 cursor-pointer"
                  >
                    <td className="p-4 font-mono text-xs text-indigo-400">
                      {sale.id.substring(0, 8)}...
                    </td>
                    <td className="p-4 font-semibold text-slate-800 dark:text-white">
                      {productMap[sale.productId] || sale.productName || "Unknown"}
                    </td>
                    <td className="p-4 text-center font-bold text-slate-800 dark:text-white">
                      {sale.quantity}
                    </td>
                    <td className="p-4">
                      <div className="flex flex-col">
                        <span className="font-bold text-xs text-emerald-500">
                          ${(sale.quantity * (sale.salePrice || 0)).toFixed(2)}
                        </span>
                        <span className="text-[9px] text-slate-400">
                          Unit: ${sale.salePrice?.toFixed(2)}
                        </span>
                      </div>
                    </td>
                    <td className="p-4 text-xs">
                      {new Date(
                        sale.timestamp || sale.saleDate || sale.createdAt
                      ).toLocaleString()}
                    </td>
                    <td className="p-4" onClick={(e) => e.stopPropagation()}>
                      <div className="flex items-center justify-center gap-1.5">
                        <button
                          onClick={() => openViewModal(sale)}
                          title="View Details"
                          className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 bg-slate-50 dark:bg-[#0a0a0f] text-slate-400 hover:text-indigo-500 hover:bg-slate-100 transition-colors cursor-pointer"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                        {isAdmin && (
                          <button
                            onClick={() => openDeleteModal(sale)}
                            title="Delete Record"
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
          itemName="sales transactions"
        />
      </div>

      {/* RECORD SALE MODAL */}
      <SaleFormModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onSubmit={handleAddSale}
        loading={modalLoading}
        formError={formError}
      />

      {/* DELETE CONFIRMATION */}
      <DeleteSaleModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        onConfirm={handleDeleteSale}
        saleId={selectedSale?.id || ""}
        loading={modalLoading}
      />

      {/* SALE DETAIL VIEW MODAL */}
      <SaleViewModal
        isOpen={isViewModalOpen}
        onClose={() => setIsViewModalOpen(false)}
        sale={selectedSale}
        productMap={productMap}
      />
    </div>
  );
}
