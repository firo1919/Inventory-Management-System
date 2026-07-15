import React, { useState, useEffect } from "react";
import { Image as ImageIcon, Calendar, Percent, Tag, ShieldCheck } from "lucide-react";
import { Modal } from "@/components/ui/Modal";

interface ProductViewModalProps {
  isOpen: boolean;
  onClose: () => void;
  product: any;
}

export function ProductViewModal({ isOpen, onClose, product }: ProductViewModalProps) {
  const [activeImageIndex, setActiveImageIndex] = useState(0);

  // Reset active image index when modal opens or product changes
  useEffect(() => {
    if (isOpen) {
      setActiveImageIndex(0);
    }
  }, [isOpen, product]);

  if (!product) return null;

  const hasImages = product.imageUrls && product.imageUrls.length > 0;
  const currentImageUrl = hasImages ? product.imageUrls[activeImageIndex] : null;

  // Calculations
  const profit = product.sellingPrice - product.costPrice;
  const margin = product.sellingPrice > 0 ? (profit / product.sellingPrice) * 100 : 0;
  const totalValuation = product.sellingPrice * product.quantity;

  const isOutOfStock = product.quantity === 0;
  const isLowStock = product.quantity <= product.lowStockThreshold && product.quantity > 0;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Product Detail" maxWidthClass="max-w-2xl">
      <div className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-12 gap-6">
          {/* LEFT: IMAGE GALLERY (5 cols) */}
          <div className="md:col-span-5 flex flex-col gap-3">
            <div className="aspect-square bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-2xl flex items-center justify-center overflow-hidden relative shadow-inner">
              {currentImageUrl ? (
                <img
                  src={currentImageUrl}
                  alt={product.name}
                  className="w-full h-full object-cover transition-all duration-300 hover:scale-105"
                />
              ) : (
                <ImageIcon className="w-12 h-12 text-slate-400" />
              )}

              {/* Status Badge */}
              <div className="absolute top-3 left-3">
                <span
                  className={`px-2 py-0.5 rounded-lg text-[9px] font-extrabold uppercase tracking-wider ${
                    isOutOfStock
                      ? "bg-red-500/15 text-red-500 border border-red-500/20"
                      : isLowStock
                      ? "bg-amber-500/15 text-amber-500 border border-amber-500/20"
                      : "bg-emerald-500/15 text-emerald-500 border border-emerald-500/20"
                  }`}
                >
                  {isOutOfStock ? "Out of Stock" : isLowStock ? "Low Stock" : "In Stock"}
                </span>
              </div>
            </div>

            {/* Thumbnails list */}
            {hasImages && product.imageUrls.length > 1 && (
              <div className="flex gap-2 overflow-x-auto py-1 custom-scrollbar">
                {product.imageUrls.map((url: string, index: number) => (
                  <button
                    key={index}
                    onClick={() => setActiveImageIndex(index)}
                    className={`w-12 h-12 rounded-lg border overflow-hidden shrink-0 transition-all ${
                      activeImageIndex === index
                        ? "border-indigo-500 ring-2 ring-indigo-500/20 scale-95"
                        : "border-slate-200 dark:border-white/5 hover:border-slate-400"
                    }`}
                  >
                    <img src={url} alt={`Thumbnail ${index + 1}`} className="w-full h-full object-cover" />
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* RIGHT: MAIN DETAILS (7 cols) */}
          <div className="md:col-span-7 space-y-4">
            <div>
              <h4 className="text-lg font-extrabold text-slate-900 dark:text-white leading-snug">
                {product.name}
              </h4>
              <div className="flex items-center gap-3 mt-1.5 text-xs text-slate-400 font-medium">
                <span className="font-mono bg-slate-100 dark:bg-white/5 px-2 py-0.5 rounded text-[10px] text-indigo-400">
                  SKU: {product.sku}
                </span>
                <span>•</span>
                <span className="flex items-center gap-1">
                  <Tag className="w-3.5 h-3.5" /> Catalog Product
                </span>
              </div>
            </div>

            {/* Price Cards */}
            <div className="grid grid-cols-2 gap-3.5 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-100 dark:border-white/5 p-4 rounded-2xl">
              <div>
                <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider block">
                  Selling Price
                </span>
                <span className="text-lg font-black text-indigo-500 mt-0.5 block">
                  ${product.sellingPrice?.toFixed(2)}
                </span>
              </div>
              <div>
                <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider block">
                  Cost Price
                </span>
                <span className="text-lg font-extrabold text-slate-700 dark:text-slate-200 mt-0.5 block">
                  ${product.costPrice?.toFixed(2)}
                </span>
              </div>
            </div>

            {/* Inventory Metrics */}
            <div className="grid grid-cols-3 gap-3 text-xs">
              <div className="bg-slate-50/50 dark:bg-white/[0.01] border border-slate-100 dark:border-white/5 p-3 rounded-xl">
                <span className="text-[9px] font-semibold text-slate-400 uppercase block">Stock Qty</span>
                <span className="text-sm font-bold text-slate-800 dark:text-white mt-1 block">
                  {product.quantity} units
                </span>
              </div>
              <div className="bg-slate-50/50 dark:bg-white/[0.01] border border-slate-100 dark:border-white/5 p-3 rounded-xl">
                <span className="text-[9px] font-semibold text-slate-400 uppercase block">Min Threshold</span>
                <span className="text-sm font-bold text-slate-800 dark:text-white mt-1 block">
                  {product.lowStockThreshold} units
                </span>
              </div>
              <div className="bg-slate-50/50 dark:bg-white/[0.01] border border-slate-100 dark:border-white/5 p-3 rounded-xl">
                <span className="text-[9px] font-semibold text-slate-400 uppercase block">Total Valuation</span>
                <span className="text-sm font-bold text-emerald-500 mt-1 block">
                  ${totalValuation.toFixed(2)}
                </span>
              </div>
            </div>

            {/* Financial Margin stats */}
            <div className="flex items-center gap-3.5 text-xs text-slate-500 dark:text-slate-400 border-t border-slate-200/50 dark:border-white/5 pt-3.5">
              <span className="flex items-center gap-1 font-medium">
                <Percent className="w-3.5 h-3.5 text-slate-400" /> Margin:{" "}
                <span className="font-bold text-slate-800 dark:text-white">{margin.toFixed(1)}%</span>
              </span>
              <span>•</span>
              <span className="flex items-center gap-1 font-medium">
                <ShieldCheck className="w-3.5 h-3.5 text-slate-400" /> Unit Profit:{" "}
                <span className="font-bold text-emerald-500">${profit.toFixed(2)}</span>
              </span>
            </div>
          </div>
        </div>

        {/* Description Section */}
        {product.description && (
          <div className="border-t border-slate-200/50 dark:border-white/5 pt-4">
            <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider block mb-1.5">
              Product Description
            </span>
            <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed bg-slate-50 dark:bg-[#0a0a0f] border border-slate-100 dark:border-white/5 p-3.5 rounded-2xl">
              {product.description}
            </p>
          </div>
        )}

        {/* Timestamps */}
        <div className="flex flex-col sm:flex-row gap-3 justify-between items-start sm:items-center text-[10px] text-slate-400 border-t border-slate-200/50 dark:border-white/5 pt-4">
          <div className="flex gap-4">
            <span className="flex items-center gap-1">
              <Calendar className="w-3 h-3" /> Added: {product.createdAt || "N/A"}
            </span>
            <span className="flex items-center gap-1">
              <Calendar className="w-3 h-3" /> Modified: {product.updatedAt || "N/A"}
            </span>
          </div>
          <button
            onClick={onClose}
            className="w-full sm:w-auto px-5 py-2 bg-slate-100 dark:bg-white/5 hover:bg-slate-200 dark:hover:bg-white/10 text-slate-700 dark:text-slate-300 font-semibold rounded-xl transition-all cursor-pointer text-xs"
          >
            Close Details
          </button>
        </div>
      </div>
    </Modal>
  );
}
