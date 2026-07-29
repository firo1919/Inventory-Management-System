import React from "react";
import Link from "next/link";
import { Lock, Mail, Warehouse } from "lucide-react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Loader } from "@/components/ui/Loader";
import { loginSchema, LoginInput } from "../schema";

interface LoginFormProps {
    onSubmit: (data: LoginInput) => Promise<void>;
    loading: boolean;
    error?: string;
}

export function LoginForm({ onSubmit, loading, error }: LoginFormProps) {
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<LoginInput>({
        resolver: zodResolver(loginSchema),
        defaultValues: {
            email: "",
            password: "",
        },
    });

    return (
        <div className="w-full max-w-md bg-[#13131a]/80 backdrop-blur-xl border border-white/5 p-8 rounded-2xl shadow-2xl relative">
            <div className="flex flex-col items-center mb-8">
                <div className="w-12 h-12 bg-gradient-to-tr from-purple-500 to-indigo-600 rounded-xl flex items-center justify-center shadow-lg shadow-indigo-500/20 mb-3">
                    <Warehouse className="w-6 h-6 text-white" />
                </div>
                <h1 className="text-xl font-bold text-white tracking-tight text-center">
                    Inventory Management System
                </h1>
                <p className="text-gray-400 text-sm mt-1">
                    Sign in to your account
                </p>
            </div>

            {error && (
                <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-sm p-3 rounded-lg mb-6 flex items-start gap-2">
                    <span className="font-semibold">Error:</span> {error}
                </div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
                <div>
                    <label
                        className="block text-gray-300 text-sm font-medium mb-1.5"
                        htmlFor="email"
                    >
                        Email Address
                    </label>
                    <div className="relative">
                        <span className="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none text-gray-500">
                            <Mail className="w-4 h-4" />
                        </span>
                        <input
                            id="email"
                            type="email"
                            {...register("email")}
                            placeholder="name@company.com"
                            className="w-full pl-10 pr-4 py-2.5 bg-[#0a0a0f]/60 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all text-sm"
                        />
                    </div>
                    {errors.email && (
                        <p className="text-xs text-red-400 mt-1.5">
                            {errors.email.message}
                        </p>
                    )}
                </div>

                <div>
                    <div className="flex justify-between items-center mb-1.5">
                        <label
                            className="block text-gray-300 text-sm font-medium"
                            htmlFor="password"
                        >
                            Password
                        </label>
                    </div>
                    <div className="relative">
                        <span className="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none text-gray-500">
                            <Lock className="w-4 h-4" />
                        </span>
                        <input
                            id="password"
                            type="password"
                            {...register("password")}
                            placeholder="••••••••"
                            className="w-full pl-10 pr-4 py-2.5 bg-[#0a0a0f]/60 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all text-sm"
                        />
                    </div>
                    {errors.password && (
                        <p className="text-xs text-red-400 mt-1.5">
                            {errors.password.message}
                        </p>
                    )}
                </div>

                <button
                    type="submit"
                    disabled={loading}
                    className="w-full py-2.5 px-4 bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white font-medium rounded-xl shadow-lg shadow-indigo-500/20 focus:outline-none focus:ring-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 transition-all text-sm mt-2 cursor-pointer"
                >
                    {loading ? (
                        <Loader
                            size={4}
                            text="Signing in..."
                            className="flex-row"
                        />
                    ) : (
                        "Sign In"
                    )}
                </button>
            </form>

            <div className="mt-8 text-center text-xs text-gray-500">
                <p>
                    First time setting up the system?{" "}
                    <Link
                        href="/auth/register"
                        className="text-indigo-400 hover:text-indigo-300 transition-colors font-medium"
                    >
                        Register Admin
                    </Link>
                </p>
            </div>
        </div>
    );
}
