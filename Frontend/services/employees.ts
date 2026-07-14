import { apiClient } from "@/lib/api-client";

export interface EmployeeParams {
  page: number;
  size: number;
}

export const employeesService = {
  async getEmployees(params?: EmployeeParams) {
    const res = await apiClient.get("/api/v1/admin/employees", { params });
    return res.data;
  },

  async createEmployee(data: any) {
    const res = await apiClient.post("/api/v1/admin/employees", data);
    return res.data;
  },

  async updateEmployee(id: string, data: any) {
    const res = await apiClient.put(`/api/v1/admin/employees/${id}`, data);
    return res.data;
  },

  async deleteEmployee(id: string) {
    const res = await apiClient.delete(`/api/v1/admin/employees/${id}`);
    return res.data;
  },

  async toggleEmployeeStatus(id: string, active: boolean) {
    const action = active ? "deactivate" : "activate";
    const res = await apiClient.post(`/api/v1/admin/employees/${id}/${action}`);
    return res.data;
  },
};
