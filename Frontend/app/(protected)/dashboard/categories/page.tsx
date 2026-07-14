"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
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
  AlertTriangle,
} from "lucide-react";

const categorySchema = z.object({
  name: z.string().min(1, "Category name is required").max(50, "Category name cannot exceed 50 characters"),
});

type CategoryInput = z.infer<typeof categorySchema>;

export default function CategoriesPage() {
  const { isAdmin } = useAuth();
  
  // Data States
  const [categories, setCategories] = useState<any[]>([]);
  
  // Pagination & Search
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState("");

  // Loading States
  const [loading, setLoading] = useState(true);

  // Modal States
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

  // Selected Category State
  const [selectedCategory, setSelectedCategory] = useState<any>(null);

  // React Hook Form
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CategoryInput>({
    resolver: zodResolver(categorySchema),
    defaultValues: {
      name: "",
    },
  });

  const fetchCategories = async () => {
    try {
      setLoading(true);
      const data = await categoriesService.getCategories({
        page,
        size: pageSize,
      });

      let content = data?.content || [];

      // Client side filtering for search query if needed
      if (search) {
        const query = search.toLowerCase();
        content = content.filter((c: any) => c.name.toLowerCase().includes(query));
      }

      setCategories(content);
      setTotalPages(data?.totalPages || 1);
      setTotalElements(data?.totalElements || content.length);
    } catch {
      toast.error("Failed to fetch categories.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, [page, pageSize]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchCategories();
  };

  // Add Category
  const handleAddCategory = async (data: CategoryInput) => {
    setLoading(true);
    try {
      await categoriesService.createCategory({
        name: data.name,
      });
      setIsAddModalOpen(false);
      reset({ name: "" });
      toast.success("Category created successfully!");
      fetchCategories();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to create category");
    } finally {
      setLoading(false);
    }
  };

  // Edit Category
  const handleEditCategory = async (data: CategoryInput) => {
    setLoading(true);
    try {
      await categoriesService.updateCategory(selectedCategory.id, {
        name: data.name,
      });
      setIsEditModalOpen(false);
      reset({ name: "" });
      toast.success("Category updated!");
      fetchCategories();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to update category");
    } finally {
      setLoading(false);
    }
  };

  // Delete Category
  const handleDeleteCategory = async () => {
    setLoading(true);
    try {
      await categoriesService.deleteCategory(selectedCategory.id);
      setIsDeleteModalOpen(false);
      toast.success("Category deleted.");
      fetchCategories();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to delete category");
    } finally {
      setLoading(false);
    }
  };

  const openAddModal = () => {
    reset({ name: "" });
    setIsAddModalOpen(true);
  };

  const openEditModal = (category: any) => {
    setSelectedCategory(category);
    reset({ name: category.name });
    setIsEditModalOpen(true);
  };

  const openDeleteModal = (category: any) => {
    setSelectedCategory(category);
    setIsDeleteModalOpen(true);
  };

  return (
    <div className="space-y-6">
      {/* HEADER */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold dark:text-white">Categories Management</h1>
          <p className="text-xs text-slate-400">Classify your products and manage stock divisions</p>
        </div>
        {isAdmin && (
          <button
            onClick={openAddModal}
            className="inline-flex items-center gap-1.5 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-505 text-white text-xs font-semibold rounded-xl transition-all shadow-md shadow-indigo-500/10 cursor-pointer"
          >
            <Plus className="w-4 h-4" /> Add Category
          </button>
        )}
      </div>

      {/* FILTER & SEARCH */}
      <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-4 flex flex-col md:flex-row gap-4 items-center justify-between shadow-sm">
        <form onSubmit={handleSearchSubmit} className="relative w-full md:max-w-xs">
          <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400 pointer-events-none">
            <Search className="w-4 h-4" />
          </span>
          <input
            type="text"
            placeholder="Search categories..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
        </form>
      </div>

      {/* CATEGORIES TABLE */}
      <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm border-collapse">
            <thead>
              <tr className="border-b border-slate-200 dark:border-white/5 bg-slate-50/50 dark:bg-[#0a0a0f]/50 text-slate-400 text-xs font-semibold">
                <th className="p-4">Category ID</th>
                <th className="p-4">Category Name</th>
                {isAdmin && <th className="p-4 text-center">Actions</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-white/5">
              {loading ? (
                Array.from({ length: 3 }).map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    <td colSpan={3} className="p-4 h-12 bg-slate-50/10 dark:bg-[#0e0e13]/10" />
                  </tr>
                ))
              ) : categories.length === 0 ? (
                <tr>
                  <td colSpan={3} className="p-8 text-center text-slate-400">
                    No categories found
                  </td>
                </tr>
              ) : (
                categories.map((c) => (
                  <tr key={c.id} className="hover:bg-slate-50/50 dark:hover:bg-[#1c1c24]/10 text-slate-600 dark:text-slate-300">
                    <td className="p-4 font-mono text-xs text-indigo-400">{c.id}</td>
                    <td className="p-4 font-semibold text-slate-800 dark:text-white">{c.name}</td>
                    {isAdmin && (
                      <td className="p-4">
                        <div className="flex items-center justify-center gap-1.5">
                          <button
                            onClick={() => openEditModal(c)}
                            title="Edit Category"
                            className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 bg-slate-50 dark:bg-[#0a0a0f] text-slate-400 hover:text-emerald-500 hover:bg-slate-100 transition-colors cursor-pointer"
                          >
                            <Edit2 className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => openDeleteModal(c)}
                            title="Delete Category"
                            className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 bg-slate-50 dark:bg-[#0a0a0f] text-slate-400 hover:text-red-500 hover:bg-slate-100 transition-colors cursor-pointer"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* PAGINATION */}
        <div className="p-4 border-t border-slate-200 dark:border-white/5 flex items-center justify-between text-xs text-slate-400 bg-slate-50/50 dark:bg-[#0a0a0f]/50">
          <p>
            Showing {categories.length} of {totalElements} categories
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

      {/* CREATE MODAL */}
      {isAddModalOpen && (
        <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-sm p-6">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">Create Category</h3>
            <form onSubmit={handleSubmit(handleAddCategory)} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Category Name *</label>
                <input
                  type="text"
                  {...register("name")}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  placeholder="e.g. Office Supplies"
                />
                {errors.name && (
                  <p className="text-[10px] text-red-400 mt-1">{errors.name.message}</p>
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
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-indigo-500/10 cursor-pointer"
                >
                  {loading ? "Creating..." : "Create Category"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT MODAL */}
      {isEditModalOpen && selectedCategory && (
        <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-sm p-6">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">Edit Category</h3>
            <form onSubmit={handleSubmit(handleEditCategory)} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Category Name *</label>
                <input
                  type="text"
                  {...register("name")}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                />
                {errors.name && (
                  <p className="text-[10px] text-red-400 mt-1">{errors.name.message}</p>
                )}
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
                  {loading ? "Saving..." : "Save Changes"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* DELETE MODAL */}
      {isDeleteModalOpen && selectedCategory && (
        <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-sm p-6 text-center">
            <AlertTriangle className="w-12 h-12 text-red-500 mx-auto mb-4" />
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-2">Delete Category</h3>
            <p className="text-xs text-slate-400 mb-6">
              Are you sure you want to delete <span className="font-semibold text-slate-700 dark:text-white">{selectedCategory.name}</span>? This will detach the category classification from all linked products.
            </p>
            <div className="flex justify-center gap-3">
              <button
                onClick={() => setIsDeleteModalOpen(false)}
                className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-505 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs"
              >
                Cancel
              </button>
              <button
                onClick={handleDeleteCategory}
                disabled={loading}
                className="px-4 py-2 bg-red-600 hover:bg-red-500 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-red-500/10 cursor-pointer"
              >
                {loading ? "Deleting..." : "Delete Category"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
