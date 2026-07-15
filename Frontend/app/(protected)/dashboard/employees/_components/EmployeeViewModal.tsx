import React from "react";
import { Shield } from "lucide-react";
import { Modal } from "@/components/ui/Modal";
import { Employee } from "@/types";

interface EmployeeViewModalProps {
  isOpen: boolean;
  onClose: () => void;
  employee: Employee | null;
}

export function EmployeeViewModal({
  isOpen,
  onClose,
  employee,
}: EmployeeViewModalProps) {
  if (!employee) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="" maxWidthClass="max-w-md">
      <div className="flex items-center justify-between mb-5">
        <h3 className="text-lg font-bold text-slate-900 dark:text-white">Employee details</h3>
        <span
          className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase border ${
            employee.role === "ADMIN"
              ? "bg-purple-500/10 text-purple-500 border-purple-500/20"
              : "bg-blue-500/10 text-blue-500 border-blue-500/20"
          }`}
        >
          <Shield className="w-3 h-3" />
          {employee.role}
        </span>
      </div>

      <div className="grid grid-cols-2 gap-4 text-xs mb-6">
        <div>
          <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">First Name</span>
          <span className="font-semibold text-slate-800 dark:text-white">{employee.firstName}</span>
        </div>
        <div>
          <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">Last Name</span>
          <span className="font-semibold text-slate-800 dark:text-white">{employee.lastName}</span>
        </div>
        <div>
          <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">Username</span>
          <span className="font-mono">{employee.username}</span>
        </div>
        <div>
          <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">User ID</span>
          <span className="font-mono text-slate-500">{employee.id}</span>
        </div>
        <div className="col-span-2">
          <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">Email Address</span>
          <span className="font-semibold">{employee.email}</span>
        </div>
        <div>
          <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">Phone Number</span>
          <span>{employee.phone || "—"}</span>
        </div>
        <div>
          <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">Account Status</span>
          <span
            className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded text-[10px] font-bold uppercase border ${
              employee.active
                ? "bg-emerald-500/10 text-emerald-500 border-emerald-500/20"
                : "bg-red-500/10 text-red-500 border-red-500/20"
            }`}
          >
            {employee.active ? "Active" : "Deactivated"}
          </span>
        </div>
      </div>

      <div className="flex justify-end pt-4 border-t border-slate-200 dark:border-white/5">
        <button
          onClick={onClose}
          className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-505 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs font-semibold cursor-pointer"
        >
          Close
        </button>
      </div>
    </Modal>
  );
}
