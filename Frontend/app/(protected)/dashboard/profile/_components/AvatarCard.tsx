import React from "react";
import { Camera, Shield, CheckCircle, XCircle } from "lucide-react";
import { Loader } from "@/components/ui/Loader";
import { Employee } from "@/types";

interface AvatarCardProps {
  profile: Employee | null;
  uploading: boolean;
  onAvatarChange: (e: React.ChangeEvent<HTMLInputElement>) => Promise<void>;
}

export function AvatarCard({ profile, uploading, onAvatarChange }: AvatarCardProps) {
  return (
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
              <Loader className="text-white" />
            </div>
          )}
        </div>
        <label className="absolute bottom-0 right-0 w-8 h-8 bg-indigo-600 hover:bg-indigo-500 text-white rounded-full border-2 border-white dark:border-[#13131a] flex items-center justify-center cursor-pointer shadow-md transition-all">
          <Camera className="w-4 h-4" />
          <input
            type="file"
            accept="image/*"
            onChange={onAvatarChange}
            className="hidden"
            disabled={uploading}
          />
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
  );
}
