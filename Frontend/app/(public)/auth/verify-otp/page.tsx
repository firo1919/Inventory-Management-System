"use client";

import React, { Suspense } from "react";
import { VerifyOtpForm } from "./_components/VerifyOtpForm";
import { Loader } from "@/components/ui/Loader";

export default function VerifyOtpPage() {
    return (
        <Suspense
            fallback={
                <div className="min-h-screen flex flex-col items-center justify-center bg-[#0a0a0f] text-white">
                    <Loader text="Initializing verification flow..." />
                </div>
            }
        >
            <div className="min-h-screen flex items-center justify-center bg-radial from-[#1e1e38] to-[#0a0a0f] px-4">
                {/* Background ambient glows */}
                <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-purple-500/10 rounded-full blur-3xl pointer-events-none" />
                <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />

                <VerifyOtpForm />
            </div>
        </Suspense>
    );
}
