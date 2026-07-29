import axios from "axios";
import { signOut } from "next-auth/react";

export const apiClient = axios.create({
    headers: {
        "Content-Type": "application/json",
    },
});

apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response?.status === 401) {
            if (typeof window !== "undefined") {
                // Trigger NextAuth signOut to clear session and redirect to login
                await signOut({ callbackUrl: "/auth/login" });
            }
        }
        return Promise.reject(error);
    },
);
