/* eslint-disable react-hooks/incompatible-library */
import React, { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { AlertTriangle } from "lucide-react";
import { saleSchema, SaleInput } from "../schema";
import SearchableSelect from "@/components/ui/SearchableSelect";
import { Loader } from "@/components/ui/Loader";
import { Modal } from "@/components/ui/Modal";
import { Product } from "@/types";

interface SaleFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: SaleInput) => Promise<void>;
  loading: boolean;
  formError?: string;
}

export function SaleFormModal({
  isOpen,
  onClose,
  onSubmit,
  loading,
  formError: externalFormError,
}: SaleFormModalProps) {
  const [selectedProductDetails, setSelectedProductDetails] = useState<Product | null>(null);
  const [formError, setFormError] = useState("");

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors },
  } = useForm<SaleInput>({
    resolver: zodResolver(saleSchema),
    defaultValues: {
      productId: "",
      quantity: 1,
      salePrice: 0,
    },
  });

  useEffect(() => {
    if (externalFormError) {
      setFormError(externalFormError);
    } else {
      setFormError("");
    }
  }, [externalFormError]);

  useEffect(() => {
    if (isOpen) {
      reset({
        productId: "",
        quantity: 1,
        salePrice: 0,
      });
      setSelectedProductDetails(null);
      setFormError("");
    }
  }, [isOpen, reset]);

  const handleProductSelect = (productId: string, rawProduct?: Product) => {
    setValue("productId", productId);
    if (rawProduct) {
      setSelectedProductDetails(rawProduct);
      setValue("salePrice", rawProduct.sellingPrice || 0);
    } else {
      setSelectedProductDetails(null);
    }
  };

  const onFormSubmit = async (data: SaleInput) => {
    setFormError("");

    if (selectedProductDetails && data.quantity > selectedProductDetails.quantity) {
      setFormError(
        `Insufficient stock! Only ${selectedProductDetails.quantity} items left in inventory.`
      );
      return;
    }

    await onSubmit(data);
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Record Product Sale" maxWidthClass="max-w-md">
      <form onSubmit={handleSubmit(onFormSubmit)} className="space-y-4">
        {(formError || externalFormError) && (
          <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-xs p-3 rounded-lg flex items-center gap-1.5 animate-fade-in">
            <AlertTriangle className="w-4 h-4" />
            <span>{formError || externalFormError}</span>
          </div>
        )}

        <div>
          <SearchableSelect
            endpoint="/api/v1/products"
            mapItem={(p: Product) => ({
              value: p.id,
              label: p.name,
              subLabel: `${p.quantity} currently in stock · cost $${p.costPrice?.toFixed(2)}`,
              disabled: !p.active,
              raw: p,
            })}
            value={watch("productId")}
            onChange={handleProductSelect}
            placeholder="Type to search product..."
            label="Select Product"
            required
            pageSize={20}
          />
          {errors.productId && (
            <p className="text-[10px] text-red-400 mt-1">{errors.productId.message}</p>
          )}
          {selectedProductDetails && (
            <p className="text-[10px] text-slate-400 mt-1 font-medium">
              Stock available:{" "}
              <span className="font-semibold text-slate-600 dark:text-slate-350">
                {selectedProductDetails.quantity} units
              </span>{" "}
              (Threshold: {selectedProductDetails.lowStockThreshold} units)
            </p>
          )}
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1">
              Quantity Sold *
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
            <label className="block text-xs font-semibold text-slate-400 mb-1">
              Sale Price ($) *
            </label>
            <input
              type="number"
              step="0.01"
              {...register("salePrice", { valueAsNumber: true })}
              className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
            />
            {errors.salePrice && (
              <p className="text-[10px] text-red-400 mt-1">{errors.salePrice.message}</p>
            )}
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
            className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-indigo-500/10 flex items-center gap-1.5 cursor-pointer"
          >
            {loading ? (
              <Loader size={3.5} text="Logging..." className="flex-row gap-1" />
            ) : (
              "Record Sale"
            )}
          </button>
        </div>
      </form>
    </Modal>
  );
}
