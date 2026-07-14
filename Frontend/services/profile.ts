import { apiClient } from "@/lib/api-client";

export const profileService = {
  async getProfile() {
    const res = await apiClient.get("/api/v1/profile");
    return res.data;
  },

  async updateProfile(data: any) {
    const res = await apiClient.put("/api/v1/profile", data);
    return res.data;
  },

  async getPresignedUrl(filename: string, contentType: string) {
    const res = await apiClient.post("/api/v1/uploads/presign", {
      filename,
      contentType,
    });
    return res.data;
  },

  async updateProfilePicture(objectKey: string) {
    const res = await apiClient.post("/api/v1/profile/profile-picture", {
      objectKey,
    });
    return res.data;
  },
};
