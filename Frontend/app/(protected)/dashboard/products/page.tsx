"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { productsService } from "@/services/products";
import { categoriesService } from "@/services/categories";
import { toast } from "sonner";
import { Plus, Search, Edit2, Trash2, Image as ImageIcon, CheckCircle, XCircle, Eye, Upload } from "lucide-react";
import { ProductFormModal } from "./_components/ProductFormModal";
import { ProductViewModal } from "./_components/ProductViewModal";
import { ImageUploadModal } from "./_components/ImageUploadModal";
import { DeleteProductModal } from "./_components/DeleteProductModal";
import { Pagination } from "@/components/ui/Pagination";
import { ProductInput } from "./schema";

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
  const [modalLoading, setModalLoading] = useState(false);

  // Modal States
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);

  // Selected Product State for Modals
  const [selectedProduct, setSelectedProduct] = useState<any>(null);

  // Image Upload State
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);

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
    setModalLoading(true);
    try {
      await productsService.createProduct(data);
      setIsAddModalOpen(false);
      toast.success("Product created successfully!");
      fetchProducts();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to create product");
    } finally {
      setModalLoading(false);
    }
  };

  // Edit Product Submit
  const handleEditProduct = async (data: ProductInput) => {
    setModalLoading(true);
    try {
      await productsService.updateProduct(selectedProduct.id, data);
      setIsEditModalOpen(false);
      toast.success("Product updated!");
      fetchProducts();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to update product");
    } finally {
      setModalLoading(false);
    }
  };

  // Delete Product
  const handleDeleteProduct = async () => {
    setModalLoading(true);
    try {
      await productsService.deleteProduct(selectedProduct.id);
      setIsDeleteModalOpen(false);
      toast.success("Product deleted.");
      fetchProducts();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to delete product");
    } finally {
      setModalLoading(false);
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
  const handleImageUpload = async (file: File) => {
    if (!selectedProduct) return;

    setUploading(true);
    setUploadProgress(10);

    try {
      const formData = new FormData();
      formData.append("file", file);

      setUploadProgress(40);
      await productsService.uploadProductImage(selectedProduct.id, formData);

      setUploadProgress(100);
      toast.success("Image uploaded!");
      setIsUploadModalOpen(false);
      fetchProducts();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to upload image");
    } finally {
      setUploading(false);
      setUploadProgress(0);
    }
  };

  const openAddModal = () => {
    setIsAddModalOpen(true);
  };

  const openEditModal = (p: any) => {
    setSelectedProduct(p);
    setIsEditModalOpen(true);
  };

  const openUploadModal = (p: any) => {
    setSelectedProduct(p);
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
          <p className="text-xs text-slate-400">Add, track, modify, and deactivate inventory items</p>
        </div>
        {isAdmin && (
          <button
            onClick={openAddModal}
            className="inline-flex items-center gap-1.5 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold rounded-xl transition-all shadow-md shadow-indigo-500/10 cursor-pointer"
          >
            <Plus className="w-4 h-4" /> Add Product
          </button>
        )}
      </div>

      {/* FILTER & SEARCH */}
      <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-4 shadow-sm space-y-4">
        <div className="flex flex-col md:flex-row gap-4 items-center justify-between">
          <form onSubmit={handleSearchSubmit} className="relative w-full md:max-w-xs">
            <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400 pointer-events-none">
              <Search className="w-4 h-4" />
            </span>
            <input
              type="text"
              placeholder="Search by name, sku..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
            />
          </form>

          {/* Filtering control boxes */}
          <div className="flex flex-wrap gap-3 items-center w-full md:w-auto">
            {/* Category selection */}
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              className="px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs font-semibold text-slate-700 dark:text-slate-300 focus:outline-none"
            >
              <option value="">All Categories</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>

            {/* Sorting property */}
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs font-semibold text-slate-700 dark:text-slate-300 focus:outline-none"
            >
              <option value="name">Sort by Name</option>
              <option value="quantity">Sort by Quantity</option>
              <option value="sellingPrice">Sort by Selling Price</option>
              <option value="costPrice">Sort by Cost Price</option>
              <option value="createdAt">Sort by Date Added</option>
            </select>

            {/* Sorting Direction */}
            <select
              value={sortDir}
              onChange={(e) => setSortDir(e.target.value)}
              className="px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs font-semibold text-slate-700 dark:text-slate-300 focus:outline-none"
            >
              <option value="asc">Ascending</option>
              <option value="desc">Descending</option>
            </select>

            {/* Stock Levels filters */}
            <div className="flex bg-slate-100 dark:bg-white/5 p-1 rounded-xl shrink-0">
              {["ALL", "LOW", "OUT"].map((filter) => (
                <button
                  key={filter}
                  onClick={() => setStockFilter(filter)}
                  className={`px-3 py-1 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
                    stockFilter === filter
                      ? "bg-white dark:bg-[#13131a] text-indigo-500 shadow-xs"
                      : "text-slate-500 hover:text-slate-800 dark:hover:text-white"
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
                      className="hover:bg-slate-50/50 dark:hover:bg-[#1c1c24]/10 text-slate-600 dark:text-slate-300 cursor-pointer"
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

        {/* PAGINATION */}
        <Pagination
          page={page}
          pageSize={pageSize}
          totalPages={totalPages}
          totalElements={totalElements}
          onPageChange={setPage}
          loading={loading}
          itemName="products"
        />
      </div>

      {/* VIEW MODAL */}
      <ProductViewModal
        isOpen={isViewModalOpen}
        onClose={() => setIsViewModalOpen(false)}
        product={selectedProduct}
      />

      {/* CREATE MODAL */}
      <ProductFormModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onSubmit={handleAddProduct}
        categories={categories}
        loading={modalLoading}
        title="Create New Product"
        submitLabel="Create Product"
      />

      {/* EDIT MODAL */}
      <ProductFormModal
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        onSubmit={handleEditProduct}
        initialData={selectedProduct ? {
          name: selectedProduct.name,
          sku: selectedProduct.sku,
          description: selectedProduct.description || "",
          costPrice: selectedProduct.costPrice,
          sellingPrice: selectedProduct.sellingPrice,
          quantity: selectedProduct.quantity,
          lowStockThreshold: selectedProduct.lowStockThreshold,
          categoryIds: selectedProduct.categoryIds || [],
        } : null}
        categories={categories}
        loading={modalLoading}
        title="Edit Product"
        submitLabel="Save Changes"
      />

      {/* UPLOAD IMAGE MODAL */}
      <ImageUploadModal
        isOpen={isUploadModalOpen}
        onClose={() => setIsUploadModalOpen(false)}
        onSubmit={handleImageUpload}
        productName={selectedProduct?.name || ""}
        uploading={uploading}
        progress={uploadProgress}
      />

      {/* DELETE MODAL */}
      <DeleteProductModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        onConfirm={handleDeleteProduct}
        productName={selectedProduct?.name || ""}
        loading={modalLoading}
      />
    </div>
  );
}
