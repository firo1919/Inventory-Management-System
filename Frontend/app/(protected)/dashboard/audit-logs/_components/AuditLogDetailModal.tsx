import React from "react";
import { Modal } from "@/components/ui/Modal";
import { AuditLog } from "@/types";

interface AuditLogDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  log: AuditLog | null;
}

export function AuditLogDetailModal({ isOpen, onClose, log }: AuditLogDetailModalProps) {
  if (!log) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Audit Log Metadata" maxWidthClass="max-w-lg">
      <div className="space-y-4">
        <div className="grid grid-cols-2 gap-4 text-xs">
          <div>
            <span className="text-[10px] text-slate-400 uppercase tracking-wider block">
              Timestamp
            </span>
            <span className="font-semibold">
              {new Date(log.timestamp || "").toLocaleString()}
            </span>
          </div>
          <div>
            <span className="text-[10px] text-slate-400 uppercase tracking-wider block">
              Correlation ID
            </span>
            <span className="font-mono text-indigo-400">
              {log.correlationId || "None"}
            </span>
          </div>
          <div>
            <span className="text-[10px] text-slate-400 uppercase tracking-wider block">
              Action
            </span>
            <span className="font-semibold text-slate-800 dark:text-white">
              {log.action}
            </span>
          </div>
          <div>
            <span className="text-[10px] text-slate-400 uppercase tracking-wider block">
              Resource Type
            </span>
            <span className="font-semibold">
              {log.resourceType}
            </span>
          </div>
          <div>
            <span className="text-[10px] text-slate-400 uppercase tracking-wider block">
              User ID
            </span>
            <span className="font-mono">
              {log.userId || "None"}
            </span>
          </div>
          <div>
            <span className="text-[10px] text-slate-400 uppercase tracking-wider block">
              Username
            </span>
            <span className="font-semibold">
              {log.username}
            </span>
          </div>
        </div>

        <div>
          <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-1">
            Status
          </span>
          <span
            className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-bold uppercase border ${
              log.status === "SUCCESS"
                ? "bg-emerald-500/10 text-emerald-500 border-emerald-500/20"
                : "bg-red-500/10 text-red-500 border-red-500/20"
            }`}
          >
            {log.status}
          </span>
        </div>

        {log.errorMessage && (
          <div>
            <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-1">
              Error Message
            </span>
            <p className="text-xs text-red-400 bg-red-500/5 p-3 rounded-xl border border-red-500/10">
              {log.errorMessage}
            </p>
          </div>
        )}
      </div>

      <div className="flex justify-end pt-6 border-t border-slate-200 dark:border-white/5 mt-6">
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
