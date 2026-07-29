import { apiClient } from "@/lib/api-client";

export const dashboardService = {
    async getInventoryValue() {
        const res = await apiClient.get("/api/v1/admin/inventory/value");
        return res.data;
    },
};
