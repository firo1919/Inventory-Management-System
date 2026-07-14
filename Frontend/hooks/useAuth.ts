"use client";

import { useSession, signIn, signOut } from "next-auth/react";
import { useRouter } from "next/navigation";

export function useAuth() {
  const router = useRouter();
  const { data: sessionData, status, update } = useSession();

  const user = sessionData?.user;
  const session = sessionData;

  const loading = status === "loading";
  const role = user?.role || "EMPLOYEE";
  const isAdmin = role === "ADMIN";
  const isEmployee = role === "EMPLOYEE";

  const login = async (email: string, password: string) => {
    try {
      const response = await signIn("credentials", {
        email,
        password,
        redirect: false,
      });

      if (response?.error) {
        throw new Error(response.error === "CredentialsSignin" ? "Invalid email or password" : response.error);
      }

      router.push("/dashboard");
      return response;
    } catch (err: any) {
      throw err;
    }
  };

  const logout = async () => {
    try {
      // Notify backend via relative proxy endpoint
      await fetch("/api/v1/auth/logout", {
        method: "POST",
      }).catch(() => {});
    } catch (e) {
      // ignore
    } finally {
      await signOut({ callbackUrl: "/auth/login" });
    }
  };

  return {
    user,
    session,
    role,
    isAdmin,
    isEmployee,
    loading,
    error: null,
    login,
    logout,
    refetch: update,
  };
}
