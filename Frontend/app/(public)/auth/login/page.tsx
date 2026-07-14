"use client";

import React, { useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { LoginForm } from "./_components/LoginForm";
import { LoginInput } from "./schema";

export default function LoginPage() {
  const { login } = useAuth();
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const onSubmit = async (data: LoginInput) => {
    setError("");
    setLoading(true);

    try {
      await login(data.email, data.password);
    } catch (err: any) {
      setError(err.message || "Invalid email or password");
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-radial from-[#1e1e38] to-[#0a0a0f] px-4">
      {/* Background ambient glows */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-purple-500/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />

      <LoginForm onSubmit={onSubmit} loading={loading} error={error} />
    </div>
  );
}
