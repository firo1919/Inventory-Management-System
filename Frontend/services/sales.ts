import { apiClient } from "@/lib/api-client";
import { SaleInput } from "@/app/(protected)/dashboard/sales/schema";

export interface SaleParams {
    page: number;
    size: number;
    sortBy?: string;
    sortDirection?: string;
    startDate?: string;
    endDate?: string;
}

export const salesService = {
    async getSales(isAdmin: boolean, params: SaleParams) {
        const endpoint = isAdmin
            ? "/api/v1/admin/sales"
            : "/api/v1/employee/sales";
        const res = await apiClient.get(endpoint, { params });
        return res.data;
    },

    async createSale(data: SaleInput) {
        const res = await apiClient.post("/api/v1/sales", data);
        return res.data;
    },

    async deleteSale(id: string) {
        const res = await apiClient.delete(`/api/v1/admin/sales/${id}`);
        return res.data;
    },
};
