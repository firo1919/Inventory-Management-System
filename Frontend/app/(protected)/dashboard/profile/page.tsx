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
  AlertTriangle,
} from "lucide-react";
import axios from "axios";

export default function ProfilePage() {
  const { user: authUser } = useAuth();
  
  // Data States
  const [profile, setProfile] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  // Form State
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    username: "",
    email: "",
    phone: "",
    password: "",
  });

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
        password: "", // Always re-confirm password for saving updates
      });
    } catch (err) {
      console.error("Failed to load profile:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setMessage("");
    setUpdating(true);

    try {
      const res = await apiClient.put("/api/v1/profile", form);
      setProfile(res.data);
      setMessage("Profile updated successfully!");
      setForm((prev) => ({ ...prev, password: "" })); // clear password input
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || "Failed to update profile");
    } finally {
      setUpdating(false);
    }
  };

  const handleAvatarChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files ? e.target.files[0] : null;
    if (!file) return;

    setUploading(true);
    setError("");
    setMessage("");

    try {
      // 1) Get Presigned URL
      const presignRes = await apiClient.post("/api/v1/uploads/presign", {
        filename: file.name,
        contentType: file.type,
      });

      const { objectKey, uploadUrl } = presignRes.data;

      // 2) Upload file directly to S3 using PUT
      await axios.put(uploadUrl, file, {
        headers: {
          "Content-Type": file.type,
        },
      });

      // 3) Link profile picture key to user profile
      const updateRes = await apiClient.post("/api/v1/profile/profile-picture", {
        objectKey,
      });

      setProfile(updateRes.data);
      setMessage("Profile picture updated successfully!");
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || "Failed to upload avatar");
    } finally {
      setUploading(false);
    }
  };

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
                profile?.firstName?.substring(0, 2) || "US"
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
              <span className="inline-flex items-center gap-1 text-emerald-500 font-bold uppercase">
                <CheckCircle className="w-3.5 h-3.5" />
                Active
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-slate-400">Account ID</span>
              <span className="font-mono text-[10px] text-slate-400 truncate max-w-[150px]">{profile?.id}</span>
            </div>
          </div>
        </div>

        {/* EDIT PROFILE FORM */}
        <div className="md:col-span-2 bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-6 shadow-sm">
          <h3 className="font-bold text-slate-900 dark:text-white text-base mb-6">Profile Settings</h3>

          {error && (
            <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-xs p-3 rounded-lg mb-6 flex items-center gap-1.5">
              <AlertTriangle className="w-4 h-4" />
              <span>{error}</span>
            </div>
          )}

          {message && (
            <div className="bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs p-3 rounded-lg mb-6">
              {message}
            </div>
          )}

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
                    className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none"
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
                    className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none"
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
                  className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none"
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
                  className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1">Phone Number *</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-slate-400">
                  <Phone className="w-3.5 h-3.5" />
                </span>
                <input
                  type="text"
                  required
                  value={form.phone}
                  onChange={(e) => setForm({ ...form, phone: e.target.value })}
                  className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1">Confirm Password (Required to save changes) *</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-slate-400">
                  <Lock className="w-3.5 h-3.5" />
                </span>
                <input
                  type="password"
                  required
                  minLength={8}
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  placeholder="Type password to confirm edits"
                  className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none"
                />
              </div>
            </div>

            <div className="flex justify-end pt-4 border-t border-slate-200 dark:border-white/5">
              <button
                type="submit"
                disabled={updating || !form.password}
                className="px-4 py-2 bg-indigo-600 hover:bg-indigo-505 text-white rounded-xl text-xs font-semibold shadow-md shadow-indigo-500/10 flex items-center gap-1.5"
              >
                {updating ? (
                  <>
                    <Loader2 className="w-3.5 h-3.5 animate-spin" /> Saving...
                  </>
                ) : (
                  "Save Profile"
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
