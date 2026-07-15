import React, { useState } from "react";
import { Upload } from "lucide-react";
import { Loader } from "@/components/ui/Loader";
import { Modal } from "@/components/ui/Modal";

interface ImageUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (file: File) => Promise<void>;
  productName: string;
  uploading: boolean;
  progress: number;
}

export function ImageUploadModal({
  isOpen,
  onClose,
  onSubmit,
  productName,
  uploading,
  progress,
}: ImageUploadModalProps) {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFile) return;
    await onSubmit(selectedFile);
    setSelectedFile(null);
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Upload Product Image" maxWidthClass="max-w-md">
      <p className="text-xs text-slate-400 mb-4">
        Upload a cover image for product:{" "}
        <span className="font-semibold text-slate-700 dark:text-white">
          {productName}
        </span>
      </p>
      <form onSubmit={handleFormSubmit} className="space-y-4">
        <div className="flex flex-col items-center justify-center border-2 border-dashed border-slate-200 dark:border-white/5 rounded-2xl p-6 bg-slate-50 dark:bg-[#0a0a0f]">
          <Upload className="w-8 h-8 text-slate-400 mb-2" />
          <input
            type="file"
            accept="image/*"
            onChange={(e) => setSelectedFile(e.target.files ? e.target.files[0] : null)}
            className="block w-full text-xs text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-xl file:border-0 file:text-xs file:font-semibold file:bg-indigo-55 file:text-indigo-700 hover:file:bg-indigo-100"
          />
          {selectedFile && (
            <p className="text-[10px] text-slate-400 mt-2 font-mono truncate max-w-xs">
              {selectedFile.name} ({(selectedFile.size / 1024).toFixed(1)} KB)
            </p>
          )}
        </div>

        {uploading && (
          <div className="space-y-1">
            <div className="w-full bg-slate-200 dark:bg-white/10 h-1.5 rounded-full overflow-hidden">
              <div
                className="bg-indigo-500 h-full rounded-full transition-all duration-300"
                style={{ width: `${progress}%` }}
              />
            </div>
            <p className="text-[10px] text-slate-400 text-right">Uploading... {progress}%</p>
          </div>
        )}

        <div className="flex justify-end gap-3 pt-4 border-t border-slate-200 dark:border-white/5">
          <button
            type="button"
            onClick={onClose}
            disabled={uploading}
            className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-505 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs cursor-pointer"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={uploading || !selectedFile}
            className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-indigo-500/10 flex items-center gap-1.5 cursor-pointer"
          >
            {uploading ? (
              <Loader size={3.5} text="Uploading" className="flex-row gap-1" />
            ) : (
              "Upload Image"
            )}
          </button>
        </div>
      </form>
    </Modal>
  );
}
