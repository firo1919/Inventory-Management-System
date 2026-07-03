"use client";

import { authClient } from "@/lib/auth-client";
import { setTokens, clearTokens } from "@/lib/api-client";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export function useAuth() {
  const router = useRouter();
  const { data: sessionData, isPending, error } = authClient.useSession();
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isPending) {
      setLoading(false);
    }
  }, [isPending]);

  const user = sessionData?.user;
  const session = sessionData?.session;
  
  // Custom properties from session / user
  const role = (user as any)?.role || "EMPLOYEE";
  const isAdmin = role === "ADMIN";
  const isEmployee = role === "EMPLOYEE";

  const login = async (email: string, password: string) => {
    setLoading(true);
    try {
      const response = await authClient.signIn.email({
        email,
        password,
        callbackURL: "/dashboard",
      });

      if (response.error) {
        throw new Error(response.error.message || "Failed to sign in");
      }

      // If successful, extract tokens from the session metadata returned
      const data = response.data as any;
      const sessionObj = data?.session;
      if (sessionObj?.accessToken && sessionObj?.refreshToken) {
        setTokens(sessionObj.accessToken, sessionObj.refreshToken);
      }
      
      router.push("/dashboard");
      return response.data;
    } catch (err: any) {
      setLoading(false);
      throw err;
    }
  };

  const logout = async () => {
    setLoading(true);
    try {
      // Get refresh token for backend logout request if needed
      const refreshToken = typeof window !== "undefined" ? localStorage.getItem("refresh_token") : null;
      if (refreshToken) {
        const apiURL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
        await fetch(`${apiURL}/api/v1/auth/logout`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ refreshToken }),
        }).catch(() => {
          // ignore logout errors on backend
        });
      }
    } catch (e) {
      // ignore
    } finally {
      clearTokens();
      await authClient.signOut();
      router.push("/auth/login");
      setLoading(false);
    }
  };

  return {
    user,
    session,
    role,
    isAdmin,
    isEmployee,
    loading,
    error,
    login,
    logout,
    refetch: authClient.useSession
  };
}
