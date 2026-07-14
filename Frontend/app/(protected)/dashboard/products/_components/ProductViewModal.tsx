import React from "react";
import { Image as ImageIcon } from "lucide-react";
import { Modal } from "@/components/ui/Modal";

interface ProductViewModalProps {
  isOpen: boolean;
  onClose: () => void;
  product: any;
}

export function ProductViewModal({ isOpen, onClose, product }: ProductViewModalProps) {
  if (!product) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Product Detail" maxWidthClass="max-w-lg">
      <div className="flex flex-col md:flex-row gap-6 mb-6">
        <div className="w-full md:w-40 h-40 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl flex items-center justify-center overflow-hidden shrink-0">
          {product.imageUrls && product.imageUrls.length > 0 ? (
            <img src={product.imageUrls[0]} alt={product.name} className="w-full h-full object-cover" />
          ) : (
            <ImageIcon className="w-8 h-8 text-slate-400" />
          )}
        </div>
        <div className="space-y-3 flex-1 min-w-0">
          <div>
            <h4 className="text-base font-bold text-slate-800 dark:text-white break-words">{product.name}</h4>
            <p className="text-xs text-slate-400 font-mono mt-0.5">SKU: {product.sku}</p>
          </div>
          <div className="grid grid-cols-2 gap-4 text-xs">
            <div>
              <span className="text-[10px] text-slate-400 uppercase tracking-wider block">Quantity</span>
              <span className="text-sm font-semibold">{product.quantity} units</span>
            </div>
            <div>
              <span className="text-[10px] text-slate-400 uppercase tracking-wider block">Threshold</span>
              <span className="text-sm font-semibold">{product.lowStockThreshold} units</span>
            </div>
            <div>
              <span className="text-[10px] text-slate-400 uppercase tracking-wider block">Selling Price</span>
              <span className="text-sm font-bold text-indigo-500">${product.sellingPrice?.toFixed(2)}</span>
            </div>
            <div>
              <span className="text-[10px] text-slate-400 uppercase tracking-wider block">Cost Price</span>
              <span className="text-sm font-bold">${product.costPrice?.toFixed(2)}</span>
            </div>
          </div>
        </div>
      </div>
      
      {product.description && (
        <div className="mb-6">
          <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-1">Description</span>
          <p className="text-xs text-slate-600 dark:text-slate-300 bg-slate-50 dark:bg-[#0a0a0f] p-3 rounded-xl border border-slate-200 dark:border-white/5">
            {product.description}
          </p>
        </div>
      )}

      <div className="flex justify-end">
        <button
          onClick={onClose}
          className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-505 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs cursor-pointer"
        >
          Close
        </button>
      </div>
    </Modal>
  );
}
