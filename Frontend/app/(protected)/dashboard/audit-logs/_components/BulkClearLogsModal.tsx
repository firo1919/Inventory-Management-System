import React, { useState } from "react";
import { AlertTriangle } from "lucide-react";
import { Modal } from "@/components/ui/Modal";

interface BulkClearLogsModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (startDate: string, endDate: string) => Promise<void>;
  loading: boolean;
}

export function BulkClearLogsModal({
  isOpen,
  onClose,
  onConfirm,
  loading,
}: BulkClearLogsModalProps) {
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!startDate || !endDate) return;
    await onConfirm(startDate, endDate);
    setStartDate("");
    setEndDate("");
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="" maxWidthClass="max-w-sm">
      <h3 className="text-lg font-bold dark:text-white mb-2 flex items-center gap-2 justify-center text-red-500">
        <AlertTriangle className="w-5 h-5 animate-pulse" /> Bulk Clear System Logs
      </h3>
      <p className="text-xs text-slate-400 text-center mb-4">
        Select the date range of logs to permanently delete from the database.
      </p>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1">Start Date *</label>
          <input
            type="date"
            required
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1">End Date *</label>
          <input
            type="date"
            required
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none"
          />
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
            disabled={loading || !startDate || !endDate}
            className="px-4 py-2 bg-red-600 hover:bg-red-500 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-red-500/10 cursor-pointer"
          >
            {loading ? "Clearing..." : "Delete Logs"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
