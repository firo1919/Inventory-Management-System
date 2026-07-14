"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { productsService } from "@/services/products";
import { categoriesService } from "@/services/categories";
import { toast } from "sonner";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import {
  Plus,
  Search,
  ChevronLeft,
  ChevronRight,
  Edit2,
  Trash2,
  Image as ImageIcon,
  CheckCircle,
  XCircle,
  Eye,
  AlertTriangle,
  Loader2,
  Upload,
} from "lucide-react";
import axios from "axios";

const productSchema = z.object({
  name: z.string().min(1, "Product name is required"),
  sku: z.string().min(1, "SKU is required"),
  description: z.string().optional(),
  costPrice: z.number().positive("Cost price must be greater than zero"),
  sellingPrice: z.number().positive("Selling price must be greater than zero"),
  quantity: z.number().int().nonnegative("Quantity must be a positive integer or zero"),
  lowStockThreshold: z.number().int().nonnegative("Low stock threshold must be a positive integer or zero"),
  categoryIds: z.array(z.string()),
});

type ProductInput = z.infer<typeof productSchema>;

export default function ProductsPage() {
  const { isAdmin } = useAuth();
  
  // States for products table
  const [products, setProducts] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [categoryMap, setCategoryMap] = useState<Record<string, string>>({});
  
  // Pagination & Filters
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("");
  const [stockFilter, setStockFilter] = useState("ALL"); // ALL, LOW, OUT
  const [sortBy, setSortBy] = useState("createdAt");
  const [sortDir, setSortDir] = useState("desc");

  // Loading States
  const [loading, setLoading] = useState(true);

  // Modal States
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);

  // Selected Product State for Modals
  const [selectedProduct, setSelectedProduct] = useState<any>(null);

  // Image Upload State
  const [uploadFile, setUploadFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);

  // React Hook Form
  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors },
  } = useForm<ProductInput>({
    resolver: zodResolver(productSchema),
    defaultValues: {
      name: "",
      sku: "",
      description: "",
      costPrice: 0,
      sellingPrice: 0,
      quantity: 0,
      lowStockThreshold: 10,
      categoryIds: [],
    },
  });

  const categoryIds = watch("categoryIds") || [];

  const handleCategorySelection = (categoryId: string) => {
    if (categoryIds.includes(categoryId)) {
      setValue("categoryIds", categoryIds.filter((id) => id !== categoryId));
    } else {
      setValue("categoryIds", [...categoryIds, categoryId]);
    }
  };

  // Fetch Categories
  useEffect(() => {
    async function fetchCategories() {
      try {
        const data = await categoriesService.getCategories({ page: 0, size: 100 });
        const content = data?.content || [];
        setCategories(content);
        const mapping: Record<string, string> = {};
        content.forEach((cat: any) => {
          mapping[cat.id] = cat.name;
        });
        setCategoryMap(mapping);
      } catch {
        toast.error("Failed to load categories.");
      }
    }
    fetchCategories();
  }, []);

  // Fetch Products based on parameters
  const fetchProducts = async () => {
    try {
      setLoading(true);
      const params = {
        page,
        size: pageSize,
        sortBy,
        sortDirection: sortDir,
      };

      let data;
      if (stockFilter === "LOW") {
        data = await productsService.getLowStockProducts(params);
      } else {
        data = await productsService.getProducts(params);
      }

      let content = data?.content || [];

      // Manual client-side filtering if search or category is active (since mock backend might not support extensive filtering queries)
      if (search) {
        const query = search.toLowerCase();
        content = content.filter(
          (p: any) =>
            p.name.toLowerCase().includes(query) ||
            p.sku.toLowerCase().includes(query) ||
            (p.description && p.description.toLowerCase().includes(query))
        );
      }

      if (selectedCategory) {
        content = content.filter((p: any) =>
          p.categoryIds?.includes(selectedCategory)
        );
      }

      if (stockFilter === "OUT") {
        content = content.filter((p: any) => p.quantity === 0);
      }

      setProducts(content);
      setTotalPages(data?.totalPages || 1);
      setTotalElements(data?.totalElements || content.length);
    } catch {
      toast.error("Failed to load products.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, [page, pageSize, stockFilter, sortBy, sortDir, selectedCategory]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchProducts();
  };

  // Add Product Submit
  const handleAddProduct = async (data: ProductInput) => {
    setLoading(true);
    try {
      await productsService.createProduct(data);
      setIsAddModalOpen(false);
      reset();
      toast.success("Product created successfully!");
      fetchProducts();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to create product");
    } finally {
      setLoading(false);
    }
  };

  // Edit Product Submit
  const handleEditProduct = async (data: ProductInput) => {
    setLoading(true);
    try {
      await productsService.updateProduct(selectedProduct.id, data);
      setIsEditModalOpen(false);
      reset();
      toast.success("Product updated!");
      fetchProducts();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to update product");
    } finally {
      setLoading(false);
    }
  };

  // Delete Product
  const handleDeleteProduct = async () => {
    setLoading(true);
    try {
      await productsService.deleteProduct(selectedProduct.id);
      setIsDeleteModalOpen(false);
      toast.success("Product deleted.");
      fetchProducts();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to delete product");
    } finally {
      setLoading(false);
    }
  };

  // Toggle Active/Inactive Status
  const handleToggleStatus = async (productId: string, currentStatus: boolean) => {
    try {
      await productsService.toggleProductStatus(productId, currentStatus);
      toast.success(currentStatus ? "Product deactivated." : "Product activated.");
      fetchProducts();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to toggle status");
    }
  };

  // Image Upload handler
  const handleImageUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!uploadFile || !selectedProduct) return;

    setUploading(true);
    setUploadProgress(10);

    try {
      const formData = new FormData();
      formData.append("file", uploadFile);

      setUploadProgress(40);
      await productsService.uploadProductImage(selectedProduct.id, formData);

      setUploadProgress(100);
      toast.success("Image uploaded!");
      setIsUploadModalOpen(false);
      setUploadFile(null);
      fetchProducts();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to upload image");
    } finally {
      setUploading(false);
      setUploadProgress(0);
    }
  };

  const openAddModal = () => {
    reset({
      name: "",
      sku: "",
      description: "",
      costPrice: 0,
      sellingPrice: 0,
      quantity: 0,
      lowStockThreshold: 10,
      categoryIds: [],
    });
    setIsAddModalOpen(true);
  };

  const openEditModal = (p: any) => {
    setSelectedProduct(p);
    reset({
      name: p.name,
      sku: p.sku,
      description: p.description || "",
      costPrice: p.costPrice,
      sellingPrice: p.sellingPrice,
      quantity: p.quantity,
      lowStockThreshold: p.lowStockThreshold,
      categoryIds: p.categoryIds || [],
    });
    setIsEditModalOpen(true);
  };

  const openUploadModal = (p: any) => {
    setSelectedProduct(p);
    setUploadFile(null);
    setIsUploadModalOpen(true);
  };

  const openDeleteModal = (p: any) => {
    setSelectedProduct(p);
    setIsDeleteModalOpen(true);
  };

  const openViewModal = (p: any) => {
    setSelectedProduct(p);
    setIsViewModalOpen(true);
  };

  return (
    <div className="space-y-6">
      {/* HEADER ACTION */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold dark:text-white">Products Catalog</h1>
          <p className="text-xs text-slate-400">Track and manage inventory stock levels, prices and details</p>
        </div>
        {isAdmin && (
          <button
            onClick={openAddModal}
            className="inline-flex items-center gap-1.5 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-505 text-white text-xs font-semibold rounded-xl transition-all shadow-md shadow-indigo-500/10 cursor-pointer"
          >
            <Plus className="w-4 h-4" /> Add Product
          </button>
        )}
      </div>

      {/* FILTERS PANEL */}
      <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-4 flex flex-col gap-4 shadow-sm">
        <div className="flex flex-wrap gap-4 items-center justify-between">
          <form onSubmit={handleSearchSubmit} className="relative w-full md:max-w-xs">
            <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400 pointer-events-none">
              <Search className="w-4 h-4" />
            </span>
            <input
              type="text"
              placeholder="Search products..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
            />
          </form>

          <div className="flex flex-wrap gap-3 w-full md:w-auto">
            {/* Category Filter */}
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              className="px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-500 focus:outline-none cursor-pointer"
            >
              <option value="">All Categories</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>

            {/* Stock Filter */}
            <div className="flex bg-slate-100 dark:bg-white/5 p-1 rounded-xl text-xs font-semibold">
              {(["ALL", "LOW", "OUT"] as const).map((filter) => (
                <button
                  key={filter}
                  onClick={() => setStockFilter(filter)}
                  className={`px-3 py-1.5 rounded-lg transition-all capitalize cursor-pointer ${
                    stockFilter === filter
                      ? "bg-white dark:bg-[#13131a] text-slate-800 dark:text-white shadow-sm"
                      : "text-slate-400 hover:text-slate-650"
                  }`}
                >
                  {filter === "ALL" ? "All Stock" : filter === "LOW" ? "Low Stock" : "Out of Stock"}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* PRODUCTS TABLE */}
      <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm border-collapse">
            <thead>
              <tr className="border-b border-slate-200 dark:border-white/5 bg-slate-50/50 dark:bg-[#0a0a0f]/50 text-slate-400 text-xs font-semibold">
                <th className="p-4">Product Info</th>
                <th className="p-4">SKU</th>
                <th className="p-4">Categories</th>
                <th className="p-4">Stock Qty</th>
                <th className="p-4">Prices</th>
                <th className="p-4">Status</th>
                {isAdmin && <th className="p-4 text-center">Actions</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-white/5">
              {loading ? (
                Array.from({ length: 3 }).map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    <td colSpan={7} className="p-4 h-16 bg-slate-50/10 dark:bg-[#0e0e13]/10" />
                  </tr>
                ))
              ) : products.length === 0 ? (
                <tr>
                  <td colSpan={7} className="p-8 text-center text-slate-400">
                    No products matching search parameters
                  </td>
                </tr>
              ) : (
                products.map((p) => {
                  const isLowStock = p.quantity <= p.lowStockThreshold && p.quantity > 0;
                  const isOutOfStock = p.quantity === 0;

                  return (
                    <tr
                      key={p.id}
                      onClick={() => openViewModal(p)}
                      className="hover:bg-slate-50/50 dark:hover:bg-[#1c1c24]/10 text-slate-650 dark:text-slate-300 cursor-pointer"
                    >
                      <td className="p-4">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 bg-slate-100 dark:bg-[#0a0a0f] rounded-lg flex items-center justify-center border border-slate-200 dark:border-white/5 overflow-hidden shrink-0">
                            {p.imageUrls && p.imageUrls.length > 0 ? (
                              <img src={p.imageUrls[0]} alt={p.name} className="w-full h-full object-cover" />
                            ) : (
                              <ImageIcon className="w-4 h-4 text-slate-400" />
                            )}
                          </div>
                          <div className="min-w-0">
                            <h4 className="font-semibold text-slate-800 dark:text-white text-sm truncate max-w-xs">{p.name}</h4>
                            <p className="text-[10px] text-slate-400 truncate max-w-xs">{p.description || "No description provided."}</p>
                          </div>
                        </div>
                      </td>
                      <td className="p-4 font-mono text-xs text-indigo-400">{p.sku}</td>
                      <td className="p-4">
                        <div className="flex flex-wrap gap-1">
                          {p.categoryIds && p.categoryIds.length > 0 ? (
                            p.categoryIds.map((cid: string) => (
                              <span key={cid} className="px-1.5 py-0.5 rounded bg-slate-100 dark:bg-white/5 text-[10px] font-semibold text-slate-500">
                                {categoryMap[cid] || cid}
                              </span>
                            ))
                          ) : (
                            <span className="text-[10px] text-slate-400">—</span>
                          )}
                        </div>
                      </td>
                      <td className="p-4">
                        <div className="flex flex-col">
                          <span
                            className={`font-semibold text-xs ${
                              isOutOfStock
                                ? "text-red-500"
                                : isLowStock
                                ? "text-amber-500"
                                : "text-slate-800 dark:text-white"
                            }`}
                          >
                            {p.quantity} units
                          </span>
                          <span className="text-[9px] text-slate-400">Limit: {p.lowStockThreshold}</span>
                        </div>
                      </td>
                      <td className="p-4">
                        <div className="flex flex-col">
                          <span className="font-bold text-xs text-indigo-500">${p.sellingPrice?.toFixed(2)}</span>
                          <span className="text-[9px] text-slate-400">Cost: ${p.costPrice?.toFixed(2)}</span>
                        </div>
                      </td>
                      <td className="p-4" onClick={(e) => e.stopPropagation()}>
                        {isAdmin ? (
                          <button
                            onClick={() => handleToggleStatus(p.id, p.active)}
                            className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase border transition-all ${
                              p.active
                                ? "bg-emerald-500/10 text-emerald-500 border-emerald-500/20 hover:bg-emerald-500/20"
                                : "bg-red-500/10 text-red-500 border-red-500/20 hover:bg-red-500/20"
                            } cursor-pointer`}
                          >
                            {p.active ? <CheckCircle className="w-3 h-3" /> : <XCircle className="w-3 h-3" />}
                            {p.active ? "Active" : "Disabled"}
                          </button>
                        ) : (
                          <span
                            className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase border ${
                              p.active
                                ? "bg-emerald-500/10 text-emerald-500 border-emerald-500/20"
                                : "bg-red-500/10 text-red-500 border-red-500/20"
                            }`}
                          >
                            {p.active ? "Active" : "Disabled"}
                          </span>
                        )}
                      </td>
                      {isAdmin && (
                        <td className="p-4" onClick={(e) => e.stopPropagation()}>
                          <div className="flex items-center justify-center gap-1.5">
                            <button
                              onClick={() => openViewModal(p)}
                              title="View details"
                              className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 bg-slate-50 dark:bg-[#0a0a0f] text-slate-400 hover:text-indigo-500 hover:bg-slate-100 transition-colors cursor-pointer"
                            >
                              <Eye className="w-4 h-4" />
                            </button>
                            <button
                              onClick={() => openUploadModal(p)}
                              title="Upload product image"
                              className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 bg-slate-50 dark:bg-[#0a0a0f] text-slate-400 hover:text-blue-500 hover:bg-slate-100 transition-colors cursor-pointer"
                            >
                              <Upload className="w-4 h-4" />
                            </button>
                            <button
                              onClick={() => openEditModal(p)}
                              title="Edit product"
                              className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 bg-slate-50 dark:bg-[#0a0a0f] text-slate-400 hover:text-emerald-500 hover:bg-slate-100 transition-colors cursor-pointer"
                            >
                              <Edit2 className="w-4 h-4" />
                            </button>
                            <button
                              onClick={() => openDeleteModal(p)}
                              title="Delete product"
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

        {/* PAGINATION FOOTER */}
        <div className="p-4 border-t border-slate-200 dark:border-white/5 flex items-center justify-between text-xs text-slate-400 bg-slate-50/50 dark:bg-[#0a0a0f]/50">
          <p>
            Showing {products.length} of {totalElements} products
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

      {/* VIEW MODAL */}
      {isViewModalOpen && selectedProduct && (
        <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-lg p-6 overflow-y-auto max-h-[90vh]">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">Product Detail</h3>
            <div className="flex flex-col md:flex-row gap-6 mb-6">
              <div className="w-full md:w-40 h-40 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl flex items-center justify-center overflow-hidden shrink-0">
                {selectedProduct.imageUrls && selectedProduct.imageUrls.length > 0 ? (
                  <img src={selectedProduct.imageUrls[0]} alt={selectedProduct.name} className="w-full h-full object-cover" />
                ) : (
                  <ImageIcon className="w-8 h-8 text-slate-400" />
                )}
              </div>
              <div className="space-y-3 flex-1 min-w-0">
                <div>
                  <h4 className="text-base font-bold text-slate-800 dark:text-white break-words">{selectedProduct.name}</h4>
                  <p className="text-xs text-slate-400 font-mono mt-0.5">SKU: {selectedProduct.sku}</p>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <span className="text-[10px] text-slate-400 uppercase tracking-wider block">Quantity</span>
                    <span className="text-sm font-semibold">{selectedProduct.quantity} units</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-slate-400 uppercase tracking-wider block">Threshold</span>
                    <span className="text-sm font-semibold">{selectedProduct.lowStockThreshold} units</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-slate-400 uppercase tracking-wider block">Selling Price</span>
                    <span className="text-sm font-bold text-indigo-500">${selectedProduct.sellingPrice?.toFixed(2)}</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-slate-400 uppercase tracking-wider block">Cost Price</span>
                    <span className="text-sm font-bold">${selectedProduct.costPrice?.toFixed(2)}</span>
                  </div>
                </div>
              </div>
            </div>
            
            {selectedProduct.description && (
              <div className="mb-6">
                <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-1">Description</span>
                <p className="text-xs text-slate-600 dark:text-slate-300 bg-slate-50 dark:bg-[#0a0a0f] p-3 rounded-xl border border-slate-200 dark:border-white/5">
                  {selectedProduct.description}
                </p>
              </div>
            )}

            <div className="flex justify-end">
              <button
                onClick={() => setIsViewModalOpen(false)}
                className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-505 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* CREATE MODAL */}
      {isAddModalOpen && (
        <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-lg p-6 overflow-y-auto max-h-[90vh]">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">Create New Product</h3>
            <form onSubmit={handleSubmit(handleAddProduct)} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Product Name *</label>
                  <input
                    type="text"
                    {...register("name")}
                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                  {errors.name && (
                    <p className="text-[10px] text-red-400 mt-1">{errors.name.message}</p>
                  )}
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">SKU *</label>
                  <input
                    type="text"
                    {...register("sku")}
                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                  {errors.sku && (
                    <p className="text-[10px] text-red-400 mt-1">{errors.sku.message}</p>
                  )}
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Description</label>
                <textarea
                  {...register("description")}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500 h-20"
                />
                {errors.description && (
                  <p className="text-[10px] text-red-400 mt-1">{errors.description.message}</p>
                )}
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Cost Price ($) *</label>
                  <input
                     type="number"
                     step="0.01"
                     {...register("costPrice", { valueAsNumber: true })}
                     className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                  {errors.costPrice && (
                    <p className="text-[10px] text-red-400 mt-1">{errors.costPrice.message}</p>
                  )}
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Selling Price ($) *</label>
                  <input
                     type="number"
                     step="0.01"
                     {...register("sellingPrice", { valueAsNumber: true })}
                     className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                  {errors.sellingPrice && (
                    <p className="text-[10px] text-red-400 mt-1">{errors.sellingPrice.message}</p>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Initial Quantity *</label>
                  <input
                    type="number"
                    {...register("quantity", { valueAsNumber: true })}
                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                  {errors.quantity && (
                    <p className="text-[10px] text-red-400 mt-1">{errors.quantity.message}</p>
                  )}
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Low Stock Threshold *</label>
                  <input
                    type="number"
                    {...register("lowStockThreshold", { valueAsNumber: true })}
                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                  {errors.lowStockThreshold && (
                    <p className="text-[10px] text-red-400 mt-1">{errors.lowStockThreshold.message}</p>
                  )}
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-2">Associate Categories</label>
                <div className="flex flex-wrap gap-2 max-h-28 overflow-y-auto p-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl">
                  {categories.map((c) => {
                    const isSelected = categoryIds.includes(c.id);
                    return (
                      <button
                        type="button"
                        key={c.id}
                        onClick={() => handleCategorySelection(c.id)}
                        className={`px-3 py-1 rounded-lg text-xs font-semibold transition-all border ${
                          isSelected
                            ? "bg-indigo-500/10 text-indigo-500 border-indigo-500/30"
                            : "bg-white dark:bg-[#13131a]/80 text-slate-500 dark:text-slate-400 border-slate-200 dark:border-white/5"
                        }`}
                      >
                        {c.name}
                      </button>
                    );
                  })}
                </div>
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
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-indigo-500/10 cursor-pointer"
                >
                  {loading ? "Creating..." : "Create Product"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT MODAL */}
      {isEditModalOpen && selectedProduct && (
        <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-lg p-6 overflow-y-auto max-h-[90vh]">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">Edit Product</h3>
            <form onSubmit={handleSubmit(handleEditProduct)} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Product Name *</label>
                  <input
                    type="text"
                    {...register("name")}
                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                  {errors.name && (
                    <p className="text-[10px] text-red-400 mt-1">{errors.name.message}</p>
                  )}
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">SKU *</label>
                  <input
                    type="text"
                    {...register("sku")}
                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                  {errors.sku && (
                    <p className="text-[10px] text-red-400 mt-1">{errors.sku.message}</p>
                  )}
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Description</label>
                <textarea
                  {...register("description")}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500 h-20"
                />
                {errors.description && (
                  <p className="text-[10px] text-red-400 mt-1">{errors.description.message}</p>
                )}
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Cost Price ($) *</label>
                  <input
                    type="number"
                    step="0.01"
                    {...register("costPrice", { valueAsNumber: true })}
                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                  {errors.costPrice && (
                    <p className="text-[10px] text-red-400 mt-1">{errors.costPrice.message}</p>
                  )}
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Selling Price ($) *</label>
                  <input
                    type="number"
                    step="0.01"
                    {...register("sellingPrice", { valueAsNumber: true })}
                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                  {errors.sellingPrice && (
                    <p className="text-[10px] text-red-400 mt-1">{errors.sellingPrice.message}</p>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Current Quantity *</label>
                  <input
                    type="number"
                    {...register("quantity", { valueAsNumber: true })}
                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                  {errors.quantity && (
                    <p className="text-[10px] text-red-400 mt-1">{errors.quantity.message}</p>
                  )}
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Low Stock Threshold *</label>
                  <input
                    type="number"
                    {...register("lowStockThreshold", { valueAsNumber: true })}
                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                  {errors.lowStockThreshold && (
                    <p className="text-[10px] text-red-400 mt-1">{errors.lowStockThreshold.message}</p>
                  )}
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-2">Associate Categories</label>
                <div className="flex flex-wrap gap-2 max-h-28 overflow-y-auto p-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl">
                  {categories.map((c) => {
                    const isSelected = categoryIds.includes(c.id);
                    return (
                      <button
                        type="button"
                        key={c.id}
                        onClick={() => handleCategorySelection(c.id)}
                        className={`px-3 py-1 rounded-lg text-xs font-semibold transition-all border ${
                          isSelected
                            ? "bg-indigo-500/10 text-indigo-500 border-indigo-500/30"
                            : "bg-white dark:bg-[#13131a]/80 text-slate-500 dark:text-slate-400 border-slate-200 dark:border-white/5"
                        }`}
                      >
                        {c.name}
                      </button>
                    );
                  })}
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-slate-200 dark:border-white/5">
                <button
                  type="button"
                  onClick={() => setIsEditModalOpen(false)}
                  className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-505 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-indigo-500/10 cursor-pointer"
                >
                  {loading ? "Updating..." : "Save Changes"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* UPLOAD IMAGE MODAL */}
      {isUploadModalOpen && selectedProduct && (
        <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-md p-6">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-2">Upload Product Image</h3>
            <p className="text-xs text-slate-400 mb-4">Upload a cover image for product: <span className="font-semibold text-slate-700 dark:text-white">{selectedProduct.name}</span></p>
            <form onSubmit={handleImageUpload} className="space-y-4">
              <div className="flex flex-col items-center justify-center border-2 border-dashed border-slate-200 dark:border-white/5 rounded-2xl p-6 bg-slate-50 dark:bg-[#0a0a0f]">
                <Upload className="w-8 h-8 text-slate-400 mb-2" />
                <input
                  type="file"
                  accept="image/*"
                  onChange={(e) => setUploadFile(e.target.files ? e.target.files[0] : null)}
                  className="block w-full text-xs text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-xl file:border-0 file:text-xs file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100"
                />
                {uploadFile && (
                  <p className="text-[10px] text-slate-400 mt-2 font-mono truncate max-w-xs">{uploadFile.name} ({(uploadFile.size / 1024).toFixed(1)} KB)</p>
                )}
              </div>

              {uploading && (
                <div className="space-y-1">
                  <div className="w-full bg-slate-200 dark:bg-white/10 h-1.5 rounded-full overflow-hidden">
                    <div className="bg-indigo-500 h-full rounded-full transition-all duration-300" style={{ width: `${uploadProgress}%` }} />
                  </div>
                  <p className="text-[10px] text-slate-400 text-right">Uploading... {uploadProgress}%</p>
                </div>
              )}

              <div className="flex justify-end gap-3 pt-4 border-t border-slate-200 dark:border-white/5">
                <button
                  type="button"
                  onClick={() => setIsUploadModalOpen(false)}
                  disabled={uploading}
                  className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-505 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={uploading || !uploadFile}
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-505 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-indigo-500/10 flex items-center gap-1.5 cursor-pointer"
                >
                  {uploading ? (
                    <>
                      <Loader2 className="w-3.5 h-3.5 animate-spin" /> Uploading
                    </>
                  ) : (
                    "Upload Image"
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* DELETE MODAL */}
      {isDeleteModalOpen && selectedProduct && (
        <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-sm p-6 text-center">
            <AlertTriangle className="w-12 h-12 text-red-500 mx-auto mb-4" />
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-2">Delete Product</h3>
            <p className="text-xs text-slate-400 mb-6">
              Are you sure you want to delete <span className="font-semibold text-slate-700 dark:text-white">{selectedProduct.name}</span>? This action cannot be undone.
            </p>
            <div className="flex justify-center gap-3">
              <button
                onClick={() => setIsDeleteModalOpen(false)}
                className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-505 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs"
              >
                Cancel
              </button>
              <button
                onClick={handleDeleteProduct}
                disabled={loading}
                className="px-4 py-2 bg-red-600 hover:bg-red-500 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-red-500/10 cursor-pointer"
              >
                {loading ? "Deleting..." : "Delete Product"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
