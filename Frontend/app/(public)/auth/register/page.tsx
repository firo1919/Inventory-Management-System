"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { User, Mail, Phone, Lock, ShieldCheck, Loader2, Warehouse, Eye, EyeOff } from "lucide-react";
import { apiClient } from "@/lib/api-client";
import { toast } from "sonner";

const getPasswordStrength = (pwd: string) => {
  if (!pwd) return { score: 0, label: "", color: "" };
  let score = 0;
  if (pwd.length >= 8) score++;
  if (pwd.length >= 12) score++;
  if (/[A-Z]/.test(pwd)) score++;
  if (/[0-9]/.test(pwd)) score++;
  if (/[^A-Za-z0-9]/.test(pwd)) score++;
  const labels = ["", "Very Weak", "Weak", "Fair", "Strong", "Very Strong"];
  const colors = ["", "bg-red-500", "bg-orange-500", "bg-yellow-500", "bg-emerald-500", "bg-emerald-600"];
  return { score, label: labels[score] || "Very Strong", color: colors[Math.min(score, 5)] };
};

export default function RegisterPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    username: "",
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
    bootstrapToken: "",
  });
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [showToken, setShowToken] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const pwdStrength = getPasswordStrength(formData.password);
  const passwordsMatch = formData.password === formData.confirmPassword;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!passwordsMatch) {
      toast.error("Passwords do not match.");
      return;
    }
    if (formData.password.length < 8) {
      toast.error("Password must be at least 8 characters.");
      return;
    }
    setLoading(true);
    try {
      const { confirmPassword, ...payload } = formData;
      await apiClient.post("/api/v1/auth/admins", payload);
      toast.success("Admin registered! Check your email for the OTP code.");
      router.push(`/auth/verify-otp?email=${encodeURIComponent(formData.email)}`);
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to register admin");
      setLoading(false);
    }
  };

  const inputClass =
    "w-full pl-9 pr-9 py-2 bg-[#0a0a0f]/60 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all text-sm";

  return (
    <div className="min-h-screen flex items-center justify-center bg-radial from-[#1e1e38] to-[#0a0a0f] py-12 px-4">
      {/* Background ambient glows */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-purple-500/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />

      <div className="w-full max-w-lg bg-[#13131a]/80 backdrop-blur-xl border border-white/5 p-8 rounded-2xl shadow-2xl relative">
        <div className="flex flex-col items-center mb-8">
          <div className="w-12 h-12 bg-gradient-to-tr from-purple-500 to-indigo-600 rounded-xl flex items-center justify-center shadow-lg shadow-indigo-500/20 mb-3">
            <Warehouse className="w-6 h-6 text-white" />
          </div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Register Admin</h1>
          <p className="text-gray-400 text-sm mt-1">Bootstrap initial system administrator</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-gray-300 text-xs font-medium mb-1" htmlFor="firstName">First Name</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-gray-500"><User className="w-3.5 h-3.5" /></span>
                <input id="firstName" name="firstName" type="text" required value={formData.firstName} onChange={handleChange} placeholder="John" className={inputClass} />
              </div>
            </div>
            <div>
              <label className="block text-gray-300 text-xs font-medium mb-1" htmlFor="lastName">Last Name</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-gray-500"><User className="w-3.5 h-3.5" /></span>
                <input id="lastName" name="lastName" type="text" required value={formData.lastName} onChange={handleChange} placeholder="Doe" className={inputClass} />
              </div>
            </div>
          </div>

          <div>
            <label className="block text-gray-300 text-xs font-medium mb-1" htmlFor="username">Username</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-gray-500"><User className="w-3.5 h-3.5" /></span>
              <input id="username" name="username" type="text" required value={formData.username} onChange={handleChange} placeholder="admin123" className={inputClass} />
            </div>
          </div>

          <div>
            <label className="block text-gray-300 text-xs font-medium mb-1" htmlFor="email">Email Address</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-gray-500"><Mail className="w-3.5 h-3.5" /></span>
              <input id="email" name="email" type="email" required value={formData.email} onChange={handleChange} placeholder="admin@company.com" className={inputClass} />
            </div>
          </div>

          <div>
            <label className="block text-gray-300 text-xs font-medium mb-1" htmlFor="phone">Phone Number</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-gray-500"><Phone className="w-3.5 h-3.5" /></span>
              <input id="phone" name="phone" type="text" required value={formData.phone} onChange={handleChange} placeholder="+1234567890" className={inputClass} />
            </div>
          </div>

          {/* Password with strength indicator */}
          <div>
            <label className="block text-gray-300 text-xs font-medium mb-1" htmlFor="password">Password (min 8 chars)</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-gray-500"><Lock className="w-3.5 h-3.5" /></span>
              <input id="password" name="password" type={showPassword ? "text" : "password"} required minLength={8} value={formData.password} onChange={handleChange} placeholder="••••••••" className={inputClass} />
              <button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute inset-y-0 right-0 flex items-center pr-3 text-gray-500 hover:text-gray-300">
                {showPassword ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
              </button>
            </div>
            {formData.password && (
              <div className="mt-1.5 space-y-1">
                <div className="flex gap-1">
                  {[1, 2, 3, 4, 5].map((i) => (
                    <div key={i} className={`h-1 flex-1 rounded-full transition-colors ${i <= pwdStrength.score ? pwdStrength.color : "bg-white/10"}`} />
                  ))}
                </div>
                <p className="text-[10px] text-gray-500">{pwdStrength.label}</p>
              </div>
            )}
          </div>

          {/* Confirm Password */}
          <div>
            <label className="block text-gray-300 text-xs font-medium mb-1" htmlFor="confirmPassword">Confirm Password</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-gray-500"><Lock className="w-3.5 h-3.5" /></span>
              <input
                id="confirmPassword"
                name="confirmPassword"
                type={showConfirm ? "text" : "password"}
                required
                value={formData.confirmPassword}
                onChange={handleChange}
                placeholder="••••••••"
                className={`w-full pl-9 pr-9 py-2 bg-[#0a0a0f]/60 border rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 transition-all text-sm ${
                  formData.confirmPassword && !passwordsMatch
                    ? "border-red-500/60 focus:border-red-500"
                    : "border-white/10 focus:border-indigo-500"
                }`}
              />
              <button type="button" onClick={() => setShowConfirm(!showConfirm)} className="absolute inset-y-0 right-0 flex items-center pr-3 text-gray-500 hover:text-gray-300">
                {showConfirm ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
              </button>
            </div>
            {formData.confirmPassword && !passwordsMatch && (
              <p className="text-[10px] text-red-400 mt-1">Passwords do not match</p>
            )}
          </div>

          {/* Bootstrap Token */}
          <div>
            <label className="block text-gray-300 text-xs font-medium mb-1" htmlFor="bootstrapToken">
              Bootstrap Token (min 32 chars)
            </label>
            <p className="text-[10px] text-gray-600 mb-1.5">Found in your server <code className="bg-white/5 px-1 rounded">.env</code> as <code className="bg-white/5 px-1 rounded">ADMIN_BOOTSTRAP_TOKEN</code></p>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-gray-500"><ShieldCheck className="w-3.5 h-3.5" /></span>
              <input id="bootstrapToken" name="bootstrapToken" type={showToken ? "text" : "password"} required minLength={32} value={formData.bootstrapToken} onChange={handleChange} placeholder="32-character hexadecimal key" className={inputClass} />
              <button type="button" onClick={() => setShowToken(!showToken)} className="absolute inset-y-0 right-0 flex items-center pr-3 text-gray-500 hover:text-gray-300">
                {showToken ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
              </button>
            </div>
          </div>

          <button
            type="submit"
            disabled={loading || !passwordsMatch || formData.password.length < 8}
            className="w-full py-2.5 px-4 bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white font-medium rounded-xl shadow-lg shadow-indigo-500/20 focus:outline-none focus:ring-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 transition-all text-sm mt-2"
          >
            {loading ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                Registering...
              </>
            ) : (
              "Register Admin"
            )}
          </button>
        </form>

        <div className="mt-6 text-center text-xs text-gray-500">
          <p>
            Already registered?{" "}
            <Link href="/auth/login" className="text-indigo-400 hover:text-indigo-300 transition-colors font-medium">
              Sign In
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
