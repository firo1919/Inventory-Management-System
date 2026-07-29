/* eslint-disable react-hooks/incompatible-library */
import React, { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { AlertTriangle } from "lucide-react";
import { restockSchema, RestockInput } from "../schema";
import SearchableSelect from "@/components/ui/SearchableSelect";
import { Loader } from "@/components/ui/Loader";
import { Modal } from "@/components/ui/Modal";
import { Product } from "@/types";

interface RestockFormModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (data: RestockInput) => Promise<void>;
    loading: boolean;
    formError?: string;
}

export function RestockFormModal({
    isOpen,
    onClose,
    onSubmit,
    loading,
    formError,
}: RestockFormModalProps) {
    const [selectedProductDetails, setSelectedProductDetails] =
        useState<Product | null>(null);

    const {
        register,
        handleSubmit,
        setValue,
        watch,
        reset,
        formState: { errors },
    } = useForm<RestockInput>({
        resolver: zodResolver(restockSchema),
        defaultValues: {
            productId: "",
            quantity: 1,
        },
    });

    useEffect(() => {
        if (isOpen) {
            reset({
                productId: "",
                quantity: 1,
            });
            setSelectedProductDetails(null);
        }
    }, [isOpen, reset]);

    const handleProductSelect = (productId: string, rawProduct?: Product) => {
        setValue("productId", productId);
        if (rawProduct) {
            setSelectedProductDetails(rawProduct);
        } else {
            setSelectedProductDetails(null);
        }
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title="Log Product Restock"
            maxWidthClass="max-w-md"
        >
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                {formError && (
                    <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-xs p-3 rounded-lg flex items-center gap-1.5">
                        <AlertTriangle className="w-4 h-4" />
                        <span>{formError}</span>
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
                        placeholder="Search & select product..."
                        label="Select Product"
                        required
                        pageSize={20}
                    />
                    {errors.productId && (
                        <p className="text-[10px] text-red-400 mt-1">
                            {errors.productId.message}
                        </p>
                    )}
                </div>

                {selectedProductDetails && (
                    <div className="p-3 bg-indigo-500/5 border border-indigo-500/10 rounded-xl text-[11px] space-y-1 text-indigo-400">
                        <p>
                            • In Inventory: {selectedProductDetails.quantity}{" "}
                            items
                        </p>
                        <p>
                            • Cost Price: $
                            {selectedProductDetails.costPrice?.toFixed(2)}
                        </p>
                    </div>
                )}

                <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">
                        Quantity Restocked *
                    </label>
                    <input
                        type="number"
                        {...register("quantity", { valueAsNumber: true })}
                        className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                    />
                    {errors.quantity && (
                        <p className="text-[10px] text-red-400 mt-1">
                            {errors.quantity.message}
                        </p>
                    )}
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
                            <Loader
                                size={3.5}
                                text="Logging..."
                                className="flex-row gap-1"
                            />
                        ) : (
                            "Record Restock"
                        )}
                    </button>
                </div>
            </form>
        </Modal>
    );
}
