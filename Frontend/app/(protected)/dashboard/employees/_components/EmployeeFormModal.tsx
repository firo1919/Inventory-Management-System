import React, { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { AlertTriangle } from "lucide-react";
import { employeeSchema, EmployeeInput } from "../schema";
import { Modal } from "@/components/ui/Modal";

interface EmployeeFormModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (data: EmployeeInput) => Promise<void>;
    initialData?: EmployeeInput | null;
    loading: boolean;
    title: string;
    submitLabel: string;
    formError?: string;
}

export function EmployeeFormModal({
    isOpen,
    onClose,
    onSubmit,
    initialData,
    loading,
    title,
    submitLabel,
    formError,
}: EmployeeFormModalProps) {
    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm<EmployeeInput>({
        resolver: zodResolver(employeeSchema),
        defaultValues: initialData || {
            firstName: "",
            lastName: "",
            username: "",
            email: "",
            password: "",
            phone: "",
            role: "EMPLOYEE",
        },
    });

    useEffect(() => {
        if (isOpen) {
            reset(
                initialData || {
                    firstName: "",
                    lastName: "",
                    username: "",
                    email: "",
                    password: "",
                    phone: "",
                    role: "EMPLOYEE",
                },
            );
        }
    }, [isOpen, initialData, reset]);

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title={title}
            maxWidthClass="max-w-md"
        >
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                {formError && (
                    <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-xs p-3 rounded-lg flex items-center gap-1.5">
                        <AlertTriangle className="w-4 h-4" />
                        <span>{formError}</span>
                    </div>
                )}

                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-xs font-semibold text-slate-400 mb-1">
                            First Name *
                        </label>
                        <input
                            type="text"
                            {...register("firstName")}
                            className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                        />
                        {errors.firstName && (
                            <p className="text-[10px] text-red-400 mt-1">
                                {errors.firstName.message}
                            </p>
                        )}
                    </div>
                    <div>
                        <label className="block text-xs font-semibold text-slate-400 mb-1">
                            Last Name *
                        </label>
                        <input
                            type="text"
                            {...register("lastName")}
                            className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                        />
                        {errors.lastName && (
                            <p className="text-[10px] text-red-400 mt-1">
                                {errors.lastName.message}
                            </p>
                        )}
                    </div>
                </div>

                <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">
                        Username *
                    </label>
                    <input
                        type="text"
                        {...register("username")}
                        className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                    />
                    {errors.username && (
                        <p className="text-[10px] text-red-400 mt-1">
                            {errors.username.message}
                        </p>
                    )}
                </div>

                <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">
                        Email Address *
                    </label>
                    <input
                        type="email"
                        {...register("email")}
                        className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                    />
                    {errors.email && (
                        <p className="text-[10px] text-red-400 mt-1">
                            {errors.email.message}
                        </p>
                    )}
                </div>

                <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">
                        {initialData
                            ? "Password (leave empty or type new one) *"
                            : "Temporary Password (min 8 chars) *"}
                    </label>
                    <input
                        type="password"
                        {...register("password")}
                        className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                    />
                    {errors.password && (
                        <p className="text-[10px] text-red-400 mt-1">
                            {errors.password.message}
                        </p>
                    )}
                </div>

                <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">
                        Phone Number *
                    </label>
                    <input
                        type="text"
                        {...register("phone")}
                        className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                        placeholder="+1234567890"
                    />
                    {errors.phone && (
                        <p className="text-[10px] text-red-400 mt-1">
                            {errors.phone.message}
                        </p>
                    )}
                </div>

                <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">
                        Select Role *
                    </label>
                    <select
                        {...register("role")}
                        className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs font-semibold text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                    >
                        <option value="EMPLOYEE">EMPLOYEE</option>
                        <option value="ADMIN">ADMIN</option>
                    </select>
                    {errors.role && (
                        <p className="text-[10px] text-red-400 mt-1">
                            {errors.role.message}
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
