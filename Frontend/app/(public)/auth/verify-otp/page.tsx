"use client";

import React, { useState, useEffect, useRef, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { KeyRound, Mail, Loader2, ArrowLeft, RefreshCw } from "lucide-react";
import { apiClient } from "@/lib/api-client";
import { toast } from "sonner";

const OTP_LENGTH = 5;

function VerifyOtpPageInner() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const emailParam = searchParams.get("email") || "";

    const [email, setEmail] = useState(emailParam);
    const [digits, setDigits] = useState<string[]>(Array(OTP_LENGTH).fill(""));
    const [loading, setLoading] = useState(false);
    const [resending, setResending] = useState(false);
    const [timer, setTimer] = useState(60);

    const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

    useEffect(() => {
        if (emailParam) setEmail(emailParam);
    }, [emailParam]);

    // Countdown timer for resend button
    useEffect(() => {
        if (timer <= 0) return;
        const interval = setInterval(() => setTimer((t) => t - 1), 1000);
        return () => clearInterval(interval);
    }, [timer]);

    // Auto-focus first box on mount
    useEffect(() => {
        inputRefs.current[0]?.focus();
    }, []);

    const otp = digits.join("");

    const handleDigitChange = (index: number, value: string) => {
        // Allow only digits
        const digit = value.replace(/\D/g, "").slice(-1);
        const next = [...digits];
        next[index] = digit;
        setDigits(next);

        // Auto-advance focus
        if (digit && index < OTP_LENGTH - 1) {
            inputRefs.current[index + 1]?.focus();
        }
    };

    const handleKeyDown = (
        index: number,
        e: React.KeyboardEvent<HTMLInputElement>,
    ) => {
        if (e.key === "Backspace") {
            if (digits[index]) {
                const next = [...digits];
                next[index] = "";
                setDigits(next);
            } else if (index > 0) {
                inputRefs.current[index - 1]?.focus();
            }
        } else if (e.key === "ArrowLeft" && index > 0) {
            inputRefs.current[index - 1]?.focus();
        } else if (e.key === "ArrowRight" && index < OTP_LENGTH - 1) {
            inputRefs.current[index + 1]?.focus();
        }
    };

    const handlePaste = (e: React.ClipboardEvent) => {
        e.preventDefault();
        const pasted = e.clipboardData
            .getData("text")
            .replace(/\D/g, "")
            .slice(0, OTP_LENGTH);
        if (!pasted) return;
        const next = [...digits];
        pasted.split("").forEach((ch, i) => {
            next[i] = ch;
        });
        setDigits(next);
        // Focus last filled box
        const lastIdx = Math.min(pasted.length - 1, OTP_LENGTH - 1);
        inputRefs.current[lastIdx]?.focus();
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (otp.length < OTP_LENGTH) {
            toast.error("Please enter all 5 digits.");
            return;
        }
        setLoading(true);
        try {
            const response = await apiClient.post("/api/v1/auth/confirm-otp", {
                otp,
                email,
            });
            toast.success(
                response.data?.message ||
                    "OTP confirmed! Redirecting to login...",
            );
            setTimeout(() => router.push("/auth/login"), 2000);
        } catch (err: any) {
            toast.error(
                err.response?.data?.message ||
                    err.message ||
                    "Failed to verify OTP",
            );
        } finally {
            setLoading(false);
        }
    };

    const handleResend = async () => {
        setResending(true);
        try {
            const response = await apiClient.post("/api/v1/auth/resend-otp", {
                email,
            });
            toast.success(
                response.data?.message || "OTP resent! Check your email.",
            );
            setTimer(60);
            // Clear boxes and refocus
            setDigits(Array(OTP_LENGTH).fill(""));
            setTimeout(() => inputRefs.current[0]?.focus(), 100);
        } catch (err: any) {
            toast.error(
                err.response?.data?.message ||
                    err.message ||
                    "Failed to resend OTP",
            );
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
                    <Link
                        href="/auth/login"
                        className="inline-flex items-center gap-1.5 text-xs text-gray-400 hover:text-white transition-colors"
                    >
                        <ArrowLeft className="w-3.5 h-3.5" /> Back to Sign In
                    </Link>
                </div>

                <div className="flex flex-col items-center mb-8 text-center">
                    <div className="w-12 h-12 bg-linear-to-tr from-purple-500 to-indigo-600 rounded-xl flex items-center justify-center shadow-lg shadow-indigo-500/20 mb-3">
                        <KeyRound className="w-6 h-6 text-white" />
                    </div>
                    <h1 className="text-2xl font-bold text-white tracking-tight">
                        Verify OTP
                    </h1>
                    <p className="text-gray-400 text-sm mt-1.5 px-4">
                        Enter the 5-digit code sent to{" "}
                        <span className="text-indigo-300 font-medium break-all">
                            {email || "your email"}
                        </span>
                    </p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-6">
                    {!emailParam && (
                        <div>
                            <label
                                className="block text-gray-300 text-xs font-medium mb-1.5"
                                htmlFor="email"
                            >
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

                    {/* 5-digit OTP input boxes */}
                    <div>
                        <label className="block text-gray-300 text-xs font-medium mb-3 text-center">
                            One-Time Password
                        </label>
                        <div
                            className="flex justify-center gap-3"
                            onPaste={handlePaste}
                        >
                            {digits.map((digit, i) => (
                                <input
                                    key={i}
                                    ref={(el) => {
                                        inputRefs.current[i] = el;
                                    }}
                                    type="text"
                                    inputMode="numeric"
                                    maxLength={1}
                                    value={digit}
                                    onChange={(e) =>
                                        handleDigitChange(i, e.target.value)
                                    }
                                    onKeyDown={(e) => handleKeyDown(i, e)}
                                    className={`w-12 h-14 text-center text-xl font-bold font-mono rounded-xl border transition-all focus:outline-none focus:ring-2 focus:ring-indigo-500/70 focus:border-indigo-500 bg-[#0a0a0f]/60 text-white ${
                                        digit
                                            ? "border-indigo-500/60 bg-indigo-500/10"
                                            : "border-white/10"
                                    }`}
                                    aria-label={`OTP digit ${i + 1}`}
                                />
                            ))}
                        </div>
                        <p className="text-center text-[11px] text-gray-600 mt-2">
                            Paste your code or type each digit
                        </p>
                    </div>

                    <button
                        type="submit"
                        disabled={loading || otp.length < OTP_LENGTH}
                        className="w-full py-2.5 px-4 bg-linear-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white font-medium rounded-xl shadow-lg shadow-indigo-500/20 focus:outline-none focus:ring-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 transition-all text-sm"
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
                        <p>
                            Resend OTP in{" "}
                            <span className="text-indigo-400 font-semibold">
                                {timer}s
                            </span>
                        </p>
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
                    <p className="text-slate-400 text-xs mt-3 animate-pulse">
                        Initializing verification flow...
                    </p>
                </div>
            }
        >
            <VerifyOtpPageInner />
        </Suspense>
    );
}
