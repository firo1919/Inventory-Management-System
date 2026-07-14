"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { profileService } from "@/services/profile";
import axios from "axios";
import { toast } from "sonner";
import { AvatarCard } from "./_components/AvatarCard";
import { ProfileDetailsForm } from "./_components/ProfileDetailsForm";
import { ChangePasswordForm } from "./_components/ChangePasswordForm";
import { Loader } from "@/components/ui/Loader";
import { ProfileDetailsInput, ChangePasswordInput } from "./schema";

export default function ProfilePage() {
  useAuth(); // ensure auth context is initialized

  // Data States
  const [profile, setProfile] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [changingPwd, setChangingPwd] = useState(false);
  const [uploading, setUploading] = useState(false);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const data = await profileService.getProfile();
      setProfile(data);
    } catch {
      toast.error("Failed to load profile. Please refresh the page.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  // --- Update profile details ---
  const handleUpdate = async (data: ProfileDetailsInput) => {
    setUpdating(true);
    try {
      const updatedData = await profileService.updateProfile({
        ...data,
        password: "", // Not changing password — send empty placeholder
      });
      setProfile(updatedData);
      toast.success("Profile updated successfully!");
    } catch (err: any) {
      toast.error(err.response?.data?.message || "Failed to update profile.");
    } finally {
      setUpdating(false);
    }
  };

  // --- Change password separately ---
  const handleChangePassword = async (data: ChangePasswordInput) => {
    setChangingPwd(true);
    try {
      await profileService.updateProfile({
        firstName: profile?.firstName,
        lastName: profile?.lastName,
        username: profile?.username,
        email: profile?.email,
        phone: profile?.phone || "",
        password: data.newPassword,
      });
      toast.success("Password changed successfully!");
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
      const presignData = await profileService.getPresignedUrl(file.name, file.type);
      const { objectKey, uploadUrl } = presignData;

      await axios.put(uploadUrl, file, {
        headers: { "Content-Type": file.type },
      });

      const updatedProfile = await profileService.updateProfilePicture(objectKey);
      setProfile(updatedProfile);
      toast.success("Profile picture updated!", { id: toastId });
    } catch (err: any) {
      toast.error(err.response?.data?.message || "Failed to upload avatar.", { id: toastId });
    } finally {
      setUploading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[50vh] text-slate-400">
        <Loader text="Loading Profile..." />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-xl font-bold dark:text-white">Account Settings</h1>
        <p className="text-xs text-slate-400">Manage your profile information and account credentials</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 animate-fade-in">
        {/* AVATAR & INFO CARD */}
        <AvatarCard
          profile={profile}
          uploading={uploading}
          onAvatarChange={handleAvatarChange}
        />

        <div className="md:col-span-2 space-y-6">
          {/* EDIT PROFILE FORM */}
          <ProfileDetailsForm
            initialData={profile ? {
              firstName: profile.firstName,
              lastName: profile.lastName,
              username: profile.username,
              email: profile.email,
              phone: profile.phone || "",
            } : null}
            onSubmit={handleUpdate}
            updating={updating}
          />

          {/* CHANGE PASSWORD FORM */}
          <ChangePasswordForm
            onSubmit={handleChangePassword}
            changingPwd={changingPwd}
          />
        </div>
      </div>
    </div>
  );
}
