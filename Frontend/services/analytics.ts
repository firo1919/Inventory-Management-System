import { apiClient } from '@/lib/api-client';

export interface TransactionSummary {
  totalSalesCount: number;
  totalSalesRevenue: number;
  totalUnitsSold: number;
  totalRestocksCount: number;
  totalUnitsRestocked: number;
  startDate: string;
  endDate: string;
}

export interface TrendPoint {
  period: string;
  salesCount: number;
  salesRevenue: number;
  restocksCount: number;
  unitsSold: number;
  unitsRestocked: number;
}

export interface TopProduct {
  productId: string;
  productName: string;
  totalQuantitySold: number;
  totalRevenue: number;
  totalTimesRestocked: number;
}

export const analyticsService = {
  async getSummary(startDate?: string, endDate?: string): Promise<TransactionSummary> {
    const params: Record<string, string> = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    const res = await apiClient.get('/api/v1/admin/analytics/summary', { params });
    return res.data;
  },
  async getTrends(granularity: 'DAILY' | 'WEEKLY' | 'MONTHLY' = 'DAILY', startDate?: string, endDate?: string): Promise<TrendPoint[]> {
    const params: Record<string, string> = { granularity };
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    const res = await apiClient.get('/api/v1/admin/analytics/trends', { params });
    return res.data;
  },
  async getTopProducts(limit: number = 10, startDate?: string, endDate?: string, sortBy: 'quantity' | 'revenue' = 'revenue'): Promise<TopProduct[]> {
    const params: Record<string, string> = { limit: String(limit), sortBy };
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    const res = await apiClient.get('/api/v1/admin/analytics/top-products', { params });
    return res.data;
  },
};
