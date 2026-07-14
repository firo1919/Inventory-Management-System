import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Lock, Eye, EyeOff } from "lucide-react";
import { Loader } from "@/components/ui/Loader";
import { changePasswordSchema, ChangePasswordInput } from "../schema";

const getPasswordStrength = (pwd: string) => {
  if (!pwd) return { score: 0, label: "", color: "" };
  let score = 0;
  if (pwd.length >= 8) score++;
  if (pwd.length >= 12) score++;
  if (/[A-Z]/.test(pwd)) score++;
  if (/[0-9]/.test(pwd)) score++;
  if (/[^A-Za-z0-9]/.test(pwd)) score++;
  const labels = ["", "Very Weak", "Weak", "Fair", "Strong", "Very Strong"];
  const colors = [
    "",
    "bg-red-500",
    "bg-orange-500",
    "bg-yellow-500",
    "bg-emerald-500",
    "bg-emerald-600",
  ];
  return {
    score,
    label: labels[score] || "Very Strong",
    color: colors[Math.min(score, 5)],
  };
};

interface ChangePasswordFormProps {
  onSubmit: (data: ChangePasswordInput) => Promise<void>;
  changingPwd: boolean;
}

export function ChangePasswordForm({ onSubmit, changingPwd }: ChangePasswordFormProps) {
  const [showNewPwd, setShowNewPwd] = useState(false);
  const [showConfirmPwd, setShowConfirmPwd] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    reset,
    formState: { errors },
  } = useForm<ChangePasswordInput>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: {
      newPassword: "",
      confirmPassword: "",
    },
  });

  const newPasswordVal = watch("newPassword") || "";
  const confirmPasswordVal = watch("confirmPassword") || "";
  const passwordsMatch =
    newPasswordVal && confirmPasswordVal ? newPasswordVal === confirmPasswordVal : true;

  const pwdStrength = getPasswordStrength(newPasswordVal);

  const handleFormSubmit = async (data: ChangePasswordInput) => {
    await onSubmit(data);
    reset({ newPassword: "", confirmPassword: "" });
  };

  return (
    <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-6 shadow-sm">
      <div className="flex items-center gap-2 mb-6">
        <Lock className="w-4 h-4 text-slate-400" />
        <h3 className="font-bold text-slate-900 dark:text-white text-base">Change Password</h3>
      </div>

      <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1">New Password *</label>
          <div className="relative">
            <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-slate-400">
              <Lock className="w-3.5 h-3.5" />
            </span>
            <input
              type={showNewPwd ? "text" : "password"}
              {...register("newPassword")}
              placeholder="At least 8 characters"
              className="w-full pl-9 pr-9 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
            />
            <button
              type="button"
              onClick={() => setShowNewPwd(!showNewPwd)}
              className="absolute inset-y-0 right-0 flex items-center pr-3 text-slate-400 hover:text-slate-650 cursor-pointer"
            >
              {showNewPwd ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
            </button>
          </div>
          {errors.newPassword && (
            <p className="text-[10px] text-red-400 mt-1">{errors.newPassword.message}</p>
          )}
          {newPasswordVal && (
            <div className="mt-1.5 space-y-1">
              <div className="flex gap-1">
                {[1, 2, 3, 4, 5].map((i) => (
                  <div
                    key={i}
                    className={`h-1 flex-1 rounded-full transition-colors ${
                      i <= pwdStrength.score ? pwdStrength.color : "bg-slate-200 dark:bg-white/10"
                    }`}
                  />
                ))}
              </div>
              <p className="text-[10px] text-slate-400">{pwdStrength.label}</p>
            </div>
          )}
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1">Confirm New Password *</label>
          <div className="relative">
            <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-slate-400">
              <Lock className="w-3.5 h-3.5" />
            </span>
            <input
              type={showConfirmPwd ? "text" : "password"}
              {...register("confirmPassword")}
              placeholder="Repeat new password"
              className={`w-full pl-9 pr-9 py-2 bg-slate-50 dark:bg-[#0a0a0f] border rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500 ${
                !passwordsMatch ? "border-red-405" : "border-slate-200 dark:border-white/5"
              }`}
            />
            <button
              type="button"
              onClick={() => setShowConfirmPwd(!showConfirmPwd)}
              className="absolute inset-y-0 right-0 flex items-center pr-3 text-slate-400 hover:text-slate-650 cursor-pointer"
            >
              {showConfirmPwd ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
            </button>
          </div>
          {errors.confirmPassword && (
            <p className="text-[10px] text-red-400 mt-1">{errors.confirmPassword.message}</p>
          )}
        </div>

        <div className="flex justify-end pt-4 border-t border-slate-200 dark:border-white/5">
          <button
            type="submit"
            disabled={changingPwd || !passwordsMatch || !newPasswordVal}
            className="px-4 py-2 bg-orange-600 hover:bg-orange-500 disabled:opacity-60 text-white rounded-xl text-xs font-semibold shadow-md shadow-orange-500/10 flex items-center gap-1.5 transition-colors cursor-pointer"
          >
            {changingPwd ? (
              <Loader size={3.5} text="Changing..." className="flex-row gap-1" />
            ) : (
              <>
                <Lock className="w-3.5 h-3.5" /> Change Password
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
}
