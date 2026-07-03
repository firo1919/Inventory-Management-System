"use client";

import React, { useState, useEffect, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { KeyRound, Mail, Loader2, ArrowLeft, RefreshCw } from "lucide-react";
import axios from "axios";

function VerifyOtpPageInner() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const emailParam = searchParams.get("email") || "";

  const [email, setEmail] = useState(emailParam);
  const [otp, setOtp] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const [timer, setTimer] = useState(60);

  useEffect(() => {
    if (emailParam) {
      setEmail(emailParam);
    }
  }, [emailParam]);

  // Countdown timer for resend button
  useEffect(() => {
    let interval: any;
    if (timer > 0) {
      interval = setInterval(() => {
        setTimer((prev) => prev - 1);
      }, 1000);
    }
    return () => clearInterval(interval);
  }, [timer]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setMessage("");
    setLoading(true);

    try {
      const apiURL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
      const response = await axios.post(`${apiURL}/api/v1/auth/confirm-otp`, {
        otp,
        email,
      });

      setMessage(response.data?.message || "OTP confirmed successfully!");
      setLoading(false);
      
      // Delay redirect to login so the user can see success message
      setTimeout(() => {
        router.push("/auth/login");
      }, 2000);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || "Failed to verify OTP");
      setLoading(false);
    }
  };

  const handleResend = async () => {
    setError("");
    setMessage("");
    setResending(true);

    try {
      const apiURL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
      const response = await axios.post(`${apiURL}/api/v1/auth/resend-otp`, {
        email,
      });

      setMessage(response.data?.message || "OTP resent successfully. Please check your email!");
      setTimer(60); // Reset countdown timer
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || "Failed to resend OTP");
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-radial from-[#1e1e38] to-[#0a0a0f] px-4">
      {/* Background ambient glows */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-purple-500/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />

      <div className="w-full max-w-md bg-[#13131a]/80 backdrop-blur-xl border border-white/5 p-8 rounded-2xl shadow-2xl relative">
        <div className="mb-6">
          <Link href="/auth/login" className="inline-flex items-center gap-1.5 text-xs text-gray-400 hover:text-white transition-colors">
            <ArrowLeft className="w-3.5 h-3.5" /> Back to Sign In
          </Link>
        </div>

        <div className="flex flex-col items-center mb-8 text-center">
          <div className="w-12 h-12 bg-gradient-to-tr from-purple-500 to-indigo-600 rounded-xl flex items-center justify-center shadow-lg shadow-indigo-500/20 mb-3">
            <KeyRound className="w-6 h-6 text-white" />
          </div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Verify OTP</h1>
          <p className="text-gray-400 text-sm mt-1.5 px-4">
            Enter the confirmation code sent to <span className="text-indigo-300 font-medium break-all">{email || "your email"}</span>
          </p>
        </div>

        {error && (
          <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-sm p-3 rounded-lg mb-6">
            <span className="font-semibold">Error:</span> {error}
          </div>
        )}

        {message && (
          <div className="bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-sm p-3 rounded-lg mb-6">
            {message}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          {!emailParam && (
            <div>
              <label className="block text-gray-300 text-xs font-medium mb-1.5" htmlFor="email">
                Confirm Email
              </label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none text-gray-500">
                  <Mail className="w-4 h-4" />
                </span>
                <input
                  id="email"
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@company.com"
                  className="w-full pl-10 pr-4 py-2.5 bg-[#0a0a0f]/60 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all text-sm"
                />
              </div>
            </div>
          )}

          <div>
            <label className="block text-gray-300 text-xs font-medium mb-1.5" htmlFor="otp">
              One-Time Password (OTP)
            </label>
            <input
              id="otp"
              type="text"
              required
              maxLength={6}
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              placeholder="123456"
              className="w-full text-center tracking-[0.5em] font-mono py-3 bg-[#0a0a0f]/60 border border-white/10 rounded-xl text-white placeholder-gray-600 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all text-lg"
            />
          </div>

          <button
            type="submit"
            disabled={loading || !otp}
            className="w-full py-2.5 px-4 bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white font-medium rounded-xl shadow-lg shadow-indigo-500/20 focus:outline-none focus:ring-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 transition-all text-sm"
          >
            {loading ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                Verifying...
              </>
            ) : (
              "Confirm OTP"
            )}
          </button>
        </form>

        <div className="mt-8 text-center text-xs text-gray-500">
          {timer > 0 ? (
            <p>Resend OTP in {timer}s</p>
          ) : (
            <button
              onClick={handleResend}
              disabled={resending || !email}
              className="text-indigo-400 hover:text-indigo-300 transition-colors font-medium flex items-center gap-1.5 mx-auto disabled:opacity-50"
            >
              {resending ? (
                <Loader2 className="w-3.5 h-3.5 animate-spin" />
              ) : (
                <RefreshCw className="w-3.5 h-3.5" />
              )}
              Resend OTP Code
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

export default function VerifyOtpPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen flex flex-col items-center justify-center bg-[#0a0a0f] text-white">
          <Loader2 className="w-10 h-10 text-indigo-500 animate-spin" />
          <p className="text-slate-400 text-xs mt-3 animate-pulse">Initializing verification flow...</p>
        </div>
      }
    >
      <VerifyOtpPageInner />
    </Suspense>
  );
}
