import { apiClient } from "@/lib/api-client";

export interface CategoryParams {
    page: number;
    size: number;
}

export interface CategoryInput {
    name: string;
}

export const categoriesService = {
    async getCategories(params?: CategoryParams) {
        const res = await apiClient.get("/api/v1/categories", { params });
        return res.data;
    },

    async createCategory(data: CategoryInput) {
        const res = await apiClient.post("/api/v1/admin/categories", data);
        return res.data;
    },

    async updateCategory(id: string, data: CategoryInput) {
        const res = await apiClient.put(`/api/v1/admin/categories/${id}`, data);
        return res.data;
    },

    async deleteCategory(id: string) {
        const res = await apiClient.delete(`/api/v1/admin/categories/${id}`);
        return res.data;
    },
};
