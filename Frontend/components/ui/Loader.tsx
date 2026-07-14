import React from "react";
import { Loader2 } from "lucide-react";

interface LoaderProps {
  className?: string;
  size?: number;
  text?: string;
}

export function Loader({ className = "", size = 6, text }: LoaderProps) {
  return (
    <div className={`flex flex-col items-center justify-center gap-2 ${className}`}>
      <Loader2 className="animate-spin text-indigo-500" style={{ width: `${size * 4}px`, height: `${size * 4}px` }} />
      {text && <p className="text-slate-400 text-xs font-medium animate-pulse">{text}</p>}
    </div>
  );
}
