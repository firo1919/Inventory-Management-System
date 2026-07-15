import React from "react";
import { Package, AlertTriangle, DollarSign, TrendingUp } from "lucide-react";

interface MetricCardsProps {
  stats: {
    totalProducts: number;
    lowStockCount: number;
    inventoryValue: number;
    salesCount: number;
  };
  isAdmin: boolean;
  loading: boolean;
}

export function MetricCards({ stats, isAdmin, loading }: MetricCardsProps) {
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

  if (loading) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {Array.from({ length: isAdmin ? 4 : 3 }).map((_, i) => (
          <div
            key={i}
            className="h-32 bg-white dark:bg-[#13131a] rounded-2xl border border-slate-200 dark:border-white/5 animate-pulse"
          />
        ))}
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      {cards.map((card, i) => {
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
                className={`w-10 h-10 bg-linear-to-tr ${card.color} rounded-xl flex items-center justify-center text-white shadow-md`}
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
  );
}
