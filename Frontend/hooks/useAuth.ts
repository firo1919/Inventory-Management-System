"use client";

import { authClient } from "@/lib/auth-client";
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

      // Tokens are handled securely by server-side cookies, no localStorage needed.
      
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
      // Notify backend via relative proxy endpoint
      await fetch("/api/v1/auth/logout", {
        method: "POST",
      }).catch(() => {});
    } catch (e) {
      // ignore
    } finally {
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
