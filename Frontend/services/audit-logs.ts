import { apiClient } from "@/lib/api-client";

export const auditLogsService = {
  async getAuditLogs(params: any) {
    const res = await apiClient.get("/api/v1/admin/audit-logs", { params });
    return res.data;
  },

  async getAuditStatistics() {
    const res = await apiClient.get("/api/v1/admin/audit-logs/statistics");
    return res.data;
  },

  async exportAuditLogs(format: "csv" | "json", filterPayload: any) {
    const res = await apiClient.post(
      `/api/v1/admin/audit-logs/export/${format}`,
      filterPayload,
      { responseType: "blob" }
    );
    return res.data;
  },

  async deleteAuditLogs(params: { startDate: string; endDate: string }) {
    const res = await apiClient.delete("/api/v1/admin/audit-logs", { params });
    return res.data;
  },
};
