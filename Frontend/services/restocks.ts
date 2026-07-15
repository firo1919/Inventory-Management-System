import { apiClient } from "@/lib/api-client";
import { RestockInput } from "@/app/(protected)/dashboard/restocks/schema";

export interface RestockParams {
  page: number;
  size: number;
  sortBy?: string;
  sortDirection?: string;
  startDate?: string;
  endDate?: string;
}

export const restocksService = {
  async getRestocks(isAdmin: boolean, params: RestockParams) {
    const endpoint = isAdmin
      ? "/api/v1/admin/restocks"
      : "/api/v1/employee/restocks";
    const res = await apiClient.get(endpoint, { params });
    return res.data;
  },

  async createRestock(data: RestockInput) {
    const res = await apiClient.post("/api/v1/restocks", data);
    return res.data;
  },

  async deleteRestock(id: string) {
    const res = await apiClient.delete(`/api/v1/admin/restocks/${id}`);
    return res.data;
  },
};
