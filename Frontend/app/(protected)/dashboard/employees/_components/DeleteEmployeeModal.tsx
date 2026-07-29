import React from "react";
import { AlertTriangle } from "lucide-react";
import { Modal } from "@/components/ui/Modal";

interface DeleteEmployeeModalProps {
    isOpen: boolean;
    onClose: () => void;
    onConfirm: () => Promise<void>;
    employeeName: string;
    loading: boolean;
}

export function DeleteEmployeeModal({
    isOpen,
    onClose,
    onConfirm,
    employeeName,
    loading,
}: DeleteEmployeeModalProps) {
    return (
        <Modal isOpen={isOpen} onClose={onClose}>
            <div className="text-center">
                <AlertTriangle className="w-12 h-12 text-red-500 mx-auto mb-4" />
                <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-2">
                    Delete Employee
                </h3>
                <p className="text-xs text-slate-400 mb-6">
                    Are you sure you want to delete{" "}
                    <span className="font-semibold text-slate-700 dark:text-white">
                        {employeeName}
                    </span>
                    ? This action is permanent and will remove access for this
                    employee.
                </p>
                <div className="flex justify-center gap-3">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-505 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs cursor-pointer"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={onConfirm}
                        disabled={loading}
                        className="px-4 py-2 bg-red-600 hover:bg-red-500 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-red-500/10 cursor-pointer"
                    >
                        {loading ? "Deleting..." : "Delete Employee"}
                    </button>
                </div>
            </div>
        </Modal>
    );
}
