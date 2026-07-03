"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { apiClient } from "@/lib/api-client";
import {
  Plus,
  ChevronLeft,
  ChevronRight,
  Trash2,
  AlertTriangle,
  Loader2,
  Calendar,
  DollarSign,
  TrendingUp,
} from "lucide-react";

export default function SalesPage() {
  const { isAdmin } = useAuth();
  
  // Data States
  const [sales, setSales] = useState<any[]>([]);
  const [products, setProducts] = useState<any[]>([]);
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
  
  // Selected Sale for Delete
  const [selectedSale, setSelectedSale] = useState<any>(null);

  // Form State
  const [form, setForm] = useState({
    productId: "",
    quantity: "",
    salePrice: "",
  });
  const [formError, setFormError] = useState("");
  const [selectedProductDetails, setSelectedProductDetails] = useState<any>(null);

  // Fetch all products (for mapping and select list)
  useEffect(() => {
    async function fetchProducts() {
      try {
        const res = await apiClient.get("/api/v1/products?page=0&size=100");
        const content = res.data?.content || [];
        setProducts(content);
        
        const mapping: Record<string, string> = {};
        content.forEach((p: any) => {
          mapping[p.id] = p.name;
        });
        setProductMap(mapping);
      } catch (err) {
        console.error("Failed to load products:", err);
      }
    }
    fetchProducts();
  }, []);

  // Fetch Sales Log
  const fetchSales = async () => {
    try {
      setLoading(true);
      const endpoint = isAdmin ? "/api/v1/admin/sales" : "/api/v1/employee/sales";
      const res = await apiClient.get(endpoint, {
        params: {
          page,
          size: pageSize,
          sortBy: "timestamp",
          sortDirection: "desc",
        },
      });

      setSales(res.data?.content || []);
      setTotalPages(res.data?.totalPages || 1);
      setTotalElements(res.data?.totalElements || 0);
    } catch (err) {
      console.error("Failed to fetch sales:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSales();
  }, [page, pageSize, isAdmin]);

  // Update selected product price/limits in form
  useEffect(() => {
    if (form.productId) {
      const prod = products.find((p) => p.id === form.productId);
      setSelectedProductDetails(prod || null);
      if (prod) {
        setForm((prev) => ({ ...prev, salePrice: prod.sellingPrice.toString() }));
      }
    } else {
      setSelectedProductDetails(null);
    }
  }, [form.productId, products]);

  // Add Sale Submit
  const handleAddSale = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError("");

    const quantityNum = parseInt(form.quantity);
    if (selectedProductDetails && quantityNum > selectedProductDetails.quantity) {
      setFormError(`Insufficient stock! Only ${selectedProductDetails.quantity} items left in inventory.`);
      return;
    }

    setLoading(true);
    try {
      const payload = {
        productId: form.productId,
        quantity: quantityNum,
        salePrice: parseFloat(form.salePrice),
      };

      await apiClient.post("/api/v1/sales", payload);
      setIsAddModalOpen(false);
      resetForm();
      fetchSales();
    } catch (err: any) {
      setFormError(err.response?.data?.message || err.message || "Failed to record sale");
    } finally {
      setLoading(false);
    }
  };

  // Delete Sale Submit
  const handleDeleteSale = async () => {
    setLoading(true);
    try {
      await apiClient.delete(`/api/v1/admin/sales/${selectedSale.id}`);
      setIsDeleteModalOpen(false);
      fetchSales();
    } catch (err: any) {
      alert(err.response?.data?.message || err.message || "Failed to delete sale record");
    } finally {
      setLoading(false);
    }
  };

  const openAddModal = () => {
    resetForm();
    setIsAddModalOpen(true);
  };

  const openDeleteModal = (sale: any) => {
    setSelectedSale(sale);
    setIsDeleteModalOpen(true);
  };

  const resetForm = () => {
    setForm({
      productId: "",
      quantity: "",
      salePrice: "",
    });
    setFormError("");
    setSelectedProductDetails(null);
  };

  return (
    <div className="space-y-6">
      {/* HEADER */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold dark:text-white">Sales Logs</h1>
          <p className="text-xs text-slate-400">
            {isAdmin ? "Audit all transactions logged across the system" : "Log and view your sales transactions"}
          </p>
        </div>
        <button
          onClick={openAddModal}
          className="inline-flex items-center gap-1.5 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-505 text-white text-xs font-semibold rounded-xl transition-all shadow-md shadow-indigo-500/10 cursor-pointer"
        >
          <Plus className="w-4 h-4" /> Log A Sale
        </button>
      </div>

      {/* SALES LOG TABLE */}
      <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm border-collapse">
            <thead>
              <tr className="border-b border-slate-200 dark:border-white/5 bg-slate-50/50 dark:bg-[#0a0a0f]/50 text-slate-400 text-xs font-semibold">
                <th className="p-4">Transaction ID</th>
                <th className="p-4">Product Name</th>
                <th className="p-4 text-right">Quantity</th>
                <th className="p-4 text-right">Unit Price</th>
                <th className="p-4 text-right">Total Price</th>
                <th className="p-4">Date Logged</th>
                {isAdmin && <th className="p-4 text-center">Actions</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-white/5">
              {loading ? (
                Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    <td colSpan={isAdmin ? 7 : 6} className="p-4 h-12 bg-slate-50/10 dark:bg-[#0e0e13]/10" />
                  </tr>
                ))
              ) : sales.length === 0 ? (
                <tr>
                  <td colSpan={isAdmin ? 7 : 6} className="p-8 text-center text-slate-400">
                    No sales recorded yet.
                  </td>
                </tr>
              ) : (
                sales.map((s) => {
                  const totalPrice = s.quantity * (s.salePrice || 0);
                  return (
                    <tr key={s.id} className="hover:bg-slate-50/50 dark:hover:bg-[#1c1c24]/10 text-slate-600 dark:text-slate-300">
                      <td className="p-4 font-mono text-xs text-indigo-400">{s.id.substring(0, 8)}...</td>
                      <td className="p-4 font-semibold text-slate-800 dark:text-white">
                        {productMap[s.productId] || "Unknown Product"}
                      </td>
                      <td className="p-4 text-right font-medium">{s.quantity}</td>
                      <td className="p-4 text-right font-medium">${s.salePrice?.toFixed(2)}</td>
                      <td className="p-4 text-right font-bold text-slate-900 dark:text-white">
                        ${totalPrice.toFixed(2)}
                      </td>
                      <td className="p-4 text-xs text-slate-400">
                        {new Date(s.timestamp || s.saleDate).toLocaleString(undefined, {
                          month: "short",
                          day: "numeric",
                          year: "numeric",
                          hour: "2-digit",
                          minute: "2-digit",
                        })}
                      </td>
                      {isAdmin && (
                        <td className="p-4">
                          <div className="flex items-center justify-center">
                            <button
                              onClick={() => openDeleteModal(s)}
                              title="Delete sale record"
                              className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 bg-slate-50 dark:bg-[#0a0a0f] text-slate-400 hover:text-red-500 hover:bg-slate-100 transition-colors cursor-pointer"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </div>
                        </td>
                      )}
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* PAGINATION */}
        <div className="p-4 border-t border-slate-200 dark:border-white/5 flex items-center justify-between text-xs text-slate-400 bg-slate-50/50 dark:bg-[#0a0a0f]/50">
          <p>
            Showing {sales.length} of {totalElements} transactions
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

      {/* LOG SALE MODAL */}
      {isAddModalOpen && (
        <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-sm p-6">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">Record Product Sale</h3>
            <form onSubmit={handleAddSale} className="space-y-4">
              {formError && (
                <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-xs p-3 rounded-lg flex items-center gap-1.5">
                  <AlertTriangle className="w-4 h-4 shrink-0" />
                  <span>{formError}</span>
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Select Product *</label>
                <select
                  required
                  value={form.productId}
                  onChange={(e) => setForm({ ...form, productId: e.target.value })}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs"
                >
                  <option value="">Choose product...</option>
                  {products.map((p) => (
                    <option key={p.id} value={p.id} disabled={!p.active || p.quantity === 0}>
                      {p.name} {p.quantity === 0 ? "(Out of stock)" : `(${p.quantity} available)`}
                    </option>
                  ))}
                </select>
              </div>

              {selectedProductDetails && (
                <div className="p-3 bg-indigo-500/5 border border-indigo-500/10 rounded-xl text-[11px] space-y-1 text-indigo-400">
                  <p>• In Inventory: {selectedProductDetails.quantity} items</p>
                  <p>• Default Price: ${selectedProductDetails.sellingPrice?.toFixed(2)}</p>
                </div>
              )}

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Quantity *</label>
                  <input
                    type="number"
                    min={1}
                    required
                    value={form.quantity}
                    onChange={(e) => setForm({ ...form, quantity: e.target.value })}
                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Sale Price ($) *</label>
                  <input
                    type="number"
                    step="0.01"
                    min={0}
                    required
                    value={form.salePrice}
                    onChange={(e) => setForm({ ...form, salePrice: e.target.value })}
                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs"
                  />
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-slate-200 dark:border-white/5">
                <button
                  type="button"
                  onClick={() => setIsAddModalOpen(false)}
                  className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-500 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading || !form.productId || !form.quantity || !form.salePrice}
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-505 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-indigo-500/10 flex items-center gap-1.5 animate-pulse-none"
                >
                  {loading ? (
                    <>
                      <Loader2 className="w-3.5 h-3.5 animate-spin" /> Logging...
                    </>
                  ) : (
                    "Record Sale"
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* DELETE CONFIRMATION */}
      {isDeleteModalOpen && selectedSale && (
        <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-sm p-6 text-center">
            <AlertTriangle className="w-12 h-12 text-red-500 mx-auto mb-4" />
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-2">Delete Transaction Record</h3>
            <p className="text-xs text-slate-400 mb-6 font-medium">
              Are you sure you want to delete sale transaction ID: <span className="font-mono text-indigo-400">{selectedSale.id.substring(0, 8)}...</span>? This deletes the log only and will not roll back inventory quantities automatically.
            </p>
            <div className="flex justify-center gap-3">
              <button
                onClick={() => setIsDeleteModalOpen(false)}
                className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-500 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs"
              >
                Cancel
              </button>
              <button
                onClick={handleDeleteSale}
                disabled={loading}
                className="px-4 py-2 bg-red-600 hover:bg-red-500 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-red-500/10"
              >
                {loading ? "Deleting..." : "Delete Log"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
