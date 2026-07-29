"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { productsService } from "@/services/products";
import { dashboardService } from "@/services/dashboard";
import { salesService } from "@/services/sales";
import { restocksService } from "@/services/restocks";
import { toast } from "sonner";

import { WelcomeBanner } from "./_components/WelcomeBanner";
import { MetricCards } from "./_components/MetricCards";
import { TransactionsAnalytics } from "./_components/TransactionsAnalytics";
import { LowStockWarnings } from "./_components/LowStockWarnings";
import { RecentActions } from "./_components/RecentActions";
import { Product, Transaction, Sale, Restock } from "@/types";

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
    const [lowStockProducts, setLowStockProducts] = useState<Product[]>([]);
    const [recentTransactions, setRecentTransactions] = useState<Transaction[]>(
        [],
    );
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function fetchDashboardData() {
            try {
                setLoading(true);

                // 1) Fetch products (to build product map & get total count)
                const productsData = await productsService.getProducts({
                    page: 0,
                    size: 100,
                });
                const totalProducts = productsData?.totalElements || 0;
                const productsList = productsData?.content || [];
                const pMap: Record<string, Product> = {};
                productsList.forEach((pr: Product) => {
                    pMap[pr.id] = pr;
                });

                // 2) Fetch low stock count & list
                const lowStockData = await productsService.getLowStockProducts({
                    page: 0,
                    size: 5,
                });
                const lowStockCount = lowStockData?.totalElements || 0;
                setLowStockProducts(lowStockData?.content || []);

                // 3) Fetch role-specific details
                let inventoryValue = 0;
                let salesCount = 0;
                let restocksCount = 0;
                let txList: Transaction[] = [];

                if (isAdmin) {
                    // Admin specific APIs
                    const valRes = await dashboardService
                        .getInventoryValue()
                        .catch(() => null);
                    inventoryValue = valRes?.totalValue || 0;

                    const salesData = await salesService
                        .getSales(true, { page: 0, size: 10 })
                        .catch(() => null);
                    salesCount = salesData?.totalElements || 0;
                    const salesList = salesData?.content || [];

                    const restocksData = await restocksService
                        .getRestocks(true, { page: 0, size: 10 })
                        .catch(() => null);
                    restocksCount = restocksData?.totalElements || 0;
                    const restocksList = restocksData?.content || [];

                    console.log("pMap:", pMap, "salesList:", salesList);
                    // Merge sales and restocks into recent transactions
                    const formattedSales: Transaction[] = salesList.map(
                        (s: Sale) => {
                            const product = pMap[s.productId];
                            return {
                                id: s.id,
                                type: "SALE",
                                productName: product?.name || "Product",
                                quantity: s.quantity,
                                totalPrice: s.salePrice
                                    ? s.quantity * s.salePrice
                                    : s.quantity * (product?.sellingPrice || 0),
                                date:
                                    s.timestamp ||
                                    s.createdAt ||
                                    s.saleDate ||
                                    "",
                                user: "Staff",
                            };
                        },
                    );

                    const formattedRestocks: Transaction[] = restocksList.map(
                        (r: Restock) => {
                            const product = pMap[r.productId];
                            return {
                                id: r.id,
                                type: "RESTOCK",
                                productName: product?.name || "Product",
                                quantity: r.quantity,
                                totalPrice:
                                    r.quantity * (product?.costPrice || 0),
                                date:
                                    r.timestamp ||
                                    r.createdAt ||
                                    r.restockDate ||
                                    "",
                                user: "Staff",
                            };
                        },
                    );

                    txList = [...formattedSales, ...formattedRestocks]
                        .sort(
                            (a, b) =>
                                new Date(b.date).getTime() -
                                new Date(a.date).getTime(),
                        )
                        .slice(0, 5);
                } else {
                    // Employee specific APIs
                    const salesData = await salesService
                        .getSales(false, { page: 0, size: 10 })
                        .catch(() => null);
                    salesCount = salesData?.totalElements || 0;
                    const salesList = salesData?.content || [];

                    const restocksData = await restocksService
                        .getRestocks(false, { page: 0, size: 10 })
                        .catch(() => null);
                    restocksCount = restocksData?.totalElements || 0;
                    const restocksList = restocksData?.content || [];

                    const formattedSales: Transaction[] = salesList.map(
                        (s: Sale) => {
                            const product = pMap[s.productId];
                            return {
                                id: s.id,
                                type: "SALE",
                                productName: product?.name || "Product",
                                quantity: s.quantity,
                                totalPrice: s.salePrice
                                    ? s.quantity * s.salePrice
                                    : s.quantity * (product?.sellingPrice || 0),
                                date:
                                    s.timestamp ||
                                    s.createdAt ||
                                    s.saleDate ||
                                    "",
                                user: "Me",
                            };
                        },
                    );

                    const formattedRestocks: Transaction[] = restocksList.map(
                        (r: Restock) => {
                            const product = pMap[r.productId];
                            return {
                                id: r.id,
                                type: "RESTOCK",
                                productName: product?.name || "Product",
                                quantity: r.quantity,
                                totalPrice:
                                    r.quantity * (product?.costPrice || 0),
                                date:
                                    r.timestamp ||
                                    r.createdAt ||
                                    r.restockDate ||
                                    "",
                                user: "Me",
                            };
                        },
                    );

                    txList = [...formattedSales, ...formattedRestocks]
                        .sort(
                            (a, b) =>
                                new Date(b.date).getTime() -
                                new Date(a.date).getTime(),
                        )
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

    return (
        <div className="space-y-8">
            {/* WELCOME BANNER */}
            <WelcomeBanner
                userName={user?.name || "User"}
                lowStockCount={stats.lowStockCount}
            />

            {/* METRIC CARDS GRID */}
            <MetricCards stats={stats} isAdmin={isAdmin} loading={loading} />

            {/* CHART & ACTIVITY GRID */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* TRANSACTIONS ANALYTICS */}
                <TransactionsAnalytics
                    salesCount={stats.salesCount}
                    restocksCount={stats.restocksCount}
                />

                {/* LOW STOCK ALERT PANEL */}
                <LowStockWarnings
                    products={lowStockProducts}
                    loading={loading}
                />
            </div>

            {/* RECENT TRANSACTION LIST */}
            <RecentActions
                transactions={recentTransactions}
                loading={loading}
            />
        </div>
    );
}
