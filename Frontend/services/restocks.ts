import { apiClient } from "@/lib/api-client";

export const restocksService = {
  async getRestocks(isAdmin: boolean, params: any) {
    const endpoint = isAdmin
      ? "/api/v1/admin/restocks"
      : "/api/v1/employee/restocks";
    const res = await apiClient.get(endpoint, { params });
    return res.data;
  },

  async createRestock(data: any) {
    const res = await apiClient.post("/api/v1/restocks", data);
    return res.data;
  },

  async deleteRestock(id: string) {
    const res = await apiClient.delete(`/api/v1/admin/restocks/${id}`);
    return res.data;
  },
};
