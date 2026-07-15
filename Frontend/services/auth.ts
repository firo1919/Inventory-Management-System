import { apiClient } from "@/lib/api-client";
import { RegisterInput } from "@/app/(public)/auth/register/schema";

export const authService = {
  async registerAdmin(payload: Omit<RegisterInput, "confirmPassword">) {
    const res = await apiClient.post("/api/v1/auth/admins", payload);
    return res.data;
  },

  async confirmOtp(payload: { otp: string; email: string }) {
    const res = await apiClient.post("/api/v1/auth/confirm-otp", payload);
    return res.data;
  },

  async resendOtp(payload: { email: string }) {
    const res = await apiClient.post("/api/v1/auth/resend-otp", payload);
    return res.data;
  },

  async logout() {
    await fetch("/api/v1/auth/logout", {
      method: "POST",
    }).catch(() => {});
  },
};
