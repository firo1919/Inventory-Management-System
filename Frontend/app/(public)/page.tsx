"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/hooks/useAuth";
import { Loader2 } from "lucide-react";

export default function PublicPage() {
    const router = useRouter();
    const { user, loading } = useAuth();

    useEffect(() => {
        if (!loading) {
            if (user) {
                router.replace("/dashboard");
            } else {
                router.replace("/auth/login");
            }
        }
    }, [user, loading, router]);

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-[#0a0a0f] text-white">
            <Loader2 className="w-10 h-10 text-indigo-500 animate-spin" />
            <p className="text-slate-400 text-xs mt-3 animate-pulse">
                Routing session...
            </p>
        </div>
    );
}
