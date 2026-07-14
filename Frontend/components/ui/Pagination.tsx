import React from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";

interface PaginationProps {
  page: number;
  pageSize: number;
  totalPages: number;
  totalElements: number;
  onPageChange: (newPage: number) => void;
  loading?: boolean;
  itemName?: string;
}

export function Pagination({
  page,
  pageSize,
  totalPages,
  totalElements,
  onPageChange,
  loading = false,
  itemName = "items",
}: PaginationProps) {
  const currentCount = Math.min((page + 1) * pageSize, totalElements);
  const startCount = totalElements === 0 ? 0 : page * pageSize + 1;

  return (
    <div className="p-4 border-t border-slate-200 dark:border-white/5 flex items-center justify-between text-xs text-slate-400 bg-slate-50/50 dark:bg-[#0a0a0f]/50">
      <p>
        Showing {startCount}-{currentCount} of {totalElements} {itemName}
      </p>
      <div className="flex gap-2">
        <button
          disabled={page === 0 || loading}
          onClick={() => onPageChange(page - 1)}
          className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 disabled:opacity-50 hover:bg-slate-100 dark:hover:bg-white/5 cursor-pointer transition-colors"
        >
          <ChevronLeft className="w-4 h-4" />
        </button>
        <span className="py-1 px-3 bg-slate-100 dark:bg-white/5 rounded-lg font-semibold text-slate-700 dark:text-white">
          Page {page + 1} of {totalPages || 1}
        </span>
        <button
          disabled={page >= totalPages - 1 || loading}
          onClick={() => onPageChange(page + 1)}
          className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 disabled:opacity-50 hover:bg-slate-100 dark:hover:bg-white/5 cursor-pointer transition-colors"
        >
          <ChevronRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
