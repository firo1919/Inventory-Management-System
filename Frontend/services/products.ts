import { apiClient } from "@/lib/api-client";

export interface ProductParams {
  page: number;
  size: number;
  sortBy?: string;
  sortDirection?: string;
}

export const productsService = {
  async getProducts(params?: ProductParams) {
    const res = await apiClient.get("/api/v1/products", { params });
    return res.data;
  },

  async getLowStockProducts(params?: ProductParams) {
    const res = await apiClient.get("/api/v1/products/low-stock", { params });
    return res.data;
  },

  async createProduct(data: any) {
    const res = await apiClient.post("/api/v1/admin/products", data);
    return res.data;
  },

  async updateProduct(id: string, data: any) {
    const res = await apiClient.put(`/api/v1/admin/products/${id}`, data);
    return res.data;
  },

  async deleteProduct(id: string) {
    const res = await apiClient.delete(`/api/v1/admin/products/${id}`);
    return res.data;
  },

  async toggleProductStatus(id: string, currentActive: boolean) {
    const action = currentActive ? "deactivate" : "activate";
    const res = await apiClient.post(`/api/v1/admin/products/${id}/${action}`);
    return res.data;
  },

  async uploadProductImage(id: string, formData: FormData) {
    const res = await apiClient.post(
      `/api/v1/admin/products/${id}/image`,
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      }
    );
    return res.data;
  },
};
