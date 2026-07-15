import React, { useEffect, useState } from "react";
import { createPortal } from "react-dom";

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  children: React.ReactNode;
  maxWidthClass?: string; // e.g. "max-w-sm", "max-w-md", "max-w-lg", "max-w-xl", "max-w-2xl", "max-w-3xl", "max-w-4xl"
}

export function Modal({
  isOpen,
  onClose,
  title,
  children,
  maxWidthClass = "max-w-sm",
}: ModalProps) {
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setTimeout(() => setMounted(true), 0);
    if (!isOpen) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen || !mounted) return null;

  const modalContent = (
    <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-[100] flex items-center justify-center p-4">
      {/* Backdrop click close */}
      <div className="absolute inset-0 cursor-default" onClick={onClose} />
      <div className={`bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full ${maxWidthClass} p-6 relative z-10 shadow-xl max-h-[90vh] overflow-y-auto`}>
        {title && (
          <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">
            {title}
          </h3>
        )}
        {children}
      </div>
    </div>
  );

  return createPortal(modalContent, document.body);
}
