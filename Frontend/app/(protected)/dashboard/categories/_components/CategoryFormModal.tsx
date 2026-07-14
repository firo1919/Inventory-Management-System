import React from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { categorySchema, CategoryInput } from "../schema";
import { Modal } from "@/components/ui/Modal";

interface CategoryFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: CategoryInput) => Promise<void>;
  initialData?: { name: string } | null;
  loading: boolean;
  title: string;
  submitLabel: string;
}

export function CategoryFormModal({
  isOpen,
  onClose,
  onSubmit,
  initialData,
  loading,
  title,
  submitLabel,
}: CategoryFormModalProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<CategoryInput>({
    resolver: zodResolver(categorySchema),
    defaultValues: initialData || { name: "" },
  });

  React.useEffect(() => {
    if (isOpen) {
      reset(initialData || { name: "" });
    }
  }, [isOpen, initialData, reset]);

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title}>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1">
            Category Name *
          </label>
          <input
            type="text"
            {...register("name")}
            className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
            placeholder="e.g. Office Supplies"
          />
          {errors.name && (
            <p className="text-[10px] text-red-400 mt-1">
              {errors.name.message}
            </p>
          )}
        </div>

        <div className="flex justify-end gap-3 pt-4 border-t border-slate-200 dark:border-white/5">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-500 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs cursor-pointer"
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
