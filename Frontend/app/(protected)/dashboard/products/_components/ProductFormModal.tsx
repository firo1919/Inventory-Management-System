/* eslint-disable react-hooks/incompatible-library */
import React, { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { productSchema, ProductInput } from "../schema";
import { Modal } from "@/components/ui/Modal";
import { Category } from "@/types";

interface ProductFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: ProductInput) => Promise<void>;
  initialData?: ProductInput | null;
  categories: Category[];
  loading: boolean;
  title: string;
  submitLabel: string;
}

export function ProductFormModal({
  isOpen,
  onClose,
  onSubmit,
  initialData,
  categories,
  loading,
  title,
  submitLabel,
}: ProductFormModalProps) {
  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors },
  } = useForm<ProductInput>({
    resolver: zodResolver(productSchema),
    defaultValues: initialData || {
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

  useEffect(() => {
    if (isOpen) {
      reset(
        initialData || {
          name: "",
          sku: "",
          description: "",
          costPrice: 0,
          sellingPrice: 0,
          quantity: 0,
          lowStockThreshold: 10,
          categoryIds: [],
        }
      );
    }
  }, [isOpen, initialData, reset]);

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title} maxWidthClass="max-w-lg">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
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
            <label className="block text-xs font-semibold text-slate-400 mb-1">
              {initialData ? "Current Quantity *" : "Initial Quantity *"}
            </label>
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
          <div className="flex flex-wrap gap-2 max-h-28 overflow-y-auto p-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl animate-fade-in">
            {categories.map((c) => {
              const isSelected = categoryIds.includes(c.id);
              return (
                <button
                  type="button"
                  key={c.id}
                  onClick={() => handleCategorySelection(c.id)}
                  className={`px-3 py-1 rounded-lg text-xs font-semibold transition-all border cursor-pointer ${
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
            onClick={onClose}
            className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-505 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs cursor-pointer"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading}
            className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-indigo-500/10 cursor-pointer"
          >
            {loading ? "Saving..." : submitLabel}
          </button>
        </div>
      </form>
    </Modal>
  );
}
