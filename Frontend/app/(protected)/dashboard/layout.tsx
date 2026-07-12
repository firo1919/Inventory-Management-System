"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/hooks/useAuth";
import {
  LayoutDashboard,
  Box,
  Tag,
  TrendingUp,
  ShoppingBag,
  Users,
  FileSpreadsheet,
  UserCircle,
  LogOut,
  Menu,
  X,
  ChevronLeft,
  ChevronRight,
  Sun,
  Moon,
  Warehouse,
  Bell,
  Loader2,
} from "lucide-react";

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const { user, role, isAdmin, logout, loading } = useAuth();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [darkMode, setDarkMode] = useState(true);

  // Restore sidebar state from localStorage on mount
  useEffect(() => {
    const saved = localStorage.getItem("sidebarCollapsed");
    if (saved !== null) setSidebarCollapsed(saved === "true");
  }, []);

  // Persist sidebar state on change
  const toggleSidebar = () => {
    const next = !sidebarCollapsed;
    setSidebarCollapsed(next);
    localStorage.setItem("sidebarCollapsed", String(next));
  };

  // Initialize theme on mount
  useEffect(() => {
    const root = window.document.documentElement;
    const initialDark = root.classList.contains("dark") || root.style.colorScheme === "dark";
    setDarkMode(initialDark);
  }, []);


  const toggleTheme = () => {
    const root = window.document.documentElement;
    if (darkMode) {
      root.classList.remove("dark");
      root.classList.add("light");
      root.style.colorScheme = "light";
      setDarkMode(false);
    } else {
      root.classList.remove("light");
      root.classList.add("dark");
      root.style.colorScheme = "dark";
      setDarkMode(true);
    }
  };

  const navItems = [
    { name: "Overview", href: "/dashboard", icon: LayoutDashboard, role: "ALL" },
    { name: "Products", href: "/dashboard/products", icon: Box, role: "ALL" },
    { name: "Categories", href: "/dashboard/categories", icon: Tag, role: "ALL" },
    { name: "Sales Log", href: "/dashboard/sales", icon: TrendingUp, role: "ALL" },
    { name: "Restocks Log", href: "/dashboard/restocks", icon: ShoppingBag, role: "ALL" },
    { name: "My Profile", href: "/dashboard/profile", icon: UserCircle, role: "ALL" },
    { name: "Employees", href: "/dashboard/employees", icon: Users, role: "ADMIN" },
    { name: "Audit Logs", href: "/dashboard/audit-logs", icon: FileSpreadsheet, role: "ADMIN" },
  ];

  const filteredNavItems = navItems.filter(
    (item) => item.role === "ALL" || (item.role === "ADMIN" && isAdmin)
  );

  // If session is loading, show a premium full-screen loading skeleton
  if (loading) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-[#0a0a0f] text-white">
        <LoaderComponent />
      </div>
    );
  }

  return (
    <div className="min-h-screen flex bg-[#f8fafc] dark:bg-[#0a0a0f] text-slate-800 dark:text-slate-100 transition-colors duration-300">
      {/* BACKGROUND DECORATIONS */}
      <div className="absolute top-0 right-0 w-96 h-96 bg-purple-500/5 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-0 left-0 w-96 h-96 bg-indigo-500/5 rounded-full blur-3xl pointer-events-none" />

      {/* MOBILE HEADER */}
      <div className="lg:hidden fixed top-0 left-0 right-0 h-16 bg-white/80 dark:bg-[#13131a]/80 backdrop-blur-md border-b border-slate-200 dark:border-white/5 z-50 flex items-center justify-between px-4">
        <div className="flex items-center gap-2">
          <Warehouse className="w-6 h-6 text-indigo-500" />
          <span className="font-bold text-lg text-slate-900 dark:text-white">StockFlow</span>
        </div>
        <button
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          className="p-1.5 rounded-lg hover:bg-slate-100 dark:hover:bg-white/5 text-slate-500 dark:text-slate-400"
        >
          {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </div>

      {/* SIDEBAR - DESKTOP */}
      <aside
        className={`hidden lg:flex flex-col bg-white dark:bg-[#13131a] border-r border-slate-200 dark:border-white/5 transition-all duration-300 fixed left-0 top-0 bottom-0 z-30 ${
          sidebarCollapsed ? "w-20" : "w-64"
        }`}
      >
        {/* LOGO */}
        <div className="h-16 flex items-center justify-between px-6 border-b border-slate-200 dark:border-white/5">
          <div className="flex items-center gap-2.5 overflow-hidden">
            <div className="w-9 h-9 bg-gradient-to-tr from-purple-500 to-indigo-600 rounded-lg flex items-center justify-center shrink-0">
              <Warehouse className="w-5 h-5 text-white" />
            </div>
            {!sidebarCollapsed && (
              <span className="font-bold text-base text-slate-900 dark:text-white tracking-tight">
                StockFlow
              </span>
            )}
          </div>
          <button
            onClick={toggleSidebar}
            className="hidden lg:block p-1 rounded-lg hover:bg-slate-100 dark:hover:bg-white/5 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
          >
            {sidebarCollapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
          </button>
        </div>

        {/* NAV ITEMS */}
        <nav className="flex-1 py-6 px-3 space-y-1 overflow-y-auto">
          {filteredNavItems.map((item) => {
            const isActive = pathname === item.href || (pathname.startsWith(item.href) && item.href !== "/dashboard");
            const Icon = item.icon;
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`flex items-center gap-3.5 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all group relative ${
                  isActive
                    ? "bg-indigo-500/10 text-indigo-600 dark:text-indigo-400"
                    : "text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-white/5"
                }`}
              >
                <Icon className={`w-5 h-5 shrink-0 ${isActive ? "text-indigo-600 dark:text-indigo-400" : "text-slate-400 group-hover:text-slate-600 dark:group-hover:text-slate-200"}`} />
                {!sidebarCollapsed && <span>{item.name}</span>}
                {sidebarCollapsed && (
                  <div className="absolute left-full ml-2 px-2 py-1 bg-slate-900 dark:bg-white text-white dark:text-slate-900 text-xs rounded opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity z-50 shadow-md whitespace-nowrap">
                    {item.name}
                  </div>
                )}
              </Link>
            );
          })}
        </nav>

        {/* BOTTOM USER INFO */}
        <div className="p-4 border-t border-slate-200 dark:border-white/5 bg-slate-50/50 dark:bg-[#0e0e13]/30">
          <div className="flex items-center justify-between gap-3 overflow-hidden">
            <div className="flex items-center gap-2.5 overflow-hidden">
              <div className="w-9 h-9 bg-indigo-500/10 text-indigo-500 rounded-lg flex items-center justify-center shrink-0 font-bold uppercase text-sm border border-indigo-500/20">
                {user?.name?.substring(0, 2) || "US"}
              </div>
              {!sidebarCollapsed && (
                <div className="overflow-hidden min-w-0">
                  <p className="text-sm font-semibold text-slate-800 dark:text-white truncate">
                    {user?.name}
                  </p>
                  <p className="text-xs text-slate-400 font-medium truncate uppercase tracking-wider">
                    {role}
                  </p>
                </div>
              )}
            </div>
            {!sidebarCollapsed && (
              <button
                onClick={logout}
                title="Log Out"
                className="p-1.5 rounded-lg hover:bg-red-500/10 hover:text-red-500 text-slate-400 transition-colors"
              >
                <LogOut className="w-4 h-4" />
              </button>
            )}
          </div>
          {sidebarCollapsed && (
            <button
              onClick={logout}
              className="mt-2 w-full p-2 rounded-lg hover:bg-red-500/10 hover:text-red-500 text-slate-400 transition-colors flex justify-center"
              title="Log Out"
            >
              <LogOut className="w-4 h-4" />
            </button>
          )}
        </div>
      </aside>

      {/* MOBILE SIDEBAR OVERLAY */}
      {mobileMenuOpen && (
        <div
          className="lg:hidden fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-40"
          onClick={() => setMobileMenuOpen(false)}
        />
      )}

      {/* MOBILE SIDEBAR DRAWER */}
      <aside
        className={`lg:hidden fixed left-0 top-0 bottom-0 w-64 bg-white dark:bg-[#13131a] border-r border-slate-200 dark:border-white/5 z-50 flex flex-col transition-transform duration-300 ${
          mobileMenuOpen ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="h-16 flex items-center justify-between px-6 border-b border-slate-200 dark:border-white/5">
          <div className="flex items-center gap-2">
            <Warehouse className="w-6 h-6 text-indigo-500" />
            <span className="font-bold text-lg text-slate-900 dark:text-white">StockFlow</span>
          </div>
          <button
            onClick={() => setMobileMenuOpen(false)}
            className="p-1 rounded-lg hover:bg-slate-100 dark:hover:bg-white/5 text-slate-400"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <nav className="flex-1 py-4 px-4 space-y-1 overflow-y-auto">
          {filteredNavItems.map((item) => {
            const isActive = pathname === item.href || (pathname.startsWith(item.href) && item.href !== "/dashboard");
            const Icon = item.icon;
            return (
              <Link
                key={item.href}
                href={item.href}
                onClick={() => setMobileMenuOpen(false)}
                className={`flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-medium transition-all ${
                  isActive
                    ? "bg-indigo-500/10 text-indigo-600 dark:text-indigo-400"
                    : "text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-white/5"
                }`}
              >
                <Icon className="w-5 h-5 shrink-0" />
                <span>{item.name}</span>
              </Link>
            );
          })}
        </nav>

        <div className="p-4 border-t border-slate-200 dark:border-white/5 bg-slate-50 dark:bg-[#0e0e13]/30">
          <div className="flex items-center justify-between gap-3">
            <div className="flex items-center gap-2.5 overflow-hidden">
              <div className="w-9 h-9 bg-indigo-500/10 text-indigo-500 rounded-lg flex items-center justify-center shrink-0 font-bold uppercase">
                {user?.name?.substring(0, 2) || "US"}
              </div>
              <div className="overflow-hidden min-w-0">
                <p className="text-sm font-semibold text-slate-800 dark:text-white truncate">
                  {user?.name}
                </p>
                <p className="text-xs text-slate-400 font-medium truncate uppercase">
                  {role}
                </p>
              </div>
            </div>
            <button
              onClick={logout}
              className="p-1.5 rounded-lg hover:bg-red-500/10 hover:text-red-500 text-slate-400"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </aside>

      {/* MAIN CONTAINER */}
      <div
        className={`flex-1 flex flex-col min-w-0 transition-all duration-300 lg:pl-${
          sidebarCollapsed ? "20" : "64"
        }`}
        style={{
          paddingLeft: typeof window !== "undefined" && window.innerWidth >= 1024 
            ? sidebarCollapsed ? "5rem" : "16rem" 
            : "0px"
        }}
      >
        {/* HEADER / NAVBAR */}
        <header className="h-16 shrink-0 bg-white/80 dark:bg-[#13131a]/80 backdrop-blur-md border-b border-slate-200 dark:border-white/5 z-20 flex items-center justify-between px-6 sticky top-0">
          <div className="flex items-center gap-4">
            <h2 className="text-lg font-bold text-slate-800 dark:text-white capitalize">
              {pathname === "/dashboard"
                ? "Overview"
                : pathname.split("/").pop()?.replace("-", " ") || "Dashboard"}
            </h2>
          </div>

          <div className="flex items-center gap-4">
            {/* THEME TOGGLE */}
            <button
              onClick={toggleTheme}
              className="p-2 rounded-xl border border-slate-200 dark:border-white/5 bg-slate-50 dark:bg-[#0a0a0f] hover:bg-slate-100 dark:hover:bg-white/5 text-slate-500 dark:text-slate-400 transition-colors"
              title={darkMode ? "Switch to Light Mode" : "Switch to Dark Mode"}
            >
              {darkMode ? <Sun className="w-4.5 h-4.5 text-amber-400" /> : <Moon className="w-4.5 h-4.5" />}
            </button>

            {/* QUICK PROFILE ICON */}
            <Link
              href="/dashboard/profile"
              className="flex items-center gap-2 p-1.5 pr-3.5 rounded-xl border border-slate-200 dark:border-white/5 bg-slate-50 dark:bg-[#0a0a0f] hover:bg-slate-100 dark:hover:bg-white/5 transition-colors"
            >
              <div className="w-7 h-7 bg-indigo-500/10 text-indigo-500 rounded-lg flex items-center justify-center font-bold text-xs uppercase">
                {user?.name?.substring(0, 2) || "US"}
              </div>
              <span className="text-xs font-semibold text-slate-600 dark:text-slate-300 hidden md:block">
                {user?.name}
              </span>
            </Link>
          </div>
        </header>

        {/* PAGE CONTENT */}
        <main className="flex-1 p-6 overflow-y-auto mt-16 lg:mt-0 relative z-10">
          {children}
        </main>
      </div>
    </div>
  );
}

function LoaderComponent() {
  return (
    <div className="flex flex-col items-center gap-3">
      <Loader2 className="w-10 h-10 text-indigo-500 animate-spin" />
      <p className="text-slate-400 text-sm font-medium animate-pulse">Initializing session...</p>
    </div>
  );
}
