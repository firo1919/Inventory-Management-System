"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { apiClient } from "@/lib/api-client";
import { toast } from "sonner";
import {
  Package,
  AlertTriangle,
  DollarSign,
  TrendingUp,
  BarChart2,
  ArrowUpRight,
  ArrowDownRight,
  Clock,
  Sparkles,
} from "lucide-react";
import Link from "next/link";

interface Stats {
  totalProducts: number;
  lowStockCount: number;
  inventoryValue: number;
  salesCount: number;
  restocksCount: number;
}

export default function DashboardOverview() {
  const { user, isAdmin } = useAuth();
  const [stats, setStats] = useState<Stats>({
    totalProducts: 0,
    lowStockCount: 0,
    inventoryValue: 0,
    salesCount: 0,
    restocksCount: 0,
  });
  const [lowStockProducts, setLowStockProducts] = useState<any[]>([]);
  const [recentTransactions, setRecentTransactions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchDashboardData() {
      try {
        setLoading(true);
        
        // 1) Fetch products (for total count)
        const productsRes = await apiClient.get("/api/v1/products?page=0&size=1");
        const totalProducts = productsRes.data?.totalElements || 0;

        // 2) Fetch low stock count & list
        const lowStockRes = await apiClient.get("/api/v1/products/low-stock?page=0&size=5");
        const lowStockCount = lowStockRes.data?.totalElements || 0;
        setLowStockProducts(lowStockRes.data?.content || []);

        // 3) Fetch role-specific details
        let inventoryValue = 0;
        let salesCount = 0;
        let restocksCount = 0;
        let txList: any[] = [];

        if (isAdmin) {
          // Admin specific APIs
          const valRes = await apiClient.get("/api/v1/admin/inventory/value").catch(() => null);
          inventoryValue = valRes?.data?.totalValue || 0;

          const salesRes = await apiClient.get("/api/v1/admin/sales?page=0&size=10").catch(() => null);
          salesCount = salesRes?.data?.totalElements || 0;
          const salesList = salesRes?.data?.content || [];

          const restocksRes = await apiClient.get("/api/v1/admin/restocks?page=0&size=10").catch(() => null);
          restocksCount = restocksRes?.data?.totalElements || 0;
          const restocksList = restocksRes?.data?.content || [];

          // Merge sales and restocks into recent transactions
          const formattedSales = salesList.map((s: any) => ({
            id: s.id,
            type: "SALE",
            productName: s.productName || "Product",
            quantity: s.quantity,
            totalPrice: s.totalPrice || (s.quantity * s.unitPrice),
            date: s.createdAt || s.saleDate,
            user: s.employeeName || "System",
          }));

          const formattedRestocks = restocksList.map((r: any) => ({
            id: r.id,
            type: "RESTOCK",
            productName: r.productName || "Product",
            quantity: r.quantity,
            totalPrice: r.totalPrice || (r.quantity * r.unitPrice),
            date: r.createdAt || r.restockDate,
            user: r.employeeName || "System",
          }));

          txList = [...formattedSales, ...formattedRestocks]
            .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
            .slice(0, 5);

        } else {
          // Employee specific APIs
          const salesRes = await apiClient.get("/api/v1/employee/sales?page=0&size=10").catch(() => null);
          salesCount = salesRes?.data?.totalElements || 0;
          const salesList = salesRes?.data?.content || [];

          const restocksRes = await apiClient.get("/api/v1/employee/restocks?page=0&size=10").catch(() => null);
          restocksCount = restocksRes?.data?.totalElements || 0;
          const restocksList = restocksRes?.data?.content || [];

          const formattedSales = salesList.map((s: any) => ({
            id: s.id,
            type: "SALE",
            productName: s.productName || "Product",
            quantity: s.quantity,
            totalPrice: s.totalPrice || (s.quantity * s.unitPrice),
            date: s.createdAt || s.saleDate,
            user: "Me",
          }));

          const formattedRestocks = restocksList.map((r: any) => ({
            id: r.id,
            type: "RESTOCK",
            productName: r.productName || "Product",
            quantity: r.quantity,
            totalPrice: r.totalPrice || (r.quantity * r.unitPrice),
            date: r.createdAt || r.restockDate,
            user: "Me",
          }));

          txList = [...formattedSales, ...formattedRestocks]
            .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
            .slice(0, 5);
        }

        setStats({
          totalProducts,
          lowStockCount,
          inventoryValue,
          salesCount,
          restocksCount,
        });

        setRecentTransactions(txList);

      } catch {
        toast.error("Failed to load dashboard data. Please refresh.");
      } finally {
        setLoading(false);
      }
    }

    fetchDashboardData();
  }, [isAdmin]);

  const cards = [
    {
      title: "Total Products",
      value: stats.totalProducts,
      icon: Package,
      color: "from-blue-500 to-indigo-600",
      description: "Active catalog products",
    },
    {
      title: "Low Stock Items",
      value: stats.lowStockCount,
      icon: AlertTriangle,
      color: "from-amber-500 to-orange-600",
      description: "Needs attention immediately",
      alert: stats.lowStockCount > 0,
    },
    ...(isAdmin
      ? [
          {
            title: "Total Inventory Value",
            value: `$${stats.inventoryValue.toLocaleString(undefined, {
              minimumFractionDigits: 2,
              maximumFractionDigits: 2,
            })}`,
            icon: DollarSign,
            color: "from-emerald-500 to-teal-600",
            description: "Asset cumulative valuation",
          },
        ]
      : []),
    {
      title: isAdmin ? "Total Sales Logged" : "My Logged Sales",
      value: stats.salesCount,
      icon: TrendingUp,
      color: "from-purple-500 to-pink-600",
      description: "Sales transactions recorded",
    },
  ];

  return (
    <div className="space-y-8">
      {/* WELCOME BANNER */}
      <div className="relative p-6 md:p-8 rounded-3xl bg-gradient-to-r from-purple-900/40 to-indigo-900/40 border border-white/5 overflow-hidden backdrop-blur-md">
        <div className="absolute top-0 right-0 w-80 h-80 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />
        <div className="relative z-10 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <div className="inline-flex items-center gap-1.5 px-3 py-1 bg-indigo-500/10 border border-indigo-500/20 rounded-full text-indigo-400 text-xs font-semibold mb-3">
              <Sparkles className="w-3.5 h-3.5" /> Workspace Overview
            </div>
            <h1 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight">
              Welcome back, {user?.name}!
            </h1>
            <p className="text-slate-400 text-sm mt-1.5 max-w-md">
              Here is what is happening in your inventory database today. You have{" "}
              <span className="text-amber-400 font-semibold">{stats.lowStockCount} low stock alerts</span>.
            </p>
          </div>
          <div className="flex gap-3">
            <Link
              href="/dashboard/sales"
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-xs font-semibold transition-all shadow-lg shadow-indigo-500/20"
            >
              Log A Sale
            </Link>
            <Link
              href="/dashboard/restocks"
              className="px-4 py-2 bg-white/5 hover:bg-white/10 border border-white/10 text-white rounded-xl text-xs font-semibold transition-all"
            >
              Log A Restock
            </Link>
          </div>
        </div>
      </div>

      {/* METRIC CARDS GRID */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {loading
          ? Array.from({ length: isAdmin ? 4 : 3 }).map((_, i) => (
              <div
                key={i}
                className="h-32 bg-white dark:bg-[#13131a] rounded-2xl border border-slate-200 dark:border-white/5 animate-pulse"
              />
            ))
          : cards.map((card, i) => {
              const Icon = card.icon;
              return (
                <div
                  key={i}
                  className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-5 shadow-sm relative overflow-hidden"
                >
                  <div className="flex justify-between items-start">
                    <div>
                      <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                        {card.title}
                      </p>
                      <h3 className="text-2xl font-bold text-slate-900 dark:text-white mt-1.5">
                        {card.value}
                      </h3>
                    </div>
                    <div
                      className={`w-10 h-10 bg-gradient-to-tr ${card.color} rounded-xl flex items-center justify-center text-white shadow-md`}
                    >
                      <Icon className="w-5 h-5" />
                    </div>
                  </div>
                  <p className="text-xs text-slate-400 mt-4">{card.description}</p>
                  {card.alert && (
                    <div className="absolute top-0 right-0 w-2 h-2 bg-red-500 rounded-full mt-2 mr-2 animate-ping" />
                  )}
                </div>
              );
            })}
      </div>

      {/* CHART & ACTIVITY GRID */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* ANALYTICS PLACEHOLDER */}
        <div className="lg:col-span-2 bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-5 shadow-sm">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="font-bold text-slate-900 dark:text-white text-base">Transactions Analytics</h3>
              <p className="text-xs text-slate-400 mt-0.5">Weekly sales &amp; restock trends</p>
            </div>
          </div>

          <div className="h-72 w-full flex flex-col items-center justify-center gap-4 rounded-xl border border-dashed border-slate-200 dark:border-white/10 bg-slate-50/50 dark:bg-white/[0.02]">
            <div className="w-12 h-12 rounded-2xl bg-indigo-500/10 flex items-center justify-center">
              <BarChart2 className="w-6 h-6 text-indigo-500" />
            </div>
            <div className="text-center">
              <p className="text-sm font-semibold text-slate-700 dark:text-white">Analytics Coming Soon</p>
              <p className="text-xs text-slate-400 mt-1 max-w-xs">
                Real-time transaction charts will appear here once the backend analytics aggregation endpoint is available.
                Your data is being recorded in the Sales and Restocks logs.
              </p>
            </div>
            <div className="flex gap-6 text-xs text-slate-400">
              <span className="flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-indigo-500 block" /> {stats.salesCount} total sales
              </span>
              <span className="flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-purple-500 block" /> {stats.restocksCount} total restocks
              </span>
            </div>
          </div>
        </div>

        {/* LOW STOCK ALERT PANEL */}
        <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-5 shadow-sm flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="font-bold text-slate-900 dark:text-white text-base">Low Stock Warnings</h3>
              <p className="text-xs text-slate-400 mt-0.5">Products below threshold</p>
            </div>
            <Link
              href="/dashboard/products"
              className="text-xs text-indigo-500 hover:text-indigo-400 font-semibold"
            >
              View All
            </Link>
          </div>

          <div className="flex-1 space-y-3.5 overflow-y-auto max-h-72 pr-1">
            {loading ? (
              Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className="h-14 bg-slate-50 dark:bg-slate-900/30 rounded-xl animate-pulse" />
              ))
            ) : lowStockProducts.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-full text-slate-400 py-10">
                <Package className="w-10 h-10 opacity-35 mb-2 text-indigo-500" />
                <p className="text-xs font-semibold">All products fully stocked</p>
              </div>
            ) : (
              lowStockProducts.map((p) => (
                <div
                  key={p.id}
                  className="flex items-center justify-between p-3.5 bg-slate-50 dark:bg-[#1a1a24]/50 border border-slate-200/50 dark:border-white/5 rounded-xl hover:border-indigo-500/20 transition-all"
                >
                  <div className="min-w-0">
                    <p className="text-xs font-semibold text-slate-800 dark:text-white truncate">
                      {p.name}
                    </p>
                    <p className="text-[10px] text-slate-400 font-medium truncate mt-0.5">
                      SKU: {p.sku} • Threshold: {p.lowStockThreshold || 10}
                    </p>
                  </div>
                  <div className="text-right shrink-0">
                    <span className="text-xs font-bold text-red-500 bg-red-500/10 px-2 py-1 rounded-lg">
                      {p.quantity} left
                    </span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* RECENT TRANSACTION LIST */}
      <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-5 shadow-sm">
        <h3 className="font-bold text-slate-900 dark:text-white text-base mb-4">Recent Actions</h3>
        <div className="overflow-x-auto">
          {loading ? (
            <div className="space-y-3.5">
              {Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className="h-10 bg-slate-50 dark:bg-slate-900/30 rounded-xl animate-pulse" />
              ))}
            </div>
          ) : recentTransactions.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-10 text-slate-400">
              <Clock className="w-8 h-8 opacity-30 mb-2" />
              <p className="text-xs font-medium">No recent actions recorded</p>
            </div>
          ) : (
            <table className="w-full text-left text-sm border-collapse">
              <thead>
                <tr className="border-b border-slate-200 dark:border-white/5 text-slate-400 text-xs font-semibold">
                  <th className="pb-3">Type</th>
                  <th className="pb-3">Product Name</th>
                  <th className="pb-3 text-right">Quantity</th>
                  <th className="pb-3 text-right">Amount</th>
                  <th className="pb-3">Logged By</th>
                  <th className="pb-3 text-right">Date</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-white/5">
                {recentTransactions.map((tx) => (
                  <tr key={tx.id} className="text-slate-600 dark:text-slate-300">
                    <td className="py-3 font-semibold">
                      <span
                        className={`inline-flex items-center gap-1 text-[10px] font-bold uppercase px-2 py-0.5 rounded-md ${
                          tx.type === "SALE"
                            ? "bg-emerald-500/10 text-emerald-500"
                            : "bg-indigo-500/10 text-indigo-500"
                        }`}
                      >
                        {tx.type === "SALE" ? (
                          <ArrowUpRight className="w-3 h-3" />
                        ) : (
                          <ArrowDownRight className="w-3 h-3" />
                        )}
                        {tx.type}
                      </span>
                    </td>
                    <td className="py-3 font-semibold text-slate-800 dark:text-slate-200">
                      {tx.productName}
                    </td>
                    <td className="py-3 text-right font-medium">{tx.quantity}</td>
                    <td className="py-3 text-right font-bold text-slate-900 dark:text-white">
                      ${tx.totalPrice.toFixed(2)}
                    </td>
                    <td className="py-3 text-xs">{tx.user}</td>
                    <td className="py-3 text-right text-xs text-slate-400">
                      {new Date(tx.date).toLocaleDateString(undefined, {
                        month: "short",
                        day: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}
