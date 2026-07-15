import React from "react";
import { Modal } from "@/components/ui/Modal";
import { Sale } from "@/types";

interface SaleViewModalProps {
  isOpen: boolean;
  onClose: () => void;
  sale: Sale | null;
  productMap: Record<string, string>;
}

export function SaleViewModal({ isOpen, onClose, sale, productMap }: SaleViewModalProps) {
  if (!sale) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="" maxWidthClass="max-w-md">
      <div className="flex items-center justify-between mb-5">
        <h3 className="text-lg font-bold text-slate-900 dark:text-white">
          Sale Details
        </h3>
        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase bg-emerald-500/10 text-emerald-500 border border-emerald-500/20">
          SALE
        </span>
      </div>

      <div className="grid grid-cols-2 gap-4 text-xs mb-4">
        <div className="col-span-2">
          <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">
            Transaction ID
          </span>
          <span className="font-mono text-indigo-400">
            {sale.id}
          </span>
        </div>
        <div>
          <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">
            Product
          </span>
          <span className="font-semibold text-slate-800 dark:text-white">
            {productMap[sale.productId] || sale.productName || "Unknown"}
          </span>
        </div>
        <div>
          <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">
            Quantity Sold
          </span>
          <span className="font-bold text-slate-800 dark:text-white text-lg">
            {sale.quantity}
          </span>
        </div>
        <div>
          <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">
            Unit Sale Price
          </span>
          <span className="font-semibold">
            ${sale.salePrice?.toFixed(2) ?? "—"}
          </span>
        </div>
        <div>
          <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">
            Total Revenue
          </span>
          <span className="font-bold text-emerald-500">
            ${(sale.quantity * (sale.salePrice || 0)).toFixed(2)}
          </span>
        </div>
        <div className="col-span-2">
          <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">
            Date Logged
          </span>
          <span>
            {new Date(
              sale.timestamp || sale.saleDate || sale.createdAt || ""
            ).toLocaleString()}
          </span>
        </div>
        {sale.employeeName && (
          <div>
            <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">
              Logged By
            </span>
            <span className="font-semibold">
              {sale.employeeName}
            </span>
          </div>
        )}
      </div>

      <div className="flex justify-end pt-4 border-t border-slate-200 dark:border-white/5">
        <button
          onClick={onClose}
          className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-550 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs font-semibold cursor-pointer"
        >
          Close
        </button>
      </div>
    </Modal>
  );
}
