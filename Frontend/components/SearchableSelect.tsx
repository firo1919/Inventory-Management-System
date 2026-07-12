"use client";

import React, { useState, useEffect, useRef, useCallback } from "react";
import { Search, ChevronDown, X, Loader2, Package } from "lucide-react";
import { apiClient } from "@/lib/api-client";

interface SelectOption {
  value: string;
  label: string;
  subLabel?: string;
  disabled?: boolean;
  raw?: any;
}

interface SearchableSelectProps {
  /** URL to fetch from e.g. "/api/v1/products" */
  endpoint: string;
  /** map a raw API item to SelectOption */
  mapItem: (item: any) => SelectOption;
  value: string;
  onChange: (value: string, raw?: any) => void;
  placeholder?: string;
  label?: string;
  required?: boolean;
  /** Extra query params merged into every request */
  extraParams?: Record<string, any>;
  pageSize?: number;
}

export default function SearchableSelect({
  endpoint,
  mapItem,
  value,
  onChange,
  placeholder = "Search...",
  label,
  required,
  extraParams = {},
  pageSize = 20,
}: SearchableSelectProps) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState("");
  const [options, setOptions] = useState<SelectOption[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(false);
  const [selectedLabel, setSelectedLabel] = useState("");

  const containerRef = useRef<HTMLDivElement>(null);
  const searchRef = useRef<HTMLInputElement>(null);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Fetch items from API
  const fetchOptions = useCallback(
    async (searchTerm: string, pageNum: number, append = false) => {
      setLoading(true);
      try {
        const res = await apiClient.get(endpoint, {
          params: { page: pageNum, size: pageSize, ...extraParams },
        });
        const content: any[] = res.data?.content || [];
        const totalPages: number = res.data?.totalPages || 1;

        // Client-side filter on search term (works even if API has no search param)
        const filtered = searchTerm
          ? content.filter((item) => {
              const opt = mapItem(item);
              return opt.label.toLowerCase().includes(searchTerm.toLowerCase());
            })
          : content;

        const mapped = filtered.map(mapItem);
        setOptions((prev) => (append ? [...prev, ...mapped] : mapped));
        setHasMore(pageNum < totalPages - 1);
        setPage(pageNum);
      } catch {
        // silent — parent handles data errors
      } finally {
        setLoading(false);
      }
    },
    [endpoint, pageSize, extraParams, mapItem]
  );

  // Debounced search
  useEffect(() => {
    if (!open) return;
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      fetchOptions(search, 0, false);
    }, 300);
    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, [search, open, fetchOptions]);

  // Open dropdown: fetch first page
  const handleOpen = () => {
    setOpen(true);
    setSearch("");
    setOptions([]);
    fetchOptions("", 0, false);
    setTimeout(() => searchRef.current?.focus(), 50);
  };

  // Close on outside click
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  const handleSelect = (opt: SelectOption) => {
    onChange(opt.value, opt.raw);
    setSelectedLabel(opt.label);
    setOpen(false);
    setSearch("");
  };

  const handleClear = (e: React.MouseEvent) => {
    e.stopPropagation();
    onChange("", undefined);
    setSelectedLabel("");
  };

  const loadMore = () => {
    fetchOptions(search, page + 1, true);
  };

  return (
    <div ref={containerRef} className="relative">
      {label && (
        <label className="block text-xs font-semibold text-slate-400 mb-1">
          {label} {required && <span className="text-red-400">*</span>}
        </label>
      )}

      {/* Trigger button */}
      <button
        type="button"
        onClick={handleOpen}
        className={`w-full flex items-center justify-between px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border rounded-xl text-xs text-left transition-colors ${
          open
            ? "border-indigo-500 ring-1 ring-indigo-500/30"
            : "border-slate-200 dark:border-white/5 hover:border-slate-300 dark:hover:border-white/10"
        }`}
      >
        <span className={value ? "text-slate-800 dark:text-white" : "text-slate-400"}>
          {value ? selectedLabel || options.find((o) => o.value === value)?.label || value : placeholder}
        </span>
        <span className="flex items-center gap-1 shrink-0 ml-2">
          {value && (
            <span
              onClick={handleClear}
              className="p-0.5 rounded hover:bg-slate-200 dark:hover:bg-white/10 text-slate-400 cursor-pointer"
            >
              <X className="w-3 h-3" />
            </span>
          )}
          <ChevronDown className={`w-3.5 h-3.5 text-slate-400 transition-transform ${open ? "rotate-180" : ""}`} />
        </span>
      </button>

      {/* Dropdown */}
      {open && (
        <div className="absolute z-50 mt-1 w-full bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/10 rounded-xl shadow-xl overflow-hidden">
          {/* Search input */}
          <div className="p-2 border-b border-slate-100 dark:border-white/5">
            <div className="relative">
              <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-slate-400 pointer-events-none" />
              <input
                ref={searchRef}
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Type to filter..."
                className="w-full pl-8 pr-3 py-1.5 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-lg text-xs focus:outline-none focus:ring-1 focus:ring-indigo-500 text-slate-800 dark:text-white"
              />
            </div>
          </div>

          {/* Options list */}
          <div className="max-h-52 overflow-y-auto">
            {loading && options.length === 0 ? (
              <div className="flex items-center justify-center gap-2 py-6 text-slate-400 text-xs">
                <Loader2 className="w-4 h-4 animate-spin" /> Loading...
              </div>
            ) : options.length === 0 ? (
              <div className="flex flex-col items-center justify-center gap-2 py-6 text-slate-400 text-xs">
                <Package className="w-5 h-5 opacity-40" />
                No results found
              </div>
            ) : (
              <>
                {options.map((opt) => (
                  <button
                    key={opt.value}
                    type="button"
                    disabled={opt.disabled}
                    onClick={() => !opt.disabled && handleSelect(opt)}
                    className={`w-full flex flex-col items-start px-3 py-2 text-xs text-left transition-colors ${
                      opt.disabled
                        ? "opacity-40 cursor-not-allowed text-slate-400"
                        : opt.value === value
                        ? "bg-indigo-500/10 text-indigo-500"
                        : "hover:bg-slate-50 dark:hover:bg-white/5 text-slate-700 dark:text-slate-300 cursor-pointer"
                    }`}
                  >
                    <span className="font-semibold">{opt.label}</span>
                    {opt.subLabel && <span className="text-slate-400 text-[10px]">{opt.subLabel}</span>}
                  </button>
                ))}

                {/* Load more */}
                {hasMore && (
                  <button
                    type="button"
                    onClick={loadMore}
                    disabled={loading}
                    className="w-full py-2 text-[10px] text-indigo-400 hover:text-indigo-300 font-semibold border-t border-slate-100 dark:border-white/5 flex items-center justify-center gap-1.5 cursor-pointer"
                  >
                    {loading ? <Loader2 className="w-3 h-3 animate-spin" /> : null}
                    Load more results
                  </button>
                )}
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
