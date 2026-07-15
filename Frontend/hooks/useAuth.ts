"use client";

import { useSession, signIn, signOut } from "next-auth/react";
import { useRouter } from "next/navigation";

import { authService } from "@/services/auth";

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
    } catch (err: unknown) {
      throw err;
    }
  };

  const logout = async () => {
    try {
      await authService.logout();
    } catch {
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
