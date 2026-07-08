"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { apiClient } from "@/lib/api-client";
import {
  User,
  Mail,
  Phone,
  Lock,
  Camera,
  Shield,
  Loader2,
  CheckCircle,
  XCircle,
  Eye,
  EyeOff,
} from "lucide-react";
import axios from "axios";
import { toast } from "sonner";

export default function ProfilePage() {
  useAuth(); // ensure auth context is initialized

  // Data States
  const [profile, setProfile] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [changingPwd, setChangingPwd] = useState(false);
  const [uploading, setUploading] = useState(false);

  // Profile form state (no password)
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    username: "",
    email: "",
    phone: "",
  });

  // Separate password change state
  const [pwdForm, setPwdForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [showCurrentPwd, setShowCurrentPwd] = useState(false);
  const [showNewPwd, setShowNewPwd] = useState(false);
  const [showConfirmPwd, setShowConfirmPwd] = useState(false);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const res = await apiClient.get("/api/v1/profile");
      setProfile(res.data);
      setForm({
        firstName: res.data.firstName,
        lastName: res.data.lastName,
        username: res.data.username,
        email: res.data.email,
        phone: res.data.phone || "",
      });
    } catch {
      toast.error("Failed to load profile. Please refresh the page.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  // --- Update profile details (no password required) ---
  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    setUpdating(true);
    try {
      // Backend PUT /profile requires password; we send an empty string
      // for field-only updates — the backend will only validate password
      // if it is non-empty (confirmed by backend implementation).
      // We send a placeholder so the contract is satisfied.
      const res = await apiClient.put("/api/v1/profile", {
        ...form,
        password: "", // Not changing password — send empty
      });
      setProfile(res.data);
      toast.success("Profile updated successfully!");
    } catch (err: any) {
      toast.error(err.response?.data?.message || "Failed to update profile.");
    } finally {
      setUpdating(false);
    }
  };

  // --- Change password separately ---
  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (pwdForm.newPassword !== pwdForm.confirmPassword) {
      toast.error("New password and confirm password do not match.");
      return;
    }
    if (pwdForm.newPassword.length < 8) {
      toast.error("New password must be at least 8 characters.");
      return;
    }
    setChangingPwd(true);
    try {
      await apiClient.put("/api/v1/profile", {
        ...form,
        password: pwdForm.newPassword,
      });
      toast.success("Password changed successfully!");
      setPwdForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
    } catch (err: any) {
      toast.error(err.response?.data?.message || "Failed to change password.");
    } finally {
      setChangingPwd(false);
    }
  };

  // --- Avatar upload ---
  const handleAvatarChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files ? e.target.files[0] : null;
    if (!file) return;

    setUploading(true);
    const toastId = toast.loading("Uploading avatar...");
    try {
      const presignRes = await apiClient.post("/api/v1/uploads/presign", {
        filename: file.name,
        contentType: file.type,
      });
      const { objectKey, uploadUrl } = presignRes.data;

      await axios.put(uploadUrl, file, {
        headers: { "Content-Type": file.type },
      });

      const updateRes = await apiClient.post("/api/v1/profile/profile-picture", {
        objectKey,
      });
      setProfile(updateRes.data);
      toast.success("Profile picture updated!", { id: toastId });
    } catch (err: any) {
      toast.error(err.response?.data?.message || "Failed to upload avatar.", { id: toastId });
    } finally {
      setUploading(false);
    }
  };

  // Password strength calculation
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

  const pwdStrength = getPasswordStrength(pwdForm.newPassword);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[50vh] text-slate-400">
        <Loader2 className="w-8 h-8 animate-spin text-indigo-500 mb-2" />
        <p className="text-xs">Loading Profile...</p>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-xl font-bold dark:text-white">Account Settings</h1>
        <p className="text-xs text-slate-400">Manage your profile information and account credentials</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* AVATAR & INFO CARD */}
        <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-6 flex flex-col items-center text-center shadow-sm h-fit">
          <div className="relative group">
            <div className="w-24 h-24 bg-indigo-500/10 text-indigo-500 border border-slate-200 dark:border-white/5 rounded-full flex items-center justify-center overflow-hidden font-bold uppercase text-2xl relative">
              {profile?.profilePictureUrl ? (
                <img src={profile.profilePictureUrl} alt="Avatar" className="w-full h-full object-cover" />
              ) : (
                `${profile?.firstName?.charAt(0) || ""}${profile?.lastName?.charAt(0) || ""}` || "U"
              )}
              {uploading && (
                <div className="absolute inset-0 bg-black/60 backdrop-blur-xs flex items-center justify-center">
                  <Loader2 className="w-6 h-6 animate-spin text-white" />
                </div>
              )}
            </div>
            <label className="absolute bottom-0 right-0 w-8 h-8 bg-indigo-600 hover:bg-indigo-500 text-white rounded-full border-2 border-white dark:border-[#13131a] flex items-center justify-center cursor-pointer shadow-md transition-all">
              <Camera className="w-4 h-4" />
              <input type="file" accept="image/*" onChange={handleAvatarChange} className="hidden" disabled={uploading} />
            </label>
          </div>

          <h3 className="font-bold text-slate-800 dark:text-white text-base mt-4">
            {profile?.firstName} {profile?.lastName}
          </h3>
          <p className="text-xs text-slate-400 font-mono mt-0.5">@{profile?.username}</p>

          <div className="w-full mt-6 pt-6 border-t border-slate-200 dark:border-white/5 space-y-3.5 text-left text-xs">
            <div className="flex items-center justify-between">
              <span className="text-slate-400">Role</span>
              <span className="inline-flex items-center gap-1 font-bold uppercase text-indigo-500">
                <Shield className="w-3.5 h-3.5" />
                {profile?.role}
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-slate-400">Status</span>
              {profile?.active ? (
                <span className="inline-flex items-center gap-1 text-emerald-500 font-bold uppercase">
                  <CheckCircle className="w-3.5 h-3.5" />
                  Active
                </span>
              ) : (
                <span className="inline-flex items-center gap-1 text-red-400 font-bold uppercase">
                  <XCircle className="w-3.5 h-3.5" />
                  Inactive
                </span>
              )}
            </div>
            <div className="flex items-center justify-between">
              <span className="text-slate-400">Account ID</span>
              <span className="font-mono text-[10px] text-slate-400 truncate max-w-[150px]">{profile?.id}</span>
            </div>
          </div>
        </div>

        <div className="md:col-span-2 space-y-6">
          {/* EDIT PROFILE FORM */}
          <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-6 shadow-sm">
            <h3 className="font-bold text-slate-900 dark:text-white text-base mb-6">Profile Details</h3>

            <form onSubmit={handleUpdate} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">First Name *</label>
                  <div className="relative">
                    <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-slate-400">
                      <User className="w-3.5 h-3.5" />
                    </span>
                    <input
                      type="text"
                      required
                      value={form.firstName}
                      onChange={(e) => setForm({ ...form, firstName: e.target.value })}
                      className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Last Name *</label>
                  <div className="relative">
                    <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-slate-400">
                      <User className="w-3.5 h-3.5" />
                    </span>
                    <input
                      type="text"
                      required
                      value={form.lastName}
                      onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                      className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                    />
                  </div>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Username *</label>
                <div className="relative">
                  <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-slate-400">
                    <User className="w-3.5 h-3.5" />
                  </span>
                  <input
                    type="text"
                    required
                    value={form.username}
                    onChange={(e) => setForm({ ...form, username: e.target.value })}
                    className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Email Address *</label>
                <div className="relative">
                  <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-slate-400">
                    <Mail className="w-3.5 h-3.5" />
                  </span>
                  <input
                    type="email"
                    required
                    value={form.email}
                    onChange={(e) => setForm({ ...form, email: e.target.value })}
                    className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Phone Number</label>
                <div className="relative">
                  <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-slate-400">
                    <Phone className="w-3.5 h-3.5" />
                  </span>
                  <input
                    type="text"
                    value={form.phone}
                    onChange={(e) => setForm({ ...form, phone: e.target.value })}
                    className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                </div>
              </div>

              <div className="flex justify-end pt-4 border-t border-slate-200 dark:border-white/5">
                <button
                  type="submit"
                  disabled={updating}
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-60 text-white rounded-xl text-xs font-semibold shadow-md shadow-indigo-500/10 flex items-center gap-1.5 transition-colors"
                >
                  {updating ? (
                    <>
                      <Loader2 className="w-3.5 h-3.5 animate-spin" /> Saving...
                    </>
                  ) : (
                    "Save Changes"
                  )}
                </button>
              </div>
            </form>
          </div>

          {/* CHANGE PASSWORD FORM */}
          <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-6 shadow-sm">
            <div className="flex items-center gap-2 mb-6">
              <Lock className="w-4 h-4 text-slate-400" />
              <h3 className="font-bold text-slate-900 dark:text-white text-base">Change Password</h3>
            </div>

            <form onSubmit={handleChangePassword} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">New Password *</label>
                <div className="relative">
                  <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-slate-400">
                    <Lock className="w-3.5 h-3.5" />
                  </span>
                  <input
                    type={showNewPwd ? "text" : "password"}
                    required
                    minLength={8}
                    value={pwdForm.newPassword}
                    onChange={(e) => setPwdForm({ ...pwdForm, newPassword: e.target.value })}
                    placeholder="At least 8 characters"
                    className="w-full pl-9 pr-9 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                  <button type="button" onClick={() => setShowNewPwd(!showNewPwd)} className="absolute inset-y-0 right-0 flex items-center pr-3 text-slate-400 hover:text-slate-600">
                    {showNewPwd ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
                  </button>
                </div>
                {pwdForm.newPassword && (
                  <div className="mt-1.5 space-y-1">
                    <div className="flex gap-1">
                      {[1, 2, 3, 4, 5].map((i) => (
                        <div key={i} className={`h-1 flex-1 rounded-full transition-colors ${i <= pwdStrength.score ? pwdStrength.color : "bg-slate-200 dark:bg-white/10"}`} />
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
                    required
                    value={pwdForm.confirmPassword}
                    onChange={(e) => setPwdForm({ ...pwdForm, confirmPassword: e.target.value })}
                    placeholder="Repeat new password"
                    className={`w-full pl-9 pr-9 py-2 bg-slate-50 dark:bg-[#0a0a0f] border rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500 ${
                      pwdForm.confirmPassword && pwdForm.newPassword !== pwdForm.confirmPassword
                        ? "border-red-400"
                        : "border-slate-200 dark:border-white/5"
                    }`}
                  />
                  <button type="button" onClick={() => setShowConfirmPwd(!showConfirmPwd)} className="absolute inset-y-0 right-0 flex items-center pr-3 text-slate-400 hover:text-slate-600">
                    {showConfirmPwd ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
                  </button>
                </div>
                {pwdForm.confirmPassword && pwdForm.newPassword !== pwdForm.confirmPassword && (
                  <p className="text-[10px] text-red-400 mt-1">Passwords do not match</p>
                )}
              </div>

              <div className="flex justify-end pt-4 border-t border-slate-200 dark:border-white/5">
                <button
                  type="submit"
                  disabled={changingPwd || !pwdForm.newPassword || pwdForm.newPassword !== pwdForm.confirmPassword}
                  className="px-4 py-2 bg-orange-600 hover:bg-orange-500 disabled:opacity-60 text-white rounded-xl text-xs font-semibold shadow-md shadow-orange-500/10 flex items-center gap-1.5 transition-colors"
                >
                  {changingPwd ? (
                    <>
                      <Loader2 className="w-3.5 h-3.5 animate-spin" /> Changing...
                    </>
                  ) : (
                    <>
                      <Lock className="w-3.5 h-3.5" /> Change Password
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
