import React from "react";
import { Clock, ArrowUpRight, ArrowDownRight } from "lucide-react";
import { Transaction } from "@/types";

interface RecentActionsProps {
    transactions: Transaction[];
    loading: boolean;
}

export function RecentActions({ transactions, loading }: RecentActionsProps) {
    return (
        <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-5 shadow-sm">
            <h3 className="font-bold text-slate-900 dark:text-white text-base mb-4">
                Recent Actions
            </h3>
            <div className="overflow-x-auto">
                {loading ? (
                    <div className="space-y-3.5">
                        {Array.from({ length: 3 }).map((_, i) => (
                            <div
                                key={i}
                                className="h-10 bg-slate-50 dark:bg-slate-900/30 rounded-xl animate-pulse"
                            />
                        ))}
                    </div>
                ) : transactions.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-10 text-slate-400">
                        <Clock className="w-8 h-8 opacity-30 mb-2" />
                        <p className="text-xs font-medium">
                            No recent actions recorded
                        </p>
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
                            {transactions.map((tx) => (
                                <tr
                                    key={tx.id}
                                    className="text-slate-600 dark:text-slate-300"
                                >
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
                                    <td className="py-3 text-right font-medium">
                                        {tx.quantity}
                                    </td>
                                    <td className="py-3 text-right font-bold text-slate-900 dark:text-white">
                                        ${tx.totalPrice.toFixed(2)}
                                    </td>
                                    <td className="py-3 text-xs">{tx.user}</td>
                                    <td className="py-3 text-right text-xs text-slate-400">
                                        {new Date(tx.date).toLocaleDateString(
                                            undefined,
                                            {
                                                month: "short",
                                                day: "numeric",
                                                hour: "2-digit",
                                                minute: "2-digit",
                                            },
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
}
